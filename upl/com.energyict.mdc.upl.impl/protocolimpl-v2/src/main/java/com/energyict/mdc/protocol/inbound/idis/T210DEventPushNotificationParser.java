package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityContextV2EncryptionHandler;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events.T210DMeterAlarmParser;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierLikeSerialNumber;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByObisCodeAndDevice;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cisac on 7/13/2016.
 */
public class T210DEventPushNotificationParser extends DataPushNotificationParser {

    public static final String IP_ADDRESS_PROPERTY_NAME = "host";
    private static final String LOAD_PROFILE_1_OBIS = "1.0.99.1.0.255";
    private static final String LOAD_PROFILE_2_OBIS = "1.0.99.2.0.255";
    private static final String PUSH_ON_ALARM_OBJECT_LIST_OBIS = "0.4.25.9.0.255";
    private static final String PUSH_ON_INTERVAL_OBJECT_LIST_OBIS = "0.1.25.9.0.255";
    private static final String PUSH_ON_INSTALLATION_OBJECT_LIST_OBIS = "0.7.25.9.0.255";
    private static final String PUSH_ON_CONNECTIVITY_OBJECT_LIST_OBIS = "0.0.25.9.0.255";
    List<T210DPushObjectListEntry> pushObjectList;
    private CollectedDeviceInfo collectedDeviceIpAddress;
    private List<CollectedLoadProfile> collectedLoadProfileList;
    private ObisCode pushObjectListObisCode;

