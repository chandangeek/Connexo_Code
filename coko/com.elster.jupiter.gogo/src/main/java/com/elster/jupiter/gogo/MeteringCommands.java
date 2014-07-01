package com.elster.jupiter.gogo;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                "osgi.command.function=storeIntervalData",
                "osgi.command.function=listEndDeviceEventTypes",
                "osgi.command.function=addDeviceEvent",
        }, immediate = true)
public class MeteringCommands {

    private volatile MeteringService meteringService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");

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

    public void createMeter(final String amrId, final String mrId) {
        Meter meter = executeTransaction(new Transaction<Meter>() {
            @Override
            public Meter perform() {
                AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
                Meter meter = amrSystem.newMeter(amrId, mrId);
                meter.save();
                return meter;
            }
        });
        System.out.println("meter = " + meter);
        System.out.println(" id = " + meter.getId());
    }

    public void createActivation(long id, String date, final String... readingTypes) {
        final Optional<Meter> endDevice = meteringService.findMeter(id);
        if (endDevice.isPresent()) {
            try {
                final Date activationDate = dateFormat.parse(date);
                MeterActivation activation = executeTransaction(new Transaction<MeterActivation>() {
                    @Override
                    public MeterActivation perform() {
                        MeterActivation activate = endDevice.get().activate(activationDate);
                        for (String readingType : readingTypes) {
                            Optional<ReadingType> readingTypeOptional = meteringService.getReadingType(readingType);
                            if (readingTypeOptional.isPresent()) {
                                activate.createChannel(readingTypeOptional.get());
                            } else {
                                System.out.println("Unknown reading type '" + readingType + "'. Skipping.");
                            }
                        }
                        return activate;
                    }
                });
                System.out.println("activation = " + activation);
                System.out.println(" id = " + activation.getId());
                for (Channel channel : activation.getChannels()) {
                    System.out.println("  channel " + channel.getId() + " : " + channel.getMainReadingType().getMRID());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No meter found with id " + id);
        }
    }

    public void storeIntervalData(long meterId, String readingType, String startDateTime, int numberOfInterval, double minValue, double maxValue) {
        final Optional<Meter> endDevice = meteringService.findMeter(meterId);
        if (endDevice.isPresent()) {
            try {
                Calendar startDate = Calendar.getInstance();
                startDate.setTime(dateTimeFormat.parse(startDateTime));
                Optional<ReadingType> readingTypeOptional = meteringService.getReadingType(readingType);
                if (readingTypeOptional.isPresent()) {
                    int intervalInSeconds = getIntervalInSeconds(readingTypeOptional.get());
                    if (intervalInSeconds > 0) {
                        final MeterReadingImpl meterReading = new MeterReadingImpl();
                        IntervalBlockImpl intervalBlock = new IntervalBlockImpl(readingType);
                        for (int i = 0; i < numberOfInterval; i++) {
                            intervalBlock.addIntervalReading(new IntervalReadingImpl(startDate.getTime(), BigDecimal.valueOf(randomBetween(minValue, maxValue))));
                            startDate.add(Calendar.SECOND, intervalInSeconds);
                        }
                        meterReading.addIntervalBlock(intervalBlock);

                        executeTransaction(new VoidTransaction() {
                            @Override
                            protected void doPerform() {
                                endDevice.get().store(meterReading);
                            }
                        });
                    } else {
                        System.out.println("Reading type is not valid for interval data");
                    }

                } else {
                    System.out.println("Unknown reading type '" + readingType + "'. Skipping.");
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No meter found with id " + meterId);
        }
    }

    private double randomBetween(double minValue, double maxValue) {
        return (Math.random() * (maxValue - minValue)) + minValue;
    }

    private int getIntervalInSeconds(ReadingType readingType) {
        return readingType.getMeasuringPeriod().getMinutes() * 60;
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
                            endDeviceEventRecord = meter.addEventRecord(type.get(), dateTimeFormat.parse(dateTime));
                            endDeviceEventRecord.save();
                        } catch (ParseException e) {
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
