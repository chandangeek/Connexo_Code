/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CancellableTaskExecutorServiceTest {

    @Test
    public void testShutDownCancelsRunningTaskByCallingCustomCancel() throws InterruptedException {
        CountDownLatch arrival = new CountDownLatch(1);

        CancellableTaskExecutorService service = new CancellableTaskExecutorService(1, Executors.defaultThreadFactory());

        ProvidesCancellableFutureForTest task = new ProvidesCancellableFutureForTest(arrival);

        Future<?> future = service.submit(task);

        arrival.await(); // wait until executing task

        future.cancel(true);

        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);

        assertThat(task.wasCanceled).isTrue();
    }

    @Test
    public void testShutDownCancelsNormalRunnableNormally() throws InterruptedException {
        final CountDownLatch arrival = new CountDownLatch(1);

        CancellableTaskExecutorService service = new CancellableTaskExecutorService(1, Executors.defaultThreadFactory());

        Runnable task = new Runnable() {
            private CountDownLatch waitLatch = new CountDownLatch(1);

            @Override
            public void run() {
                arrival.countDown();
                try {
                    waitLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Future<?> future = service.submit(task);

        arrival.await(); // wait until executing task

        future.cancel(true);

        service.shutdown();
        assertThat(service.awaitTermination(1, TimeUnit.MINUTES)).isTrue();
    }



    private class ProvidesCancellableFutureForTest implements ProvidesCancellableFuture {

        private final CountDownLatch canceled = new CountDownLatch(1);
        private final CountDownLatch arrival;
        private volatile boolean wasCanceled = false;

        private ProvidesCancellableFutureForTest(CountDownLatch arrival) {
            this.arrival = arrival;
        }

        @Override
        public <T> RunnableFuture<T> newTask(T result) {
            return new FutureTask<T>(ProvidesCancellableFutureForTest.this, null) {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    boolean cancel = super.cancel(false);
                    wasCanceled = true;
                    canceled.countDown();
                    return cancel;
                }
            };
        }

        @Override
        public void run() {
            try {
                arrival.countDown();
                canceled.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
