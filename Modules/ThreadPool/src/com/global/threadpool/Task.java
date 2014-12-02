package com.global.threadpool;

/**
 * @author Róbert Dóczi
 *         Date: 2014.12.02.
 */
public abstract class Task {

    private Flags flag;

    public Task(Flags flag) {
        this.flag = flag;
    }

    abstract void run();

    public Flags getFlag() {
        return flag;
    }

    public static class TaskCompleted {
        private Task task;

        public TaskCompleted(Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }
    }

    public enum Flags implements BitField {
        NoFlag(0x0),
        AllFlag(~0x0),

        Repeating(0x1),
        Threadsafe(0x1 << 1),
        FrameSync(0x1 << 2),

        SingleThreader(NoFlag.value),
        SingleThreaderRepeating(Repeating.value),

        Background(Threadsafe.value),
        BackgroundRepeating(Threadsafe.value | Repeating.value),

        BackgroundSync(Threadsafe.value | FrameSync.value),
        BackgroundRepeatingSync(Threadsafe.value | Repeating.value | FrameSync.value)
        ;

        private int value;

        Flags(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
