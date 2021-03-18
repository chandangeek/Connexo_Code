/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
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
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.a2.properties.A2Properties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.energyict.protocolimplv2.dlms.a2.profile.A2ProfileDataReader.getEiServerStatus;

public class EI7DataPushNotificationParser extends EventPushNotificationParser {

    private static byte COMPACT_FRAME_40 = (byte) 0x28;
    private static byte COMPACT_FRAME_47 = (byte) 0x2F;
    private static byte COMPACT_FRAME_48 = (byte) 0x30;
    private static byte COMPACT_FRAME_49 = (byte) 0x31;
    private static byte COMPACT_FRAME_51 = (byte) 0x33;

    private static final ObisCode PP4_NETWORK_STATUS_OBISCODE = ObisCode.fromString("0.1.96.5.4.255");
    private static final ObisCode DISCONNECT_CONTROL_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    private static final ObisCode METROLOGICAL_EVENT_COUNTER_OBISCODE = ObisCode.fromString("0.0.96.15.1.255");
    private static final ObisCode EVENT_COUNTER_OBISCODE = ObisCode.fromString("0.0.96.15.2.255");
    private static final ObisCode DAILY_DIAGNOSTIC_OBISCODE = ObisCode.fromString("7.1.96.5.1.255");
    private static final ObisCode CONVERTED_VOLUME_INDEX = ObisCode.fromString("7.0.13.2.0.255");
    private static final ObisCode CONVERTED_UNDER_ALARM_VOLUME_INDEX = ObisCode.fromString("7.0.12.2.0.255");
    private static final ObisCode CURRENT_ACTIVE_TARIFF = ObisCode.fromString("0.0.96.14.0.255");

    private static final ObisCode[] REGISTERS_TO_READ = new ObisCode[]{
            PP4_NETWORK_STATUS_OBISCODE,
            DISCONNECT_CONTROL_OBISCODE,
            METROLOGICAL_EVENT_COUNTER_OBISCODE,
            EVENT_COUNTER_OBISCODE,
            DAILY_DIAGNOSTIC_OBISCODE,
            CONVERTED_VOLUME_INDEX,
            CONVERTED_UNDER_ALARM_VOLUME_INDEX
    };

    private static final ObisCode MANAGEMENT_FRAME_COUNTER_ONLINE = ObisCode.fromString("0.0.43.1.1.255");
    private static final ObisCode CF40_VOLUME_UNITS_SCALAR = ObisCode.fromString("7.0.13.2.3.255");
    private static final ObisCode HALF_HOUR_LP_INTERVAL_LENGTH_SECONDS = ObisCode.fromString("7.4.99.99.1.255");

    private static final ObisCode[] CF_40_REGISTERS = new ObisCode[] {
            MANAGEMENT_FRAME_COUNTER_ONLINE,
            METROLOGICAL_EVENT_COUNTER_OBISCODE,
            EVENT_COUNTER_OBISCODE,
            DAILY_DIAGNOSTIC_OBISCODE,
            CF40_VOLUME_UNITS_SCALAR,
            HALF_HOUR_LP_INTERVAL_LENGTH_SECONDS
    };

    private static final ObisCode DAILY_LOAD_PROFILE = ObisCode.fromString("7.0.99.99.3.255");
    private static final ObisCode HALF_HOUR_LOAD_PROFILE = ObisCode.fromString("7.0.99.99.1.255");

