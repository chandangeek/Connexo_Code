package com.elster.jupiter.gogo;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.Pair;

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

/**
 * Copyrights EnergyICT
 * Date: 17/06/2014
 * Time: 18:00
 */
@Component(name = "com.elster.jupiter.playground.metering.console", service = MeteringCommands.class,
        property = {"osgi.command.scope=metering",
                "osgi.command.function=createMeter",
                "osgi.command.function=createActivation",
                "osgi.command.function=listReadingTypes",
                "osgi.command.function=storeRegisterData",
                "osgi.command.function=storeIntervalData",
                "osgi.command.function=storeCumulativeIntervalData",
                "osgi.command.function=listEndDeviceEventTypes",
                "osgi.command.function=addDeviceEvent",
                "osgi.command.function=listReadingQualityTypes",
                "osgi.command.function=addReadingQuality"
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

    public void createMeter(final Long amrId, final String mrId) {
        Meter meter = executeTransaction(new Transaction<Meter>() {
            @Override
            public Meter perform() {
                AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
                Meter meter = amrSystem.newMeter(String.valueOf(amrId))
                        .setMRID(mrId)
                        .create();
                return meter;
            }
        });
        System.out.println("meter = " + meter);
        System.out.println(" id = " + meter.getId());
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
        final Optional<Meter> endDevice = meteringService.findMeter(id);
        if (endDevice.isPresent()) {
            try {
                final Instant activationDate = LocalDate.from(dateFormat.parse(date)).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
                MeterActivation activation = executeTransaction(new Transaction<MeterActivation>() {
                    @Override
                    public MeterActivation perform() {
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
                    }
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

    public void storeRegisterData(String mRID, String readingType, String startDateTime, int intervalInSeconds, int numberOfInterval, double minValue, double maxValue) {
        final Optional<Meter> endDevice = meteringService.findMeter(mRID);
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
            System.out.println("No meter found with mRID: " + mRID);
        }
    }


    public void storeIntervalData(String mRID, String readingType, String startDateTime, int numberOfInterval, double minValue, double maxValue) {
        final Optional<Meter> endDevice = meteringService.findMeter(mRID);
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
            System.out.println("No meter found with mRID: " + mRID);
        }
    }

    public void storeCumulativeIntervalData(String mRID, String readingType, String startDateTime, int numberOfInterval, double startValue, double minValue, double maxValue) {
        storeCumulativeIntervalData(mRID, readingType, startDateTime, numberOfInterval, startValue, minValue, maxValue, null);
    }

    public void storeCumulativeIntervalData(String mRID, String readingType, String startDateTime, int numberOfInterval, double startValue, double minValue, double maxValue, String readingQualityCIMCode) {
        final Optional<Meter> endDevice = meteringService.findMeter(mRID);
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
            System.out.println("No meter found with id " + mRID);
        }
    }

    private ZonedDateTime nextIntervalTime(ZonedDateTime time, ReadingType readingType) {
        MacroPeriod macroPeriod = readingType.getMacroPeriod();
        if (macroPeriod.getId() != 0) {
            switch (macroPeriod) {
                case DAILY:
                    return time.plusDays(1);
                case MONTHLY:
                    return time.plusMonths(1);
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
            Collections.sort(availableReadingTypes, new Comparator<ReadingType>() {
                @Override
                public int compare(ReadingType type1, ReadingType type2) {
                    return type1.getName().compareTo(type2.getName());
                }
            });
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
            Collections.sort(availableReadingTypes, new Comparator<EndDeviceEventType>() {
                @Override
                public int compare(EndDeviceEventType type1, EndDeviceEventType type2) {
                    return type1.getName().compareTo(type2.getName());
                }
            });

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

    public void addDeviceEvent(long meterId, String mRID, final String dateTime) {
        final Optional<Meter> endDevice = meteringService.findMeter(meterId);
        if (endDevice.isPresent()) {
            final Meter meter = endDevice.get();
            final Optional<EndDeviceEventType> type = meteringService.getEndDeviceEventType(mRID);
            if (type.isPresent()) {
                executeTransaction(new VoidTransaction() {
                    @Override
                    protected void doPerform() {
                        EndDeviceEventRecord endDeviceEventRecord = null;
                        try {
                            endDeviceEventRecord = meter.addEventRecord(type.get(), LocalDateTime.from(dateTimeFormat.parse(dateTime)).atZone(ZoneId.systemDefault()).toInstant()).create();
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

    public void addReadingQuality(String mRID, String readingTypeMRID, String time, String readingQualityCode) {
        //find meter
        Optional<Meter> endDevice = meteringService.findMeter(mRID);
        if (!endDevice.isPresent()) {
            System.out.println("No meter found with id " + mRID);
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
            if(!readingQualityType.system().isPresent() || !readingQualityType.category().isPresent() || !readingQualityType.qualityIndex().isPresent()) {
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

    private <T> T executeTransaction(Transaction<T> transaction) {
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
        return new Principal() {

            @Override
            public String getName() {
                return "console";
            }
        };
    }

}
