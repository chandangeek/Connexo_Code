package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.NestedIOException;
import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityContextV2EncryptionHandler;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.generalblocktransfer.GeneralBlockTransferFrame;
import com.energyict.mdc.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdw.core.DeviceOfflineFlags;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineLoadProfile;
import com.energyict.mdw.offline.OfflineLoadProfileChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocol.exceptions.InboundFrameException;
import com.energyict.protocolimpl.dlms.idis.events.PowerFailureEventLog;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.am130.events.*;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events.T210DDisconnectorControlLog;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events.T210DMBusEventLog;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events.T210DMeterAlarmParser;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events.T210DStandardEventLog;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierLikeSerialNumber;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByObisCodeAndDevice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by cisac on 7/13/2016.
 */
public class T210DEventPushNotificationParser extends DataPushNotificationParser {

    private static final boolean DEBUG = true;
    public static final String IP_ADDRESS_PROPERTY_NAME = "host";
    private static final ObisCode LOGICAL_NAME_OBIS = ObisCode.fromString("0.0.42.0.0.255");
    private static final ObisCode LOAD_PROFILE_1_OBIS = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode LOAD_PROFILE_2_OBIS = ObisCode.fromString("1.0.99.2.0.255");
    private static final ObisCode MBUS_VALUE_CHANNEL_OBIS = ObisCode.fromString("0.x.24.2.1.255");
    private static final ObisCode PUSH_ON_ALARM_OBJECT_LIST_OBIS = ObisCode.fromString("0.4.25.9.0.255");
    private static final ObisCode PUSH_ON_INTERVAL_1_OBJECT_LIST_OBIS = ObisCode.fromString("0.1.25.9.0.255");
    private static final ObisCode PUSH_ON_INTERVAL_2_OBJECT_LIST_OBIS = ObisCode.fromString("0.2.25.9.0.255");
    private static final ObisCode PUSH_ON_INTERVAL_3_OBJECT_LIST_OBIS = ObisCode.fromString("0.3.25.9.0.255");
    private static final ObisCode PUSH_ON_INSTALLATION_OBJECT_LIST_OBIS = ObisCode.fromString("0.7.25.9.0.255");
    private static final ObisCode PUSH_ON_CONNECTIVITY_OBJECT_LIST_OBIS = ObisCode.fromString("0.0.25.9.0.255");

    private static final ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode FRAUD_DETECTION_LOG = ObisCode.fromString("0.0.99.98.1.255");
    private static final ObisCode DISCONNECTOR_CONTROL_LOG = ObisCode.fromString("0.0.99.98.2.255");
    private static final ObisCode MBUS_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");    //General MBus log, describing events for all slave devices
    private static final ObisCode POWER_QUALITY_LOG = ObisCode.fromString("0.0.99.98.4.255");
    private static final ObisCode COMMUNICATION_LOG = ObisCode.fromString("0.0.99.98.5.255");
    private static final ObisCode MBUS_CONTROL_LOG_1 = ObisCode.fromString("0.1.24.5.0.255");   //Specific log for MBus slave device
    private static final ObisCode MBUS_CONTROL_LOG_2 = ObisCode.fromString("0.2.24.5.0.255");   //Specific log for MBus slave device
    private static final ObisCode MBUS_CONTROL_LOG_3 = ObisCode.fromString("0.3.24.5.0.255");   //Specific log for MBus slave device
    private static final ObisCode MBUS_CONTROL_LOG_4 = ObisCode.fromString("0.4.24.5.0.255");   //Specific log for MBus slave device
    private static final ObisCode POWER_FAILURE_EVENT_LOG = ObisCode.fromString("1.0.99.97.0.255");

    List<T210DPushObjectListEntry> pushObjectList;
    int dataObjectsOffset = 0;
    private CollectedDeviceInfo collectedDeviceIpAddress;
    private List<CollectedLoadProfile> collectedLoadProfileList;
    private ObisCode pushObjectListObisCode;
    private int blockNumber;
    private int acknowledgedBlockNumber;
    private byte[] responseData;
    private byte[] storedBlockDataFromPreviousResponse;
    private List<CollectedLogBook> collectedLogBooks = new ArrayList<>();

