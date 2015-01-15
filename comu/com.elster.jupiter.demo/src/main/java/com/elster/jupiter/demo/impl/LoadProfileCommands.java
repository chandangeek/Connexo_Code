package com.elster.jupiter.demo.impl;

import com.elster.jupiter.demo.DemoService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.ProfileStatus.Flag;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import static com.elster.jupiter.util.Checks.is;

@Component(name = "com.elster.jupiter.demo.loadprofile", service = LoadProfileCommands.class, property = { "osgi.command.scope=loadprofile",
        "osgi.command.function=uploadData",
        "osgi.command.function=createMeasurementType"}, immediate = true)
@SuppressWarnings("unused")
public class LoadProfileCommands {
    public static final int MANDATORY_COLUMN_SIZE = 3;

    public static final int COLUMN_DATE = 0;
    public static final int COLUMN_CODE = 1;
    public static final int COLUMN_FLAGS = 2;

    public static final String COLUMN_DATE_NAME = "Date";
    public static final String COLUMN_CODE_NAME = "Code";
    public static final String COLUMN_FLAGS_NAME = "Flags";
    public static final String COLUMN_LASTMOD_NAME = "Last Mod.";

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

    public void createLoadProfileType(String name, String obisCode, TimeDuration duration, RegisterType[] registerTypes, DeviceType deviceType) {
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(name, ObisCode.fromString(obisCode), duration, Arrays.asList(registerTypes));
        loadProfileType.save();
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.save();
    }

    public List<RegisterType> findRegisterTypes(String[] readingTypes) {
        List<RegisterType> registerTypes = new ArrayList<>();
        for (String readingType : readingTypes) {
            Optional<ReadingType> rt = meteringService.getReadingType(readingType);
            if (!rt.isPresent()) {
                System.err.println("There is no reading type: " + rt);
                continue;
            }
            Optional<RegisterType> registerType = masterDataService.findRegisterTypeByReadingType(rt.get());
            if (!registerType.isPresent()) {
                System.err.println("There is no register type for reading type: " + rt);
                continue;
            }

            registerTypes.add(registerType.get());
        }
        if (registerTypes.size() != readingTypes.length) {
            System.out.println("Unable to find all register types. Please create necessary before.");
            return null;
        }
        return registerTypes;
    }

    public void uploadData(String mrid, String startTime, String csvFilePath) {
        final Optional<Meter> endDevice = meteringService.findMeter(mrid);
        if (!endDevice.isPresent()) {
            System.out.println("==> Unable to find meter with mrid = " + mrid);
            return;
        }

        Date dateShift;
        try {
            dateShift = dateTimeFormat.parse(startTime);
        } catch (ParseException e) {
            System.out.println("==> Unable to parse start time. Please use the following format: " + DATE_PATTERN);
            return;
        }

        try {
            File loadProfileData = new File(csvFilePath);
            Scanner scanner = new Scanner(loadProfileData);
            String header = scanner.nextLine();

            List<String> readingTypes = readHeader(header);

            final MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            Map<String, IntervalBlockImpl> blocks = new HashMap<>();
            for (String readingType : readingTypes) {
                IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(readingType);
                blocks.put(readingType, intervalBlock);
            }


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
                        if (!is(stringValue).emptyOrOnlyWhiteSpace()){
                            double doubleValue = Double.valueOf(stringValue);
                            IntervalReadingImpl intervalReading = IntervalReadingImpl.of(time, BigDecimal.valueOf(doubleValue));
                            intervalReading.setProfileStatus(ProfileStatus.of(realFlags));
                            blocks.get(readingTypes.get(i)).addIntervalReading(intervalReading);
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            for (IntervalBlockImpl block : blocks.values()) {
                meterReading.addIntervalBlock(block);
            }

            executeTransaction(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    (endDevice.get()).store(meterReading);
                }
            });

            scanner.close();

            executeTransaction(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    Device device = deviceService.findByUniqueMrid(endDevice.get().getMRID());
                    List<LoadProfile> loadProfiles = device.getLoadProfiles();
                    for (LoadProfile loadProfile : loadProfiles) {
                        LoadProfile.LoadProfileUpdater updater = device.getLoadProfileUpdaterFor(loadProfile);
                        for (Channel channel : loadProfile.getChannels()) {
                            channel.getLastDateTime().ifPresent(t -> updater.setLastReadingIfLater(t));
                        }
                        updater.update();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //command
    public void createMeasurementType(final String name, final String obisCode, final String unit, final String registerReadingType, String channelReadingType) {
        final Optional<ReadingType> regReadingType = meteringService.getReadingType(registerReadingType);
        final Optional<ReadingType> chReadingType = meteringService.getReadingType(channelReadingType);
        if (regReadingType.isPresent() && chReadingType.isPresent()) {
            executeTransaction(new VoidTransaction() {

                @Override
                protected void doPerform() {
                    RegisterType newRegisterType = masterDataService.newRegisterType(name, ObisCode.fromString(obisCode), Unit.get(unit), regReadingType.get(), 0);
                    newRegisterType.save();
                    System.out.println("Created register type id = " + newRegisterType.getId());
                    TimeDuration interval = TimeDuration.minutes(regReadingType.get().getMeasuringPeriod().getMinutes());
                    ChannelType newChannelType = masterDataService.newChannelType(newRegisterType, interval, chReadingType.get());
                    newChannelType.save();
                    System.out.println("Created channel type id = " + newChannelType.getId());
                }
            });
        } else {
            System.out.println("Reading Type is not found: " + registerReadingType + " or" + channelReadingType);
        }
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
                System.out.println("Channel name is empty..");
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

}