/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by bvn on 9/16/15.
 */
public class ConsumptionExportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ConsumptionExportGenerator.class);
    private final String csvFormat = "D|{0}|{1}|{2}|{3}|{4,number,#}|{5}|{6,number,#########.##########}";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Clock clock;
    private final ScheduledThreadPoolExecutor executor;

    private ScheduledFuture<?> scheduledFuture;
    private volatile String path = System.getProperty("java.io.tmpdir");
    private long outputFrequency;
    private List<UsagePoint> usagePoints = Collections.emptyList();
    private Integer timeAcceleration;
    private ReadingType readingType;

    public ConsumptionExportGenerator(Clock clock, ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        this.clock = clock;
        executor = scheduledThreadPoolExecutor;
        executor.setRemoveOnCancelPolicy(true);
    }

    public void start() {
        if (scheduledFuture != null) {
            logger.debug("Cancelling export simulator task");
            scheduledFuture.cancel(false);
        }
        if (outputFrequency > 0L) {
            logger.info("Scheduling export simulation task every " + outputFrequency + " seconds");
            ZonedDateTime now = ZonedDateTime.now(clock);
            long delayToNextExportGeneration = outputFrequency - (now.getLong(ChronoField.SECOND_OF_DAY) % outputFrequency);
            // go back in time to simulate readings for 24 hour without exceeding current time
            ZonedDateTime simulationStartTime = now.minusMinutes(readingType.getMeasuringPeriod() * timeAcceleration * 24);
            simulationStartTime = simulationStartTime.plusSeconds((readingType.getMeasuringPeriod() * 60) - simulationStartTime.get(ChronoField.SECOND_OF_DAY) % (readingType.getMeasuringPeriod() * 60));
            simulationStartTime = simulationStartTime.with(ChronoField.MILLI_OF_SECOND,0);
            logger.info("Simulation start date& time is "+simulationStartTime);
            long readingsPerExportGeneration = (outputFrequency * timeAcceleration) / (readingType.getMeasuringPeriod()*60);
            logger.info("Every export file will cover a simulated timespan of "+(outputFrequency*timeAcceleration)+" seconds, thus containing "+readingsPerExportGeneration+" readings per usage point");
            if (readingsPerExportGeneration==0) {
                throw new IllegalStateException("There would be no readings in the export files with the configured settings");
            }
            ExportGeneratorTask exportGeneratorTask = new ExportGeneratorTask(simulationStartTime, Duration.ofMinutes(readingType.getMeasuringPeriod()), readingsPerExportGeneration);
            scheduledFuture = executor.scheduleAtFixedRate(exportGeneratorTask, delayToNextExportGeneration, outputFrequency, TimeUnit.SECONDS);
        }

    }

    public void shutdown() {
        executor.shutdownNow();
    }

    public void setConfiguration(Configuration configuration) {
        validateConfig(configuration);
        this.path = configuration.getDestinationFilePath();
        this.usagePoints = new ArrayList<>(configuration.getUsagePoints());
        this.outputFrequency = configuration.getOutputFrequency();
        this.timeAcceleration = configuration.getTimeAcceleration();
        this.readingType = new ReadingType(configuration.getReadingType());
    }

    private void validateConfig(Configuration configuration) {
        if (configuration.getDestinationFilePath()==null || configuration.getDestinationFilePath().trim().isEmpty()) {
            throw new IllegalArgumentException("'destinationFilePath' is missing in the configuration");
        }
        if (configuration.getUsagePoints()==null || configuration.getUsagePoints().isEmpty()) {
            throw new IllegalArgumentException("'usagePoints' is empty, nothing to do");
        } else {
            configuration.getUsagePoints().forEach(usagePoint -> {
                if (usagePoint.getmRID()==null||usagePoint.getmRID().trim().isEmpty()) {
                    throw new IllegalArgumentException("'mRID' is empty on usage point "+(configuration.getUsagePoints().indexOf(usagePoint)+1));
                }
                if (usagePoint.getDevice_mRID()==null||usagePoint.getDevice_mRID().trim().isEmpty()) {
                    throw new IllegalArgumentException("'device_mRID' is empty on usage point "+(configuration.getUsagePoints().indexOf(usagePoint)+1));
                }
            });
        }
        if (configuration.getOutputFrequency()==null || configuration.getOutputFrequency()==0) {
            throw new IllegalArgumentException("'outputFrequency' is empty or 0");
        }
        if (configuration.getReadingType()==null) {
            throw new IllegalArgumentException("'readingType' is missing");
        }
        if (configuration.getTimeAcceleration()==null || configuration.getTimeAcceleration()==0) {
            throw new IllegalArgumentException("'timeAcceleration' is missing");
        }
    }

    public Optional<UsagePoint> getUsagePoint(String mRID) {
        return usagePoints.stream().filter(usagePoint -> usagePoint.getmRID().equals(mRID)).findAny();
    }

    class ExportGeneratorTask implements Runnable {
        private ZonedDateTime now;
        private final Duration duration;
        private final long readingsPerTaskRun;

        public ExportGeneratorTask(ZonedDateTime startTime, Duration increment, long readingsPerTaskRun) {
            now = startTime;
            this.duration = increment;
            this.readingsPerTaskRun = readingsPerTaskRun;
        }

        @Override
        public void run() {
            if (now.isAfter(ZonedDateTime.now(clock))) {
                logger.error("Simulated time has caught up with real time: will not create export file");
                throw new IllegalStateException("Simulated time has caught up with real time: will not create export file");
            }
            String actualPath = (path == null || path.trim().isEmpty()) ? System.getProperty("java.io.tmpdir") : path;
            String fileName = actualPath + "/Elster_profile1_" +
                    now.format(dateTimeFormatter) + "_" +
                    now.toEpochSecond() + ".dat";
            try (FileWriter writer = new FileWriter(fileName)) {
                logger.debug("Writing file " + fileName);
                IntStream.range(0, (int) readingsPerTaskRun).forEach(iteration-> {
                    usagePoints.stream()
                            .map(usagePoint ->
                                    MessageFormat.format(csvFormat,
                                            usagePoint.getDevice_mRID(),
                                            usagePoint.getmRID(),
                                            readingType,
                                            "3.0.0",
                                            now.toEpochSecond(),
                                            now.getOffset(),
                                            usagePoint.getStatus() == Status.connected ? randomize(usagePoint.getConsumption()) : 0.0)
                            ).forEach((str) -> {
                                try {
                                    logger.trace(now.toString());
                                    writer.write(str + "\n");
                                } catch (IOException e) {
                                    logger.error("Failed to write in file " + fileName + ":" + e);
                                }
                    });
                    now = now.plus(duration);
                });
            } catch (IOException e) {
                logger.error("Failed to write file " + fileName + ":" + e);
            }
        }

        private double randomize(MinMax consumption) {
            return consumption.getMin()+(ThreadLocalRandom.current().nextDouble()*(consumption.getMax()-consumption.getMin()));
        }

        ZonedDateTime getNow() {
            return now;
        }

        long getReadingsPerTaskRun() {
            return readingsPerTaskRun;
        }
    }
}
