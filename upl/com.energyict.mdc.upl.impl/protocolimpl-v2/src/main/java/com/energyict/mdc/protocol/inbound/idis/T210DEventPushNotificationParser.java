package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfileChannel;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityContextV2EncryptionHandler;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.generalblocktransfer.GeneralBlockTransferFrame;
import com.energyict.mdw.core.DeviceOfflineFlags;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocol.exceptions.InboundFrameException;
import com.energyict.protocolimpl.dlms.idis.events.PowerFailureEventLog;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130CommunicationLog;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130FraudDetectionLog;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130MBusControlLog1;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130MBusControlLog2;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130MBusControlLog3;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130MBusControlLog4;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130PowerQualityEventLog;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.InboundSimulator;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events.T210DDisconnectorControlLog;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events.T210DMBusEventLog;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events.T210DMeterAlarmParser;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events.T210DStandardEventLog;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierLikeSerialNumber;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByObisCodeAndDevice;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Created by cisac on 7/13/2016.
 */
public class T210DEventPushNotificationParser extends DataPushNotificationParser {

    public static final String IP_ADDRESS_PROPERTY_NAME = "host";
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
        parseInboundFrame(inboundFrame);
    }

    private void parseInboundFrame(ByteBuffer inboundFrame){
        byte[] header = new byte[8];
        inboundFrame.get(header);
        byte tag = inboundFrame.get();
        if (tag == getCosemNotificationAPDUTag()) {
            parseAPDU(inboundFrame);
        } else if (tag == DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING) {
            parseEncryptedFrame(inboundFrame);
        } else if(tag == DLMSCOSEMGlobals.COSEM_GENERAL_BLOCK_TRANSFER) {
//            try {
////                byte[] remainingData = new byte[inboundFrame.remaining()];
////                inboundFrame.get(remainingData);
//                byte[] gbtFrame = ProtocolTools.concatByteArrays(new byte[]{tag}, inboundFrame.array());
//                parseInboundFrame(ByteBuffer.wrap(doHandleGeneralBlockTransfer(gbtFrame)));
////                parseInboundFrame(inboundFrame);
//            } catch (IOException e) {
//                throw DataParseException.ioException(new ProtocolException("Parsing received push event notification using General Block Transfer failed."));
//            }

            //the following is just a hack to be able to reuse this connection. remove it when GBT will work
            System.out.println("GBT tag received, ignore and use dummy push on alarm inbound data to continue");
            parseInboundFrame(ByteBuffer.wrap(DatatypeConverter.parseHexBinary(InboundSimulator.pushOnAlarm)));
        } else if (tag == 48){//unknown tag (yet) that we receive from device. Simulate some other data so we can reuse this connection for testing
            System.out.println("Tag 48 received, ignore and use dummy push on alarm inbound data to continue");
            parseInboundFrame(ByteBuffer.wrap(DatatypeConverter.parseHexBinary(InboundSimulator.pushOnAlarm)));
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

        int nrOfElements = structure.nrOfDataTypes();
        parsePushObjectList(structure.getNextDataType());
        parseLogicalDeviceName(structure.getNextDataType());

        if(pushObjectListObisCode.equals(PUSH_ON_INSTALLATION_OBJECT_LIST_OBIS) || pushObjectListObisCode.equals(PUSH_ON_CONNECTIVITY_OBJECT_LIST_OBIS)){
            //the structure will contain 7 elements in case of a Push on Installation Data Notification
            parsePushOnInstallatioEvent(structure);
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
            throw DataParseException.ioException(new ProtocolException("Expected the event-payload to be a structure with 3, or 7 elements, but received a structure with " + nrOfElements + " element(s)"));
        }

    }

    private void parsePushOnAlarm(Structure structure) {
        Date eventDate = parseClockTime(structure.getNextDataType());
        parseAlarmRegister(structure.getNextDataType(), eventDate, 1);
        parseAlarmRegister(structure.getNextDataType(), eventDate, 2);
        parseAlarmRegister(structure.getNextDataType(), eventDate, 3);
        if (!getCollectedLogBook().getCollectedMeterEvents().isEmpty()) {
            getCollectedLogBooks().add(getCollectedLogBook());
        }
    }

    private void parsePushOnInterval3(Structure structure) {
        parsePushOnInterval1(structure);
        //TODO: proper implementation after device will support this
    }

    private void parsePushOnInterval2(Structure structure) {
        for(int i = 2; i <= pushObjectList.size() - 1; i++){
            ObisCode obisCode = pushObjectList.get(i).getObisCode();
            DataContainer dataContainer = new DataContainer();
            dataContainer.parseObjectList(structure.getNextDataType().getBEREncodedByteArray(), Logger.getLogger(this.getClass().getName()));
            List<MeterProtocolEvent> meterProtocolEventList = parseEvents(dataContainer, obisCode);
            if (!meterProtocolEventList.isEmpty()) {
                CollectedLogBook collectedLogBook = this.getCollectedDataFactory().createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, obisCode));
                collectedLogBook.addCollectedMeterEvents(meterProtocolEventList);
                getCollectedLogBooks().add(collectedLogBook);
            }
        }
    }

    private void parsePushOnInterval1(Structure structure) {
        DeviceOfflineFlags offlineContext = new DeviceOfflineFlags(DeviceOfflineFlags.ALL_LOAD_PROFILES_FLAG);
        OfflineDevice offlineDevice = inboundDAO.getOfflineDevice(deviceIdentifier, offlineContext);
        List<OfflineLoadProfile> allOfflineLoadProfiles = offlineDevice.getAllOfflineLoadProfiles();
        for(int i = 2; i <= pushObjectList.size() - 1; i++){
            ObisCode obisCode = pushObjectList.get(i).getObisCode();
            if(obisCode.equals(LOAD_PROFILE_1_OBIS) || obisCode.equals(LOAD_PROFILE_2_OBIS)){
                getColectedLoadProfile(structure.getNextDataType(), obisCode, getOfflineLoadProfile(allOfflineLoadProfiles, obisCode));
            } else if (obisCode.equalsIgnoreBChannel(MBUS_VALUE_CHANNEL_OBIS)){
                getCollectedMbusChannelValue();
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

    private void parseAlarmRegister(AbstractDataType nextDataType, Date eventDate, int alarmRegister) {
        long alarmDescriptor = nextDataType.getUnsigned32().getValue();
        System.out.println("AlarmDescriptor = "+alarmDescriptor);
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
            System.out.println("classID = "+classId+ " obisCode = "+obisCode.toString()+ " attributeNr = "+attributeNr+ " maxBlockSize = "+maxBlockSize);
        }
    }

    private void parseLogicalDeviceName(AbstractDataType dataType) {
        if (!(dataType instanceof OctetString)) {
            throw DataParseException.ioException(new ProtocolException("The second element of the Data-notification body should be the of type OctetString"));
        }
        String logicalDeviceName = dataType.getOctetString().stringValue();
        deviceIdentifier = getDeviceIdentifierBasedOnLogicalDeviceName(logicalDeviceName);
        System.out.println("logicalDeviceName = "+ logicalDeviceName);
    }

    private void parseEquipementType(AbstractDataType dataType) {
        if (!(dataType instanceof TypeEnum)) {
            throw DataParseException.ioException(new ProtocolException("The third element of the Data-notification body should be the of type TypeEnum"));
        }
        int equipementType = dataType.getTypeEnum().getValue();
        System.out.println("equipementType = "+ EquipementType.values()[equipementType]);
    }

    private void parseMobileNetworkIMSI(AbstractDataType dataType) {
        if (!(dataType instanceof OctetString)) {
            throw DataParseException.ioException(new ProtocolException("The fourth element of the Data-notification body should be the of type OctetString"));
        }
        String mobileNetworkIdentifierIMSI = dataType.getOctetString().stringValue();
        System.out.println("mobileNetworkIdentifierIMSI = "+ mobileNetworkIdentifierIMSI);
    }

    private void parseMobileNetworkMSISDN(AbstractDataType dataType) {
        if (!(dataType instanceof OctetString)) {
            throw DataParseException.ioException(new ProtocolException("The fifth element of the Data-notification body should be the of type OctetString"));
        }
        String mobileNetworkIdentifierMSISDN = dataType.getOctetString().stringValue();
        System.out.println("mobileNetworkIdentifierMSISDN = "+ mobileNetworkIdentifierMSISDN);
    }

    private void parseIPAddress(AbstractDataType dataType) {
        if (!(dataType instanceof Unsigned32)) {
            throw DataParseException.ioException(new ProtocolException("The sixth element of the Data-notification body should be the of type Unsigned32"));
        }
        String ipAddress = getIpAddress(dataType.getUnsigned32().longValue());
        createCollectedDeviceIpAddres(ipAddress);
        System.out.println("ipAddress = "+ ipAddress);
    }

    private Date parseClockTime(AbstractDataType dataType) {
        if (!(dataType instanceof OctetString)) {
            throw DataParseException.ioException(new ProtocolException("The seventh element of the Data-notification body should be the of type OctetString"));
        }
        Date clockTime = parseDateTime(dataType.getOctetString());
        System.out.println("clockTime = "+ clockTime.toString());
        return clockTime;
    }

    private void getCollectedMbusChannelValue() {
        //TODO: implement this
    }

    private void getColectedLoadProfile(AbstractDataType dataType, ObisCode loadProfileObisCode, OfflineLoadProfile offlineLoadProfile) {
        if (!(dataType instanceof Array)) {
            throw DataParseException.ioException(new ProtocolException("The third element of the Data-notification body should be the of type Array"));
        }

        CollectedLoadProfile collectedLoadProfile = this.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierByObisCodeAndDevice(loadProfileObisCode, deviceIdentifier));
        List<ChannelInfo> channelInfos = getDeviceChannelInfo(loadProfileObisCode, offlineLoadProfile);
        List<IntervalData> collectedIntervalData;
        if(offlineLoadProfile != null){
            collectedIntervalData = getCollectedIntervalDataUserDefinedChannels((Array) dataType, offlineLoadProfile);
        } else {
            collectedIntervalData = getCollectedIntervalDataForHardCodedChannels((Array) dataType, loadProfileObisCode);
        }
        collectedLoadProfile.setCollectedIntervalData(collectedIntervalData, channelInfos);
        collectedLoadProfile.setDoStoreOlderValues(true);
        getCollectedLoadProfile().add(collectedLoadProfile);
    }

    private List<IntervalData> getCollectedIntervalDataUserDefinedChannels(Array dataType, OfflineLoadProfile offlineLoadProfile) {
        List<IntervalData> collectedIntervalData = new ArrayList<>();
        List<OfflineLoadProfileChannel> offlineChannels = offlineLoadProfile.getOfflineChannels();

        for(AbstractDataType structure: dataType.getAllDataTypes()){
            Structure struct = structure.getStructure();
            if(offlineChannels.size() > struct.nrOfDataTypes() - 2){
                throw DataParseException.ioException(new ProtocolException("Configuration mismatch: The number of channels configured in the device is not the same as the number of channels configured in HES"));
            }
            List<IntervalValue> intervalValues = new ArrayList<>();
            Date clockTime = parseDateTime(struct.getDataType(0).getOctetString());
            int status = struct.getDataType(1).getUnsigned8().intValue();
            int eiStatus = getEiServerStatus(status);
            for(int i = 2; i < offlineChannels.size() + 2; i++){
                intervalValues.add(new IntervalValue(struct.getDataType(i).getUnsigned32().intValue(), status, eiStatus));
                System.out.println("clockTime = " + clockTime + " status = " + status + " eiStatus = " + eiStatus + " intervalValue = " + struct.getDataType(i).getUnsigned32().intValue());
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
                System.out.println("clockTime = " + clockTime + " status = " + status + " activeEnergyImport = " + activeEnergyImport + " activeEnergyExport = " + activeEnergyExport + " reactiveEnergyImport = " + reactiveEnergyImport + " reactiveEnergyExport = " + reactiveEnergyExport);
            } else {
                collectedIntervalData.add(getIntervalData(clockTime, status, activeEnergyImport, activeEnergyExport));
                System.out.println("clockTime = " + clockTime + " status = " + status + " activeEnergyImport = " + activeEnergyImport + " activeEnergyExport = " + activeEnergyExport);
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

        System.out.println("Hardcoded channels are used for load profile with obiscode: "+loadProfileObisCode);
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
            ChannelInfo channelInfo = new ChannelInfo(id, offlineLoadProfileChannel.getObisCode().getValue(), offlineLoadProfileChannel.getUnit(), getDeviceId());
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

        ChannelInfo ci1 = new ChannelInfo(0, "1.0.1.8.0.255", Unit.get(BaseUnit.UNITLESS, scale), getDeviceId());
        ChannelInfo ci2 = new ChannelInfo(1, "1.0.2.8.0.255", Unit.get(BaseUnit.UNITLESS, scale), getDeviceId());
        ci1.setCumulative();
        ci2.setCumulative();
        channelInfos.add(ci1);
        channelInfos.add(ci2);

        return channelInfos;
    }

    private String getDeviceId() {
        return (String) deviceIdentifier.forIntrospection().getValue("databaseValue");
    }

    private List<ChannelInfo> getDeviceChannelInfoLP2() { //This is hardcoded list as we do not receive the captured objects in the notification
        int scale = 1;
        List<ChannelInfo> channelInfos = new ArrayList<>();

        ChannelInfo ci1 = new ChannelInfo(0, "1.0.1.8.0.255", Unit.get(BaseUnit.WATTHOUR, scale), getDeviceId());
        ChannelInfo ci2 = new ChannelInfo(1, "1.0.2.8.0.255", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scale), getDeviceId());
        ChannelInfo ci3 = new ChannelInfo(2, "1.0.3.8.0.255", Unit.get(BaseUnit.UNITLESS, scale), getDeviceId());
        ChannelInfo ci4 = new ChannelInfo(3, "1.0.4.8.0.255", Unit.get(BaseUnit.UNITLESS, scale), getDeviceId());
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
            this.collectedLogBook = this.getCollectedDataFactory().createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, logbookObisCode));
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
        this.collectedDeviceIpAddress = this.getCollectedDataFactory().createDeviceIpAddress(this.deviceIdentifier, deviceIpAddress, IP_ADDRESS_PROPERTY_NAME);
    }

    public byte[] doHandleGeneralBlockTransfer(byte[] rawResponse) throws IOException {
        GeneralBlockTransferFrame responseFrame = new GeneralBlockTransferFrame();

        // Parse the first general block transfer frame
        responseFrame.parseFrame(rawResponse, 9);
        setBlockNumber(responseFrame.getAcknowledgedBlockNumber());
        setAcknowledgedBlockNumber(responseFrame.getBlockNumber());
        if(responseFrame.getBlockData()[0] == DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING){
            ByteBuffer byteBuffer = ByteBuffer.wrap(responseFrame.getBlockData());
            byteBuffer.get(); //skip tag
            setResponseData(parseHeaderAndDecrypt(byteBuffer).array());
        } else {
            setResponseData(responseFrame.getBlockData());
        }

        int timeout = 10000;
        long timeoutMoment = System.currentTimeMillis() + timeout;

        boolean isLastBlock = responseFrame.getBlockControl().isLastBlock();
        while (!isLastBlock) {
            // Flush the storedBlockDataFromPreviousResponse byte array
            flushStoredBlockDataFromPreviousResponse();

            // Receive the next 'windowSize' number of blocks
            if(getComChannel().available() > 0){
                byte[] buffer = new byte[getComChannel().available()];
                getComChannel().read(buffer);

                GeneralBlockTransferFrame generalBlockTransferFrame = new GeneralBlockTransferFrame();
                byte[] response = ProtocolTools.concatByteArrays(getStoredBlockDataFromPreviousResponse(), buffer);
                int offset = generalBlockTransferFrame.parseFrame(buffer, 8);

                if (generalBlockTransferFrame.getLengthOfBlockData() == 0) {
                    throw new ProtocolException("GeneralBlockTransferHandler, receiveNextBlocksFromDevice - Fetch of block " + generalBlockTransferFrame.getBlockNumber() + " failed, the block content was empty.");
                }

                setStoredBlockDataFromPreviousResponse(ProtocolTools.getSubArray(response, offset));
                setAcknowledgedBlockNumber(generalBlockTransferFrame.getBlockNumber());
                setBlockNumber(generalBlockTransferFrame.getAcknowledgedBlockNumber());
                isLastBlock = generalBlockTransferFrame.getBlockControl().isLastBlock();
                // Add decrypted block data from the responseData byte array and update the block numbers
                if(responseFrame.getBlockData()[0] == DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING){
                    ByteBuffer byteBuffer = ByteBuffer.wrap(generalBlockTransferFrame.getBlockData());
                    byteBuffer.get(); //skip tag
                    addNextBlockDataToResponseData(parseHeaderAndDecrypt(byteBuffer).array());
                } else {
                    addNextBlockDataToResponseData(generalBlockTransferFrame.getBlockData());
                }
            }

            delay(100);
            // do not wait more than timeout for the last block
            if (System.currentTimeMillis() - timeoutMoment > 0) {
                if (getResponseData() == null || getResponseData().length == 0) {
                    throw InboundFrameException.timeoutException(String.format("Timeout: didn't receive an inbound frame after %d ms.", timeout));
                } else {
                    throw InboundFrameException.timeoutException(String.format("Timeout: didn't receive the next block after %d ms.", timeout));
                }
            }
        }

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

}