    public T210DEventPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        super(comChannel, context);
    }

    @Override
    protected void parseEncryptedFrame(ByteBuffer inboundFrame) {
        parseGeneralGlobalFrame(inboundFrame);
    }

    private void parseGeneralGlobalFrame(ByteBuffer inboundFrame) {

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

        byte plainTag = decryptedFrame.get();
        if (plainTag != getCosemNotificationAPDUTag()) {
            throw DataParseException.ioException(new ProtocolException("Unexpected tag after decrypting an incoming event push notification: " + plainTag + ", expected " + getCosemNotificationAPDUTag()));
        }
        parseAPDU(decryptedFrame);
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

        if(pushObjectListObisCode.equals(ObisCode.fromString(PUSH_ON_INSTALLATION_OBJECT_LIST_OBIS)) || pushObjectListObisCode.equals(ObisCode.fromString(PUSH_ON_CONNECTIVITY_OBJECT_LIST_OBIS))){
            //the structure will contain 7 elements in case of a Push on Installation Data Notification
            parsePushOnInstallatioEvent(structure);
        } else if(pushObjectListObisCode.equals(ObisCode.fromString(PUSH_ON_INTERVAL_OBJECT_LIST_OBIS))) {
            //TODO: see if this will remain 3
            //the structure will contain 3 elements in case of a Push on Interval Data Notification
            parsePushOnInterval(structure);
        } else if(pushObjectListObisCode.equals(ObisCode.fromString(PUSH_ON_ALARM_OBJECT_LIST_OBIS))) {
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
    }

    private void parsePushOnInterval(Structure structure) {
        parseCollectedData(structure.getNextDataType());
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
        createCollectedLogBook(T210DMeterAlarmParser.parseAlarmCode(eventDate, alarmDescriptor, alarmRegister));
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
            long element3 = struct.getDataType(3).longValue();
            pushObjectList.add(new T210DPushObjectListEntry(classId, obisCode, attributeNr, element3));
            System.out.println("classID = "+classId+ " obisCode = "+obisCode.toString()+ " attributeNr = "+attributeNr+ " element3 = "+element3);
        }
    }

    private void parseLogicalDeviceName(AbstractDataType dataType) {
        if (!(dataType instanceof OctetString)) {
            throw DataParseException.ioException(new ProtocolException("The second element of the Data-notification body should be the of type OctetString"));
        }
        String logicalDeviceName = dataType.getOctetString().stringValue();
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

    private void parseCollectedData(AbstractDataType dataType) {
        for(int i = 2; i <= pushObjectList.size() - 1; i++){
            if(pushObjectList.get(i).getObisCode().equals(ObisCode.fromString(LOAD_PROFILE_1_OBIS))){
                getColectedLoadProfile1(dataType);
            }
        }
    }

    private void getColectedLoadProfile1(AbstractDataType dataType) {
        if (!(dataType instanceof Array)) {
            throw DataParseException.ioException(new ProtocolException("The third element of the Data-notification body should be the of type Array"));
        }
        for(AbstractDataType structure: ((Array) dataType).getAllDataTypes()){
            //TODO: LP configuration will be fixed so we have to hardcode the values to the channels
            Structure struct = structure.getStructure();
            Date clockTime = parseDateTime(struct.getDataType(0).getOctetString());
            int status = struct.getDataType(1).getUnsigned8().intValue();
            int activeEnergyImport = struct.getDataType(2).getUnsigned32().intValue();
            int activeEnergyExport = struct.getDataType(3).getUnsigned32().intValue();
            int reactiveEnergyImport = struct.getDataType(4).getUnsigned32().intValue();
            int reactiveEnergyExport = struct.getDataType(5).getUnsigned32().intValue();
            ObisCode loadProfileObisCode = ObisCode.fromString(LOAD_PROFILE_1_OBIS);
            getCollectedLoadProfile().add(MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierByObisCodeAndDevice(loadProfileObisCode, deviceIdentifier)));
            System.out.println("clockTime = "+clockTime+" status = "+status+" activeEnergyImport = "+activeEnergyImport+" activeEnergyExport = "+activeEnergyExport+" reactiveEnergyImport = "+reactiveEnergyImport+" reactiveEnergyExport = "+reactiveEnergyExport);
        }
    }

    private void getColectedLoadProfile2(AbstractDataType dataType) {
        if (!(dataType instanceof Array)) {
            throw DataParseException.ioException(new ProtocolException("The third element of the Data-notification body should be the of type Array"));
        }
        for(AbstractDataType structure: ((Array) dataType).getAllDataTypes()){
            //TODO: LP configuration will be fixed so we have to hardcode the values to the channels
            Structure struct = structure.getStructure();
            Date clockTime = parseDateTime(struct.getDataType(0).getOctetString());
            int status = struct.getDataType(1).getUnsigned8().intValue();
            int activeEnergyImport = struct.getDataType(2).getUnsigned32().intValue();
            int activeEnergyExport = struct.getDataType(3).getUnsigned32().intValue();
            int reactiveEnergyImport = struct.getDataType(4).getUnsigned32().intValue();
            int reactiveEnergyExport = struct.getDataType(5).getUnsigned32().intValue();
            ObisCode loadProfileObisCode = ObisCode.fromString(LOAD_PROFILE_2_OBIS);
            getCollectedLoadProfile().add(MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierByObisCodeAndDevice(loadProfileObisCode, deviceIdentifier)));
            System.out.println("clockTime = "+clockTime+" status = "+status+" activeEnergyImport = "+activeEnergyImport+" activeEnergyExport = "+activeEnergyExport+" reactiveEnergyImport = "+reactiveEnergyImport+" reactiveEnergyExport = "+reactiveEnergyExport);
        }
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
     System title value is the as LogicalDeviceName
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

    @Override
    public CollectedRegisterList getCollectedRegisters() {
        if (this.collectedRegisters == null)  {
            this.collectedRegisters = MdcManager.getCollectedDataFactory().createCollectedRegisterList(getDeviceIdentifier());
        }
        return this.collectedRegisters;
    }

    @Override
    public CollectedLogBook getCollectedLogBook() {
        if(this.collectedLogBook == null) {
            this.collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, logbookObisCode));
        }
        return this.collectedLogBook;
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
}
