package com.global.threadpool;

import com.global.channels.Channel;

/**
 * @author Róbert Dóczi
 *         Date: 2014.12.02.
 */
public class Main {
    public static void main(String[] args) {
        Channel eventChannel = new Channel();
        TaskManager tm = new TaskManager();

        Task main = new Task(Task.Flags.Repeating) {
            int i = 0;

            @Override
            void run() {
                System.out.println(i++);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Task bg = new Task(Task.Flags.BackgroundRepeating) {
            int i = 0;

            {
                eventChannel.add(TenSec.class, (TenSec e) -> i = 0);
            }

            @Override
            void run() {
                System.out.println("." + i++);
                if(i == 10) eventChannel.broadcast(new TenSec());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            class TenSec{}
        };

        tm.add(main);
        tm.add(bg);

        tm.start();
    }
}
