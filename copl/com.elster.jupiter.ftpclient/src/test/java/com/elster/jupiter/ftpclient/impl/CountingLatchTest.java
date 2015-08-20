package com.elster.jupiter.ftpclient.impl;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class CountingLatchTest {

    private ExecutorService executor;

    @Test(timeout = 1000L)
    public void testAwaitWhenNotBlocked() throws InterruptedException {
        CountingLatch latch = new CountingLatch();
        latch.await();
    }

    @Test
    public void testAwaitWhenBlocked() throws InterruptedException, ExecutionException, TimeoutException {
        CountDownLatch marker = new CountDownLatch(1);
        CountingLatch latch = new CountingLatch();
        latch.acquire();
        executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> future = executor.submit(() -> {
                try {
                    marker.countDown();
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            marker.await();

            try {
                future.get(50, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // expected
            }
            latch.release();
            future.get(50, TimeUnit.MILLISECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test(timeout = 2000)
    public void testAwaitHundredThreads() throws InterruptedException, ExecutionException, TimeoutException {
        CountDownLatch marker = new CountDownLatch(100);
        CountingLatch latch = new CountingLatch();
        executor = Executors.newFixedThreadPool(100);
        try {
            for (int i = 0; i < 100; i++) {
                executor.submit(() -> {
                    latch.acquire();
                    marker.countDown();
                    latch.release();
                });
            }
            marker.await();

            latch.await();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testSeveralAwaits() {
        int count = 5;
        CountDownLatch marker = new CountDownLatch(count);
        CountingLatch latch = new CountingLatch();
        latch.acquire();
        AtomicInteger sum = new AtomicInteger();
        executor = Executors.newFixedThreadPool(count);
        try {
            for (int i = 0; i < count; i++) {
                executor.submit(() -> {
                    try {
                        marker.countDown();
                        latch.await();
                        sum.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            executor.shutdown();
            marker.await();
            latch.release();
            executor.awaitTermination(500, TimeUnit.MILLISECONDS);
            assertThat(sum.get()).isEqualTo(count);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }

    }

}