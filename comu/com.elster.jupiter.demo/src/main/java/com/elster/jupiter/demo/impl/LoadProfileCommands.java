package com.elster.jupiter.demo.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.ProfileStatus.Flag;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import static com.elster.jupiter.util.Checks.is;

@Component(name = "com.elster.jupiter.demo.loadprofile", service = LoadProfileCommands.class, property = {"osgi.command.scope=loadprofile",
        "osgi.command.function=uploadData",
        "osgi.command.function=uploadRegisters"}, immediate = true)
@SuppressWarnings("unused")
public class LoadProfileCommands {
    public static final int MANDATORY_COLUMN_SIZE = 3;

    public static final int COLUMN_DATE = 0;
    public static final int COLUMN_CODE = 1;
    public static final int COLUMN_FLAGS = 2;

    public static final String COLUMN_DATE_NAME = "Date";
    public static final String COLUMN_CODE_NAME = "Code";
    public static final String COLUMN_FLAGS_NAME = "Flags";

    public static final String DATE_PATTERN = "dd.MM.yyyy HH:mm";
    private static DateFormat dateTimeFormat = new SimpleDateFormat(DATE_PATTERN);

    private volatile MasterDataService masterDataService;
    private volatile TransactionService transactionService;
    private volatile MeteringService meteringService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile DeviceService deviceService;

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setDeviceConfigService(DeviceConfigurationService deviceConfigService) {
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public void uploadData(String mrid, String startTime, String csvFilePath) {
        Meter meter = findMeterByMridOrThrowException(mrid);
        Date dateShift = parseDate(startTime);

        ValueProcessor processor = new LoadProfileDataProcessor(meter);
        parseFile(csvFilePath, meter, dateShift, processor);
    }

    public void uploadRegisters(String mrid, String startTime, String csvFilePath) {
        Meter meter = findMeterByMridOrThrowException(mrid);
        Date dateShift = parseDate(startTime);

        ValueProcessor processor = new RegisterDataProcessor(meter);
        parseFile(csvFilePath, meter, dateShift, processor);
    }

    private Date parseDate(String startTime) {
        Date dateShift = null;
        try {
            dateShift = dateTimeFormat.parse(startTime);
        } catch (ParseException e) {
            throw new UnableToCreate("Unable to parse start time. Please use the following format: " + DATE_PATTERN);
        }
        return dateShift;
    }

    private void parseFile(String csvFilePath, Meter meter, Date dateShift, ValueProcessor valuesProcessor) {
        try {
            File loadProfileData = new File(csvFilePath);
            Scanner scanner = new Scanner(loadProfileData);
            String header = scanner.nextLine();
            List<String> readingTypes = readHeader(header);
            valuesProcessor.processReadingTypes(readingTypes);
            parseBodyOfReadings(dateShift, scanner, readingTypes, valuesProcessor);
            scanner.close();
            executeTransaction(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    valuesProcessor.stopProcessing();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseBodyOfReadings(Date dateShift, Scanner scanner, List<String> readingTypes, ValueProcessor valuesProcessor) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] columns = line.split(";");

            String dateTime = columns[COLUMN_DATE];
            String flags = columns[COLUMN_FLAGS];

            try {
                List<String> values = Arrays.asList(Arrays.copyOfRange(columns, MANDATORY_COLUMN_SIZE, columns.length));

                Instant time = dateShift.toInstant();
                time = time.plus(Integer.valueOf(dateTime.substring(0, 3)), ChronoUnit.DAYS);
                time = time.plus(Integer.valueOf(dateTime.substring(4, 6)), ChronoUnit.HOURS);
                time = time.plus(Integer.valueOf(dateTime.substring(7, 9)), ChronoUnit.MINUTES);

                Flag[] realFlags = parstIntervalFlags(flags.split(","));

                for (int i = 0; i < readingTypes.size() && i < values.size(); i++) {
                    String stringValue = values.get(i).replace(",", ".").replace(" ", "");
                    if (!is(stringValue).emptyOrOnlyWhiteSpace()) {
                        double doubleValue = Double.valueOf(stringValue);
                        valuesProcessor.process(readingTypes.get(i), time, doubleValue, realFlags);
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void setLastReadingTypeForLoadProfile(final String mrid) {
        Device device = deviceService.findByUniqueMrid(mrid);
        List<LoadProfile> loadProfiles = device.getLoadProfiles();
        for (LoadProfile loadProfile : loadProfiles) {
            LoadProfile.LoadProfileUpdater updater = device.getLoadProfileUpdaterFor(loadProfile);
            for (Channel channel : loadProfile.getChannels()) {
                channel.getLastDateTime().ifPresent(t -> updater.setLastReadingIfLater(t));
            }
            updater.update();
        }
    }

    private Meter findMeterByMridOrThrowException(String mrid) {
        Optional<Meter> endDevice = meteringService.findMeter(mrid);
        if (!endDevice.isPresent()) {
            throw new UnableToCreate("Unable to find meter with mrid" + mrid);
        }
        return endDevice.get();
    }

    private List<String> readHeader(String header) {
        List<String> channels = new ArrayList<>();
        String[] columns = header.split(";");
        if (columns.length < MANDATORY_COLUMN_SIZE + 1) {
            System.out.println("Incorrect file header: should be 'Date, Code, Flags' + channels(one per column)");
            return channels;
        }
        if (!(COLUMN_DATE_NAME.equals(columns[COLUMN_DATE]) && COLUMN_CODE_NAME.equals(columns[COLUMN_CODE]) && COLUMN_FLAGS_NAME.equals(columns[COLUMN_FLAGS]))) {
            System.out.println("Incorrect header: should be 'Date, Code, Flags' + channels(one per column)");
            return channels;
        }

        for (int i = MANDATORY_COLUMN_SIZE; i < columns.length; i++) {
            if (columns[i].isEmpty()) {
                System.out.println("Reading type is empty..");
                continue;
            }
            channels.add(columns[i]);
        }
        return channels;
    }

    private Flag[] parstIntervalFlags(String... intervalFlags) {
        List<Flag> flags = new ArrayList<>();
        for (int i = 0; i < intervalFlags.length; i++) {
            if (!intervalFlags[i].isEmpty()) {
                flags.add(Flag.valueOf(intervalFlags[i]));
            }
        }
        return flags.toArray(new Flag[flags.size()]);
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
        return () -> "console";
    }

    private class LoadProfileDataProcessor implements ValueProcessor {
        private Map<String, IntervalBlockImpl> blocks = new HashMap<>();
        private MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        private Meter meter;

        public LoadProfileDataProcessor(Meter meter) {
            this.meter = meter;
        }

        @Override
        public void processReadingTypes(Collection<String> readingTypes) {
            for (String readingType : readingTypes) {
                IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(readingType);
                blocks.put(readingType, intervalBlock);
            }
        }

        @Override
        public void process(String readingType, Instant time, Double value, Object... additional) {
            IntervalReadingImpl intervalReading = IntervalReadingImpl.of(time, BigDecimal.valueOf(value));
            intervalReading.setProfileStatus(ProfileStatus.of((Flag[]) additional));
            blocks.get(readingType).addIntervalReading(intervalReading);
        }

        @Override
        public void stopProcessing() {
            for (IntervalBlockImpl block : blocks.values()) {
                meterReading.addIntervalBlock(block);
            }

            meter.store(meterReading);
            setLastReadingTypeForLoadProfile(meter.getMRID());
        }
    }

    private class RegisterDataProcessor implements ValueProcessor {
        private Map<String, IntervalBlockImpl> blocks = new HashMap<>();
        private MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        private Meter meter;

        public RegisterDataProcessor(Meter meter) {
            this.meter = meter;
        }

        @Override
        public void processReadingTypes(Collection<String> readingTypes) {
        }

        @Override
        public void process(String readingType, Instant time, Double value, Object... additional) {
            meterReading.addReading(ReadingImpl.of(readingType, BigDecimal.valueOf(value), time));
        }

        @Override
        public void stopProcessing() {
            meter.store(meterReading);
        }
    }
}