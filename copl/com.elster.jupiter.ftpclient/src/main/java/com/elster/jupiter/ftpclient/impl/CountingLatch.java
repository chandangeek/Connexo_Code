package com.elster.jupiter.ftpclient.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

class CountingLatch {

    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == 0;
        }

        protected int tryAcquireShared(int acquires) {
            // Increment count; signal when transition to zero
            for (;;) {
                int c = getState();
                int nextc = c+1;
                if (compareAndSetState(c, nextc)) {
                    return 1;
                }
            }
        }

        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0) {
                    return false;
                }
                int nextc = c-1;
                if (compareAndSetState(c, nextc)) {
                    return nextc == 0;
                }
            }
        }
    }

    private final Sync sync;

    public CountingLatch() {
        this.sync = new Sync(0);
    }

    public void await() throws InterruptedException {
        sync.acquire(1);
    }

    public boolean await(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    public void release() {
        sync.releaseShared(1);
    }

    public void acquire() {
        sync.acquireShared(1);
    }

    public long getCount() {
        return sync.getCount();
    }

}
