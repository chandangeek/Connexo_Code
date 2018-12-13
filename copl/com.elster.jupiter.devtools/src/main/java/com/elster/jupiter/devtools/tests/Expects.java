/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Fail.fail;

public enum Expects {
    ;

    public static TimeOutExpectationBuilder1 expect(Runnable runnable) {
        return new TimeOutExpectationBuilder1(runnable);
    }

    public static class TimeOutExpectationBuilder1 {
        private final Runnable runnableUnderTest;

        private TimeOutExpectationBuilder1(Runnable runnableUnderTest) {
            this.runnableUnderTest = runnableUnderTest;
        }

        public TimeOutExpectationBuilder2 toTimeOutAfter(long count, TimeUnit timeUnit) {
            TimeOutExpectation timeOutExpectation = new TimeOutExpectation(runnableUnderTest, count, timeUnit);
            return new TimeOutExpectationBuilder2(timeOutExpectation);
        }

        public void toThrow(Class<? extends Throwable> throwableClass) {
            boolean failWithoutThrowable = false;
            try {
                runnableUnderTest.run();
                failWithoutThrowable = true;
            } catch (Throwable throwable) {
                if (!throwableClass.isInstance(throwable)) {
                    fail("Expected " + throwableClass + " to be thrown but instead got " + throwable.getClass(), throwable);
                }
            }
            if (failWithoutThrowable) {
                fail("Expected " + throwableClass + " to be thrown but nothing was thrown.");
            }
        }
    }

    public static class TimeOutExpectationBuilder2 {
        private final TimeOutExpectation timeOutExpectation;

        private TimeOutExpectationBuilder2(TimeOutExpectation timeOutExpectation) {
            this.timeOutExpectation = timeOutExpectation;
        }

        public void andCancelWith(Runnable runnable) {
            if (!timeOutExpectation.timesOut(runnable)) {
                fail("Expected to time out.");
            }
        }
    }

    public static class TimeOutExpectation {
        private final Runnable runnableUnderTest;
        private final long count;
        private final TimeUnit timeUnit;

        TimeOutExpectation(Runnable runnableUnderTest, long count, TimeUnit timeUnit) {
            this.runnableUnderTest = runnableUnderTest;
            this.count = count;
            this.timeUnit = timeUnit;
        }

        private boolean timesOut(Runnable finisher) {
            AtomicBoolean finished = new AtomicBoolean(false);
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            Future<?> future = executorService.submit(() -> {
                runnableUnderTest.run();
                finished.set(true);
            });
            try {
                future.get(count, timeUnit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            } catch (TimeoutException e) {
                return true;
            } finally {
                finisher.run();
                executorService.shutdownNow();
                try {
                    executorService.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (!executorService.isTerminated()) {
                    fail("Failed to properly cancel timeout thread.");
                }
            }
            return false;
        }
    }

}