    public T210DEventPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        super(comChannel, context);
    }

    public void parseInboundFrame() {
        ByteBuffer inboundFrame = readInboundFrame();
        parseInboundFrame(inboundFrame, true);
    }

    private void parseInboundFrame(ByteBuffer inboundFrame, boolean hasHeader){
        log("Received Notification = " + ProtocolTools.getHexStringFromBytes(inboundFrame.array()));
        if(hasHeader) {
            byte[] header = new byte[8];
            inboundFrame.get(header);
            log("Received header = " + ProtocolTools.getHexStringFromBytes(header));
        }
        byte tag = inboundFrame.get();
        if (tag == getCosemNotificationAPDUTag()) {
            parseAPDU(inboundFrame);
        } else if (tag == DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING) {
            parseEncryptedFrame(inboundFrame);
        } else if(tag == DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER) {
            try {
                byte[] gbt = new byte[inboundFrame.remaining()];
                inboundFrame.get(gbt);
                log("Received GBT = " + ProtocolTools.getHexStringFromBytes(gbt));
                byte[] gbtFrame = ProtocolTools.concatByteArrays(new byte[]{tag}, gbt);
                parseInboundFrame(ByteBuffer.wrap(doHandleGeneralBlockTransfer(gbtFrame)), false);
            } catch (IOException e) {
                throw DataParseException.ioException(new ProtocolException("Parsing received push event notification using General Block Transfer failed."));
            }

        } else {
            //TODO support general ciphering & general signing (suite 0, 1 and 2)
            throw DataParseException.ioException(new ProtocolException("Unexpected tag '" + tag + "' in received push event notification. Expected '" + getCosemNotificationAPDUTag() + "' or '" + DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING + "'"));
        }
    }

    @Override
    protected void parseEncryptedFrame(ByteBuffer inboundFrame) {
        parseGeneralGlobalFrame(inboundFrame);
    }

    private void parseGeneralGlobalFrame(ByteBuffer inboundFrame) {

        ByteBuffer decryptedFrame = parseHeaderAndDecrypt(inboundFrame);

        byte plainTag = decryptedFrame.get();
        if (plainTag != getCosemNotificationAPDUTag()) {
            throw DataParseException.ioException(new ProtocolException("Unexpected tag after decrypting an incoming event push notification: " + plainTag + ", expected " + getCosemNotificationAPDUTag()));
        }
        parseAPDU(decryptedFrame);
    }

    private ByteBuffer parseHeaderAndDecrypt(ByteBuffer inboundFrame) {
        byte[] systemTitle = initializeDeviceIdentifier(inboundFrame.asReadOnlyBuffer());
        SecurityContext securityContext = getSecurityContext();
        securityContext.setResponseSystemTitle(systemTitle);

        byte[] remaining = new byte[inboundFrame.remaining()];
        inboundFrame.get(remaining);
        byte[] generalGlobalResponse = ProtocolTools.concatByteArrays(new byte[]{DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING}, remaining);

        ByteBuffer decryptedFrame = null;
        try {
            decryptedFrame = ByteBuffer.wrap(SecurityContextV2EncryptionHandler.dataTransportGeneralGloOrDedDecryption(securityContext, generalGlobalResponse));
        } catch (DLMSConnectionException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
        }
        return decryptedFrame;
    }

    @Override
    protected void parseAPDU(ByteBuffer inboundFrame) {
        pushObjectList = new ArrayList<>();

        // 1. long-invoke-id-and-priority
        byte[] invokeIdAndPriority = new byte[4];   // 32-bits long format used
        inboundFrame.get(invokeIdAndPriority);

        //2. date-time (skip date time length and value)
        int dateTimeAxdrLength = DLMSUtils.getAXDRLength(inboundFrame.array(), inboundFrame.position());
        inboundFrame.get(new byte[DLMSUtils.getAXDRLengthOffset(dateTimeAxdrLength)]); // Increment ByteBuffer position
        byte[] octetString = new byte[dateTimeAxdrLength];
        inboundFrame.get(octetString);

        //3. notification-body
        Structure structure;
        try {
            structure = AXDRDecoder.decode(inboundFrame.array(), inboundFrame.position(), Structure.class);
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }

        parsePushObjectList(structure.getNextDataType());

        if(pushObjectList.get(1).getObisCode().equals(LOGICAL_NAME_OBIS)) {
            parseLogicalDeviceName(structure.getNextDataType());
        }

        //TODO: make this parsing generic. That would be usefull if the push objects will change over time and not remain fixed
        if(pushObjectListObisCode.equals(PUSH_ON_INSTALLATION_OBJECT_LIST_OBIS)) {
            //the structure will contain 7 elements in case of a Push on Installation Data Notification
            parsePushOnInstallatioEvent(structure);
        } else if(pushObjectListObisCode.equals(PUSH_ON_CONNECTIVITY_OBJECT_LIST_OBIS)) {
            parsePushOnConnectivityEvent(structure);
        } else if(pushObjectListObisCode.equals(PUSH_ON_INTERVAL_1_OBJECT_LIST_OBIS)) {
            parsePushOnInterval1(structure);
        } else if(pushObjectListObisCode.equals(PUSH_ON_INTERVAL_2_OBJECT_LIST_OBIS)) {
            parsePushOnInterval2(structure);
        } else if(pushObjectListObisCode.equals(PUSH_ON_INTERVAL_3_OBJECT_LIST_OBIS)) {
            parsePushOnInterval3(structure);
        } else if(pushObjectListObisCode.equals(PUSH_ON_ALARM_OBJECT_LIST_OBIS)) {
            //the structure will contain 6 elements in case of a Push on Alarm Data Notification
            parsePushOnAlarm(structure);
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected the event-payload to be a structure with 3, or 7 elements, but received a structure with " + structure.nrOfDataTypes() + " element(s)"));
        }

    }

    private void incrementElementOffset() {
        dataObjectsOffset++;
    }

    private int getElementOffset() {
        return dataObjectsOffset;
    }

    private void parsePushOnAlarm(Structure structure) {
        Date eventDate = parseClockTime(structure.getNextDataType());
        parseAlarmRegister(structure.getNextDataType(), eventDate, 1);
        parseAlarmRegister(structure.getNextDataType(), eventDate, 2);
        parseAlarmRegister(structure.getNextDataType(), eventDate, 3);
        if(getCollectedLogBook().getCollectedMeterEvents().size() > 0){
            getCollectedLogBooks().add(getCollectedLogBook());
        }
    }

    private void parsePushOnInterval3(Structure structure) {
        DeviceOfflineFlags offlineContext = new DeviceOfflineFlags(DeviceOfflineFlags.ALL_LOAD_PROFILES_FLAG);
        OfflineDevice offlineDevice = inboundDAO.goOfflineDevice(deviceIdentifier, offlineContext);
        List<OfflineLoadProfile> allOfflineLoadProfiles = offlineDevice.getAllOfflineLoadProfiles();
        for(int i = dataObjectsOffset; i <= pushObjectList.size() - 1; i++){
            ObisCode obisCode = pushObjectList.get(i).getObisCode();
            getColectedLoadProfile(structure.getNextDataType(), obisCode, getOfflineLoadProfile(allOfflineLoadProfiles, obisCode), false);
        }
    }

    private void parsePushOnInterval2(Structure structure) {
        for(int i = 2; i <= pushObjectList.size() - 1; i++){
            ObisCode obisCode = pushObjectList.get(i).getObisCode();
            DataContainer dataContainer = new DataContainer();
            dataContainer.parseObjectList(structure.getNextDataType().getBEREncodedByteArray(), Logger.getLogger(this.getClass().getName()));
            List<MeterProtocolEvent> meterProtocolEventList = parseEvents(dataContainer, obisCode);
            log("Received "+meterProtocolEventList.size()+" events for logbook with obiscode: "+obisCode);
            if(meterProtocolEventList.size() > 0){
                CollectedLogBook collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, obisCode));
                collectedLogBook.addCollectedMeterEvents(meterProtocolEventList);
                getCollectedLogBooks().add(collectedLogBook);
            }
        }
    }

    private void parsePushOnInterval1(Structure structure) {
        DeviceOfflineFlags offlineContext = new DeviceOfflineFlags(DeviceOfflineFlags.ALL_LOAD_PROFILES_FLAG);
        OfflineDevice offlineDevice = inboundDAO.goOfflineDevice(deviceIdentifier, offlineContext);
        List<OfflineLoadProfile> allOfflineLoadProfiles = offlineDevice.getAllOfflineLoadProfiles();
        for(int i = dataObjectsOffset; i <= pushObjectList.size() - 1; i++){
            ObisCode obisCode = pushObjectList.get(i).getObisCode();
            if(obisCode.equals(LOAD_PROFILE_1_OBIS) || obisCode.equals(LOAD_PROFILE_2_OBIS)){
                getColectedLoadProfile(structure.getNextDataType(), obisCode, getOfflineLoadProfile(allOfflineLoadProfiles, obisCode), true);
            } else if (obisCode.equalsIgnoreBChannel(MBUS_VALUE_CHANNEL_OBIS)){
                getCollectedMbusChannelValue(structure.getNextDataType(), obisCode);
            }
        }
    }

    private OfflineLoadProfile getOfflineLoadProfile(List<OfflineLoadProfile> allOfflineLoadProfiles, ObisCode obisCode){
        for(OfflineLoadProfile loadProfile: allOfflineLoadProfiles){
            if (loadProfile.getObisCode().equals(obisCode)){
                return loadProfile;
            }
        }
        return null;
    }

    private void parsePushOnInstallatioEvent(Structure structure) {
        if(structure.nrOfDataTypes() == 7){
            parseEquipementType(structure.getNextDataType());
        }
        parseMobileNetworkIMSI(structure.getNextDataType());
        parseMobileNetworkMSISDN(structure.getNextDataType());
        parseIPAddress(structure.getNextDataType());
        parseClockTime(structure.getNextDataType());
    }

    private void parsePushOnConnectivityEvent(Structure structure) {
        parseMobileNetworkIMSI(structure.getNextDataType());
        parseMobileNetworkMSISDN(structure.getNextDataType());
        parseIPAddress(structure.getNextDataType());
        parsePortNumber(structure.getNextDataType());
    }

    private void parseAlarmRegister(AbstractDataType nextDataType, Date eventDate, int alarmRegister) {
        long alarmDescriptor = nextDataType.getUnsigned32().getValue();
        log("AlarmDescriptor = " + alarmDescriptor);
        addMeterEventsToCollectedLogBook(T210DMeterAlarmParser.parseAlarmCode(eventDate, alarmDescriptor, alarmRegister));
    }

    protected void addMeterEventsToCollectedLogBook(List<MeterEvent> meterEvents) {
        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
        for(MeterEvent meterEvent: meterEvents){
            meterProtocolEvents.add(MeterEvent.mapMeterEventToMeterProtocolEvent(meterEvent));
        }
        getCollectedLogBook().addCollectedMeterEvents(meterProtocolEvents);
    }

    private void parsePushObjectList(AbstractDataType dataType) {
        incrementElementOffset();
        if (!(dataType instanceof Array)) {
            throw DataParseException.ioException(new ProtocolException("The first element of the Data-notification body should be the of type Array"));
        }
        boolean isPushObjectListStructure = true;
        for(AbstractDataType abstractDataType: dataType.getArray().getAllDataTypes()){
            if(!(abstractDataType instanceof Structure)){
                throw DataParseException.ioException(new ProtocolException("All elements from the push_objects_list Array should be of type Structure"));
            }
            Structure struct = abstractDataType.getStructure();
            long classId = struct.getDataType(0).longValue();
            ObisCode obisCode = ObisCode.fromByteArray(struct.getDataType(1).getOctetString().getOctetStr());
            if(isPushObjectListStructure){
                pushObjectListObisCode = obisCode;
                isPushObjectListStructure = false;
            }
            int attributeNr = struct.getDataType(2).intValue();
            long maxBlockSize = struct.getDataType(3).longValue();
            pushObjectList.add(new T210DPushObjectListEntry(classId, obisCode, attributeNr, maxBlockSize));
            log("classID = " + classId + " obisCode = " + obisCode.toString() + " attributeNr = " + attributeNr + " maxBlockSize = " + maxBlockSize);
        }
    }

    private void parseLogicalDeviceName(AbstractDataType dataType) {
        incrementElementOffset();
        if (!(dataType instanceof OctetString)) {
            throw DataParseException.ioException(new ProtocolException("Element "+getElementOffset()+" of the Data-notification body should be the of type OctetString"));
        }
        String logicalDeviceName = dataType.getOctetString().stringValue();
        deviceIdentifier = getDeviceIdentifierBasedOnLogicalDeviceName(logicalDeviceName);
        log("logicalDeviceName = " + logicalDeviceName);
    }

    private void parseEquipementType(AbstractDataType dataType) {
        incrementElementOffset();
        if (!(dataType instanceof TypeEnum)) {
            throw DataParseException.ioException(new ProtocolException("Element "+getElementOffset()+" of the Data-notification body should be the of type TypeEnum"));
        }
        int equipementType = dataType.getTypeEnum().getValue();
        log("equipementType = " + equipementType);
    }

    private void parseMobileNetworkIMSI(AbstractDataType dataType) {
        incrementElementOffset();
        if (!(dataType instanceof OctetString)) {
            throw DataParseException.ioException(new ProtocolException("Element "+getElementOffset()+" of the Data-notification body should be the of type OctetString"));
        }
        String mobileNetworkIdentifierIMSI = dataType.getOctetString().stringValue();
        log("mobileNetworkIdentifierIMSI = " + mobileNetworkIdentifierIMSI);
    }

    private void parseMobileNetworkMSISDN(AbstractDataType dataType) {
        incrementElementOffset();
        if (!(dataType instanceof OctetString)) {
            throw DataParseException.ioException(new ProtocolException("Element "+getElementOffset()+" of the Data-notification body should be the of type OctetString"));
        }
        String mobileNetworkIdentifierMSISDN = dataType.getOctetString().stringValue();
        log("mobileNetworkIdentifierMSISDN = " + mobileNetworkIdentifierMSISDN);
    }

    private void parseIPAddress(AbstractDataType dataType) {
        incrementElementOffset();
        if (!(dataType instanceof Unsigned32)) {
            throw DataParseException.ioException(new ProtocolException("Element "+getElementOffset()+" of the Data-notification body should be the of type Unsigned32"));
        }
        String ipAddress = getIpAddress(dataType.getUnsigned32().longValue());
        createCollectedDeviceIpAddres(ipAddress);
        log("ipAddress = " + ipAddress);
    }

    private Date parseClockTime(AbstractDataType dataType) {
        incrementElementOffset();
        if (!(dataType instanceof OctetString)) {
            throw DataParseException.ioException(new ProtocolException("Element "+getElementOffset()+" of the Data-notification body should be of type OctetString"));
        }
        Date clockTime = parseDateTime(dataType.getOctetString());
        log("clockTime = " + clockTime.toString());
        return clockTime;
    }

    private void parsePortNumber(AbstractDataType dataType) {
        incrementElementOffset();
        if (!(dataType instanceof Unsigned16)) {
            throw DataParseException.ioException(new ProtocolException("Element "+getElementOffset()+" of the Data-notification body should be of type OctetString"));
        }
        Unsigned16 portNumber = dataType.getUnsigned16();
        log("portNumber = " + portNumber.getValue());
    }

    private void getCollectedMbusChannelValue(AbstractDataType dataType, ObisCode obisCode) {
        int channel = obisCode.getB();
        if (!(dataType instanceof Unsigned32)) {
            throw DataParseException.ioException(new ProtocolException("MBus Channel "+channel+" value 1 should be of type Unsigned32"));
        }
        Unsigned32 mbusValueChannel = dataType.getUnsigned32();
        log("MBus Channel " + channel + " value 1 = " + mbusValueChannel.getValue());
        Date dateTime = Calendar.getInstance().getTime();
        addCollectedRegister(obisCode, mbusValueChannel.longValue(), null, dateTime, null);
        //TODO: see if we should store Mbus Channel values
    }

    private void getColectedLoadProfile(AbstractDataType dataType, ObisCode loadProfileObisCode, OfflineLoadProfile offlineLoadProfile, boolean hasStatusInformation) {
        if (!(dataType instanceof Array)) {
            throw DataParseException.ioException(new ProtocolException("The third element of the Data-notification body should be the of type Array"));
        }

        CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierByObisCodeAndDevice(loadProfileObisCode, deviceIdentifier));
        List<ChannelInfo> channelInfos = getDeviceChannelInfo(loadProfileObisCode, offlineLoadProfile);
        List<IntervalData> collectedIntervalData;
        log("reading load profile " + loadProfileObisCode.toString());
        if(offlineLoadProfile != null){
            collectedIntervalData = getCollectedIntervalDataUserDefinedChannels((Array) dataType, offlineLoadProfile, hasStatusInformation);
        } else {
            collectedIntervalData = getCollectedIntervalDataForHardCodedChannels((Array) dataType, loadProfileObisCode);
        }
        collectedLoadProfile.setCollectedIntervalData(collectedIntervalData, channelInfos);
        collectedLoadProfile.setDoStoreOlderValues(true);
        getCollectedLoadProfile().add(collectedLoadProfile);
    }

    private List<IntervalData> getCollectedIntervalDataUserDefinedChannels(Array dataType, OfflineLoadProfile offlineLoadProfile, boolean hasStatusInformation) {
        List<IntervalData> collectedIntervalData = new ArrayList<>();
        List<OfflineLoadProfileChannel> offlineChannels = offlineLoadProfile.getOfflineChannels();
        int channelOffset = hasStatusInformation ? 2 : 1;
        for(AbstractDataType structure: dataType.getAllDataTypes()){
            Structure struct = structure.getStructure();
            if(offlineChannels.size() > struct.nrOfDataTypes() - channelOffset){
                throw DataParseException.ioException(new ProtocolException("Configuration mismatch: The number of channels configured in the device is not the same as the number of channels configured in HES"));
            }
            List<IntervalValue> intervalValues = new ArrayList<>();
            Date clockTime = parseDateTime(struct.getDataType(0).getOctetString());
            int status = hasStatusInformation ? struct.getDataType(1).getUnsigned8().intValue() : 0;
            int eiStatus = getEiServerStatus(status);
            for(int i = channelOffset; i < struct.nrOfDataTypes(); i++){
                intervalValues.add(new IntervalValue(struct.getDataType(i).getUnsigned32().intValue(), status, eiStatus));
                log("Channel " + (i - 1) + "reading: clockTime = " + clockTime + " status = " + status + " eiStatus = " + eiStatus + " intervalValue = " + struct.getDataType(i).getUnsigned32().intValue());
            }
            collectedIntervalData.add(new IntervalData(clockTime, eiStatus, status, 0, intervalValues));
        }

        return collectedIntervalData;
    }

    private List<IntervalData> getCollectedIntervalDataForHardCodedChannels(Array dataType, ObisCode loadProfileObisCode) {
        List<IntervalData> collectedIntervalData = new ArrayList<>();
        for(AbstractDataType structure: dataType.getAllDataTypes()){
            Structure struct = structure.getStructure();
            Date clockTime = parseDateTime(struct.getDataType(0).getOctetString());
            int status = struct.getDataType(1).getUnsigned8().intValue();
            int activeEnergyImport = struct.getDataType(2).getUnsigned32().intValue();
            int activeEnergyExport = struct.getDataType(3).getUnsigned32().intValue();
            if(loadProfileObisCode.equals(LOAD_PROFILE_2_OBIS)){
                int reactiveEnergyImport = struct.getDataType(4).getUnsigned32().intValue();
                int reactiveEnergyExport = struct.getDataType(5).getUnsigned32().intValue();
                collectedIntervalData.add(getIntervalData(clockTime, status, activeEnergyImport, activeEnergyExport, reactiveEnergyImport, reactiveEnergyExport));
                log("clockTime = " + clockTime + " status = " + status + " activeEnergyImport = " + activeEnergyImport + " activeEnergyExport = " + activeEnergyExport + " reactiveEnergyImport = " + reactiveEnergyImport + " reactiveEnergyExport = " + reactiveEnergyExport);
            } else {
                collectedIntervalData.add(getIntervalData(clockTime, status, activeEnergyImport, activeEnergyExport));
                log("clockTime = " + clockTime + " status = " + status + " activeEnergyImport = " + activeEnergyImport + " activeEnergyExport = " + activeEnergyExport);
            }

        }
        return collectedIntervalData;
    }

    /**
     * NOTE: CapturedObjects must be added as parameters in the exact order as they are configured in the LoadProfile
     * @param date
     * @param status
     * @param capturedObjects
     * @return
     */
    private IntervalData getIntervalData(Date date, int status, int ... capturedObjects) {
        List<IntervalValue> intervalValues = new ArrayList<>();
        int eiStatus = getEiServerStatus(status);
        for(int value: capturedObjects){
            intervalValues.add(new IntervalValue(value, status, eiStatus));
        }
        return new IntervalData(date, eiStatus, status, 0, intervalValues);
    }

    private List<ChannelInfo> getDeviceChannelInfo(ObisCode loadProfileObisCode, OfflineLoadProfile offlineLoadProfile) {
        if(offlineLoadProfile != null){
            return getDeviceChannelInfoFromUserDefinedChannels(offlineLoadProfile.getOfflineChannels());
        }

        log("Hardcoded channels are used for load profile with obiscode: " + loadProfileObisCode);
        // if we did not found an offlineLoadProfile with loadProfileObisCode for our device then use the default hardcoded values
        if(loadProfileObisCode.equals(LOAD_PROFILE_2_OBIS)){
            return getDeviceChannelInfoLP2();
        }
        return getDeviceChannelInfoLP1();
    }

    private List<ChannelInfo> getDeviceChannelInfoFromUserDefinedChannels(List<OfflineLoadProfileChannel> offlineChannels) {
        List<ChannelInfo> channelInfos = new ArrayList<>();

        int id = 0;
        for(OfflineLoadProfileChannel offlineLoadProfileChannel: offlineChannels){
            ChannelInfo channelInfo = new ChannelInfo(id, offlineLoadProfileChannel.getObisCode().getValue(), offlineLoadProfileChannel.getUnit(), deviceIdentifier.getIdentifier());
            channelInfos.add(channelInfo);
            if (isCumulative(offlineLoadProfileChannel.getObisCode())) {
                channelInfo.setCumulative();
            }
            id++;
        }

        return channelInfos;
    }

    private boolean isCumulative(ObisCode obisCode) {
        return ParseUtils.isObisCodeCumulative(obisCode);
    }

    private List<ChannelInfo> getDeviceChannelInfoLP1() { //This is hardcoded list as we do not receive the captured objects in the notification
        int scale = 1;
        List<ChannelInfo> channelInfos = new ArrayList<>();

        ChannelInfo ci1 = new ChannelInfo(0, "1.0.1.8.0.255", Unit.get(BaseUnit.UNITLESS, scale), deviceIdentifier.getIdentifier());
        ChannelInfo ci2 = new ChannelInfo(1, "1.0.2.8.0.255", Unit.get(BaseUnit.UNITLESS, scale), deviceIdentifier.getIdentifier());
        ci1.setCumulative();
        ci2.setCumulative();
        channelInfos.add(ci1);
        channelInfos.add(ci2);

        return channelInfos;
    }

    private List<ChannelInfo> getDeviceChannelInfoLP2() { //This is hardcoded list as we do not receive the captured objects in the notification
        int scale = 1;
        List<ChannelInfo> channelInfos = new ArrayList<>();

        ChannelInfo ci1 = new ChannelInfo(0, "1.0.1.8.0.255", Unit.get(BaseUnit.WATTHOUR, scale), deviceIdentifier.getIdentifier());
        ChannelInfo ci2 = new ChannelInfo(1, "1.0.2.8.0.255", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scale), deviceIdentifier.getIdentifier());
        ChannelInfo ci3 = new ChannelInfo(2, "1.0.3.8.0.255", Unit.get(BaseUnit.UNITLESS, scale), deviceIdentifier.getIdentifier());
        ChannelInfo ci4 = new ChannelInfo(3, "1.0.4.8.0.255", Unit.get(BaseUnit.UNITLESS, scale), deviceIdentifier.getIdentifier());
        ci1.setCumulative();
        ci2.setCumulative();
        ci3.setCumulative();
        ci4.setCumulative();

        channelInfos.add(ci1);
        channelInfos.add(ci2);
        channelInfos.add(ci3);
        channelInfos.add(ci4);

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

    private List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone(); //TODO: use proper timezone
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(POWER_QUALITY_LOG)) {
            meterEvents = new AM130PowerQualityEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(POWER_FAILURE_EVENT_LOG)) {
            meterEvents = new PowerFailureEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(DISCONNECTOR_CONTROL_LOG)) {
            meterEvents = new T210DDisconnectorControlLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(FRAUD_DETECTION_LOG)) {
            meterEvents = new AM130FraudDetectionLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterEvents = new T210DStandardEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(COMMUNICATION_LOG)) {
            meterEvents = new AM130CommunicationLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(MBUS_EVENT_LOG)) {
            meterEvents = new T210DMBusEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(MBUS_CONTROL_LOG_1)) {
            meterEvents = new AM130MBusControlLog1(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(MBUS_CONTROL_LOG_2)) {
            meterEvents = new AM130MBusControlLog2(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(MBUS_CONTROL_LOG_3)) {
            meterEvents = new AM130MBusControlLog3(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(MBUS_CONTROL_LOG_4)) {
            meterEvents = new AM130MBusControlLog4(timeZone, dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);

    }


    /**
     * It will initialize the deviceIdentifier object based on the system title then return the system title
     * @param inboundFrame
     * @return the system title
     */
    private byte[] initializeDeviceIdentifier(ByteBuffer inboundFrame) {
        int systemTitleLength = inboundFrame.get() & 0xFF;
        byte[] systemTitle = new byte[systemTitleLength];
        inboundFrame.get(systemTitle);
        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(systemTitle);
        return systemTitle;
    }

    /**
     System title value is the same as LogicalDeviceName.
     To get the serial number out of it the last 28 bits (big-endian encoding) must be converted to Integer

     * @param systemTitle the system title encoded to fit in 8 bytes
     * @return
     */
    @Override
    protected DeviceIdentifier getDeviceIdentifierBasedOnSystemTitle(byte[] systemTitle) {
        String serverSystemTitle = ProtocolTools.getHexStringFromBytes(systemTitle, "");
        String factorySequenceNumber = serverSystemTitle.substring(9, serverSystemTitle.length());
        return new DeviceIdentifierLikeSerialNumber("%" + Integer.parseInt(factorySequenceNumber, 16) + "%");
    }

    /**
     To get the serial number out of it the last 28 bits (big-endian encoding) must be converted to Integer
     * @return
     */
    protected DeviceIdentifier getDeviceIdentifierBasedOnLogicalDeviceName(String logicalDeviceName) {
        return new DeviceIdentifierLikeSerialNumber("%" + logicalDeviceName.substring(8, logicalDeviceName.length()) + "%");
    }

    /**
     * Converts a long to an IP value format
     * @param ip in long value format
     * @return the converted ip
     */
    public String getIpAddress(long ip) {
        return ((ip >> 24) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "."
                + (ip & 0xFF);
    }

    public enum EquipementType {
        PLC_E_Meter("PLC E-Meter"),
        P2P_E_Meter("P2P E-Meter");

        private String value;

        EquipementType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public CollectedLogBook getCollectedLogBook() {
        if(this.collectedLogBook == null) {
            this.collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, logbookObisCode));
        }
        return this.collectedLogBook;
    }

    public List<CollectedLogBook> getCollectedLogBooks(){
        return collectedLogBooks;
    }

    public List<CollectedLoadProfile> getCollectedLoadProfile() {
        if(this.collectedLoadProfileList == null) {
            this.collectedLoadProfileList = new ArrayList<>();
        }
        return this.collectedLoadProfileList;
    }

    public CollectedDeviceInfo getCollectedDeviceIpAddres() {
        return this.collectedDeviceIpAddress;
    }

    private void createCollectedDeviceIpAddres(String deviceIpAddress) {
        this.collectedDeviceIpAddress = MdcManager.getCollectedDataFactory().createDeviceIpAddress(this.deviceIdentifier, deviceIpAddress, IP_ADDRESS_PROPERTY_NAME);
    }

    public byte[] doHandleGeneralBlockTransfer(byte[] rawData) throws IOException {
        GeneralBlockTransferFrame responseFrame = new GeneralBlockTransferFrame();

        // Parse the first general block transfer frame
        log("Received GBT = " + ProtocolTools.getHexStringFromBytes(rawData, ""));
        int offset = responseFrame.parseFrame(rawData);
        setStoredBlockDataFromPreviousResponse(ProtocolTools.getSubArray(rawData, offset));
        setBlockNumber(responseFrame.getAcknowledgedBlockNumber());
        setAcknowledgedBlockNumber(responseFrame.getBlockNumber());
        log("getBlockNumber = " + responseFrame.getBlockNumber());
        log("getAcknowledgedBlockNumber = " + responseFrame.getAcknowledgedBlockNumber());
        log("getLengthOfBlockData = " + responseFrame.getLengthOfBlockData());
        log("getBlockData = " + ProtocolTools.getHexStringFromBytes(responseFrame.getBlockData(), ""));
        log("BlockControl = " + ProtocolTools.getHexStringFromBytes(responseFrame.getBlockControl().getBytes(), ""));
        log("BlockControl.windowSize = " + responseFrame.getBlockControl().getWindowSize());
        log("BlockControl.isLastBlock = " + responseFrame.getBlockControl().isLastBlock());
        log("BlockControl.isStreamingMode = " + responseFrame.getBlockControl().isStreamingMode());

        setResponseData(responseFrame.getBlockData());

        int timeout = 300000;
        long timeoutMoment = System.currentTimeMillis() + timeout;

        boolean isLastBlock = responseFrame.getBlockControl().isLastBlock();
        while (!isLastBlock) {
            // Receive the next 'windowSize' number of blocks
            int availableBytes = getComChannel().available();
            if(availableBytes > 0 || getStoredBlockDataFromPreviousResponse().length > 0){
                byte[] buffer = new byte[0];
                if(availableBytes > 0) {
                    buffer = new byte[getComChannel().available()];
                    getComChannel().read(buffer);
                    log("Received next TCP data = " + ProtocolTools.getHexStringFromBytes(buffer, ""));
                }

                GeneralBlockTransferFrame generalBlockTransferFrame = new GeneralBlockTransferFrame();
                byte[] blockData = ProtocolTools.concatByteArrays(getStoredBlockDataFromPreviousResponse(), buffer);

                try{
                    int offset1 = generalBlockTransferFrame.parseFrame(blockData, 8);

                    if (generalBlockTransferFrame.getLengthOfBlockData() == 0) {
                        throw new ProtocolException("GeneralBlockTransferHandler, receiveNextBlocksFromDevice - Fetch of block " + generalBlockTransferFrame.getBlockNumber() + " failed, the block content was empty.");
                    }

                    setStoredBlockDataFromPreviousResponse(ProtocolTools.getSubArray(blockData, offset1));
                    setAcknowledgedBlockNumber(generalBlockTransferFrame.getBlockNumber());
                    setBlockNumber(generalBlockTransferFrame.getAcknowledgedBlockNumber());
                    isLastBlock = generalBlockTransferFrame.getBlockControl().isLastBlock();
                    // Add decrypted block data from the responseData byte array and update the block numbers
                    log("generalBlockTransferFrame - getBlockNumber = " + generalBlockTransferFrame.getBlockNumber());
                    log("generalBlockTransferFrame - getAcknowledgedBlockNumber = " + generalBlockTransferFrame.getAcknowledgedBlockNumber());
                    log("generalBlockTransferFrame - getLengthOfBlockData = " + generalBlockTransferFrame.getLengthOfBlockData());
                    log("generalBlockTransferFrame - getBlockData = " + ProtocolTools.getHexStringFromBytes(generalBlockTransferFrame.getBlockData(), ""));
                    log("generalBlockTransferFrame - BlockControl = " + ProtocolTools.getHexStringFromBytes(generalBlockTransferFrame.getBlockControl().getBytes(), ""), Level.FINE);
                    log("generalBlockTransferFrame - BlockControl.windowSize = " + generalBlockTransferFrame.getBlockControl().getWindowSize());
                    log("generalBlockTransferFrame - BlockControl.isLastBlock = " + generalBlockTransferFrame.getBlockControl().isLastBlock());
                    log("generalBlockTransferFrame - BlockControl.isStreamingMode = " + generalBlockTransferFrame.getBlockControl().isStreamingMode());
                    addNextBlockDataToResponseData(generalBlockTransferFrame.getBlockData());
//                    firstPartOfGBTBlockData = null; //reset the firstPartOfGBTBlockData
                } catch (Exception e) {
                    log("Received incomplete GBT block! The missing content may come in a next TCP data push. Waiting for it!");
                    e.printStackTrace();
                }
            }

            delay(1000);
            // do not wait more than timeout for the last block
            if (System.currentTimeMillis() - timeoutMoment > 0) {
                if (getResponseData() == null || getResponseData().length == 0) {
                    throw InboundFrameException.timeoutException(String.format("Timeout: didn't receive an inbound frame after %d ms.", timeout));
                } else {
                    throw InboundFrameException.timeoutException(String.format("Timeout: didn't receive the next block after %d ms.", timeout));
                }
            }
        }
        // Flush the storedBlockDataFromPreviousResponse byte array
        flushStoredBlockDataFromPreviousResponse();

        return getResponseData();
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }

    protected void addNextBlockDataToResponseData(byte[] nextBlockData) {
        setResponseData(ProtocolTools.concatByteArrays(getResponseData(), nextBlockData));
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public void setAcknowledgedBlockNumber(int acknowledgedBlockNumber) {
        this.acknowledgedBlockNumber = acknowledgedBlockNumber;
    }

    public void setResponseData(byte[] responseData) {
        this.responseData = responseData;
    }

    /**
     * Getter for the block data that was kept from previous response packet *
     */
    private byte[] getStoredBlockDataFromPreviousResponse() {
        return storedBlockDataFromPreviousResponse != null ? storedBlockDataFromPreviousResponse : new byte[0];
    }

    private void setStoredBlockDataFromPreviousResponse(byte[] storedBlockDataFromPreviousResponse) {
        this.storedBlockDataFromPreviousResponse = storedBlockDataFromPreviousResponse;
    }

    protected void flushStoredBlockDataFromPreviousResponse() {
        this.storedBlockDataFromPreviousResponse = new byte[0];
    }

    public byte[] getResponseData() {
        return responseData;
    }

    protected void log(String message) {
        log(message, Level.FINE);
        System.out.println(message);
    }

}
