package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.LittleEndianInputStream;
import com.energyict.cbo.TimeConstants;
import com.energyict.cbo.Unit;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.protocol.exceptions.CommunicationException;
import com.energyict.mdc.protocol.exceptions.DataEncryptionException;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;
import com.energyict.protocol.*;
import com.energyict.protocolimplv2.MdcManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileBuilder {

    private static final int[] DEVICEEVENTMAPPING = {
            MeterEvent.OTHER,                    // 0
            MeterEvent.POWERDOWN,
            MeterEvent.POWERUP,
            MeterEvent.SETCLOCK_BEFORE,
            MeterEvent.SETCLOCK_AFTER,
            MeterEvent.RAM_MEMORY_ERROR,        // 5
            MeterEvent.WATCHDOGRESET,
            MeterEvent.REGISTER_OVERFLOW,
            MeterEvent.REGISTER_OVERFLOW,
            MeterEvent.REGISTER_OVERFLOW,
            MeterEvent.REGISTER_OVERFLOW,        //10
            MeterEvent.CONFIGURATIONCHANGE,
            MeterEvent.CONFIGURATIONCHANGE,
            MeterEvent.OTHER,
            MeterEvent.OTHER,
            MeterEvent.REGISTER_OVERFLOW,        //15
            MeterEvent.OTHER,
            MeterEvent.PROGRAM_FLOW_ERROR,
            MeterEvent.OTHER,
            MeterEvent.CLEAR_DATA                //19
    };
    private static final int METER_READING_VERSION_BIT_MASK = 0x40;
    private static final int EVENT_DATA_VERSION_BIT_MASK = 0x20;
    // appears only in the logbook (pure VDEW)
    private static final int CLEAR_LOADPROFILE = 0x4000;
    private static final int CLEAR_LOGBOOK = 0x2000;
    private static final int END_OF_ERROR = 0x0400;
    private static final int BEGIN_OF_ERROR = 0x0200;
    private static final int VARIABLE_SET = 0x0100;
    // appears in the logbook and the intervalstatus
    private static final int POWER_FAILURE = 0x0080;
    private static final int POWER_RECOVERY = 0x0040;
    private static final int DEVICE_CLOCK_SET_INCORRECT = 0x0020;  // Changed KV 12062003
    private static final int DEVICE_RESET = 0x0010;
    private static final int SEASONAL_SWITCHOVER = 0x0008;
    private static final int DISTURBED_MEASURE = 0x0004;
    private static final int RUNNING_RESERVE_EXHAUSTED = 0x0002;
    private static final int FATAL_DEVICE_ERROR = 0x0001;
    private Logger logger;
    private PacketBuilder packetBuilder;
    private ProfileData profileData = null;
    private List<BigDecimal> meterReadings;

    public ProfileBuilder(PacketBuilder packetBuilder) throws IOException {
        this(packetBuilder, Logger.getAnonymousLogger());
    }

    public ProfileBuilder(PacketBuilder packetBuilder, Logger logger) throws IOException {
        super();
        this.logger = logger;
        this.packetBuilder = packetBuilder;
        this.buildData();
    }

    public ProfileData getProfileData() {
        return profileData;
    }

    public List<BigDecimal> getMeterReadings() {
        return meterReadings;
    }

    public byte[] getConfigFile() {
        if (isConfigFileMode()) {
            return packetBuilder.getData();
        } else {
            return new byte[0];
        }
    }

    public void removeFutureData(Logger logger, Date dateInFuture) {
        Iterator<IntervalData> it = this.profileData.getIntervalDatas().iterator();
        while (it.hasNext()) {
            IntervalData intervalData = it.next();
            if (intervalData.getEndTime().after(dateInFuture)) {
                logger.info("Device: " + this.packetBuilder.getDeviceIdentifier() + " reports future interval at " + intervalData.getEndTime());
                it.remove();
            }
        }
    }

    private void buildChannelInfo() {
        int channelCount = 0;
        long mask = packetBuilder.getMask();
        // Build channelinfo
        for (int i = 0; i < PacketBuilder.MAX_CHANNELS; i++) {
            if ((mask & (1 << i)) != 0) {
                ChannelInfo chanInfo = new ChannelInfo(channelCount, i, this.buildChannelName(i), Unit.get(BaseUnit.COUNT));
                profileData.addChannel(chanInfo);
                channelCount++;
            }
        }
    }

    private String buildChannelName(int channelId) {
        // Remember that obis codes use 1-based indexing and channelId is using zero-based indexing.
        return "0." + (channelId + 1) + ".128.0.0.255";
    }

    private void buildData() throws IOException {
        profileData = new ProfileData();
        profileData.setStoreOlderValues(true);
        byte[] data = packetBuilder.getData();
        meterReadings = new ArrayList<>();

        if (data == null) {
            throw CommunicationException.missingInboundData(this.packetBuilder.getDeviceIdentifier());
        }

        buildChannelInfo();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        LittleEndianInputStream is = new LittleEndianInputStream(bis);

        if ((packetBuilder.getVersion() & METER_READING_VERSION_BIT_MASK) != 0) {
            this.buildMeterReadingData(is);
        }

        if ((packetBuilder.getVersion() & EVENT_DATA_VERSION_BIT_MASK) != 0) {  // log records
            this.buildEventData(is);
        }

        // Build intervaldata - but not when a config file is send (version == 0x10). (STW 20080115)
        if (!isConfigFileMode()) {
            this.buildIntervalData(is);
        }
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.finest("ProfileData:\n" + profileData.toString());
        }
    }

    private void buildMeterReadingData(LittleEndianInputStream is) throws IOException {
        for (int t = 0; t < packetBuilder.getNrOfChannels(); t++) {
            BigDecimal meterReading = new BigDecimal(is.readLEUnsignedInt());
            meterReadings.add(meterReading);
        }
    }

    private void buildEventData(LittleEndianInputStream is) throws IOException {
        int count = is.readUnsignedByte();
        for (int t = 0; t < count; t++) {
            long seconds80 = is.readLEUnsignedInt();
            int code = is.readLEUnsignedShort();
            int length = is.readUnsignedByte();
            String description = is.readString(length);
            profileData.addEvent(
                    new MeterEvent(
                            new Date((seconds80 + EIWebConstants.SECONDS10YEARS) * TimeConstants.MILLISECONDS_IN_SECOND),
                            mapEventCode(code),
                            code,
                            description));
        }
    }

    private void buildIntervalData(LittleEndianInputStream is) throws IOException {
        for (int i = 0; i < packetBuilder.getNrOfRecords(); i++) {
            long rawValue = is.readLEUnsignedInt();
            long ldate = (rawValue + EIWebConstants.SECONDS10YEARS) * TimeConstants.MILLISECONDS_IN_SECOND;
            Date date = new Date(ldate);

            if ((i == 0) && (!packetBuilder.isTimeCorrect(date))) {
                throw new DataEncryptionException(this.packetBuilder.getDeviceIdentifier());
            }
            this.buildIntervalDataForRecord(is, date);
        }
    }

    private void buildIntervalDataForRecord(LittleEndianInputStream is, Date date) throws IOException {
        IntervalData intervalData = new IntervalData(date);
        // KV 22072003 Add tarifcode...
        int code = is.readByte() & 0xFF;
        if (code == 99) {
            intervalData.addStatus(IntervalData.WATCHDOGRESET);
        } else {
            intervalData.setTariffCode(code);
        }

        switch (packetBuilder.getVersion() & 0x0F) {
            case PacketBuilder.VERSION_WITHOUT_STATEBITS_1:
                break;

            case PacketBuilder.VERSION_WITH_STATEBITS_2:
            case PacketBuilder.VERSION_32BITS_3:
                int stateBits = is.readLEUnsignedShort() & 0xFFFF;
                intervalData.setProtocolStatus(stateBits);
                intervalData.setEiStatus(mapStateBits2EICode(stateBits));
                break;

            default:
                throw CommunicationException.unsupportedVersion(packetBuilder.getVersion(), "EIWeb packet builder");
        }

        for (int t = 0; t < packetBuilder.getNrOfChannels(); t++) {
            this.buildIntervalDataForRecordAndChannel(is, intervalData);
        }
        profileData.addInterval(intervalData);
    }

    private void buildIntervalDataForRecordAndChannel(LittleEndianInputStream is, IntervalData intervalData) throws IOException {
        switch (packetBuilder.getVersion() & 0x0F) {
            case PacketBuilder.VERSION_32BITS_3:
                intervalData.addValue(is.readLEInt());
                break;

            case PacketBuilder.VERSION_WITH_STATEBITS_2:
            case PacketBuilder.VERSION_WITHOUT_STATEBITS_1:
                intervalData.addValue(is.readLEUnsignedShort());
                break;

            default:
                throw CommunicationException.unsupportedVersion(packetBuilder.getVersion(), "EIWeb packet builder");
        }
    }

    private boolean isConfigFileMode() {
        return (packetBuilder.getVersion() & 0x10) == 0x10;
    }

    private int mapEventCode(int deviceCode) {
        if (deviceCode >= 0 && deviceCode < DEVICEEVENTMAPPING.length) {
            return DEVICEEVENTMAPPING[deviceCode];
        }
        return MeterEvent.OTHER;
    }

    private int mapStateBits2EICode(int stateBits) {
        int eiCode = 0;
        for (int t = 0; t < 16; t++) {
            if ((stateBits & (short) (0x0001 << t)) != 0) {
                eiCode |= mapStatus2IntervalStateBits((stateBits & (short) (0x0001 << t)) & 0xFFFF);
            }
        }
        return eiCode;
    }

    private int mapStatus2IntervalStateBits(int status) {
        switch (status) {
            case CLEAR_LOADPROFILE:
                return (IntervalStateBits.OTHER);
            case CLEAR_LOGBOOK:
                return (IntervalStateBits.OTHER);
            case END_OF_ERROR:
                return (IntervalStateBits.OTHER);
            case BEGIN_OF_ERROR:
                return (IntervalStateBits.OTHER);
            case VARIABLE_SET:
                return (IntervalStateBits.CONFIGURATIONCHANGE);
            case DEVICE_CLOCK_SET_INCORRECT:
                return (IntervalStateBits.SHORTLONG);
            case SEASONAL_SWITCHOVER:
                return (IntervalStateBits.SHORTLONG);
            case FATAL_DEVICE_ERROR:
                return (IntervalStateBits.OTHER);
            case DISTURBED_MEASURE:
                return (IntervalStateBits.CORRUPTED);
            case POWER_FAILURE:
                return (IntervalStateBits.POWERDOWN);
            case POWER_RECOVERY:
                return (IntervalStateBits.POWERUP);
            case DEVICE_RESET:
                return (IntervalStateBits.OTHER);
            case RUNNING_RESERVE_EXHAUSTED:
                return (IntervalStateBits.OTHER);
            default:
                return (IntervalStateBits.OTHER);

        }
    }

    public void addCollectedData(List<CollectedData> collectedData) {
        CollectedLoadProfile loadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new FirstLoadProfileOnDevice(this.packetBuilder.getDeviceIdentifier(), DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE));
        loadProfile.setCollectedIntervalData(this.profileData.getIntervalDatas(), this.profileData.getChannelInfos());
        loadProfile.setDoStoreOlderValues(this.profileData.shouldStoreOlderValues());
        loadProfile.setAllowIncompleteLoadProfileData(true);
        collectedData.add(loadProfile);
    }

}