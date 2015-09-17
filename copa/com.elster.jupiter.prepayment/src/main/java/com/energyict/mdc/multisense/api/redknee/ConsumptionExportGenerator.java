package com.energyict.mdc.multisense.api.redknee;


import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by bvn on 9/16/15.
 */
public class ConsumptionExportGenerator {
//    private final String csvFormat = "D,{0,string},{1,string},{2,string},{3,string},{4},{5,string},{6,number}";
    private final String csvFormat = "D,{0},{1},{2},{3},{4,number,#},{5},{6,number,#}";

    private ScheduledThreadPoolExecutor executor;
    private ScheduledFuture<?> scheduledFuture;

    private volatile String path = System.getProperty("java.io.tmpdir");
    private long intervalInSeconds;
    private List<UsagePoint> usagePoints = Collections.emptyList();

    public ConsumptionExportGenerator() {
        executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
    }

    public void start() {
        if (scheduledFuture!=null) {
            System.out.println("Cancelling export simulator task");
            scheduledFuture.cancel(false);
        }
        if (intervalInSeconds>0L) {
            System.out.println("Scheduling export simulation task for " + intervalInSeconds + " seconds");
            scheduledFuture = executor.scheduleAtFixedRate(new FileGeneratorTask(), 0L, intervalInSeconds, TimeUnit.SECONDS);
        }

    }

    public void shutdown() {
        executor.shutdownNow();
    }

    public void setConfiguration(Configuration configuration) {
        this.path = configuration.getDestinationFilePath();
        this.usagePoints = new ArrayList<>(configuration.getUsagePoints());
        this.intervalInSeconds = configuration.getOutputFrequency();

    }

    class FileGeneratorTask implements Runnable {
        @Override
        public void run() {
            Date now = new Date();
            String actualPath = (path==null || path.trim().isEmpty()) ? System.getProperty("java.io.tmpdir") : path;
            String fileName = actualPath + "/ICE-" + now.getTime() + ".dat";
            try (FileWriter writer = new FileWriter(fileName)) {
                System.out.println("Writing file " + fileName);
                usagePoints.stream()
                    .map(usagePoint ->
                        MessageFormat.format(csvFormat,
                                usagePoint.getDevice_mRID(),
                                usagePoint.getmRID(),
                                usagePoint.getReadingType(),
                                "3.0.0",
                                now.getTime(),
                                "+00:00",
                                usagePoint.getStatus()==Status.connected?usagePoint.getConsumption():0L
                        )
                    ).forEach((str) -> {
                    try {
                        System.out.println(str);
                        writer.write(str + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                System.err.println("Failed to write file "+fileName + ":" +e);
            }
            System.out.println("Wrote file " + fileName);
        }
    }
}
