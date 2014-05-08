package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.Environment;

public class PooledThread extends Thread {

    public PooledThread(Runnable r) {
        super(r);
    }

    public void run() {
        try {
            super.run();
        } finally {
            Environment.DEFAULT.get().closeConnection();
        }
    }
}
