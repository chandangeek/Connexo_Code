package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.g3.EventPushNotificationParser;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.a2.properties.A2Properties;
import com.energyict.protocolimplv2.dlms.ei7.frames.CommunicationType;
import com.energyict.protocolimplv2.dlms.ei7.frames.DailyReadings;
import com.energyict.protocolimplv2.dlms.ei7.frames.Frame30;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static com.energyict.protocolimplv2.dlms.a2.profile.A2ProfileDataReader.getEiServerStatus;
import static com.energyict.protocolimplv2.dlms.ei7.properties.EI7ConfigurationSupport.COMMUNICATION_TYPE_STR;

public class EI7DataPushNotificationParser extends EventPushNotificationParser {

    private static byte COMPACT_FRAME_30 = (byte) 0x1E;
    private static byte COMPACT_FRAME_40 = (byte) 0x28;
    private static byte COMPACT_FRAME_47 = (byte) 0x2F;
    private static byte COMPACT_FRAME_48 = (byte) 0x30;
    private static byte COMPACT_FRAME_49 = (byte) 0x31;
    private static byte COMPACT_FRAME_51 = (byte) 0x33;

    private static final ObisCode PP4_NETWORK_STATUS_OBISCODE = ObisCode.fromString("0.1.96.5.4.255");
    private static final ObisCode DISCONNECT_CONTROL_OUTPUT_STATE = ObisCode.fromString("0.2.96.3.10.255");
    private static final ObisCode DISCONNECT_CONTROL_CONTROL_STATE = ObisCode.fromString("0.3.96.3.10.255");
    private static final ObisCode METROLOGICAL_EVENT_COUNTER_OBISCODE = ObisCode.fromString("0.0.96.15.1.255");
    private static final ObisCode EVENT_COUNTER_OBISCODE = ObisCode.fromString("0.0.96.15.2.255");
    private static final ObisCode DAILY_DIAGNOSTIC_OBISCODE = ObisCode.fromString("7.0.96.5.1.255");
    private static final ObisCode CONVERTED_VOLUME_INDEX = ObisCode.fromString("7.0.13.2.0.255");
    private static final ObisCode CONVERTED_VOLUME_INDEX_F1_RATE = ObisCode.fromString("7.0.13.2.1.255");
    private static final ObisCode CONVERTED_VOLUME_INDEX_F2_RATE = ObisCode.fromString("7.0.13.2.2.255");
    private static final ObisCode CONVERTED_VOLUME_INDEX_F3_RATE = ObisCode.fromString("7.0.13.2.3.255");
    private static final ObisCode CONVERTED_UNDER_ALARM_VOLUME_INDEX = ObisCode.fromString("7.0.12.2.0.255");
    private static final ObisCode MAXIMUM_CONVENTIONAL_CONVERTED_GAS_FLOW = ObisCode.fromString("7.0.43.45.0.255");
    private static final ObisCode BILLING_SNAPSHOT_PERIOD_COUNTER = ObisCode.fromString("7.0.0.1.0.255");
    private static final ObisCode SPARE_OBJECT = ObisCode.fromString("0.0.94.39.40.255");

    private static final ObisCode MANAGEMENT_FRAME_COUNTER_ONLINE = ObisCode.fromString("0.0.43.1.1.255");
    private final static ObisCode MANAGEMENT_FRAME_COUNTER_OFFLINE = ObisCode.fromString("0.1.43.1.1.255");
    private final static ObisCode GUARANTOR_AUTHORITY_COUNTER = ObisCode.fromString("0.0.43.1.48.255");
    private final static ObisCode INSTALLER_MAINTAINER_COUNTER = ObisCode.fromString("0.0.43.1.3.255");
    private static final ObisCode CF40_VOLUME_UNITS_SCALAR = ObisCode.fromString("7.0.13.2.3.255");
    private static final ObisCode HALF_HOUR_LP_INTERVAL_LENGTH_SECONDS = ObisCode.fromString("7.4.99.99.1.255");
    private static final ObisCode COSEM_LOGICAL_DEVICE_NAME = ObisCode.fromString("0.0.42.0.0.255");

