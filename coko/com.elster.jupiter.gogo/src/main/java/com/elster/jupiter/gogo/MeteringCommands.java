/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.gogo;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.playground.metering.console", service = MeteringCommands.class,
        property = {"osgi.command.scope=metering",
                "osgi.command.function=createMeter",
                "osgi.command.function=createActivation",
                "osgi.command.function=listReadingTypes",
                "osgi.command.function=storeRegisterData",
                "osgi.command.function=storeIntervalData",
                "osgi.command.function=storeCumulativeIntervalData",
                "osgi.command.function=storeFixedUsagePointIntervalData",
                "osgi.command.function=listEndDeviceEventTypes",
                "osgi.command.function=addDeviceEvent",
                "osgi.command.function=listReadingQualityTypes",
                "osgi.command.function=addReadingQuality",
                "osgi.command.function=aggregatedChannelGetReading",
                "osgi.command.function=aggregatedChannelGetReadingBefore",
                "osgi.command.function=aggregatedChannelGetReadingOnorBefore"
        }, immediate = true)
public class MeteringCommands {

    private volatile MeteringService meteringService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Clock clock;
    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Reference
    public void setMeteringService(MeteringService service) {
        this.meteringService = service;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void createMeter(final Long amrId, final String name) {
        Meter meter = executeTransaction(() -> {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            return amrSystem.newMeter(String.valueOf(amrId), name)
                    .create();
        });
        System.out.println("meter = " + meter);
        System.out.println(" id = " + meter.getId());
        System.out.println(" MRID = " + meter.getMRID());
    }

    private Instant parseEffectiveTimestamp(String effectiveTimestamp) {
        try {
            if (effectiveTimestamp == null) {
                return this.clock.instant();
            } else {
                return LocalDate
                        .from(DateTimeFormatter.ISO_LOCAL_DATE.parse(effectiveTimestamp))
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant();
            }
        } catch (DateTimeParseException e) {
            System.out.println("Please respect the following format for the effective timestamp: " + DateTimeFormatter.ISO_LOCAL_DATE.toFormat().toString());
            throw e;
        }
    }

    public void createActivation(long id, String date, final String... readingTypes) {
        final Optional<Meter> endDevice = meteringService.findMeterById(id);
        if (endDevice.isPresent()) {
            try {
                final Instant activationDate = LocalDate.from(dateFormat.parse(date)).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
                MeterActivation activation = executeTransaction(() -> {
                    MeterActivation activate = endDevice.get().activate(activationDate);
                    for (String readingType : readingTypes) {
                        Optional<ReadingType> readingTypeOptional = meteringService.getReadingType(readingType);
                        if (readingTypeOptional.isPresent()) {
                            activate.getChannelsContainer().createChannel(readingTypeOptional.get());
                        } else {
                            System.out.println("Unknown reading type '" + readingType + "'. Skipping.");
                        }
                    }
                    return activate;
                });
                System.out.println("activation = " + activation);
                System.out.println(" id = " + activation.getId());
                for (Channel channel : activation.getChannelsContainer().getChannels()) {
                    System.out.println("  channel " + channel.getId() + " : " + channel.getMainReadingType().getMRID());
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No meter found with id " + id);
        }
    }

    public void storeRegisterData(String deviceName, String readingType, String startDateTime, int intervalInSeconds, int numberOfInterval, double minValue, double maxValue) {
        final Optional<Meter> endDevice = meteringService.findMeterByName(deviceName);
        if (endDevice.isPresent()) {
            try {
                ZonedDateTime startDate = LocalDateTime.from(dateTimeFormat.parse(startDateTime)).atZone(ZoneId.systemDefault());
                Optional<ReadingType> readingTypeOptional = meteringService.getReadingType(readingType);
                if (readingTypeOptional.isPresent()) {
                    if (!readingTypeOptional.get().getMeasuringPeriod().isApplicable()) {
                        final MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                        for (int i = 0; i < numberOfInterval; i++) {
                            meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(randomBetween(minValue, maxValue)), startDate.toInstant()));
                            startDate = startDate.plusSeconds(intervalInSeconds);
                        }
                        executeTransaction(new VoidTransaction() {
                            @Override
                            protected void doPerform() {
                                endDevice.get().store(QualityCodeSystem.MDC, meterReading);
                            }
                        });
                    } else {
                        System.out.println("Reading type is not valid for register data");
                    }

                } else {
                    System.out.println("Unknown reading type '" + readingType + "'. Skipping.");
                }

            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No meter found with name: " + deviceName);
        }
    }

    public void storeIntervalData(String deviceName, String readingType, String startDateTime, int numberOfInterval, double minValue, double maxValue) {
        final Optional<Meter> endDevice = meteringService.findMeterByName(deviceName);
        if (endDevice.isPresent()) {
            try {
                ZonedDateTime startDate = LocalDateTime.from(dateTimeFormat.parse(startDateTime)).atZone(ZoneId.systemDefault());
                Optional<ReadingType> readingTypeOptional = meteringService.getReadingType(readingType);
                if (readingTypeOptional.isPresent()) {
                    int intervalInSeconds = getIntervalInSeconds(readingTypeOptional.get());
                    if (intervalInSeconds > 0) {
                        final MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(readingType);
                        for (int i = 0; i < numberOfInterval; i++) {
                            intervalBlock.addIntervalReading(IntervalReadingImpl.of(startDate.toInstant(), BigDecimal.valueOf(randomBetween(minValue, maxValue)), Collections.emptyList()));
                            startDate = startDate.plusSeconds(intervalInSeconds);
                        }
                        meterReading.addIntervalBlock(intervalBlock);

                        executeTransaction(new VoidTransaction() {
                            @Override
                            protected void doPerform() {
                                endDevice.get().store(QualityCodeSystem.MDC, meterReading);
                            }
                        });
                    } else {
                        System.out.println("Reading type is not valid for interval data");
                    }

                } else {
                    System.out.println("Unknown reading type '" + readingType + "'. Skipping.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No meter found with name: " + deviceName);
        }
    }

    public void storeCumulativeIntervalData(String deviceName, String readingType, String startDateTime, int numberOfInterval, double startValue, double minValue, double maxValue) {
        storeCumulativeIntervalData(deviceName, readingType, startDateTime, numberOfInterval, startValue, minValue, maxValue, null);
    }

    public void storeCumulativeIntervalData(String deviceName, String readingType, String startDateTime, int numberOfInterval, double startValue, double minValue, double maxValue, String readingQualityCIMCode) {
        final Optional<Meter> endDevice = meteringService.findMeterByName(deviceName);
        if (endDevice.isPresent()) {
            try {
                ZonedDateTime startDate = LocalDateTime.from(dateTimeFormat.parse(startDateTime)).atZone(ZoneId.systemDefault());
                Optional<ReadingType> readingTypeOptional = meteringService.getReadingType(readingType);
                if (readingTypeOptional.isPresent()) {
                    try {
                        final MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(readingType);
                        BigDecimal cumulativeValue = BigDecimal.valueOf(startValue);
                        for (int i = 0; i < numberOfInterval; i++) {
                            cumulativeValue = cumulativeValue.add(BigDecimal.valueOf(randomBetween(minValue, maxValue)));
                            IntervalReadingImpl reading = IntervalReadingImpl.of(startDate.toInstant(), cumulativeValue, Collections.emptyList());
                            if (readingQualityCIMCode != null) {
                                reading.addQuality(readingQualityCIMCode);
                            }
                            intervalBlock.addIntervalReading(reading);
                            startDate = nextIntervalTime(startDate, readingTypeOptional.get());
                        }
                        meterReading.addIntervalBlock(intervalBlock);

                        executeTransaction(new VoidTransaction() {
                            @Override
                            protected void doPerform() {
                                endDevice.get().store(QualityCodeSystem.MDC, meterReading);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("Unknown reading type '" + readingType + "'. Skipping.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No meter found with name " + deviceName);
        }
    }

    public void storeFixedUsagePointIntervalData() {
        System.out.println("storeFixedUsagePointIntervalData <usagepoint name> <readingType> <startDateTime yyyyMMddHHmmss> <number of intervals> <value> [readingQualityCIMCodes e.g. 3.12.0]");
        System.out.println("ONLY WORKS FOR BILLING REGISTERS FOR NOW");
    }

    public void storeFixedUsagePointIntervalData(String usagePointName, String readingType, String startDateTime, int numberOfIntervals, double value, String... readingQualityCIMCodes) {
        Optional<UsagePoint> usagePoint = meteringService.findUsagePointByName(usagePointName);
        if (usagePoint.isPresent()) {
            final ZonedDateTime[] startDate = {LocalDateTime.from(dateTimeFormat.parse(startDateTime)).atZone(ZoneId.systemDefault())};
            Instant startFinalInstant = startDate[0].toInstant();
            Optional<ReadingType> readingTypeOptional = meteringService.getReadingType(readingType);
            if (readingTypeOptional.isPresent()) {
                Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfiguration = usagePoint.get().getEffectiveMetrologyConfiguration(startFinalInstant);
                try {
                    executeTransaction(new VoidTransaction() {
                        @Override
                        protected void doPerform() {
                            if (effectiveMetrologyConfiguration.isPresent()) {
                                ReadingStorer readingStorer = meteringService.createOverrulingStorer();
                                List<Channel> channels = effectiveMetrologyConfiguration.map(effective ->
                                        effective.getChannelsContainer(effectiveMetrologyConfiguration.get()
                                                .getMetrologyConfiguration()
                                                .getContracts()
                                                .stream()
                                                .filter(metrologyContract -> metrologyContract.getMetrologyPurpose().getName().equals("Billing"))
                                                .findAny()
                                                .get()))
                                        .flatMap(cc -> cc.map(ccc -> ccc.getChannels().stream())).map(s -> s.collect(Collectors.toList())).orElse(Collections.emptyList());
                                channels.stream()
                                        .filter(channel -> channel.getMainReadingType().equals(readingTypeOptional.get()))
                                        .forEach(channel -> {
                                            CimChannel cimChannel = channel.getCimChannel(channel.getMainReadingType()).orElseThrow(IllegalArgumentException::new);
                                            BigDecimal givenValue = BigDecimal.valueOf(value);
                                            for (int i = 0; i < numberOfIntervals; i++) {
                                                IntervalReadingImpl reading = IntervalReadingImpl.of(startDate[0].toInstant(), givenValue);
                                                Arrays.stream(readingQualityCIMCodes)
                                                        .filter(readingQualityCIMCode -> readingQualityCIMCode != null)
                                                        .forEach(readingQualityCode -> cimChannel.createReadingQuality(new ReadingQualityType(readingQualityCode), reading));
                                                readingStorer.addReading(cimChannel, reading);
                                                startDate[0] = nextIntervalTime(startDate[0], readingTypeOptional.get());
                                            }
                                        });

                                readingStorer.execute(QualityCodeSystem.MDM);
                            } else {
                                System.out.println("No effictive metrology configuration at given start time: " + startDateTime);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Unknown reading type '" + readingType + "'. Skipping.");
            }
        } else {
            System.out.println("No usage point with name " + usagePointName);
        }
    }

    private ZonedDateTime calculateEndDate(ZonedDateTime startDate, ReadingType readingType, int numberOfIntervals) {
        for (int i = 0; i < numberOfIntervals; i++) {
            startDate = nextIntervalTime(startDate, readingType);
        }
        return startDate;
    }

    private ZonedDateTime nextIntervalTime(ZonedDateTime time, ReadingType readingType) {
        MacroPeriod macroPeriod = readingType.getMacroPeriod();
        if (macroPeriod.getId() != 0) {
            switch (macroPeriod) {
                case DAILY:
                    return time.plusDays(1);
                case MONTHLY:
                    return time.plusMonths(1);
                case YEARLY:
                    return time.plusYears(1);
                case WEEKLYS:
                    return time.plusWeeks(1);
                default:
                    throw new IllegalArgumentException("Unsupported macro period: " + macroPeriod.toString());
            }

        } else {
            return time.plusSeconds(getIntervalInSeconds(readingType));
        }
    }

    private int getIntervalInSeconds(ReadingType readingType) {
        return readingType.getMeasuringPeriod().getMinutes() * 60;
    }

    private double randomBetween(double minValue, double maxValue) {
        return (Math.random() * (maxValue - minValue)) + minValue;
    }

    public void listReadingTypes(int... timeAttribute) {
        try {
            List<ReadingType> availableReadingTypes = meteringService.getAvailableReadingTypes();
            Collections.sort(availableReadingTypes, Comparator.comparing(ReadingType::getName));
            Set<TimeAttribute> timeAttributeFilter = new HashSet<>();
            if (timeAttribute.length == 0) {
                timeAttributeFilter.addAll(Arrays.asList(TimeAttribute.values()));
            } else {
                for (int timeAttributeId : timeAttribute) {
                    timeAttributeFilter.add(TimeAttribute.get(timeAttributeId));

                }
            }
            System.out.println("|\t" + String.format("%-80s", "Name") + "\t|\t" + String.format("%-36s", "mRID") + "\t|\tDescription\t|");
            for (ReadingType readingType : availableReadingTypes) {
                if (timeAttributeFilter.contains(readingType.getMeasuringPeriod())) {
                    System.out.println("|\t" + String.format("%-80s", readingType.getName()) + "\t|\t" + readingType.getMRID() + "\t|\t" + readingType.getDescription() + "\t|");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listEndDeviceEventTypes(String... filter) {
        try {
            List<EndDeviceEventType> availableReadingTypes = meteringService.getAvailableEndDeviceEventTypes();
            Collections.sort(availableReadingTypes, Comparator.comparing(EndDeviceEventType::getName));

            EndDeviceEventTypeFilter endDeviceEventTypeFilter = new EndDeviceEventTypeFilter(filter);
            System.out.println("|\t" + String.format("%-80s", "Name") + "\t|\t" + String.format("%-13s", "mRID") + "\t|\tDescription\t|");
            for (EndDeviceEventType readingType : availableReadingTypes) {
                if (endDeviceEventTypeFilter.isApplicable(readingType)) {
                    System.out.println("|\t" + String.format("%-80s", readingType.getName()) + "\t|\t" + String.format("%-13s", readingType.getMRID()) + "\t|\t" + readingType.getDescription() + "\t|");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addDeviceEvent(long meterId, String mRID, final String dateTime, long logBookId) {
        final Optional<Meter> endDevice = meteringService.findMeterById(meterId);
        if (endDevice.isPresent()) {
            final Meter meter = endDevice.get();
            final Optional<EndDeviceEventType> type = meteringService.getEndDeviceEventType(mRID);
            if (type.isPresent()) {
                executeTransaction(new VoidTransaction() {
                    @Override
                    protected void doPerform() {
                        try {
                            meter.addEventRecord(type.get(), LocalDateTime.from(dateTimeFormat.parse(dateTime)).atZone(ZoneId.systemDefault()).toInstant(), logBookId).create();
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } else {
                System.out.println("No EndDeviceEventType found with mRID \'" + mRID + "\'");
            }
        } else {
            System.out.println("No meter found with id " + meterId);
        }
    }

    public void listReadingQualityTypes(String... filter) {
        Pattern pattern = filter.length > 0 ? Pattern.compile(filter[0], Pattern.CASE_INSENSITIVE) : Pattern.compile(".*");
        System.out.println("|\t" + String.format("%-80s", "Name") + "\t|\t" + String.format("%-13s", "Code"));
        Arrays.stream(QualityCodeIndex.values())
                .filter(index -> Arrays.asList(
                        QualityCodeCategory.DIAGNOSTICS,
                        QualityCodeCategory.POWERQUALITY,
                        QualityCodeCategory.TAMPER,
                        QualityCodeCategory.DATACOLLECTION
                ).contains(index.category()))
                .map(index -> Pair.of(index.getTranslationKey().getDefaultFormat(), QualityCodeSystem.MDC.ordinal() + "." + index.category().ordinal() + "." + index.index()))
                .filter(pair -> pattern.matcher(pair.getFirst()).matches() || pattern.matcher(pair.getLast()).matches())
                .forEach(pair -> System.out.println("|\t" + String.format("%-80s", pair.getFirst()) + "\t|\t" + String.format("%-13s", pair.getLast())));
    }

    public void addReadingQuality(String deviceName, String readingTypeMRID, String time, String readingQualityCode) {
        //find meter
        Optional<Meter> endDevice = meteringService.findMeterByName(deviceName);
        if (!endDevice.isPresent()) {
            System.out.println("No meter found with name " + deviceName);
            return;
        }
        //find reading type
        Optional<ReadingType> readingType = meteringService.getReadingType(readingTypeMRID);
        if (!readingType.isPresent()) {
            System.out.println("Unknown reading type '" + readingTypeMRID + "'. Skipping.");
            return;
        }
        //parse timestamp
        ZonedDateTime timeStamp = LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss").parse(time)).atZone(ZoneId.systemDefault());
        //find meter activation
        Optional<? extends MeterActivation> meterActivation = endDevice.get().getMeterActivation(timeStamp.toInstant());
        if (!meterActivation.isPresent()) {
            System.out.println("No meter activation found at " + time);
            return;
        }
        //find channel
        Optional<Channel> channel = meterActivation.get().getChannelsContainer().getChannels().stream().filter(ch -> ch.getReadingTypes().contains(readingType.get())).findFirst();
        if (!channel.isPresent()) {
            System.out.println("No channel found for reading type " + readingTypeMRID);
            return;
        }
        //validate reading quality code
        ReadingQualityType readingQualityType = new ReadingQualityType(readingQualityCode);
        try {
            if (!readingQualityType.system().isPresent() || !readingQualityType.category().isPresent() || !readingQualityType.qualityIndex().isPresent()) {
                System.out.println("Reading quality code is not valid: " + readingQualityCode);
                return;
            }
        } catch (Exception e) {
            System.out.println("Reading quality code is not valid: " + readingQualityCode);
            return;
        }
        //create reading quality
        try {
            executeTransaction(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    channel.get().createReadingQuality(readingQualityType, readingType.get(), timeStamp.toInstant());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void aggregatedChannelGetReading(String usagePointName, String contractName, String readingTypeMRID, String dateTime) {
        //find meter
        Optional<UsagePoint> usagePoint = meteringService.findUsagePointByName(usagePointName);
        if (!usagePoint.isPresent()) {
            System.out.println("No usagepoint found with name " + usagePoint);
            return;
        }
        //find reading type
        Optional<ReadingType> readingType = meteringService.getReadingType(readingTypeMRID);
        if (!readingType.isPresent()) {
            System.out.println("Unknown reading type '" + readingTypeMRID + "'. Skipping.");
            return;
        }
        //parse timestamp
        ZonedDateTime timeStamp = LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(dateTime)).atZone(ZoneId.systemDefault());

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = usagePoint.get().getCurrentEffectiveMetrologyConfiguration().get();
        Optional<MetrologyContract> metrologyContract = effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(mc -> mc.getMetrologyPurpose().getName().equals(contractName))
                .findFirst();

        if (!metrologyContract.isPresent()) {
            System.out.println("No contract found with name " + contractName);
        }

        transactionService.execute(() -> {
            Optional<BaseReadingRecord> reading = effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract.get(), readingType.get()).get().getReading(timeStamp.toInstant());
            printReading(reading.get());
            return null;
        });

    }

    private void printReading(BaseReadingRecord readingRecord) {
        StringBuilder builder = new StringBuilder(readingRecord.toString()).append("\n");
        builder.append("  ").append("timeStamp ").append(readingRecord.getTimeStamp().atZone(ZoneId.systemDefault())).append("\n");
        builder.append("  ").append("reportedTimeStamp ").append(readingRecord.getReportedDateTime().atZone(ZoneId.systemDefault())).append("\n");
        builder.append("  ").append("value ").append(readingRecord.getValue()).append("\n");
        System.out.println(builder.toString());
    }

    public void aggregatedChannelGetReadingBefore(String usagePointName, String contractName, String readingTypeMRID, String dateTime, int readingCount) {
        //find meter
        Optional<UsagePoint> usagePoint = meteringService.findUsagePointByName(usagePointName);
        if (!usagePoint.isPresent()) {
            System.out.println("No usagepoint found with name " + usagePoint);
            return;
        }
        //find reading type
        Optional<ReadingType> readingType = meteringService.getReadingType(readingTypeMRID);
        if (!readingType.isPresent()) {
            System.out.println("Unknown reading type '" + readingTypeMRID + "'. Skipping.");
            return;
        }
        //parse timestamp
        ZonedDateTime timeStamp = LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(dateTime)).atZone(ZoneId.systemDefault());

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = usagePoint.get().getCurrentEffectiveMetrologyConfiguration().get();
        Optional<MetrologyContract> metrologyContract = effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(mc -> mc.getMetrologyPurpose().getName().equals(contractName))
                .findFirst();

        if (!metrologyContract.isPresent()) {
            System.out.println("No contract found with name " + contractName);
        }

        transactionService.execute(() -> {
            List<BaseReadingRecord> readingsBefore = effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract.get(), readingType.get())
                    .get()
                    .getReadingsBefore(timeStamp.toInstant(), readingCount);
            readingsBefore.forEach(this::printReading);
            return null;
        });
    }

    public void aggregatedChannelGetReadingOnorBefore(String usagePointName, String contractName, String readingTypeMRID, String dateTime, int readingCount) {
        //find meter
        Optional<UsagePoint> usagePoint = meteringService.findUsagePointByName(usagePointName);
        if (!usagePoint.isPresent()) {
            System.out.println("No usagepoint found with name " + usagePoint);
            return;
        }
        //find reading type
        Optional<ReadingType> readingType = meteringService.getReadingType(readingTypeMRID);
        if (!readingType.isPresent()) {
            System.out.println("Unknown reading type '" + readingTypeMRID + "'. Skipping.");
            return;
        }
        //parse timestamp
        ZonedDateTime timeStamp = LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(dateTime)).atZone(ZoneId.systemDefault());

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = usagePoint.get().getCurrentEffectiveMetrologyConfiguration().get();
        Optional<MetrologyContract> metrologyContract = effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(mc -> mc.getMetrologyPurpose().getName().equals(contractName))
                .findFirst();

        if (!metrologyContract.isPresent()) {
            System.out.println("No contract found with name " + contractName);
        }

        transactionService.execute(() -> {
            List<BaseReadingRecord> readingsBefore = effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract.get(), readingType.get())
                    .get()
                    .getReadingsOnOrBefore(timeStamp.toInstant(), readingCount);
            readingsBefore.forEach(this::printReading);
            return null;
        });
    }

    private <T> T executeTransaction(ExceptionThrowingSupplier<T, RuntimeException> transaction) {
        setPrincipal();
        try {
            return transactionService.execute(transaction);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            clearPrincipal();
        }
    }

    private void setPrincipal() {
        threadPrincipalService.set(getPrincipal());
    }

    private void clearPrincipal() {
        threadPrincipalService.clear();
    }

    private Principal getPrincipal() {
        return () -> "console";
    }

}
