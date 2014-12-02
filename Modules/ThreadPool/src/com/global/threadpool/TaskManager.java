package com.global.threadpool;

import com.global.channels.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import static com.global.threadpool.Util.compareFlags;

/**
 * @author Róbert Dóczi
 *         Date: 2014.12.02.
 */
public class TaskManager {

    private Channel eventChannel;

    private List<LinkedBlockingDeque<Task>> taskList;
    private BlockingDeque<Task>             backgroundTasks;
    private LinkedBlockingDeque<Task>       syncTasks;

    private List<Thread> threads;

    private boolean running;

    final int readList  = 0;
    final int writeList = 1;

    final Object syncObject;
    int numTasksToWaitFor;

    TaskManager() {
        eventChannel = new Channel();

        taskList = new ArrayList<>(2);
        taskList.add(new LinkedBlockingDeque<>());
        taskList.add(new LinkedBlockingDeque<>());
        backgroundTasks = new LinkedBlockingDeque<>();
        syncTasks = new LinkedBlockingDeque<>();

        threads = new ArrayList<>();

        running = false;

        syncObject = new Object();
    }

    void add(Task task) {
        if (compareFlags(task.getFlag(), Task.Flags.Threadsafe)) {
            if (compareFlags(task.getFlag(), Task.Flags.FrameSync))
                syncTasks.push(task);
            else
                backgroundTasks.push(task);
        } else
            taskList.get(writeList).push(task);
    }

    void execute(Task task) {
        task.run();
        eventChannel.broadcast(new Task.TaskCompleted(task));
    }

    void start() {
        running = true;

        add(new BackgroundDummyClass());

        eventChannel.add(TerminationEvent.class, (TerminationEvent e) -> stop());
        eventChannel.add(Task.TaskCompleted.class, (Task.TaskCompleted e) -> {
            if (compareFlags(e.getTask().getFlag(), Task.Flags.Repeating))
                add(e.getTask());
        });

        int numThreads = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < numThreads; i++) {
            threads.add(new Thread(() -> {
                while (running) {
                    try {
                        Task task = backgroundTasks.take();
                        execute(task);
                        if (compareFlags(task.getFlag(), Task.Flags.FrameSync)) {
                            synchronized (syncObject) {
                                --numTasksToWaitFor;
                                syncObject.notify();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }));
        }

        threads.forEach(Thread::start);

        while (running) {
            if (taskList.get(readList).isEmpty()) {
                synchronize();
                LinkedBlockingDeque<Task> temp = taskList.get(readList);
                taskList.set(readList, taskList.get(writeList));
                taskList.set(writeList, temp);
            }
            try {
                execute(taskList.get(readList).take());
                Thread.yield();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void synchronize() {
        synchronized (syncObject) {

            try {
                while (numTasksToWaitFor > 0)
                    syncObject.wait();

                numTasksToWaitFor = syncTasks.size();

                while (!syncTasks.isEmpty())
                    backgroundTasks.push(syncTasks.pop());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    void stop() {
        running = false;
    }

    private class BackgroundDummyClass extends Task {

        public BackgroundDummyClass() {
            super(Flags.BackgroundRepeating);
        }

        @Override
        void run() {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
