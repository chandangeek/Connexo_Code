/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public abstract class BasicLongCounterTest {

    abstract LongCounter newLongCounter();

    abstract boolean allowsDecrements();

    abstract boolean allowsNegativeTotals();

    boolean isThreadSafe() {
    	return false;
    }

    @Test
    public void testNewStartsAtZero() {
        assertThat(newLongCounter().getValue()).isEqualTo(0);
    }

    @Test
    public void testIncrementOnce() {
        LongCounter counter = newLongCounter();
        counter.increment();
        assertThat(counter.getValue()).isEqualTo(1);
    }

    @Test
    public void testIncrementMoreThanOnce() {
        LongCounter counter = newLongCounter();
        for (int i = 1; i < 5; i++) {
            counter.increment();
            assertThat(counter.getValue()).isEqualTo(i);
        }
    }


    @Test
    public void testReset() {
        LongCounter counter = newLongCounter();
        for (int i = 1; i < 5; i++) {
            counter.increment();
        }
        counter.reset();
        assertThat(counter.getValue()).isEqualTo(0);
    }

    @Test
    public void testIncrementByValue() {
        LongCounter counter = newLongCounter();
        counter.add(5);
        assertThat(counter.getValue()).isEqualTo(5);
    }

    @Test
    public void testIncrementByValueMultipleTimes() {
        LongCounter counter = newLongCounter();
        for (int i = 1; i < 5; i++) {
            counter.add(2);
            assertThat(counter.getValue()).isEqualTo(2 * i);
        }
    }

    @Test
    public void testDisallowingDecrements() {
        if (!allowsDecrements()) {
            try {
                LongCounter counter = newLongCounter();
                counter.add(7);
                counter.add(-5);
                fail("expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // pass
            }
        }
    }

    @Test
    public void testDecrement() {
        if (allowsDecrements()) {
            LongCounter counter = newLongCounter();
            counter.add(7);
            counter.add(-5);
            assertThat(counter.getValue()).isEqualTo(2);
        }
    }

    @Test
    public void testNegativeTotal() {
        if (allowsDecrements() && allowsNegativeTotals()) {
            LongCounter counter = newLongCounter();
            counter.add(2);
            counter.add(-5);
            assertThat(counter.getValue()).isEqualTo(-3);
        }
    }

    @Test
    public void testDisallowNegativeTotal() {
        if (allowsDecrements() && !allowsNegativeTotals()) {
            try {
                LongCounter counter = newLongCounter();
                counter.add(2);
                counter.add(-5);
                assertThat(counter.getValue()).isEqualTo(-3);
                fail("expected IllegalArgumentException");
            } catch (Exception e) {
                // pass
            }
        }
    }

    @Test
    public void testThreadSafe() throws InterruptedException {
        if (isThreadSafe()) {
            final LongCounter counter = newLongCounter();

            int nThreads = 10;
            final int countPerThread = 100;
            ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

            final CountDownLatch starter = new CountDownLatch(nThreads);

            for (int i = 0; i < nThreads; i++) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        starter.countDown();
                        for (int j = 0; j < countPerThread; j++) {
                            counter.increment();
                        }
                    }
                });
            }

            starter.countDown();

            executorService.shutdown();

            executorService.awaitTermination(5, TimeUnit.SECONDS);

            assertThat(counter.getValue()).isEqualTo(nThreads * countPerThread);
        }
    }

}
