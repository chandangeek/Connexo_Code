package com.elster.jupiter.gogo;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;

import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import org.joda.time.DateTimeConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-17 (14:17)
 */
@Component(name = "com.elster.jupiter.gogo.RegisterReadings", service = RegisterReadings.class,
        property = {"osgi.command.scope=mdc.metering", "osgi.command.function=printReadings","osgi.command.function=addReading" },
        immediate = true)
public class RegisterReadings {

    private volatile DeviceDataService deviceDataService;
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
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
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
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void addReading (String deviceMRID, String readingTypeMRID, String... formattedDates) {
        Device device = this.deviceDataService.findByUniqueMrid(deviceMRID);
        if (device != null) {
            Optional<Register> register = this.findRegister(device, readingTypeMRID);
            if (register.isPresent()) {
                List<Date> readingTimestamps = this.toTimestamps(formattedDates);
                this.addReadings(device, readingTypeMRID, readingTimestamps);
            }
            else {
                System.out.println("No register found with mRID " + readingTypeMRID + " on device with mRID " + deviceMRID);
            }
        }
        else {
            System.out.println("No device with mRID " + deviceMRID);
        }
    }

    private Optional<Register> findRegister(Device device, String readingTypeMRID) {
        for (Register register : device.getRegisters()) {
            if (register.getRegisterSpec().getRegisterMapping().getReadingType().getMRID().equals(readingTypeMRID)) {
                return Optional.of(register);
            }
        }
        return Optional.absent();
    }

    private void addReadings(final Device device, final String readingTypeMRID, final List<Date> readingTimestamps) {
        this.executeAsBatchExecutor(new VoidTransaction() {
            @Override
            protected void doPerform() {
                MeterReadingImpl meterReading = new MeterReadingImpl();
                for (Date readingTimestamp : readingTimestamps) {
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
        }
        finally {
            this.threadPrincipalService.clear();
        }
    }

    private ReadingImpl newReading(String readingTypeMRID, Date readingTimestamp) {
        BigDecimal readingValue = new BigDecimal(this.random.nextDouble()).multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
        Date eventEnd = new Date(readingTimestamp.getTime() + DateTimeConstants.MILLIS_PER_DAY);
        ReadingImpl reading = new ReadingImpl(readingTypeMRID, readingValue, eventEnd);
        reading.setInterval(readingTimestamp, eventEnd);
        return reading;
    }

    private List<Date> toTimestamps (String... formattedDates) {
        List<Date> parsed = new ArrayList<>(formattedDates.length);
        for (String formattedDate : formattedDates) {
            try {
                parsed.add(this.parseDateFormat.parse(formattedDate));
            }
            catch (ParseException e) {
                e.printStackTrace(System.err);
            }
        }
        return parsed;
    }

    public void printReadings (String deviceMRID) {
        Device device = this.deviceDataService.findByUniqueMrid(deviceMRID);
        if (device != null) {
            Interval sinceEpoch = Interval.sinceEpoch();
            System.out.print("Readings of registers for device with mRID ");
            System.out.print(deviceMRID);
            this.printInterval(sinceEpoch);
            for (Register register : device.getRegisters()) {
                this.printReadings(register, sinceEpoch);
            }
        }
        else {
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
            System.out.println("No readings for register " + register.getRegisterSpec().getRegisterMapping().getReadingType().getMRID());
        }
        else {
            System.out.println("Readings for register " + register.getRegisterSpec().getRegisterMapping().getReadingType().getMRID());
            for (R reading : readings) {
                System.out.print(this.printDateFormat.format(reading.getTimeStamp()) + " - ");
                if (reading instanceof NumericalReading) {
                    NumericalReading numericalReading = (NumericalReading) reading;
                    System.out.println(numericalReading.getQuantity().toString());
                }
                else {
                    System.out.println(reading.getClass().getSimpleName());
                }
            }
        }
    }

}