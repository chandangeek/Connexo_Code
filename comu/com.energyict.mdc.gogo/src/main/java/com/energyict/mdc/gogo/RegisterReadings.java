/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.gogo;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.streams.ExceptionThrowingRunnable;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import org.joda.time.DateTimeConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-17 (14:17)
 */
@Component(name = "com.energyict.mdc.gogo.RegisterReadings", service = RegisterReadings.class,
        property = {"osgi.command.scope=mdc.metering", "osgi.command.function=printReadings", "osgi.command.function=addReading", "osgi.command.function=addDeviceEvent", "osgi.command.function=addDefaultAlarmEvents"},
        immediate = true)
@SuppressWarnings("unused")
public class RegisterReadings {

    private volatile DeviceService deviceService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private Clock clock;
    private DateTimeFormatter printDateFormat;
    private DateTimeFormatter parseDateFormat;
    private Random random;

    public RegisterReadings() {
        super();
        this.printDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
        this.parseDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss").withZone(ZoneId.of("UTC"));
        this.random = new Random(System.currentTimeMillis());
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDeviceService(DeviceService deviceDataService) {
        this.deviceService = deviceDataService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @SuppressWarnings("unused")
    public void addReading(String deviceName, String readingTypeMRID, String... formattedDates) {
        Optional<Device> device = this.deviceService.findDeviceByName(deviceName);
        if (device.isPresent()) {
            Optional<Register<Reading, RegisterSpec>> register = this.findRegister(device.get(), readingTypeMRID);
            if (register.isPresent()) {
                List<Instant> readingTimestamps = this.toTimestamps(formattedDates);
                try {
                    this.addReadings(device.get(), readingTypeMRID, readingTimestamps);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("No register found with mRID " + readingTypeMRID + " on device with name " + deviceName);
            }
        } else {
            System.out.println("No device with name " + deviceName);
        }
    }

    public void addDeviceEvent(String... mistakenArgs) {
        System.out.println("Usage : \n" +
                "\taddDeviceEvent deviceName eventCode dateTime mrid name alias description reason severity status type issuerId issuerTrackingId logBookId logBookPosition data" +
                "\n" +
                "\tdateTime : format yyyy-MM-dd@HH:mm:ss\n" +
                "\tdata     : comma-separated properties formatted as field=value\n");
    }

    public void addDeviceEvent(String deviceName, String eventCode, String dateTime, String mrid, String name, String alias,
                               String description, String reason, String severity, String status, String type, String issuerId,
                               String issuerTrackingId, long logBookId, int logBookPosition, String data) {
        try {
            Instant eventTime = Instant.from(parseDateFormat.parse(dateTime));
            Optional<Device> found = this.deviceService.findDeviceByName(deviceName);
            found.ifPresent(device -> {
                MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                EndDeviceEventImpl endDeviceEvent = EndDeviceEventImpl.of(eventCode, eventTime);
                endDeviceEvent.setMrid(mrid);
                endDeviceEvent.setName(name);
                endDeviceEvent.setAliasName(alias);
                endDeviceEvent.setDescription(description);
                endDeviceEvent.setReason(reason);
                endDeviceEvent.setSeverity(severity);
                endDeviceEvent.setStatus(parseStatus(status));
                endDeviceEvent.setType(type);
                endDeviceEvent.setIssuerId(issuerId);
                endDeviceEvent.setIssuerTrackingId(issuerTrackingId);
                endDeviceEvent.setLogBookId(logBookId);
                endDeviceEvent.setLogBookPosition(logBookPosition);
                endDeviceEvent.setEventData(parseMap(data));
                meterReading.addEndDeviceEvent(endDeviceEvent);
                executeAsBatchExecutor(() -> device.store(meterReading));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addDefaultAlarmEvents() {
        String raisedOnEvent = "0.12.40.257";
        String reason = "Tampering";
        this.deviceService.findAllDevices(Condition.TRUE)
                .stream()
                .filter(device -> device.getState().getName().equals(DefaultState.ACTIVE.getKey())
                        && device.getLogBooks().size() > 0)
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream().limit((long) Math.ceil(collected.size() * 0.03));
                })).forEach(device ->
                addDeviceEvent(
                        device.getName(),
                        raisedOnEvent,
                        parseDateFormat.format(ZonedDateTime.now(clock).minusDays(1).minusHours(1).minusMinutes(1)),
                        device.getmRID(),
                        "TamperingEvent",
                        "TamperingEventAlias",
                        "TamperingEventDescription",
                        reason,
                        "Medium",
                        "Open",
                        String.valueOf(device.getDeviceType().getId()),
                        String.valueOf(new Random().nextInt()),
                        String.valueOf(new Random().nextInt()),
                        device.getLogBooks().stream().findFirst().map(LogBook::getId).get(),
                        0,
                        "data=" + device.getName() + "_data")
        );
    }

    private Status parseStatus(String status) {
        return Status.builder().value(status).build();
    }

    private Map<String, String> parseMap(String data) {
        return Pattern.compile(",").splitAsStream(data)
                .map(entry -> entry.split("\\="))
                .collect(Collectors.toMap(d -> d[0], d -> d[1]));
    }

    private Optional<Register<Reading, RegisterSpec>> findRegister(Device device, String readingTypeMRID) {
        for (Register<Reading, RegisterSpec> register : device.getRegisters()) {
            if (register.getRegisterSpec().getRegisterType().getReadingType().getMRID().equals(readingTypeMRID)) {
                return Optional.of(register);
            }
        }
        return Optional.empty();
    }

    private void addReadings(final Device device, final String readingTypeMRID, final List<Instant> readingTimestamps) {
        this.executeAsBatchExecutor(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            for (Instant readingTimestamp : readingTimestamps) {
                meterReading.addReading(newReading(readingTypeMRID, readingTimestamp));
            }
            device.store(meterReading);
        });
    }

    private void executeAsBatchExecutor(ExceptionThrowingRunnable<RuntimeException> transaction) {
        try {
            Optional<User> user = this.userService.findUser("batch executor");
            this.threadPrincipalService.set(user.get());
            this.transactionService.run(transaction);
        } finally {
            this.threadPrincipalService.clear();
        }
    }

    private ReadingImpl newReading(String readingTypeMRID, Instant readingTimestamp) {
        BigDecimal readingValue = new BigDecimal(this.random.nextDouble()).multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
        Instant eventEnd = readingTimestamp.plusMillis(DateTimeConstants.MILLIS_PER_DAY);
        ReadingImpl reading = ReadingImpl.of(readingTypeMRID, readingValue, eventEnd);
        reading.setTimePeriod(readingTimestamp, eventEnd);
        return reading;
    }

    private List<Instant> toTimestamps(String... formattedDates) {
        List<Instant> parsed = new ArrayList<>(formattedDates.length);
        for (String formattedDate : formattedDates) {
            try {
                parsed.add(Instant.from(this.parseDateFormat.parse(formattedDate)));
            } catch (DateTimeParseException e) {
                e.printStackTrace(System.err);
            }
        }
        return parsed;
    }

    @SuppressWarnings("unused")
    public void printReadings(String deviceName) {
        Optional<Device> device = this.deviceService.findDeviceByName(deviceName);
        if (device.isPresent()) {
            Interval sinceEpoch = Interval.sinceEpoch();
            System.out.print("Readings of registers for device with name ");
            System.out.print(deviceName);
            this.printInterval(sinceEpoch);
            for (Register register : device.get().getRegisters()) {
                this.printReadings(register, sinceEpoch);
            }
        } else {
            System.out.println("No device with name " + deviceName);
        }
    }

    private void printInterval(Interval sinceEpoch) {
        if (sinceEpoch.getStart() != null) {
            System.out.print(" from ");
            System.out.print(this.printDateFormat.format(sinceEpoch.getStart()));
        }
        if (sinceEpoch.getEnd() != null) {
            System.out.print(" to ");
            System.out.print(this.printDateFormat.format(sinceEpoch.getEnd()));
        }
        System.out.println();
    }

    private <R extends Reading, RS extends RegisterSpec> void printReadings(Register<R, RS> register, Interval sinceEpoch) {
        List<R> readings = register.getReadings(sinceEpoch);
        if (readings.isEmpty()) {
            System.out.println("No readings for register " + register.getRegisterSpec().getRegisterType().getReadingType().getMRID());
        } else {
            System.out.println("Readings for register " + register.getRegisterSpec().getRegisterType().getReadingType().getMRID());
            for (R reading : readings) {
                System.out.print(this.printDateFormat.format(reading.getTimeStamp()) + " - ");
                if (reading instanceof NumericalReading) {
                    NumericalReading numericalReading = (NumericalReading) reading;
                    System.out.println(numericalReading.getQuantity().toString());
                } else {
                    System.out.println(reading.getClass().getSimpleName());
                }
            }
        }
    }
}
