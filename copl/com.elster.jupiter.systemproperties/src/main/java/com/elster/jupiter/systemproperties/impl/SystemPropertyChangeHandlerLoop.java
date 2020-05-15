package com.elster.jupiter.systemproperties.impl;

import com.elster.jupiter.systemproperties.SystemPropertyService;
import com.elster.jupiter.time.TimeDuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemPropertyChangeHandlerLoop implements Runnable {

    private final ScheduledThreadPoolExecutor executor;
    private final SystemPropertyService systemPropertyService;
    private static final Logger LOGGER = Logger.getLogger(SystemPropertyChangeHandlerLoop.class.getName());

    SystemPropertyChangeHandlerLoop(SystemPropertyService systemPropertyService) {
        executor = new ScheduledThreadPoolExecutor(1);
        this.systemPropertyService = systemPropertyService;
    }

    private void updateStatus() {
        systemPropertyService.readAndProcessUpdatedProperties();
    }

    /*Separate thread is used to read system properties from DB by timeout.
     * This mechanism needed to synchronize different instances of connecxo.
     * Message queue can be used only on instances that have configured Application Server.
     * That is why such mechanism was added. But it should be improved. message queue independent from
     * Application server should be introduced. */
    @Override
    public void run() {
        try {
            updateStatus();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            String[] value = systemPropertyService.getPropertyValue("evictiontime").split(":");
            TimeDuration time = new TimeDuration(Integer.valueOf(value[0]), Integer.valueOf(value[1]));
            executor.schedule(this, time.getSeconds(), TimeUnit.SECONDS);
        }
    }

    public void shutDown() {
        executor.shutdownNow();
    }
}