    private static final ObisCode[] supportedLoadProfiles = new ObisCode[] {
            HALF_HOUR_LOAD_PROFILE, DAILY_LOAD_PROFILE
    };

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
            if (compactFrameTag == COMPACT_FRAME_40) {
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
                throw DataParseException.generalParseException( new ProtocolException("Compact Frame " + compactFrameTag + " not supported!") );
            }
        } else {
            throw DataParseException.ioException(new ProtocolException("The element of the Data-notification body should contain the compact frame '" + dataType.getClass().getSimpleName() + "'"));
        }
    }

    private void readCompactFrame40(byte[] compactFrame) {
        try {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, 2, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Date dateTime = getDateTime(unixTime);
            for (ObisCode obisCode : CF_40_REGISTERS) {
                readManagementFcOnline(obisCode, dateTime, compactFrame, 6, 4);
                readMetrologicalEventCounter(obisCode, dateTime, compactFrame, 10, 2);
                readEventCounter(obisCode, dateTime, compactFrame, 12, 2);
                readDailyDiagnostic(obisCode, dateTime, compactFrame, 14, 2);
                readVolumeUnitsAndScalar(obisCode, dateTime, compactFrame, 16, 2);
                readHalfHourLpIntervalLengthSeconds(obisCode, dateTime, compactFrame, 47, 4);
            }
            readLoadProfile(DAILY_LOAD_PROFILE, compactFrame, 18);
            readLoadProfile(HALF_HOUR_LOAD_PROFILE, compactFrame, 51);
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    private void readCompactFrame47(byte[] compactFrame) {
        try {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, 1, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Date dateTime = getDateTime(unixTime);
            List<OfflineRegister> allOfflineRegisters = getOfflineRegisters();
            for (ObisCode obisCode : REGISTERS_TO_READ) {
                Optional<OfflineRegister> offlineRegister = allOfflineRegisters.stream().filter(olr -> obisCode.equals(olr.getObisCode())).findAny();
                if (offlineRegister.isPresent()) {
                    readNetworkStatus(obisCode, dateTime, compactFrame, 5, 2);
                    readDisconnectControl(obisCode, dateTime, compactFrame, 7, 1);
                    readMetrologicalEventCounter(obisCode, dateTime, compactFrame, 9, 2);
                    readEventCounter(obisCode, dateTime, compactFrame, 11, 2);
                    readDailyDiagnostic(obisCode, dateTime, compactFrame, 13, 2);
                    readConvertedVolumeIndex(obisCode, dateTime, compactFrame, 15, 4);
                    readConvertedUnderAlarmVolumeIndex(obisCode, dateTime, compactFrame, 19, 4);
                }
            }
            Unsigned8 snapshotPeriodCounter = new Unsigned8(getByteArray(compactFrame, 23, 1, AxdrType.UNSIGNED), 0);
            Unsigned32 managementFrameCounter = new Unsigned32(getByteArray(compactFrame, 24, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    private void readCompactFrame48(byte[] compactFrame) {
        try {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, 1, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Date dateTime = getDateTime(unixTime);
            List<OfflineRegister> allOfflineRegisters = getOfflineRegisters();
            for (ObisCode obisCode : REGISTERS_TO_READ) {
                Optional<OfflineRegister> offlineRegister = allOfflineRegisters.stream().filter(olr -> obisCode.equals(olr.getObisCode())).findAny();
                if (offlineRegister.isPresent()) {
                    readNetworkStatus(obisCode, dateTime, compactFrame, 5, 2);
                    readDisconnectControl(obisCode, dateTime, compactFrame, 7, 1);
                    readEventCounter(obisCode, dateTime, compactFrame, 8, 2);
                    readDailyDiagnostic(obisCode, dateTime, compactFrame, 10, 2);
                    readConvertedVolumeIndex(obisCode, dateTime, compactFrame, 12, 4);
                    readConvertedUnderAlarmVolumeIndex(obisCode, dateTime, compactFrame, 16, 4);
                }
            }
            readLoadProfile(DAILY_LOAD_PROFILE, compactFrame, 20);
            Unsigned8 snapshotPeriodCounter = new Unsigned8(getByteArray(compactFrame, 63, 1, AxdrType.UNSIGNED), 0);
            Unsigned32 managementFrameCounter = new Unsigned32(getByteArray(compactFrame, 64, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    private void readCompactFrame49(byte[] compactFrame) {
        try {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, 1, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Date dateTime = getDateTime(unixTime);
            List<OfflineRegister> allOfflineRegisters = getOfflineRegisters();
            for (ObisCode obisCode : REGISTERS_TO_READ) {
                Optional<OfflineRegister> offlineRegister = allOfflineRegisters.stream().filter(olr -> obisCode.equals(olr.getObisCode())).findAny();
                if (offlineRegister.isPresent()) {
                    readNetworkStatus(obisCode, dateTime, compactFrame, 5, 2);
                    readDisconnectControl(obisCode, dateTime, compactFrame, 7, 1);
                    readMetrologicalEventCounter(obisCode, dateTime, compactFrame, 9, 2);
                    readEventCounter(obisCode, dateTime, compactFrame, 11, 2);
                    readDailyDiagnostic(obisCode, dateTime, compactFrame, 13, 2);
                    readConvertedVolumeIndex(obisCode, dateTime, compactFrame, 15, 4);
                    readConvertedUnderAlarmVolumeIndex(obisCode, dateTime, compactFrame, 19, 4);
                }
            }
            readLoadProfile(DAILY_LOAD_PROFILE, compactFrame, 23);
            Unsigned8 snapshotPeriodCounter = new Unsigned8(getByteArray(compactFrame, 66, 1, AxdrType.UNSIGNED), 0);
            readSnapshotPeriodDate(compactFrame, 67, 50);
            Unsigned32 managementFrameCounter = new Unsigned32(getByteArray(compactFrame, 117, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    private void readCompactFrame51(byte[] compactFrame) {
        try {
            readLogicalDeviceName(compactFrame, 1, 17);
            Unsigned32 managementFrameCounterOnline = new Unsigned32(getByteArray(compactFrame, 18, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned32 managementFrameCounterOffline = new Unsigned32(getByteArray(compactFrame, 22, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned32 guarantorAuthorityFrameCounter = new Unsigned32(getByteArray(compactFrame, 26, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned32 installerMainteinerFrameCounter = new Unsigned32(getByteArray(compactFrame, 32, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    private Date getDateTime(Unsigned32 unixTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(getDeviceTimeZone());
        calendar.setTimeInMillis(unixTime.longValue()*1000);
        return calendar.getTime();
    }

    private List<OfflineRegister> getOfflineRegisters() {
        DeviceOfflineFlags offlineContext = new DeviceOfflineFlags(DeviceOfflineFlags.REGISTERS_FLAG);
        OfflineDevice offlineDevice = inboundDAO.getOfflineDevice(deviceIdentifier, offlineContext);
        return offlineDevice.getAllOfflineRegisters();
    }

    private void readManagementFcOnline(ObisCode obisCode, Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        if (MANAGEMENT_FRAME_COUNTER_ONLINE.equals(obisCode)) {
            Unsigned32 managementFcOnline = new Unsigned32(getByteArray(compactFrame, offset, length, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            addCollectedRegister(obisCode, managementFcOnline.getValue(), null, dateTime, null);
        }
    }

    private void readNetworkStatus(ObisCode obisCode, Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        if (PP4_NETWORK_STATUS_OBISCODE.equals(obisCode)) {
            Unsigned16 pp4NetworkStatus = new Unsigned16(getByteArray(compactFrame, offset, length, AxdrType.LONG_UNSIGNED), 0);
            addCollectedRegister(obisCode, pp4NetworkStatus.getValue(), null, dateTime, null);
        }
    }

    private void readDisconnectControl(ObisCode obisCode, Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        if (DISCONNECT_CONTROL_OBISCODE.equals(obisCode)) {
            BooleanObject disconnectControl = new BooleanObject(getByteArray(compactFrame, offset, length, AxdrType.BOOLEAN), 0);
            addCollectedRegister(obisCode, disconnectControl.longValue(), null, dateTime, null);
        }
    }

    private void readMetrologicalEventCounter(ObisCode obisCode, Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        if (METROLOGICAL_EVENT_COUNTER_OBISCODE.equals(obisCode)) {
            Unsigned16 metrologicalEventCounter = new Unsigned16(getByteArray(compactFrame, offset, length, AxdrType.LONG_UNSIGNED), 0);
            addCollectedRegister(obisCode, metrologicalEventCounter.longValue(), null, dateTime, null);
        }
    }

    private void readEventCounter(ObisCode obisCode, Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        if (EVENT_COUNTER_OBISCODE.equals(obisCode)) {
            Unsigned16 eventCounter = new Unsigned16(getByteArray(compactFrame, offset, length, AxdrType.LONG_UNSIGNED), 0);
            addCollectedRegister(obisCode, eventCounter.longValue(), null, dateTime, null);
        }
    }

    private void readDailyDiagnostic(ObisCode obisCode, Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        if (DAILY_DIAGNOSTIC_OBISCODE.equals(obisCode)) {
            Unsigned16 dailyDiagnostic = new Unsigned16(getByteArray(compactFrame, offset, length, AxdrType.LONG_UNSIGNED), 0);
            addCollectedRegister(obisCode, dailyDiagnostic.longValue(), null, dateTime, null);
        }
    }

    private void readVolumeUnitsAndScalar(ObisCode obisCode, Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        if (CF40_VOLUME_UNITS_SCALAR.equals(obisCode)) {
            final byte[] scalarAndUnit = getByteArray(compactFrame, offset, length, AxdrType.NULL);
            final byte scalar = scalarAndUnit[1]; // skip the first null byte
            final BaseUnit unit = BaseUnit.get(scalarAndUnit[2]);
            String volumeUnitsAndScalar = "Scalar = " + scalar + ", Unit = " + unit.toString();
            addCollectedRegister(obisCode, 0, null, dateTime, volumeUnitsAndScalar);
            this.loadProfileUnitScalar = Unit.get(unit.getDlmsCode(), scalar);
        }
    }

    private void readHalfHourLpIntervalLengthSeconds(ObisCode obisCode, Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        if (HALF_HOUR_LP_INTERVAL_LENGTH_SECONDS.equals(obisCode)) {
            Unsigned32 halfHourCapturePeriod = new Unsigned32(getByteArray(compactFrame, offset, length, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            addCollectedRegister(obisCode, halfHourCapturePeriod.longValue(), null, dateTime, null);
        }
    }

    private void readConvertedVolumeIndex(ObisCode obisCode, Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        if (CONVERTED_VOLUME_INDEX.equals(obisCode)) {
            Unsigned32 eventCounter = new Unsigned32(getByteArray(compactFrame, offset, length, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            addCollectedRegister(obisCode, eventCounter.longValue(), null, dateTime, null);
        }
    }

    private void readConvertedUnderAlarmVolumeIndex(ObisCode obisCode, Date dateTime, byte[] compactFrame, int offset, int length) throws IOException {
        if (CONVERTED_UNDER_ALARM_VOLUME_INDEX.equals(obisCode)) {
            Unsigned32 eventCounter = new Unsigned32(getByteArray(compactFrame, offset, length, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            addCollectedRegister(obisCode, eventCounter.longValue(), null, dateTime, null);
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

    private void readLogicalDeviceName(byte[] compactFrame, int offset, int length) throws IOException {
    }

    private void readSnapshotPeriodDate(byte[] compactFrame, int offset, int length) throws IOException {
    }

    private byte[] getByteArray(byte[] compactFrame, int offset, int length, AxdrType type) {
        byte[] array = new byte[length + 1];
        System.arraycopy(compactFrame, offset, array, 1, length);
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
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, offset, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned16 dailyDiagnostic = new Unsigned16(getByteArray(compactFrame, offset + 4, 2, AxdrType.LONG_UNSIGNED), 0);
            Unsigned32 currentIndexOfConvertedVolume = new Unsigned32(getByteArray(compactFrame, offset + 6, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned32 currentIndexOfConvertedVolumeUnderAlarm = new Unsigned32(getByteArray(compactFrame, offset + 10, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            if (offlineLoadProfile.getObisCode().equals(HALF_HOUR_LOAD_PROFILE)) {
                Unsigned8 currentActiveTariff = new Unsigned8(getByteArray(compactFrame, offset + 14, 1, AxdrType.UNSIGNED), 0);
                createCollectedIntervalData(offlineLoadProfile, collectedIntervalData, unixTime, dailyDiagnostic, currentIndexOfConvertedVolume,
                        currentIndexOfConvertedVolumeUnderAlarm, currentActiveTariff);
                offset += 15;
            } else {
                createCollectedIntervalData(offlineLoadProfile, collectedIntervalData, unixTime, dailyDiagnostic, currentIndexOfConvertedVolume,
                        currentIndexOfConvertedVolumeUnderAlarm, null);
                offset += 14;
            }
        }
        return collectedIntervalData;
    }

    private void createCollectedIntervalData(OfflineLoadProfile offlineLoadProfile, List<IntervalData> collectedIntervalData,
                                             Unsigned32 unixTime, Unsigned16 dailyDiagnostic,
                                             Unsigned32 currentIndexOfConvertedVolume,
                                             Unsigned32 currentIndexOfConvertedVolumeUnderAlarm,
                                             Unsigned8 currentActiveTariff) {
        Calendar dateTime = Calendar.getInstance(getDeviceTimeZone());
        dateTime.setTimeInMillis(unixTime.longValue() * 1000);
        // set seconds and milliseconds to 0 just in case
        dateTime.set(Calendar.SECOND, 0);
        dateTime.set(Calendar.MILLISECOND, 0);
        if (offlineLoadProfile.getObisCode().equals(DAILY_LOAD_PROFILE)) {
            // Fix: Connexo only accepts daily intervals at 00:00
            dateTime.set(Calendar.HOUR_OF_DAY, 0);
        }
        List<IntervalValue> intervalValues = new ArrayList<>();
        intervalValues.add(new IntervalValue(currentIndexOfConvertedVolume.intValue(), 0, getEiServerStatus(0)));
        intervalValues.add(new IntervalValue(currentIndexOfConvertedVolumeUnderAlarm.intValue(), 0, getEiServerStatus(0)));
        if (currentActiveTariff != null) {
            intervalValues.add(new IntervalValue(currentActiveTariff.intValue(), 0, getEiServerStatus(0)));
        }
        collectedIntervalData.add(new IntervalData(dateTime.getTime(), getEiServerStatus(dailyDiagnostic.intValue()), dailyDiagnostic.intValue(), 0, intervalValues));
    }

    private List<ChannelInfo> getDeviceChannelInfo(OfflineLoadProfile offlineLoadProfile) throws ProtocolException {
        if (isSupported(offlineLoadProfile)) {
            List<ChannelInfo> channelInfos = new ArrayList<>();

            int ch = 0;
            final ChannelInfo channel1 = new ChannelInfo(ch++, CONVERTED_VOLUME_INDEX.toString(),
                    loadProfileUnitScalar == null ? Unit.getUndefined() : loadProfileUnitScalar, getDeviceId());
            if (isCumulative( channel1.getChannelObisCode() )) {
                channel1.setCumulative();
            }
            channelInfos.add(channel1);

            final ChannelInfo channel2 = new ChannelInfo(ch++, CONVERTED_UNDER_ALARM_VOLUME_INDEX.toString(),
                    loadProfileUnitScalar == null ? Unit.getUndefined() : loadProfileUnitScalar, getDeviceId());
            if (isCumulative( channel2.getChannelObisCode() )) {
                channel2.setCumulative();
            }
            channelInfos.add(channel2);

            if (offlineLoadProfile.getObisCode().equals(HALF_HOUR_LOAD_PROFILE)) {
                final ChannelInfo channel3 = new ChannelInfo(ch, CURRENT_ACTIVE_TARIFF.toString(), Unit.getUndefined(), getDeviceId());
                channelInfos.add(channel3);
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