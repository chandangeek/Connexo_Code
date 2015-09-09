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
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.events.G3GatewayEvents;
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

    private static final ObisCode EVENT_NOTIFICATION_OBISCODE = ObisCode.fromString("0.0.128.0.12.255");
    private static final int LAST_EVENT_ATTRIBUTE_NUMBER = 3;

    private final ComChannel comChannel;
    private final InboundDAO inboundDAO;
    private final InboundComPort inboundComPort;
    private CollectedLogBook collectedLogBook;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    private DeviceIdentifier deviceIdentifier;

    public EventPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        this.comChannel = comChannel;
        this.inboundDAO = context.getInboundDAO();
        this.inboundComPort = context.getComPort();
    }

    protected ComChannel getComChannel() {
        return comChannel;
    }

    public void parseInboundFrame() {
        ByteBuffer inboundFrame = readInboundFrame();
        byte[] header = new byte[8];
        inboundFrame.get(header);
        byte tag = inboundFrame.get();
        if (tag == getCosemNotificationAPDUTag()) {
            parseAPDU(inboundFrame);
        } else if (tag == DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING) {
            parseEncryptedFrame(inboundFrame);
        } else {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Unexpected tag '" + tag + "' in received push event notification. Expected '" + getCosemNotificationAPDUTag() + "' or '" + DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING + "'"));
        }
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    private void parseEncryptedFrame(ByteBuffer inboundFrame) {
        int systemTitleLength = inboundFrame.get() & 0xFF;
        byte[] systemTitle = new byte[systemTitleLength];
        inboundFrame.get(systemTitle);
        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(systemTitle);

        int remainingLength = inboundFrame.get() & 0xFF;
        int securityPolicy = inboundFrame.get() & 0xFF;

        if (getSecurityPropertySet().getEncryptionDeviceAccessLevel() != (securityPolicy / 16)) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException(
                    "Security mismatch: received incoming event push notification encrypted with security policy " + (securityPolicy / 16) + ", but device in EIServer is configured with security level " + getSecurityPropertySet().getEncryptionDeviceAccessLevel()));
        }

        SecurityContext securityContext = getSecurityContext();
        securityContext.setResponseSystemTitle(systemTitle);

        ByteBuffer decryptedFrame;
        byte[] cipherFrame = new byte[inboundFrame.remaining()];
        inboundFrame.get(cipherFrame);
        byte[] fullCipherFrame = ProtocolTools.concatByteArrays(new byte[]{(byte) 0x00, (byte) remainingLength, (byte) securityPolicy}, cipherFrame);
        decryptedFrame = ByteBuffer.wrap(SecurityContextV2EncryptionHandler.dataTransportDecryption(securityContext, fullCipherFrame));
        byte plainTag = decryptedFrame.get();
        if (plainTag != getCosemNotificationAPDUTag()) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Unexpected tag after decrypting an incoming event push notification: " + plainTag + ", expected " + getCosemNotificationAPDUTag()));
        }

        parseAPDU(decryptedFrame);
    }

    protected byte getCosemNotificationAPDUTag() {
        return DLMSCOSEMGlobals.COSEM_EVENTNOTIFICATIONRESUEST;
    }

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
                throw MdcManager.getComServerExceptionFactory().notConfiguredForInboundCommunication(deviceIdentifier);
            }
        }
        return this.securityPropertySet;
    }

    private ByteBuffer readInboundFrame() {
        byte[] header = new byte[8];
        getComChannel().startReading();
        int readBytes = getComChannel().read(header);
        if (readBytes != 8) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Attempted to read out 8 header bytes but received " + readBytes + " bytes instead..."));
        }
        int length = ProtocolTools.getIntFromBytes(header, 6, 2);

        byte[] frame = new byte[length];
        readBytes = getComChannel().read(frame);
        if (readBytes != length) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Attempted to read out full frame (" + length + " bytes), but received " + readBytes + " bytes instead..."));
        }
        return ByteBuffer.wrap(ProtocolTools.concatByteArrays(header, frame));
    }

    protected void parseAPDU(ByteBuffer inboundFrame) {
        byte optionalDate = inboundFrame.get(); //Should be 0x00, meaning there is no date.

        short classId = inboundFrame.getShort();
        if (classId != DLMSClassId.EVENT_NOTIFICATION.getClassId()) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Expected push event notification from object with class ID '" + DLMSClassId.EVENT_NOTIFICATION.getClassId() + "' but was '" + classId + "'"));
        }

        byte[] obisCodeBytes = new byte[6];
        inboundFrame.get(obisCodeBytes);
        ObisCode obisCode = ObisCode.fromByteArray(obisCodeBytes);
        if (!obisCode.equals(EVENT_NOTIFICATION_OBISCODE)) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Expected push event notification from object with obiscode '" + EVENT_NOTIFICATION_OBISCODE + "' but was '" + obisCode.toString() + "'"));
        }

        int attributeNumber = inboundFrame.get() & 0xFF;
        if (attributeNumber != LAST_EVENT_ATTRIBUTE_NUMBER) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Expected push event notification attribute '" + LAST_EVENT_ATTRIBUTE_NUMBER + "' but was '" + attributeNumber + "'"));
        }

        byte[] eventData = new byte[inboundFrame.remaining()];
        inboundFrame.get(eventData);

        Structure structure;
        try {
            structure = AXDRDecoder.decode(eventData, Structure.class);
        } catch (ProtocolException e) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(e);
        }
        int nrOfDataTypes = structure.nrOfDataTypes();
        if (nrOfDataTypes != 5) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Expected a structure with 5 elements, but received a structure with " + nrOfDataTypes + " element(s)"));
        }
        OctetString equipmentIdentifier = structure.getDataType(0).getOctetString();
        if (equipmentIdentifier == null) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Expected the first element of the received structure (equipment identifier) to be of type OctetString"));
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
        collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, G3GatewayEvents.OBIS_STANDARD_EVENT_LOG));
        collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
    }

    public CollectedLogBook getCollectedLogBook() {
        return collectedLogBook;
    }

    private String parseDescription(Structure structure) {
        OctetString octetString = structure.getDataType(4).getOctetString();
        if (octetString == null) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Expected the fourth element of the received structure to be an OctetString"));
        }
        return octetString.stringValue();
    }

    private Date parseDateTime(Structure structure) {
        OctetString octetString = structure.getDataType(1).getOctetString();
        if (octetString == null) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Expected the second element of the received structure to be an OctetString"));
        }
        try {
            return new AXDRDateTime(octetString).getValue().getTime();
        } catch (ProtocolException e) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(e);
        }
    }

    protected InboundDAO getInboundDAO() {
        return inboundDAO;
    }
}