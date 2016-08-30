package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cbo.NestedIOException;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityContextV2EncryptionHandler;
import com.energyict.dlms.aso.framecounter.DefaultRespondingFrameCounterHandler;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDAO;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.am540.events.MeterEventParser;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierLikeSerialNumber;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/09/2014 - 9:19
 */
public class EventPushNotificationParser {

    /**
     * The default obiscode of the logbook to store the received events in
     */
    private static final ObisCode DEFAULT_OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");

    private static final ObisCode EVENT_NOTIFICATION_OBISCODE = ObisCode.fromString("0.0.128.0.12.255");
    private static final int LAST_EVENT_ATTRIBUTE_NUMBER = 3;

    private static final int DROP = 0;
    private static final int PASSTHROUGH = 1;
    private static final int ADD_ORIGIN_HEADER = 2;
    private static final int WRAP_AS_SERVER_EVENT = 3;

    private final ComChannel comChannel;
    private final InboundDAO inboundDAO;
    private final InboundComPort inboundComPort;
    protected final ObisCode logbookObisCode;
    protected CollectedLogBook collectedLogBook;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    protected DeviceIdentifier deviceIdentifier;
    private int sourceSAP = 0;
    private int destinationSAP = 0;
    private int notificationType = 0;

    public EventPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        this.comChannel = comChannel;
        this.inboundDAO = context.getInboundDAO();
        this.inboundComPort = context.getComPort();
        this.logbookObisCode = DEFAULT_OBIS_STANDARD_EVENT_LOG;
    }

    public EventPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context, ObisCode logbookObisCode) {
        this.comChannel = comChannel;
        this.inboundDAO = context.getInboundDAO();
        this.inboundComPort = context.getComPort();
        this.logbookObisCode = logbookObisCode;
    }

    protected ComChannel getComChannel() {
        return comChannel;
    }

    /**
     * Parses the inbound frame. Currently supported security:
     * - plain event notification frame (tag 0xC2 COSEM_EVENTNOTIFICATIONREQUEST)
     * - event notification frame secured with general-global ciphering (tag 0xDB GENERAL_GLOBAL_CIPHERING)
     * - event notification frame secured with general ciphering (tag 0xDD GENERAL_CIPHERING)
     * - event notification frame secured with general signing (tag 0xDF GENERAL_SIGNING)
     * <p/>
     * Not supported:
     * - ded-event-notification-request (tag 0xCA DED_EVENTNOTIFICATION_REQUEST) because we don't have a dedicated session key available
     * - glo-event-notification-request (tag 0xD2 GLO_EVENTNOTIFICATION_REQUEST) because it does not contain a system-title to identify the device
     */
    public void readAndParseInboundFrame() {
        ByteBuffer inboundFrame = readInboundFrame();
        byte[] header = new byte[8];
        inboundFrame.get(header);

        readAndParseInboundFrame(inboundFrame);
    }

    public void readAndParseInboundFrame(ByteBuffer inboundFrame) {
        byte tag = inboundFrame.get();

        switch (tag) {
            case DLMSCOSEMGlobals.COSEM_EVENTNOTIFICATIONRESUEST:
                parsePlainEventAPDU(inboundFrame);
                break;
            case DLMSCOSEMGlobals.COSEM_DATANOTIFICATIONREQUEST:
                parsePlainDataAPDU(inboundFrame);
                break;
            case DLMSCOSEMGlobals.GENERAL_CIPHERING:
                parseGeneralCipheringFrame(inboundFrame);
                break;
            case DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING:
                parseGeneralGlobalFrame(inboundFrame);
                break;
            case DLMSCOSEMGlobals.GENERAL_SIGNING:
                parseGeneralSigningFrame(inboundFrame);
                break;
            default:
                throw DataParseException.ioException(new ProtocolException("Unexpected tag '" + tag + "' in received push event notification. Expected '" +
                        DLMSCOSEMGlobals.COSEM_EVENTNOTIFICATIONRESUEST + "' or '" +
                        DLMSCOSEMGlobals.COSEM_DATANOTIFICATIONREQUEST + "', '" +
                        DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING + "', '" +
                        DLMSCOSEMGlobals.GENERAL_CIPHERING + "' or '" +
                        DLMSCOSEMGlobals.GENERAL_SIGNING + "'"));
        }
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
            e.printStackTrace();
        }
        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(securityContext.getResponseSystemTitle());

        readAndParseInboundFrame(decryptedFrame);
    }

    private void parseGeneralCipheringFrame(ByteBuffer inboundFrame) {
        byte[] systemTitle = initializeDeviceIdentifier(inboundFrame.asReadOnlyBuffer());
        SecurityContext securityContext = getSecurityContext();
        securityContext.setResponseSystemTitle(systemTitle);

        byte[] remaining = new byte[inboundFrame.remaining()];
        inboundFrame.get(remaining);
        byte[] generalCipheredResponse = ProtocolTools.concatByteArrays(new byte[]{DLMSCOSEMGlobals.GENERAL_CIPHERING}, remaining);

        ByteBuffer decryptedFrame = null;
        try {
            decryptedFrame = ByteBuffer.wrap(SecurityContextV2EncryptionHandler.dataTransportGeneralDecryption(securityContext, generalCipheredResponse));
        } catch (DLMSConnectionException e) {
            e.printStackTrace();
        }
        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(securityContext.getResponseSystemTitle());

        //Now parse the resulting APDU again, it could be a plain or a ciphered APDU.
        readAndParseInboundFrame(decryptedFrame);
    }

    private void parseGeneralSigningFrame(ByteBuffer inboundFrame) {
        byte[] systemTitle = initializeDeviceIdentifier(inboundFrame.asReadOnlyBuffer());
        SecurityContext securityContext = getSecurityContext();
        securityContext.setResponseSystemTitle(systemTitle);
        int remainingLength = inboundFrame.get() & 0xFF;
        int securityPolicy = inboundFrame.get() & 0xFF;

        ByteBuffer decryptedFrame;
        byte[] cipherFrame = new byte[inboundFrame.remaining()];
        inboundFrame.get(cipherFrame);
        byte[] fullCipherFrame = ProtocolTools.concatByteArrays(new byte[]{(byte) 0x00, (byte) remainingLength, (byte) securityPolicy}, cipherFrame);
        try {
            decryptedFrame = ByteBuffer.wrap(SecurityContextV2EncryptionHandler.dataTransportDecryption(securityContext, fullCipherFrame));
        } catch (DLMSConnectionException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
        }

        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(securityContext.getResponseSystemTitle());

        //Now parse the resulting APDU again, it could be a plain or a ciphered APDU.
        readAndParseInboundFrame(decryptedFrame);
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    /**
     * EventNotificationRequest ::= SEQUENCE
     * - date-time (OCTET STRING, optional)
     * - cosem-attribute-descriptor (class ID, obiscode and attribute number)
     * - attribute-value (Data)
     */
    protected void parsePlainEventAPDU(ByteBuffer inboundFrame) {
        byte dateLength = inboundFrame.get();
        inboundFrame.get(new byte[dateLength]); //Skip the date field

        short classId = inboundFrame.getShort();
        if (classId != DLMSClassId.EVENT_NOTIFICATION.getClassId()) {
            throw DataParseException.ioException(new ProtocolException("Expected push event notification from object with class ID '" + DLMSClassId.EVENT_NOTIFICATION.getClassId() + "' but was '" + classId + "'"));
        }

        byte[] obisCodeBytes = new byte[6];
        inboundFrame.get(obisCodeBytes);
        ObisCode obisCode = ObisCode.fromByteArray(obisCodeBytes);
        if (!obisCode.equals(EVENT_NOTIFICATION_OBISCODE)) {
            throw DataParseException.ioException(new ProtocolException("Expected push event notification from object with obiscode '" + EVENT_NOTIFICATION_OBISCODE + "' but was '" + obisCode.toString() + "'"));
        }

        int attributeNumber = inboundFrame.get() & 0xFF;
        if (attributeNumber != LAST_EVENT_ATTRIBUTE_NUMBER) {
            throw DataParseException.ioException(new ProtocolException("Expected push event notification attribute '" + LAST_EVENT_ATTRIBUTE_NUMBER + "' but was '" + attributeNumber + "'"));
        }

        byte[] eventData = new byte[inboundFrame.remaining()];
        inboundFrame.get(eventData);

        Structure structure;
        try {
            structure = AXDRDecoder.decode(eventData, Structure.class);
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }

        int nrOfDataTypes = structure.nrOfDataTypes();
        if (nrOfDataTypes == 5) {
            //This is the case for the G3 gateway and the Beacon gateway/DC with firmware < 1.4.0
            validateCosemAttributeDescriptorOriginatingFromGateway(classId, obisCode, attributeNumber);
            parseNotificationWith5Elements(structure);
        } else if (nrOfDataTypes == 4) {
            //Relayed meter event
            parseGatewayRelayedEventWith4Elements(structure);
            /*throw DataParseException.ioException(new ProtocolException("Receiving relayed meter events is currently not supported"));*/
            //parseGatewayRelayedEventWith4Elements(structure); use this method if support is added in firmware for this. The cosem attribute descriptor has to contain the original values from the slave meter
        } else if (nrOfDataTypes == 3) {
            //This is the case for the Beacon gateway/DC with firmware >= 1.4.0
            parseNotificationWith3Elements(structure, classId, obisCode, attributeNumber);
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected a structure with 3, 4 or 5 elements, but received a structure with " + nrOfDataTypes + " element(s)"));
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
     * DataNotificationRequest ::= SEQUENCE
     * - long-invoke-id-and-priority (LONG)
     * - date-time (OCTET STRING, optional)
     * - notification-body (Data)
     *         STRUCTURE {
     *         Equipment-Identifier  OCTET STRING,  (RTU serial number)
     *         Logical-Device-Id     Unsigned16     (Originating logical device (fixed to 1))
     *         Event-Payload ::= STRUCTURE {
     *             TimeStamp         COSEM DATE TIME   ( Timestamp of event )
     *             Event-Code        Unsigned16        ( Event code )
     *             Device-Code       Unsigned16        ( Device code )
     *             Event-Message     OCTET-STRING      ( Additional message )
     *             }
     *         }
     */
    protected void parsePlainDataAPDU(ByteBuffer inboundFrame) {

        /* long-invoke-id-and-priority*/
        byte[] invokeIdAndPriority = new byte[4];   // 32-bits long format used
        inboundFrame.get(invokeIdAndPriority);

        /* date-time (skip date/time length and value)*/
        int dateTimeAxdrLength = DLMSUtils.getAXDRLength(inboundFrame.array(), inboundFrame.position());
        inboundFrame.get(new byte[DLMSUtils.getAXDRLengthOffset(dateTimeAxdrLength)]); // Increment ByteBuffer position
        byte[] octetString = new byte[dateTimeAxdrLength];
        inboundFrame.get(octetString);

        /* notification-body*/
        Structure bodyStructure = null;
        try {
            bodyStructure = AXDRDecoder.decode(inboundFrame.array(), inboundFrame.position(), 1, Structure.class);
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }

        int nrOfDataTypes = bodyStructure.nrOfDataTypes();

        if (nrOfDataTypes != 3) {
            throw DataParseException.ioException(new ProtocolException("Expected a structure with 3 elements, but received a structure with " + nrOfDataTypes + " element(s)"));
        }

        OctetString equipmentIdentifier = bodyStructure.getDataType(0).getOctetString();
        if (equipmentIdentifier == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (equipment identifier) to be of type OctetString"));
        }
        deviceIdentifier = new DeviceIdentifierBySerialNumber(equipmentIdentifier.stringValue());

        Unsigned16 logicalDeviceId = bodyStructure.getDataType(1).getUnsigned16();
        if (logicalDeviceId == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (logical device id) to be of type Unsigned16"));
        }

        /* event payload */
        Structure eventPayload = bodyStructure.getDataType(2).getStructure();

        Date dateTime = parseDateTime(eventPayload.getDataType(0).getOctetString());
        Unsigned16 eventCode = eventPayload.getDataType(1).getUnsigned16();
        Unsigned16 deviceCode = eventPayload.getDataType(2).getUnsigned16();
        String description = parseDescriptionFromOctetString(eventPayload.getDataType(3).getOctetString());

        createCollectedLogBook(dateTime, deviceCode.getValue(), eventCode.getValue(), description);
    }

    private String parseDescriptionFromOctetString(OctetString octetString) {
        if (octetString == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the fourth element of the received structure to be an OctetString"));
        }
        return octetString.stringValue();
    }

    private String parseDescription(Structure structure) {
        OctetString octetString = structure.getDataType(4).getOctetString();
        if (octetString == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the fourth element of the received structure to be an OctetString"));
        }
        return octetString.stringValue();
    }

    private void parseEvent(Structure structure) {
        Date dateTime = parseDateTime(structure);
        int eiCode = structure.getDataType(2).intValue();
        int protocolCode = structure.getDataType(3).intValue();
        String description = parseDescription(structure);

        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
        meterProtocolEvents.add(MeterEvent.mapMeterEventToMeterProtocolEvent(new MeterEvent(dateTime, eiCode, protocolCode, description)));
        collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, logbookObisCode));
        collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
    }

    protected void parseEncryptedFrame(ByteBuffer inboundFrame) {
        int systemTitleLength = inboundFrame.get() & 0xFF;
        byte[] systemTitle = new byte[systemTitleLength];
        inboundFrame.get(systemTitle);
        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(systemTitle);

        int remainingLength = inboundFrame.get() & 0xFF;
        int securityPolicy = inboundFrame.get() & 0xFF;

        if (getSecurityPropertySet().getEncryptionDeviceAccessLevel() != (securityPolicy / 16)) {
            throw DataParseException.ioException(new ProtocolException(
                    "Security mismatch: received incoming event push notification encrypted with security policy " + (securityPolicy / 16) + ", but device in EIServer is configured with security level " + getSecurityPropertySet().getEncryptionDeviceAccessLevel()));
        }

        SecurityContext securityContext = getSecurityContext();
        securityContext.setResponseSystemTitle(systemTitle);

        ByteBuffer decryptedFrame;
        byte[] cipherFrame = new byte[inboundFrame.remaining()];
        inboundFrame.get(cipherFrame);
        byte[] fullCipherFrame = ProtocolTools.concatByteArrays(new byte[]{(byte) 0x00, (byte) remainingLength, (byte) securityPolicy}, cipherFrame);
        try {
            decryptedFrame = ByteBuffer.wrap(SecurityContextV2EncryptionHandler.dataTransportDecryption(securityContext, fullCipherFrame));
        } catch (DLMSConnectionException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
        }
        byte plainTag = decryptedFrame.get();
        if (plainTag != getCosemEventNotificationAPDUTag()) {
            throw DataParseException.ioException(new ProtocolException("Unexpected tag after decrypting an incoming event push notification: " + plainTag + ", expected " + getCosemEventNotificationAPDUTag()));
        }

        parseAPDU(decryptedFrame);
    }

    protected byte getCosemEventNotificationAPDUTag() {
        return DLMSCOSEMGlobals.COSEM_EVENTNOTIFICATIONRESUEST;
    }

    protected byte getCosemDataNotificationAPDUTag() {
        return DLMSCOSEMGlobals.COSEM_DATANOTIFICATIONREQUEST;
    }

    //TODO this might change in the RTU3
    protected DeviceIdentifier getDeviceIdentifierBasedOnSystemTitle(byte[] systemTitle) {
        String serialNumber = new String(systemTitle);
        serialNumber = serialNumber.replace("DC", "");      //Strip off the "DC" prefix
        return new DeviceIdentifierLikeSerialNumber("%" + serialNumber + "%");
    }

    protected SecurityContext getSecurityContext() {
        DlmsProperties securityProperties = getNewInstanceOfProperties();
        securityProperties.setSecurityPropertySet(getSecurityPropertySet());
        securityProperties.addProperties(getSecurityPropertySet().getSecurityProperties());

        DummyComChannel dummyComChannel = new DummyComChannel();    //Dummy channel, no bytes will be read/written
        TypedProperties comChannelProperties = TypedProperties.empty();
        comChannelProperties.setProperty(ComChannelType.TYPE, ComChannelType.SocketComChannel.getType());
        dummyComChannel.addProperties(comChannelProperties);

        DlmsSession dlmsSession = new DlmsSession(dummyComChannel, securityProperties);
        SecurityContext securityContext = dlmsSession.getAso().getSecurityContext();
        securityContext.getSecurityProvider().setRespondingFrameCounterHandling(new DefaultRespondingFrameCounterHandler());
        return securityContext;
    }

    protected DlmsProperties getNewInstanceOfProperties() {
        return new G3GatewayProperties();
    }

    protected Boolean getInboundComTaskOnHold() {
        return inboundDAO.getInboundComTaskOnHold(deviceIdentifier, inboundComPort);
    }

    public DeviceProtocolSecurityPropertySet getSecurityPropertySet() {
        if (securityPropertySet == null) {
            List<SecurityProperty> securityProperties = inboundDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, inboundComPort);
            if (securityProperties != null && !securityProperties.isEmpty()) {
                this.securityPropertySet = new DeviceProtocolSecurityPropertySetImpl(securityProperties);
            } else {
                throw CommunicationException.notConfiguredForInboundCommunication(deviceIdentifier);
            }
        }
        return this.securityPropertySet;
    }

    public ByteBuffer readInboundFrame() {
        byte[] header = new byte[8];
        getComChannel().startReading();
        int readBytes = getComChannel().read(header);
        if (readBytes != 8) {
            throw DataParseException.ioException(new ProtocolException("Attempted to read out 8 header bytes but received " + readBytes + " bytes instead..."));
        }

        setSourceSAP(ProtocolTools.getIntFromBytes(header, 2, 2));
        setDestinationSAP(ProtocolTools.getIntFromBytes(header, 4, 2));

        int length = ProtocolTools.getIntFromBytes(header, 6, 2);

        byte[] frame = new byte[length];
        readBytes = getComChannel().read(frame);
        if (readBytes != length) {
            throw DataParseException.ioException(new ProtocolException("Attempted to read out full frame (" + length + " bytes), but received " + readBytes + " bytes instead..."));
        }
        return ByteBuffer.wrap(ProtocolTools.concatByteArrays(header, frame));
    }

    /**
     * EventNotificationRequest ::= SEQUENCE
     * - date-time (OCTET STRING, optional)
     * - cosem-attribute-descriptor (class ID, obiscode and attribute number)
     * - attribute-value (Data)
     */
    protected void parseAPDU(ByteBuffer inboundFrame) {
        byte dateLength = inboundFrame.get();
        inboundFrame.get(new byte[dateLength]); //Skip the date field

        short classId = inboundFrame.getShort();

        byte[] obisCodeBytes = new byte[6];
        inboundFrame.get(obisCodeBytes);
        ObisCode obisCode = ObisCode.fromByteArray(obisCodeBytes);


        int attributeNumber = inboundFrame.get() & 0xFF;

        byte[] eventData = new byte[inboundFrame.remaining()];
        inboundFrame.get(eventData);

        validateCosemAttributeDescriptorOriginatingFromGateway(classId, obisCode, attributeNumber);
        Structure structure;
        try {
            structure = AXDRDecoder.decode(eventData, Structure.class);
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }
        int nrOfDataTypes = structure.nrOfDataTypes();

        if (nrOfDataTypes == 5) {
            //This is the case for the G3 gateway and the Beacon gateway/DC with firmware < 1.4.0
            validateCosemAttributeDescriptorOriginatingFromGateway(classId, obisCode, attributeNumber);
            parseNotificationWith5Elements(structure);
        } else if (nrOfDataTypes == 4) {
            //Relayed meter event
            throw DataParseException.ioException(new ProtocolException("Receiving relayed meter events is currently not supported"));
            //parseGatewayRelayedEventWith4Elements(structure); use this method if support is added in firmware for this. The cosem attribute descriptor has to contain the original values from the slave meter
        } else if (nrOfDataTypes == 3) {
            //This is the case for the Beacon gateway/DC with firmware >= 1.4.0
            parseNotificationWith3Elements(structure, classId, obisCode, attributeNumber);
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected a structure with 3, 4 or 5 elements, but received a structure with " + nrOfDataTypes + " element(s)"));
        }
    }

    private void validateCosemAttributeDescriptorOriginatingFromGateway(short classId, ObisCode obisCode, int attributeNumber) {
        if (classId != DLMSClassId.EVENT_NOTIFICATION.getClassId()) {
            throw DataParseException.ioException(new ProtocolException("Expected push event notification from object with class ID '" + DLMSClassId.EVENT_NOTIFICATION.getClassId() + "' but was '" + classId + "'"));
        }

        if (!obisCode.equals(EVENT_NOTIFICATION_OBISCODE)) {
            throw DataParseException.ioException(new ProtocolException("Expected push event notification from object with obiscode '" + EVENT_NOTIFICATION_OBISCODE + "' but was '" + obisCode.toString() + "'"));
        }

        if (attributeNumber != LAST_EVENT_ATTRIBUTE_NUMBER) {
            throw DataParseException.ioException(new ProtocolException("Expected push event notification attribute '" + LAST_EVENT_ATTRIBUTE_NUMBER + "' but was '" + attributeNumber + "'"));
        }
    }

    private void parseNotificationWith5Elements(Structure eventPayLoad) {
        OctetString equipmentIdentifier = eventPayLoad.getDataType(0).getOctetString();
        if (equipmentIdentifier == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (equipment identifier) to be of type OctetString"));
        }
        deviceIdentifier = new DeviceIdentifierBySerialNumber(equipmentIdentifier.stringValue());

        Date dateTime = parseDateTime(eventPayLoad.getDataType(1).getOctetString());
        int eiCode = eventPayLoad.getDataType(2).intValue();
        int protocolCode = eventPayLoad.getDataType(3).intValue();
        String description = parseDescription(eventPayLoad.getDataType(4).getOctetString());

        createCollectedLogBook(dateTime, eiCode, protocolCode, description);
    }

    private void parseNotificationWith3Elements(Structure eventWrapper, short classId, ObisCode obisCode, int attributeNumber) {
        OctetString equipmentIdentifier = eventWrapper.getDataType(0).getOctetString();
        if (equipmentIdentifier == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (equipment identifier) to be of type OctetString"));
        }
        deviceIdentifier = new DeviceIdentifierBySerialNumber(equipmentIdentifier.stringValue());

        AbstractDataType dataType = eventWrapper.getDataType(2);
        if (!dataType.isStructure()) {
            throw DataParseException.ioException(new ProtocolException("The event-payload (third element in the event-wrapper structure) should be of type Structure"));
        }

        Structure eventPayLoad = dataType.getStructure();
        int nrOfDataTypes = eventPayLoad.nrOfDataTypes();
        if (nrOfDataTypes == 2) {
            parseGatewayRelayedEventWith2Elements(eventPayLoad, getAlarmRegister(obisCode));
        } else if (nrOfDataTypes == 4) {
            validateCosemAttributeDescriptorOriginatingFromGateway(classId, obisCode, attributeNumber);
            Date dateTime = parseDateTime(eventPayLoad.getDataType(0).getOctetString());
            int eiCode = eventPayLoad.getDataType(1).intValue();
            int protocolCode = eventPayLoad.getDataType(2).intValue();
            String description = parseDescription(eventPayLoad.getDataType(3).getOctetString());
            createCollectedLogBook(dateTime, eiCode, protocolCode, description);
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected the event-payload to be a structure with 3, 4 or 5 elements, but received a structure with " + nrOfDataTypes + " element(s)"));
        }
    }

    private void parseGatewayRelayedEventWith4Elements(Structure eventPayLoad) {
        OctetString equipmentIdentifier = eventPayLoad.getDataType(0).getOctetString();
        OctetString logicalDeviceName = eventPayLoad.getDataType(1).getOctetString();
        Unsigned16 logicalDeviceId = eventPayLoad.getDataType(2).getUnsigned16();
        Unsigned32 attributeValue = eventPayLoad.getDataType(3).getUnsigned32();// alarm descriptor value

        if (equipmentIdentifier == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (equipment identifier) to be of type OctetString"));
        }
        if (logicalDeviceName == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (logical device name) to be of type OctetString"));
        }
        if (logicalDeviceId == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (logical device id) to be of type Unsigned16"));
        }

        Date dateTime = Calendar.getInstance().getTime();
        deviceIdentifier = new DialHomeIdDeviceIdentifier(logicalDeviceName.toString());

        createCollectedLogBook(MeterEventParser.parseEventCode(dateTime, attributeValue.getValue(), 1));
    }

    private void parseGatewayRelayedEventWith2Elements(Structure eventPayLoad, int alarmRegister) {
        OctetString logicalDeviceName = eventPayLoad.getDataType(0).getOctetString();
        Unsigned32 attributeValue = eventPayLoad.getDataType(1).getUnsigned32();// alarm descriptor value
        if (alarmRegister == 0) {
            throw DataParseException.ioException(new ProtocolException("Expected relayed meter event from Alarm Descriptor 1 or Alarm Descriptor 2, but came from somewhere else"));
        }

        Date dateTime = Calendar.getInstance().getTime();//TODO: see what timezone should be used
        deviceIdentifier = new DialHomeIdDeviceIdentifier(logicalDeviceName.toString());
        createCollectedLogBook(MeterEventParser.parseEventCode(dateTime, attributeValue.getValue(), alarmRegister));
    }

    private void createCollectedLogBook(Date dateTime, int eiCode, int protocolCode, String description) {
        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
        meterProtocolEvents.add(MeterEvent.mapMeterEventToMeterProtocolEvent(new MeterEvent(dateTime, eiCode, protocolCode, description)));
        collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, logbookObisCode));
        collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
    }

    protected void createCollectedLogBook(List<MeterEvent> meterEvents) {
        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
        for(MeterEvent meterEvent: meterEvents){
            meterProtocolEvents.add(MeterEvent.mapMeterEventToMeterProtocolEvent(meterEvent));
        }
        collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, logbookObisCode));
        collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
    }

    public CollectedLogBook getCollectedLogBook() {
        return collectedLogBook;
    }

    private String parseDescription(OctetString octetString) {
        if (octetString == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the fourth element of the received structure to be an OctetString"));
        }
        return octetString.stringValue();
    }

    private Date parseDateTime(Structure structure) {
        OctetString octetString = structure.getDataType(1).getOctetString();
        if (octetString == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the second element of the received structure to be an OctetString"));
        }
        try {
            return new AXDRDateTime(octetString).getValue().getTime();
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }
    }

    private Date parseDateTime(OctetString octetString) {
        if (octetString == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the second element of the received structure to be an OctetString"));
        }
        try {
            return new AXDRDateTime(octetString).getValue().getTime();
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }
    }

    private int getAlarmRegister(ObisCode obisCode){
        if (obisCode.equals(ObisCode.fromString("0.0.97.98.20.255"))) {
            return 1;
        } else if (obisCode.equals(ObisCode.fromString("0.0.97.98.21.255"))) {
            return 2;
        }
        return 0;
    }

    protected InboundDAO getInboundDAO() {
        return inboundDAO;
    }

    public int getSourceSAP() {
        return sourceSAP;
    }

    public int getDestinationSAP() {
        return destinationSAP;
    }

    public void setSourceSAP(int logicalDeviceID) {
        sourceSAP = logicalDeviceID;
    }

    public void setDestinationSAP(int clientID) {
        destinationSAP = clientID;
    }

    public int getNotificatioType() {
        return notificationType;
    }

    public void setNotificatioType(int notificatioType) {
        this.notificationType = notificatioType;
    }
}