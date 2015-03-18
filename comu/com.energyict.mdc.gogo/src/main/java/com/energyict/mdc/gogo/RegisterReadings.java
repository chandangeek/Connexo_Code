package com.energyict.mdc.gogo;

import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import org.joda.time.DateTimeConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-17 (14:17)
 */
@Component(name = "com.energyict.mdc.gogo.RegisterReadings", service = RegisterReadings.class,
        property = {"osgi.command.scope=mdc.metering", "osgi.command.function=printReadings", "osgi.command.function=addReading"},
        immediate = true)
@SuppressWarnings("unused")
public class RegisterReadings {

    private volatile DeviceService deviceService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private SimpleDateFormat printDateFormat;
    private SimpleDateFormat parseDateFormat;
    private Random random;

    public RegisterReadings() {
        super();
        this.printDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.printDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.parseDateFormat = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");
        this.parseDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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

    @SuppressWarnings("unused")
    public void addReading(String deviceMRID, String readingTypeMRID, String... formattedDates) {
        Optional<Device> device = this.deviceService.findByUniqueMrid(deviceMRID);
        if (device.isPresent()) {
            Optional<Register<Reading>> register = this.findRegister(device.get(), readingTypeMRID);
            if (register.isPresent()) {
                List<Instant> readingTimestamps = this.toTimestamps(formattedDates);
                try {
                    this.addReadings(device.get(), readingTypeMRID, readingTimestamps);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("No register found with mRID " + readingTypeMRID + " on device with mRID " + deviceMRID);
            }
        } else {
            System.out.println("No device with mRID " + deviceMRID);
        }
    }

    private Optional<Register<Reading>> findRegister(Device device, String readingTypeMRID) {
        for (Register<Reading> register : device.getRegisters()) {
            if (register.getRegisterSpec().getRegisterType().getReadingType().getMRID().equals(readingTypeMRID)) {
                return Optional.of(register);
            }
        }
        return Optional.empty();
    }

    private void addReadings(final Device device, final String readingTypeMRID, final List<Instant> readingTimestamps) {
        this.executeAsBatchExecutor(new VoidTransaction() {
            @Override
            protected void doPerform() {
                MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                for (Instant readingTimestamp : readingTimestamps) {
                    meterReading.addReading(newReading(readingTypeMRID, readingTimestamp));
                }
                device.store(meterReading);
            }
        });
    }

    private void executeAsBatchExecutor(Transaction transaction) {
        try {
            Optional<User> user = this.userService.findUser("batch executor");
            this.threadPrincipalService.set(user.get());
            this.transactionService.execute(transaction);
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
                parsed.add(this.parseDateFormat.parse(formattedDate).toInstant());
            } catch (ParseException e) {
                e.printStackTrace(System.err);
            }
        }
        return parsed;
    }

    @SuppressWarnings("unused")
    public void printReadings(String deviceMRID) {
        Optional<Device> device = this.deviceService.findByUniqueMrid(deviceMRID);
        if (device.isPresent()) {
            Interval sinceEpoch = Interval.sinceEpoch();
            System.out.print("Readings of registers for device with mRID ");
            System.out.print(deviceMRID);
            this.printInterval(sinceEpoch);
            for (Register register : device.get().getRegisters()) {
                this.printReadings(register, sinceEpoch);
            }
        } else {
            System.out.println("No device with mRID " + deviceMRID);
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

    private <R extends Reading> void printReadings(Register<R> register, Interval sinceEpoch) {
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