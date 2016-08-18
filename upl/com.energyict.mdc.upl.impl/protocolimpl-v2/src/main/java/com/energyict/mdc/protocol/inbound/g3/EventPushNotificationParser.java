package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityContextV2EncryptionHandler;
import com.energyict.dlms.aso.framecounter.DefaultRespondingFrameCounterHandler;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
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
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierLikeSerialNumber;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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
    private final ComChannel comChannel;
    private final InboundDAO inboundDAO;
    private final InboundComPort inboundComPort;
    private final ObisCode logbookObisCode;
    private CollectedLogBook collectedLogBook;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    private DeviceIdentifier deviceIdentifier;

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
     * - plain event notification frame (tag 0xC2 COSEM_EVENTNOTIFICATIONRESUEST)
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

        parseInboundFrame(inboundFrame);
    }

    private void parseInboundFrame(ByteBuffer inboundFrame) {
        byte tag = inboundFrame.get();
        if (tag == getCosemNotificationAPDUTag()) {
            parsePlainAPDU(inboundFrame);
        } else if (tag == DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING) {
            parseGeneralGlobalFrame(inboundFrame);
        } else if (tag == DLMSCOSEMGlobals.GENERAL_CIPHERING) {
            parseGeneralCipheringFrame(inboundFrame);
        } else if (tag == DLMSCOSEMGlobals.GENERAL_SIGNING) {
            parseGeneralSigningFrame(inboundFrame);
        } else {
            throw DataParseException.ioException(new ProtocolException("Unexpected tag '" + tag + "' in received push event notification. Expected '" +
                    getCosemNotificationAPDUTag() + "', '" +
                    DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING + "', '" +
                    DLMSCOSEMGlobals.GENERAL_CIPHERING + "' or '" +
                    DLMSCOSEMGlobals.GENERAL_SIGNING + "'"));
        }
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    private void parseGeneralGlobalFrame(ByteBuffer inboundFrame) {
        initializeDeviceIdentifier(inboundFrame.asReadOnlyBuffer());
        SecurityContext securityContext = getSecurityContext();

        byte[] remaining = new byte[inboundFrame.remaining()];
        inboundFrame.get(remaining);
        byte[] generalGlobalResponse = ProtocolTools.concatByteArrays(new byte[]{DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING}, remaining);

        ByteBuffer decryptedFrame = ByteBuffer.wrap(SecurityContextV2EncryptionHandler.dataTransportGeneralGloOrDedDecryption(securityContext, generalGlobalResponse));

        parseInboundFrame(decryptedFrame);
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

    private void parseGeneralCipheringFrame(ByteBuffer inboundFrame) {
        SecurityContext securityContext = getSecurityContext();

        byte[] remaining = new byte[inboundFrame.remaining()];
        inboundFrame.get(remaining);
        byte[] generalCipheredResponse = ProtocolTools.concatByteArrays(new byte[]{DLMSCOSEMGlobals.GENERAL_CIPHERING}, remaining);

        ByteBuffer decryptedFrame = ByteBuffer.wrap(SecurityContextV2EncryptionHandler.dataTransportGeneralDecryption(securityContext, generalCipheredResponse));
        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(securityContext.getResponseSystemTitle());

        //Now parse the resulting APDU again, it could be a plain or a ciphered APDU.
        parseInboundFrame(decryptedFrame);
    }

    private void parseGeneralSigningFrame(ByteBuffer inboundFrame) {
        SecurityContext securityContext = getSecurityContext();

        byte[] remaining = new byte[inboundFrame.remaining()];
        inboundFrame.get(remaining);
        byte[] generalSignedResponse = ProtocolTools.concatByteArrays(new byte[]{DLMSCOSEMGlobals.GENERAL_SIGNING}, remaining);

        ByteBuffer decryptedFrame = ByteBuffer.wrap(SecurityContextV2EncryptionHandler.unwrapGeneralSigning(securityContext, generalSignedResponse));
        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(securityContext.getResponseSystemTitle());

        //Now parse the resulting APDU again, it could be a plain or a ciphered APDU.
        parseInboundFrame(decryptedFrame);
    }

    protected byte getCosemNotificationAPDUTag() {
        return DLMSCOSEMGlobals.COSEM_EVENTNOTIFICATIONRESUEST;
    }

    //TODO this might change in the RTU3
    protected DeviceIdentifier getDeviceIdentifierBasedOnSystemTitle(byte[] systemTitle) {
        String serialNumber = new String(systemTitle);
        serialNumber = serialNumber.replace("DC", "");      //Strip off the "DC" prefix
        return new DeviceIdentifierLikeSerialNumber("%" + serialNumber + "%");
    }

    private SecurityContext getSecurityContext() {
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

    private ByteBuffer readInboundFrame() {
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

    /**
     * EventNotificationRequest ::= SEQUENCE
     * - date-time (OCTET STRING, optional)
     * - cosem-attribute-descriptor (class ID, obiscode and attribute number)
     * - attribute-value (Data)
     */
    protected void parsePlainAPDU(ByteBuffer inboundFrame) {
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
        if (nrOfDataTypes != 5) {
            throw DataParseException.ioException(new ProtocolException("Expected a structure with 5 elements, but received a structure with " + nrOfDataTypes + " element(s)"));
        }
        OctetString equipmentIdentifier = structure.getDataType(0).getOctetString();
        if (equipmentIdentifier == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (equipment identifier) to be of type OctetString"));
        }
        deviceIdentifier = new DeviceIdentifierBySerialNumber(equipmentIdentifier.stringValue());

        parseEvent(structure);
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

    public CollectedLogBook getCollectedLogBook() {
        return collectedLogBook;
    }

    private String parseDescription(Structure structure) {
        OctetString octetString = structure.getDataType(4).getOctetString();
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

    protected InboundDAO getInboundDAO() {
        return inboundDAO;
    }
}