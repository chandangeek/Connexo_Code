package com.energyict.mdc.multisense.api.redknee.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.Checks;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by bvn on 9/16/15.
 */
public class ConsumptionExportGenerator {
    private final Logger logger = Logger.getLogger(ConsumptionExportGenerator.class.getName());

    private final ScheduledThreadPoolExecutor executor;
    private ScheduledFuture<?> scheduledFuture;

    private volatile String path = System.getProperty("java.io.tmpdir");
    private long intervalInSeconds;
    private volatile long consumption;
    private List<UsagePoint> usagePoints = Collections.emptyList();

    public ConsumptionExportGenerator() {
        executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        logger.log(Level.FINEST, "Setting path to "+path);
        this.path = path;
    }

    public long getConsumption() {
        return consumption;
    }

    public void setConsumption(long consumption) {
        logger.log(Level.FINEST, "Setting reported consumption to "+consumption);
        this.consumption = consumption;
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    public long getIntervalInSeconds() {
        return intervalInSeconds;
    }

    public void setIntervalInSeconds(long intervalInSeconds) {
        if (intervalInSeconds!=this.intervalInSeconds) {
            this.intervalInSeconds = intervalInSeconds;
            if (scheduledFuture!=null) {
                logger.log(Level.FINEST, "Cancelling consumption generator");
                scheduledFuture.cancel(false);
            }
            if (intervalInSeconds>0L) {
                logger.log(Level.FINEST, "Scheduling consumption generator for " + intervalInSeconds + " seconds");
                scheduledFuture = executor.scheduleAtFixedRate(new FileGeneratorTask(), 5L, intervalInSeconds, TimeUnit.SECONDS);
            }
        }
    }

    public void setUsagePoints(List<UsagePoint> usagePoints) {
        this.usagePoints = new ArrayList<>(usagePoints);
    }

    public List<UsagePoint> getUsagePoints() {
        return Collections.unmodifiableList(usagePoints);
    }

    class FileGeneratorTask implements Runnable {
        @Override
        public void run() {
            String actualPath = Checks.is(path).emptyOrOnlyWhiteSpace() ? System.getProperty("java.io.tmpdir") : path;
            String fileName = actualPath + "/ICE-" + System.currentTimeMillis() + ".dat";
            try (FileWriter writer = new FileWriter(fileName)) {
                logger.log(Level.FINEST, "Writing file "+fileName);
                writer.write("Consumed "+consumption+"\n");
            } catch (IOException e) {
                System.err.println("Failed to write file "+fileName + ":" +e);
            }
        }
    }
}
