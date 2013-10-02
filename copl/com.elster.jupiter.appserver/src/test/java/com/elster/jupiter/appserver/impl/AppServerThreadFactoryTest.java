package com.elster.jupiter.appserver.impl;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppServerThreadFactoryTest {

    @Test
    public void testAppServerThreadFactory() throws InterruptedException {
        ThreadGroup group = new ThreadGroup("tes");

        ExecutorService executorService = Executors.newSingleThreadExecutor(new AppServerThreadFactory(group, "name", new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        }));

        final CountDownLatch latch = new CountDownLatch(1);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });

        latch.await();
    }

}
