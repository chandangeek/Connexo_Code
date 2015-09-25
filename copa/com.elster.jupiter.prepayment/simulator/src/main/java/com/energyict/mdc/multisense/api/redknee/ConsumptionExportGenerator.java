package com.energyict.mdc.multisense.api.redknee;


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
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by bvn on 9/16/15.
 */
public class ConsumptionExportGenerator {
    //    private final String csvFormat = "D,{0,string},{1,string},{2,string},{3,string},{4},{5,string},{6,number}";
    private final String csvFormat = "D|{0}|{1}|{2}|{3}|{4,number,#}|{5}|{6,number,#########.##########}";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Clock clock;
    private final ScheduledThreadPoolExecutor executor;

    private ScheduledFuture<?> scheduledFuture;
    private volatile String path = System.getProperty("java.io.tmpdir");
    private long outputFrequency;
    private List<UsagePoint> usagePoints = Collections.emptyList();
    private Integer timeAcceleration;

    public ConsumptionExportGenerator(Clock clock, ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        this.clock = clock;
        executor = scheduledThreadPoolExecutor;
        executor.setRemoveOnCancelPolicy(true);
    }

    public void start() {
        if (scheduledFuture != null) {
            System.out.println("Cancelling export simulator task");
            scheduledFuture.cancel(false);
        }
        if (outputFrequency > 0L) {
            System.out.println("Scheduling export simulation task every " + outputFrequency + " seconds");
            ReadingType readingType = new ReadingType(usagePoints.get(0).getReadingType());
            ZonedDateTime now = ZonedDateTime.now(clock);
            long delayToNextExportGeneration = outputFrequency - (now.getLong(ChronoField.SECOND_OF_DAY) % outputFrequency);
            ZonedDateTime simulationStartTime = now.minusMinutes(readingType.getMeasuringPeriod() * timeAcceleration * 24);
            simulationStartTime = simulationStartTime.plusSeconds((readingType.getMeasuringPeriod() * 60) - simulationStartTime.get(ChronoField.SECOND_OF_DAY) % (readingType.getMeasuringPeriod() * 60));
            long readingsPerExportGeneration = (outputFrequency * timeAcceleration) / (readingType.getMeasuringPeriod()*60);
            ExportGeneratorTask exportGeneratorTask = new ExportGeneratorTask(simulationStartTime, Duration.ofMinutes(readingType.getMeasuringPeriod()), readingsPerExportGeneration);
            scheduledFuture = executor.scheduleAtFixedRate(exportGeneratorTask, delayToNextExportGeneration, outputFrequency, TimeUnit.SECONDS);
        }

    }

    public void shutdown() {
        executor.shutdownNow();
    }

    public void setConfiguration(Configuration configuration) {
        this.path = configuration.getDestinationFilePath();
        this.usagePoints = new ArrayList<>(configuration.getUsagePoints());
        this.outputFrequency = configuration.getOutputFrequency();
        this.timeAcceleration = configuration.getTimeAcceleration();
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
            String actualPath = (path == null || path.trim().isEmpty()) ? System.getProperty("java.io.tmpdir") : path;
            String fileName = actualPath + "/ICE_CIM_profile1_" +
                    now.format(dateTimeFormatter) + "_" +
                    now.toEpochSecond() + ".dat";
            System.out.println(now);
            try (FileWriter writer = new FileWriter(fileName)) {
                System.out.println("Writing file " + fileName);
                IntStream.range(0, (int) readingsPerTaskRun).forEach(iteration-> {
                    usagePoints.stream()
                            .map(usagePoint ->
                                            MessageFormat.format(csvFormat,
                                                    usagePoint.getDevice_mRID(),
                                                    usagePoint.getmRID(),
                                                    usagePoint.getReadingType(),
                                                    "3.0.0",
                                                    now.toEpochSecond(),
                                                    now.getOffset(),
                                                    usagePoint.getStatus() == Status.connected ? usagePoint.getConsumption() : 0L
                                            )
                            ).forEach((str) -> {
                        try {
                            System.out.println(str);
                            writer.write(str + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    now = now.plus(duration);
                });
            } catch (IOException e) {
                System.err.println("Failed to write file " + fileName + ":" + e);
            }
        }

        ZonedDateTime getNow() {
            return now;
        }

        long getReadingsPerTaskRun() {
            return readingsPerTaskRun;
        }
    }
}
