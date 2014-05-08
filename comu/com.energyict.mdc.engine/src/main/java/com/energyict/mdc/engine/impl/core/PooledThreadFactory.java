package com.energyict.mdc.engine.impl.core;

import java.util.concurrent.ThreadFactory;

public class PooledThreadFactory implements ThreadFactory {

    public Thread newThread(Runnable r) {
        return new PooledThread(r);
    }

}