    private static final ObisCode DAILY_LOAD_PROFILE = ObisCode.fromString("7.0.99.99.3.255");
    private static final ObisCode HALF_HOUR_LOAD_PROFILE = ObisCode.fromString("7.0.99.99.1.255");
    private static final ObisCode SNAPSHOT_PERIOD_DATA_LOAD_PROFILE = ObisCode.fromString("7.0.98.11.0.255");

    private static final Set<ObisCode> supportedLoadProfiles = new TreeSet<>(
            Arrays.asList(HALF_HOUR_LOAD_PROFILE,
                    DAILY_LOAD_PROFILE,
                    SNAPSHOT_PERIOD_DATA_LOAD_PROFILE,
                    Frame30.ObisConst.READINGS,
                    Frame30.ObisConst.INTERVAL_DATA));

    private Unit loadProfileUnitScalar;
    private List<CollectedLoadProfile> collectedLoadProfiles;

    public EI7DataPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        super(comChannel, context);
    }

    @Override
    protected DlmsProperties getNewInstanceOfProperties() {
        return new A2Properties();
    }

    public ByteBuffer readInboundFrame() {
        byte[] header = new byte[8];
        getComChannel().startReading();
        int readBytes = getComChannel().read(header);
        if (readBytes != 8) {
            throw DataParseException.ioException(new ProtocolException("Attempted to read out 8 header bytes but received " + readBytes + " bytes instead..."));
        }

        int length = ProtocolTools.getIntFromBytes(header, 6, 2);

        byte[] frame = new byte[length];
        readBytes = getComChannel().read(frame);
        if (readBytes != length) {
            throw DataParseException.ioException(new ProtocolException("Attempted to read out full frame (" + length + " bytes), but received " + readBytes + " bytes instead..."));
        }
        return ByteBuffer.wrap(ProtocolTools.concatByteArrays(header, frame));
    }

    protected void parsePlainDataAPDU(ByteBuffer inboundFrame) {
        // 1. long-invoke-id-and-priority
        byte[] invokeIdAndPriority = new byte[4];   // 32-bits long format used
        inboundFrame.get(invokeIdAndPriority);

        //2. date-time
        final int dateTimeAxdrLength = DLMSUtils.getAXDRLength(inboundFrame.array(), inboundFrame.position());
        final int dateTimeLengthSize = DLMSUtils.getAXDRLengthOffset(dateTimeAxdrLength);

        // ... and as we're not even using this, just position the buffer index in front of the body.
        inboundFrame.position(inboundFrame.position() + dateTimeLengthSize + dateTimeAxdrLength);

        /* notification-body*/
        Structure structure = null;
        try {
            structure = AXDRDecoder.decode(inboundFrame.array(), inboundFrame.position(), 1, Structure.class);
            parseNotificationBody(structure);
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }
    }

    protected void parseNotificationBody(Structure structure) {
        AbstractDataType dataType = structure.getNextDataType();
        if (dataType instanceof OctetString) {
            byte[] compactFrame = ((OctetString) dataType).getOctetStr();
            byte compactFrameTag = compactFrame[0];
            if ( compactFrameTag == COMPACT_FRAME_30 ) {
                readCompactFrame30(compactFrame);
            } else if (compactFrameTag == COMPACT_FRAME_40) {
                readCompactFrame40(compactFrame);
            } else if (compactFrameTag == COMPACT_FRAME_47) {
                readCompactFrame47(compactFrame);
            } else if (compactFrameTag == COMPACT_FRAME_48) {
                readCompactFrame48(compactFrame);
            } else if (compactFrameTag == COMPACT_FRAME_49) {
                readCompactFrame49(compactFrame);
            } else if (compactFrameTag == COMPACT_FRAME_51) {
                readCompactFrame51(compactFrame);
            } else {
                throw DataParseException.generalParseException(new ProtocolException("Compact Frame " + compactFrameTag + " not supported!"));
            }
        } else {
            throw DataParseException.ioException(new ProtocolException("The element of the Data-notification body should contain the compact frame '" + dataType.getClass().getSimpleName() + "'"));
        }
    }

    private void readCompactFrame40(byte[] compactFrame) {
        try {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, 2, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Date dateTime = getDateTime(unixTime);

            readManagementFcOnline(dateTime, compactFrame, 6);
            readMetrologicalEventCounter(dateTime, compactFrame, 10);
            readEventCounter(dateTime, compactFrame, 12);
            readDailyDiagnostic(dateTime, compactFrame, 14);
            readVolumeUnitsAndScalar(dateTime, compactFrame, 16);
            readHalfHourLpIntervalLengthSeconds(dateTime, compactFrame, 47);

            readLoadProfile(DAILY_LOAD_PROFILE, compactFrame, 18);
            readLoadProfile(HALF_HOUR_LOAD_PROFILE, compactFrame, 51);
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    private void readCompactFrame30(byte[] compactFrame) {
        try {
            boolean isGPRS = inboundDAO.getDeviceProtocolProperties(getDeviceIdentifier()).getProperty(COMMUNICATION_TYPE_STR).equals(CommunicationType.GPRS.getName());
            Frame30.deserialize(compactFrame).save(this::addCollectedRegister, this::readLoadProfile, this::getDateTime, isGPRS);
        } catch (Exception e) {
            log("Error while reading compact frame 30:\n" + e.getMessage());
        }
    }

    private void readCompactFrame47(byte[] compactFrame) {
        try {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, 1, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Date dateTime = getDateTime(unixTime);

            readCompactFrameCommonPart(compactFrame, dateTime);

            readSnapshotPeriodCounter(dateTime, compactFrame, 23);
            readManagementFcOnline(dateTime, compactFrame, 24);
            //readSpareObject(dateTime, compactFrame, 28, 1); cannot decode OctetString with size 1
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    private void readCompactFrame48(byte[] compactFrame) {
        try {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, 1, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Date dateTime = getDateTime(unixTime);

            readCompactFrameCommonPart(compactFrame, dateTime);

            readLoadProfile(DAILY_LOAD_PROFILE, compactFrame, 23);

            readSnapshotPeriodCounter(dateTime, compactFrame, 66);
            readManagementFcOnline(dateTime, compactFrame, 67);
            //readSpareObject(dateTime, compactFrame, 71, 1); cannot decode OctetString with size 1
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    private void readCompactFrame49(byte[] compactFrame) {
        try {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, 1, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Date dateTime = getDateTime(unixTime);

            readCompactFrameCommonPart(compactFrame, dateTime);

            readLoadProfile(DAILY_LOAD_PROFILE, compactFrame, 23);

            readSnapshotPeriodCounter(dateTime, compactFrame, 66);

            //readLoadProfile(SNAPSHOT_PERIOD_DATA_LOAD_PROFILE, compactFrame, 67); TODO

            readManagementFcOnline(dateTime, compactFrame, 117);
            //readSpareObject(dateTime, compactFrame, 121, 1); cannot decode OctetString with size 1
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    /**
     * Compact Frame 47, 48 and 49 have a common first part. Extracted code into this method to avoid duplication.
     *
     * @param compactFrame pushed by device
     * @throws IOException from data type constructors
     */
    private void readCompactFrameCommonPart(byte[] compactFrame, Date dateTime) throws IOException {
        readNetworkStatus(dateTime, compactFrame, 5);
        readDisconnectControlOutputState(dateTime, compactFrame, 7);
        readDisconnectControlControlState(dateTime, compactFrame, 8);
        readMetrologicalEventCounter(dateTime, compactFrame, 9);
        readEventCounter(dateTime, compactFrame, 11);
        readDailyDiagnostic(dateTime, compactFrame, 13);
        readConvertedVolumeIndex(dateTime, compactFrame, 15);
        readConvertedUnderAlarmVolumeIndex(dateTime, compactFrame, 19);
    }

    private void readCompactFrame51(byte[] compactFrame) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(getDeviceTimeZone());
            Date dateTime = calendar.getTime();
            readLogicalDeviceName(dateTime, compactFrame, 1, 18);
            readManagementFcOnline(dateTime, compactFrame, 18);
            readManagementFcOffline(dateTime, compactFrame, 22);
            readGuarantorAuthorityFc(dateTime, compactFrame, 26);
            readInstallerMaintainerFc(dateTime, compactFrame, 30);
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    private Date getDateTime(Unsigned32 unixTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(getDeviceTimeZone());
        calendar.setTimeInMillis(unixTime.longValue() * 1000);
        return calendar.getTime();
    }

    private Date getDateTime(long unixTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(getDeviceTimeZone());
        calendar.setTimeInMillis(unixTime * 1000);
        return calendar.getTime();
    }

    private void readManagementFcOnline(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned32 managementFcOnline = new Unsigned32(getByteArray(compactFrame, offset, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        addCollectedRegister(MANAGEMENT_FRAME_COUNTER_ONLINE, managementFcOnline.getValue(), null, dateTime, null);
    }

    private void readManagementFcOffline(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned32 managementFcOffline = new Unsigned32(getByteArray(compactFrame, offset, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        addCollectedRegister(MANAGEMENT_FRAME_COUNTER_OFFLINE, managementFcOffline.getValue(), null, dateTime, null);
    }

    private void readGuarantorAuthorityFc(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned32 guarantorAuthorityFc = new Unsigned32(getByteArray(compactFrame, offset, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        addCollectedRegister(GUARANTOR_AUTHORITY_COUNTER, guarantorAuthorityFc.getValue(), null, dateTime, null);
    }

    private void readInstallerMaintainerFc(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned32 managementFcOnline = new Unsigned32(getByteArray(compactFrame, offset, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        addCollectedRegister(INSTALLER_MAINTAINER_COUNTER, managementFcOnline.getValue(), null, dateTime, null);
    }

    private void readNetworkStatus(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned16 pp4NetworkStatus = new Unsigned16(getByteArray(compactFrame, offset, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED), 0);
        addCollectedRegister(PP4_NETWORK_STATUS_OBISCODE, pp4NetworkStatus.getValue(), null, dateTime, null);
    }

    private void readDisconnectControlOutputState(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        BooleanObject outputState = new BooleanObject(getByteArray(compactFrame, offset, BooleanObject.SIZE, AxdrType.BOOLEAN), 0);
        addCollectedRegister(DISCONNECT_CONTROL_OUTPUT_STATE, outputState.longValue(), null, dateTime, null);
    }

    private void readDisconnectControlControlState(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        TypeEnum controlState = new TypeEnum(getByteArray(compactFrame, offset, TypeEnum.SIZE, AxdrType.ENUM), 0);
        addCollectedRegister(DISCONNECT_CONTROL_CONTROL_STATE, controlState.getValue(), null, dateTime, null);
    }

    private void readMetrologicalEventCounter(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned16 metrologicalEventCounter = new Unsigned16(getByteArray(compactFrame, offset, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED), 0);
        addCollectedRegister(METROLOGICAL_EVENT_COUNTER_OBISCODE, metrologicalEventCounter.longValue(), null, dateTime, null);
    }

    private void readEventCounter(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned16 eventCounter = new Unsigned16(getByteArray(compactFrame, offset, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED), 0);
        addCollectedRegister(EVENT_COUNTER_OBISCODE, eventCounter.longValue(), null, dateTime, null);
    }

    private void readDailyDiagnostic(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned16 dailyDiagnostic = new Unsigned16(getByteArray(compactFrame, offset, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED), 0);
        addCollectedRegister(DAILY_DIAGNOSTIC_OBISCODE, dailyDiagnostic.longValue(), null, dateTime, null);
    }

    private void readVolumeUnitsAndScalar(Date dateTime, byte[] compactFrame, int offset) {
        final byte[] scalarAndUnit = getByteArray(compactFrame, offset, 3, AxdrType.NULL);
        final byte scalar = scalarAndUnit[1]; // skip the first null byte
        final BaseUnit unit = BaseUnit.get(scalarAndUnit[2]);
        String volumeUnitsAndScalar = "Scalar = " + scalar + ", Unit = " + unit.toString();
        addCollectedRegister(CF40_VOLUME_UNITS_SCALAR, 0, null, dateTime, volumeUnitsAndScalar);
        this.loadProfileUnitScalar = Unit.get(unit.getDlmsCode(), scalar);
    }

    private void readHalfHourLpIntervalLengthSeconds(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned32 halfHourCapturePeriod = new Unsigned32(getByteArray(compactFrame, offset, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        addCollectedRegister(HALF_HOUR_LP_INTERVAL_LENGTH_SECONDS, halfHourCapturePeriod.longValue(), null, dateTime, null);
    }

    private void readConvertedVolumeIndex(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned32 volumeIndex = new Unsigned32(getByteArray(compactFrame, offset, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        addCollectedRegister(CONVERTED_VOLUME_INDEX, volumeIndex.longValue(), null, dateTime, null);
    }

    private void readConvertedUnderAlarmVolumeIndex(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned32 alarmVolumeIndex = new Unsigned32(getByteArray(compactFrame, offset, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        addCollectedRegister(CONVERTED_UNDER_ALARM_VOLUME_INDEX, alarmVolumeIndex.longValue(), null, dateTime, null);
    }

    private void readLoadProfile(ObisCode loadProfileToRead, DailyReadings[] dailyReadings) {
        DeviceOfflineFlags offlineContext = new DeviceOfflineFlags(DeviceOfflineFlags.ALL_LOAD_PROFILES_FLAG);
        OfflineDevice offlineDevice = inboundDAO.getOfflineDevice(deviceIdentifier, offlineContext);
        List<OfflineLoadProfile> allOfflineLoadProfiles = offlineDevice.getAllOfflineLoadProfiles();
        Optional<OfflineLoadProfile> offlineLoadProfile = allOfflineLoadProfiles.stream().filter(olp -> olp.getObisCode().equals(loadProfileToRead)).findAny();
        if (offlineLoadProfile.isPresent()) {
            log("Reading load profile " + loadProfileToRead.toString());
            try {
                List<ChannelInfo> channelInfos = getDeviceChannelInfo(offlineLoadProfile.get());
                LoadProfileIdentifierByObisCodeAndDevice loadProfileIdentifier = new LoadProfileIdentifierByObisCodeAndDevice(loadProfileToRead, deviceIdentifier);
                CollectedLoadProfile collectedLoadProfile = getCollectedDataFactory().createCollectedLoadProfile(loadProfileIdentifier);
                List<IntervalData> collectedIntervalData = new ArrayList<>();
                for (DailyReadings dr : dailyReadings) {
                    collectedIntervalData.add(createCollectedIntervalData(dr.unixTime, dr.dailyDiagnostic, dr.currentIndexOfConvertedVolume,
                            dr.currentIndexOfConvertedVolumeUnderAlarm, new Unsigned8(0)));
                }
                collectedLoadProfile.setCollectedIntervalData(collectedIntervalData, channelInfos);
                collectedLoadProfile.setDoStoreOlderValues(true);
                getCollectedLoadProfiles().add(collectedLoadProfile);
            }
            catch (IOException e) {
                log("Error while reading load profile " + loadProfileToRead.toString() + "\n" + e.getMessage());
            }
        }
    }

    private void readLoadProfile(ObisCode loadProfileToRead, byte[] compactFrame, int offset) throws IOException {
        DeviceOfflineFlags offlineContext = new DeviceOfflineFlags(DeviceOfflineFlags.ALL_LOAD_PROFILES_FLAG);
        OfflineDevice offlineDevice = inboundDAO.getOfflineDevice(deviceIdentifier, offlineContext);
        List<OfflineLoadProfile> allOfflineLoadProfiles = offlineDevice.getAllOfflineLoadProfiles();
        Optional<OfflineLoadProfile> offlineLoadProfile = allOfflineLoadProfiles.stream().filter(olp -> olp.getObisCode().equals(loadProfileToRead)).findAny();
        if (offlineLoadProfile.isPresent()) {
            log("Reading load profile " + loadProfileToRead.toString());
            List<ChannelInfo> channelInfos = getDeviceChannelInfo(offlineLoadProfile.get());
            LoadProfileIdentifierByObisCodeAndDevice loadProfileIdentifier = new LoadProfileIdentifierByObisCodeAndDevice(loadProfileToRead, deviceIdentifier);
            CollectedLoadProfile collectedLoadProfile = getCollectedDataFactory().createCollectedLoadProfile(loadProfileIdentifier);
            List<IntervalData> collectedIntervalData = readCollectedIntervalData(offlineLoadProfile.get(), compactFrame, offset);
            collectedLoadProfile.setCollectedIntervalData(collectedIntervalData, channelInfos);
            collectedLoadProfile.setDoStoreOlderValues(true);
            getCollectedLoadProfiles().add(collectedLoadProfile);
        }
    }

    private void readLogicalDeviceName(Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        VisibleString logicalDeviceName = new VisibleString(getByteArray(compactFrame, offset, length, AxdrType.VISIBLE_STRING), 0);
        addCollectedRegister(COSEM_LOGICAL_DEVICE_NAME, 0, null, dateTime, logicalDeviceName.getStr());
    }

    private void readSnapshotPeriodCounter(Date dateTime, byte[] compactFrame, int offset) throws IOException {
        Unsigned8 snapshotPeriodCounter = new Unsigned8(getByteArray(compactFrame, offset, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
        addCollectedRegister(BILLING_SNAPSHOT_PERIOD_COUNTER, snapshotPeriodCounter.getValue(), null, dateTime, null);
    }

    private void readSpareObject(Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        OctetString spareObject = new OctetString(getByteArray(compactFrame, offset, length, AxdrType.OCTET_STRING), 0);
        addCollectedRegister(SPARE_OBJECT, 0, null, dateTime, spareObject.stringValue());
    }

    private byte[] getByteArray(byte[] compactFrame, int offset, int length, AxdrType type) {
        byte[] array = new byte[length];
        System.arraycopy(compactFrame, offset, array, 1, length - 1);
        array[0] = type.getTag();
        return array;
    }

    protected DeviceIdentifier getDeviceIdentifierBasedOnSystemTitle(byte[] systemTitle) {
        byte[] remainingBytes = ProtocolTools.getSubArray(systemTitle, 2);
        remainingBytes = ProtocolTools.reverseByteArray(remainingBytes);
        String remainingData = ProtocolTools.getHexStringFromBytes(remainingBytes, "");
        String serverSystemTitle = "ELS3" + remainingData;
        return new DeviceIdentifierBySerialNumber(serverSystemTitle);
    }

    private List<IntervalData> readCollectedIntervalData(OfflineLoadProfile offlineLoadProfile, byte[] compactFrame, int offset) throws IOException {
        byte entriesNumber = compactFrame[offset++];
        List<IntervalData> collectedIntervalData = new ArrayList<>();
        for (int i = 0; i < entriesNumber; i++) {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, offset, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned16 dailyDiagnostic = new Unsigned16(getByteArray(compactFrame, offset + 4, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED), 0);
            Unsigned32 currentIndexOfConvertedVolume = new Unsigned32(getByteArray(compactFrame, offset + 6, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned32 currentIndexOfConvertedVolumeUnderAlarm = new Unsigned32(getByteArray(compactFrame, offset + 10, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            if (offlineLoadProfile.getObisCode().equals(HALF_HOUR_LOAD_PROFILE)) {
                Unsigned8 currentActiveTariff = new Unsigned8(getByteArray(compactFrame, offset + 14, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
                collectedIntervalData.add( createCollectedIntervalData(unixTime, dailyDiagnostic, currentIndexOfConvertedVolume,
                        currentIndexOfConvertedVolumeUnderAlarm, currentActiveTariff));
                offset += 15;
            } else {
                collectedIntervalData.add(createCollectedIntervalData(unixTime, dailyDiagnostic, currentIndexOfConvertedVolume,
                        currentIndexOfConvertedVolumeUnderAlarm, new Unsigned8(0)));
                offset += 14;
            }
        }
        return collectedIntervalData;
    }

    private IntervalData createCollectedIntervalData( Unsigned32 unixTime, Unsigned16 dailyDiagnostic,
                                             Unsigned32 currentIndexOfConvertedVolume,
                                             Unsigned32 currentIndexOfConvertedVolumeUnderAlarm,
                                             Unsigned8 currentActiveTariff) {
        IntervalData collectedIntervalData = new IntervalData();
        Calendar dateTime = Calendar.getInstance(getDeviceTimeZone());
        dateTime.setTimeInMillis(unixTime.longValue() * 1000);
        // set seconds and milliseconds to 0 just in case
        dateTime.set(Calendar.SECOND, 0);
        dateTime.set(Calendar.MILLISECOND, 0);
        List<IntervalValue> intervalValues = new ArrayList<>();
        intervalValues.add(new IntervalValue(currentIndexOfConvertedVolume.intValue(), 0, getEiServerStatus(0)));
        intervalValues.add(new IntervalValue(currentIndexOfConvertedVolumeUnderAlarm.intValue(), 0, getEiServerStatus(0)));

        collectedIntervalData = new IntervalData(dateTime.getTime(), getEiServerStatus(dailyDiagnostic.intValue()),
                dailyDiagnostic.intValue(), currentActiveTariff.intValue(), intervalValues);

        return collectedIntervalData;
    }

    private List<ChannelInfo> getDeviceChannelInfo(OfflineLoadProfile offlineLoadProfile) throws ProtocolException {
        if (isSupported(offlineLoadProfile)) {
            List<ChannelInfo> channelInfos = new ArrayList<>();

            int ch = 0;

            if (SNAPSHOT_PERIOD_DATA_LOAD_PROFILE.equals(offlineLoadProfile.getObisCode())) {
                final ChannelInfo channel1 = new ChannelInfo(ch++, CONVERTED_VOLUME_INDEX.toString(), Unit.getUndefined(), getDeviceId());
                if (isCumulative(channel1.getChannelObisCode())) {
                    channel1.setCumulative();
                }
                channelInfos.add(channel1);

                final ChannelInfo channel2 = new ChannelInfo(ch++, CONVERTED_VOLUME_INDEX_F1_RATE.toString(), Unit.getUndefined(), getDeviceId());
                if (isCumulative(channel2.getChannelObisCode())) {
                    channel2.setCumulative();
                }
                channelInfos.add(channel2);

                final ChannelInfo channel3 = new ChannelInfo(ch++, CONVERTED_VOLUME_INDEX_F2_RATE.toString(), Unit.getUndefined(), getDeviceId());
                if (isCumulative(channel3.getChannelObisCode())) {
                    channel3.setCumulative();
                }
                channelInfos.add(channel3);

                final ChannelInfo channel4 = new ChannelInfo(ch++, CONVERTED_VOLUME_INDEX_F3_RATE.toString(), Unit.getUndefined(), getDeviceId());
                if (isCumulative(channel4.getChannelObisCode())) {
                    channel4.setCumulative();
                }
                channelInfos.add(channel4);

                final ChannelInfo channel5 = new ChannelInfo(ch++, CONVERTED_UNDER_ALARM_VOLUME_INDEX.toString(), Unit.getUndefined(), getDeviceId());
                if (isCumulative(channel5.getChannelObisCode())) {
                    channel5.setCumulative();
                }
                channelInfos.add(channel5);

                final ChannelInfo channel6 = new ChannelInfo(ch++, MAXIMUM_CONVENTIONAL_CONVERTED_GAS_FLOW.toString(), Unit.getUndefined(), getDeviceId());
                if (isCumulative(channel6.getChannelObisCode())) {
                    channel6.setCumulative();
                }
                channelInfos.add(channel6);
            } else {
                final ChannelInfo channel1 = new ChannelInfo(ch++, CONVERTED_VOLUME_INDEX.toString(),
                        loadProfileUnitScalar == null ? Unit.getUndefined() : loadProfileUnitScalar, getDeviceId());
                if (isCumulative(channel1.getChannelObisCode())) {
                    channel1.setCumulative();
                }
                channelInfos.add(channel1);

                final ChannelInfo channel2 = new ChannelInfo(ch, CONVERTED_UNDER_ALARM_VOLUME_INDEX.toString(),
                        loadProfileUnitScalar == null ? Unit.getUndefined() : loadProfileUnitScalar, getDeviceId());
                if (isCumulative(channel2.getChannelObisCode())) {
                    channel2.setCumulative();
                }
                channelInfos.add(channel2);
            }

            return channelInfos;
        }
        throw new ProtocolException("Unsupported Load Profile " + offlineLoadProfile.getObisCode());
    }

    private static boolean isSupported(OfflineLoadProfile offlineLoadProfile) {
        for (final ObisCode supportedLoadProfile : supportedLoadProfiles) {
            if (offlineLoadProfile.getObisCode().equalsIgnoreBChannel(supportedLoadProfile)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCumulative(ObisCode obisCode) {
        return ParseUtils.isObisCodeCumulative(obisCode);
    }

    private String getDeviceId() {
        return (String) deviceIdentifier.forIntrospection().getValue("serialNumber");
    }

    public List<CollectedLoadProfile> getCollectedLoadProfiles() {
        if (this.collectedLoadProfiles == null) {
            this.collectedLoadProfiles = new ArrayList<>();
        }
        return this.collectedLoadProfiles;
    }
}