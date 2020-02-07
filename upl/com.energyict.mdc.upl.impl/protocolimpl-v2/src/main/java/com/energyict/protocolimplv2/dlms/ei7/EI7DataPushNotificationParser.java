/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.g3.EventPushNotificationParser;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfileChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.a2.properties.A2Properties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class EI7DataPushNotificationParser extends EventPushNotificationParser {

    private static byte COMPACT_FRAME_47 = (byte) 0x2F;
    private static byte COMPACT_FRAME_48 = (byte) 0x30;
    private static byte COMPACT_FRAME_49 = (byte) 0x31;
    private static byte COMPACT_FRAME_51 = (byte) 0x33;

    private static final ObisCode DAILY_LOAD_PROFILE_OBISCODE = ObisCode.fromString("7.0.99.99.3.255");

    private static final ObisCode LOAD_PROFILES_TO_READ = DAILY_LOAD_PROFILE_OBISCODE;

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
            if (compactFrameTag == COMPACT_FRAME_47)
                readCompactFrame47(compactFrame);
            else if (compactFrameTag == COMPACT_FRAME_48)
                readCompactFrame48(compactFrame);
            else if (compactFrameTag == COMPACT_FRAME_49)
                readCompactFrame49(compactFrame);
            else if (compactFrameTag == COMPACT_FRAME_51)
                readCompactFrame51(compactFrame);
            else ;
        } else {
            throw DataParseException.ioException(new ProtocolException("The element of the Data-notification body should contain the compact frame '" + dataType.getClass().getSimpleName() + "'"));
        }
    }

    private void readCompactFrame47(byte[] compactFrame) {
        try {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, 1, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned16 pp4NetworkStatus = new Unsigned16(getByteArray(compactFrame, 5, 2, AxdrType.LONG_UNSIGNED), 0);
            BooleanObject disconnectControl = new BooleanObject(getByteArray(compactFrame, 7, 1, AxdrType.BOOLEAN), 0);
            BooleanObject disconnectControl1 = new BooleanObject(getByteArray(compactFrame, 8, 1, AxdrType.BOOLEAN), 0);
            Unsigned16 metrologicalEventCounter = new Unsigned16(getByteArray(compactFrame, 9, 2, AxdrType.LONG_UNSIGNED), 0);
            Unsigned16 eventCounter = new Unsigned16(getByteArray(compactFrame, 11, 2, AxdrType.LONG_UNSIGNED), 0);
            Unsigned16 dailyDiagnostic = new Unsigned16(getByteArray(compactFrame, 13, 2, AxdrType.LONG_UNSIGNED), 0);
            Unsigned32 currentIndexOfConvertedVolume = new Unsigned32(getByteArray(compactFrame, 15, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned32 currentIndexOfConvertedVolumeUnderAlarm = new Unsigned32(getByteArray(compactFrame, 19, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned8 snapshotPeriodCounter = new Unsigned8(getByteArray(compactFrame, 23, 1, AxdrType.UNSIGNED), 0);
            Unsigned32 managementFrameCounter = new Unsigned32(getByteArray(compactFrame, 24, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    private void readCompactFrame48(byte[] compactFrame) {
        try {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, 1, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned16 pp4NetworkStatus = new Unsigned16(getByteArray(compactFrame, 5, 2, AxdrType.LONG_UNSIGNED), 0);
            BooleanObject disconnectControl = new BooleanObject(getByteArray(compactFrame, 7, 1, AxdrType.BOOLEAN), 0);
            Unsigned16 eventCounter = new Unsigned16(getByteArray(compactFrame, 8, 2, AxdrType.LONG_UNSIGNED), 0);
            Unsigned16 dailyDiagnostic = new Unsigned16(getByteArray(compactFrame, 10, 2, AxdrType.LONG_UNSIGNED), 0);
            Unsigned32 currentIndexOfConvertedVolume = new Unsigned32(getByteArray(compactFrame, 12, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned32 currentIndexOfConvertedVolumeUnderAlarm = new Unsigned32(getByteArray(compactFrame, 16, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            readDailyLoadProfile(compactFrame, 20, 43);
            Unsigned8 snapshotPeriodCounter = new Unsigned8(getByteArray(compactFrame, 63, 1, AxdrType.UNSIGNED), 0);
            Unsigned32 managementFrameCounter = new Unsigned32(getByteArray(compactFrame, 64, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    private void readCompactFrame49(byte[] compactFrame) {
        try {
            Unsigned32 unixTime = new Unsigned32(getByteArray(compactFrame, 1, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned16 pp4NetworkStatus = new Unsigned16(getByteArray(compactFrame, 5, 2, AxdrType.LONG_UNSIGNED), 0);
            BooleanObject disconnectControl = new BooleanObject(getByteArray(compactFrame, 7, 1, AxdrType.BOOLEAN), 0);
            BooleanObject disconnectControl1 = new BooleanObject(getByteArray(compactFrame, 8, 1, AxdrType.BOOLEAN), 0);
            Unsigned16 metrologicalEventCounter = new Unsigned16(getByteArray(compactFrame, 9, 2, AxdrType.LONG_UNSIGNED), 0);
            Unsigned16 eventCounter = new Unsigned16(getByteArray(compactFrame, 11, 2, AxdrType.LONG_UNSIGNED), 0);
            Unsigned16 dailyDiagnostic = new Unsigned16(getByteArray(compactFrame, 13, 2, AxdrType.LONG_UNSIGNED), 0);
            Unsigned32 currentIndexOfConvertedVolume = new Unsigned32(getByteArray(compactFrame, 15, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            Unsigned32 currentIndexOfConvertedVolumeUnderAlarm = new Unsigned32(getByteArray(compactFrame, 19, 4, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
            readDailyLoadProfile(compactFrame, 23, 43);
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

    private void readLogicalDeviceName(byte[] compactFrame, int offset, int length) throws IOException {
    }

    private void readDailyLoadProfile(byte[] compactFrame, int offset, int length) throws IOException {
        DeviceOfflineFlags offlineContext = new DeviceOfflineFlags(DeviceOfflineFlags.ALL_LOAD_PROFILES_FLAG);
        OfflineDevice offlineDevice = inboundDAO.getOfflineDevice(deviceIdentifier, offlineContext);
        List<OfflineLoadProfile> allOfflineLoadProfiles = offlineDevice.getAllOfflineLoadProfiles();
        Optional<OfflineLoadProfile> offlineLoadProfile = allOfflineLoadProfiles.stream().filter(olp -> olp.getObisCode().equals(LOAD_PROFILES_TO_READ)).findAny();
        if (offlineLoadProfile.isPresent()) {
            log("Reading load profile " + LOAD_PROFILES_TO_READ.toString());
            List<ChannelInfo> channelInfos = getDeviceChannelInfo(offlineLoadProfile.get());
            LoadProfileIdentifierByObisCodeAndDevice loadProfileIdentifier = new LoadProfileIdentifierByObisCodeAndDevice(LOAD_PROFILES_TO_READ, deviceIdentifier);
            CollectedLoadProfile collectedLoadProfile = getCollectedDataFactory().createCollectedLoadProfile(loadProfileIdentifier);
            List<IntervalData> collectedIntervalData = readCollectedIntervalData(offlineLoadProfile.get(), compactFrame, offset);
            collectedLoadProfile.setCollectedIntervalData(collectedIntervalData, channelInfos);
            collectedLoadProfile.setDoStoreOlderValues(true);
            getCollectedLoadProfiles().add(collectedLoadProfile);
        }
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
            createCollectedIntervalData(collectedIntervalData, unixTime, currentIndexOfConvertedVolume, currentIndexOfConvertedVolumeUnderAlarm);
            offset += 14;
        }
        return collectedIntervalData;
    }

    private void createCollectedIntervalData(List<IntervalData> collectedIntervalData, Unsigned32 unixTime, Unsigned32 currentIndexOfConvertedVolume, Unsigned32 currentIndexOfConvertedVolumeUnderAlarm) {
        List<IntervalValue> intervalValues = new ArrayList<>();
        intervalValues.add(new IntervalValue(currentIndexOfConvertedVolume.intValue(), 0, getEiServerStatus(0)));
        intervalValues.add(new IntervalValue(currentIndexOfConvertedVolumeUnderAlarm.intValue(), 0, getEiServerStatus(0)));
        Calendar dateTime = Calendar.getInstance(getDeviceTimeZone());
        dateTime.setTimeInMillis(unixTime.longValue()*1000);
        collectedIntervalData.add(new IntervalData(dateTime.getTime(), getEiServerStatus(0), 0, 0, intervalValues));
    }

    private List<ChannelInfo> getDeviceChannelInfo(OfflineLoadProfile offlineLoadProfile) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        int id = 0;
        for (OfflineLoadProfileChannel offlineLoadProfileChannel : offlineLoadProfile.getOfflineChannels()) {
            ChannelInfo channelInfo = new ChannelInfo(id, offlineLoadProfileChannel.getObisCode().getValue(), offlineLoadProfileChannel.getUnit(), getDeviceId());
            channelInfos.add(channelInfo);
            if (isCumulative(offlineLoadProfileChannel.getObisCode())) {
                channelInfo.setCumulative();
            }
            id++;
        }
        return channelInfos;
    }

    private int getEiServerStatus(int protocolStatus) {
        int status = IntervalStateBits.OK;
        if ((protocolStatus & 0x80) == 0x80) {
            status = status | IntervalStateBits.POWERDOWN;
        }
        if ((protocolStatus & 0x20) == 0x20) {
            status = status | IntervalStateBits.BADTIME;
        }
        if ((protocolStatus & 0x04) == 0x04) {
            status = status | IntervalStateBits.CORRUPTED;
        }
        if ((protocolStatus & 0x02) == 0x02) {
            status = status | IntervalStateBits.BADTIME;
        }
        if ((protocolStatus & 0x01) == 0x01) {
            status = status | IntervalStateBits.CORRUPTED;
        }
        return status;
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