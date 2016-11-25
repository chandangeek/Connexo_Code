package com.energyict.protocols.mdc.inbound.idis;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ComChannelType;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.exceptions.DataParseException;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.impl.channels.VoidComChannel;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityContextV2EncryptionHandler;
import com.energyict.dlms.aso.framecounter.DefaultRespondingFrameCounterHandler;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.EventPushNotificationConfig;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.DlmsProperties;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Parser class for the IDIS DataPush<br/>
 * This parser class can parse/handle a pushed DLMS Data-notification message and extract the relevant information
 * - being the deviceIdentifier and some register data - out of it.
 *
 * @author sva
 * @since 13/04/2015 - 16:45
 */
public class DataPushNotificationParser {

    CollectedRegisterList collectedRegisters;
    private ComChannel comChannel;
    private final InboundDiscoveryContext context;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    protected final ObisCode logbookObisCode;
    protected DeviceIdentifier deviceIdentifier;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    protected CollectedLogBook collectedLogBook;

    private static final ObisCode DEFAULT_OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode EVENT_NOTIFICATION_OBISCODE = ObisCode.fromString("0.0.128.0.12.255");

    public DataPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.comChannel = comChannel;
        this.context = context;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.logbookObisCode = DEFAULT_OBIS_STANDARD_EVENT_LOG;
    }

    protected InboundDiscoveryContext getContext(){
        return context;
    }

    protected void log(String message){
        log(message, Level.INFO);
    }

    protected void log(String message, Level level){
        getContext().logOnAllLoggerHandlers(message, level);
    }
    protected DeviceIdentifier getDeviceIdentifierBasedOnSystemTitle(byte[] systemTitle) {
        String serverSystemTitle = ProtocolTools.getHexStringFromBytes(systemTitle, "");
        serverSystemTitle = serverSystemTitle.replace("454C53", "ELS-");      // Replace HEX 454C53 by its ASCII 'ELS'
        return identificationService.createDeviceIdentifierByCallHomeId(serverSystemTitle);
    }

    protected DlmsProperties getNewInstanceOfProperties() {
        return new DlmsProperties(propertySpecService, thesaurus);
    }

    protected byte getCosemNotificationAPDUTag() {
        return DLMSCOSEMGlobals.COSEM_DATA_NOTIFICATION;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
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
            //TODO support general ciphering & general signing (suite 0, 1 and 2)
            throw DataParseException.ioException(new ProtocolException("Unexpected tag '" + tag + "' in received push event notification. Expected '" + getCosemNotificationAPDUTag() + "' or '" + DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING + "'"));
        }
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

    /**
     * Data-Notification ::= SEQUENCE
     * - long-invoke-id-and-priority
     * - date-time (OCTET STRING)
     * - notification-body
     */
    protected void parseAPDU(ByteBuffer inboundFrame) {
        // 1. long-invoke-id-and-priority
        byte[] invokeIdAndPriority = new byte[4];   // 32-bits long format used
        inboundFrame.get(invokeIdAndPriority);

        //2. date-time
        int dateTimeAxdrLength = DLMSUtils.getAXDRLength(inboundFrame.array(), inboundFrame.position());
        inboundFrame.get(new byte[DLMSUtils.getAXDRLengthOffset(dateTimeAxdrLength)]); // Increment ByteBuffer position

        byte[] octetString = new byte[dateTimeAxdrLength];
        inboundFrame.get(octetString);
        Date dateTime = parseDateTime(new OctetString(octetString));

        //3. notification-body
        Structure structure;
        try {
            structure = AXDRDecoder.decode(inboundFrame.array(), inboundFrame.position(), Structure.class);
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }

        AbstractDataType dataType = structure.getNextDataType();
        if (dataType instanceof OctetString) {
            ObisCode obisCode = ObisCode.fromByteArray(((OctetString) dataType).getOctetStr());
            if (!obisCode.equalsIgnoreBChannel(EventPushNotificationConfig.getDefaultObisCode())) {
                throw DataParseException.ioException(new ProtocolException("The first element of the Data-notification body should contain the obiscode of the Push Setup IC, but was unexpected obis '" + obisCode.toString() + "'"));
            }
        } else {
            throw DataParseException.ioException(new ProtocolException("The first element of the Data-notification body should contain the obiscode of the Push Setup IC, but was an element of type '" + dataType.getClass().getSimpleName() + "'"));
        }

        parseRegisters(structure);
    }

    protected void parseEncryptedFrame(ByteBuffer inboundFrame) {
        int systemTitleLength = inboundFrame.get() & 0xFF;
        byte[] systemTitle = new byte[systemTitleLength];
        inboundFrame.get(systemTitle);
        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(systemTitle);

        int remainingLength = inboundFrame.get() & 0xFF;
        int securityPolicy = inboundFrame.get() & 0xFF;

        //TODO just assume that it is ok ...
//        if (getSecurityPropertySet().getEncryptionDeviceAccessLevel() != (securityPolicy / 16)) {
//            throw DataParseException.ioException(new ProtocolException(
//                    "Security mismatch: received incoming event push notification encrypted with security policy " + (securityPolicy / 16) + ", but device in EIServer is configured with security level " + getSecurityPropertySet().getEncryptionDeviceAccessLevel()));
//        }

        SecurityContext securityContext = getSecurityContext();
        securityContext.setResponseSystemTitle(systemTitle);

        ByteBuffer decryptedFrame;
        byte[] cipherFrame = new byte[inboundFrame.remaining()];
        inboundFrame.get(cipherFrame);
        byte[] fullCipherFrame = ProtocolTools.concatByteArrays(new byte[]{(byte) 0x00, (byte) remainingLength, (byte) securityPolicy}, cipherFrame);
        try {
            decryptedFrame = ByteBuffer.wrap(SecurityContextV2EncryptionHandler.dataTransportDecryption(securityContext, fullCipherFrame));
        } catch (DLMSConnectionException e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_PROTOCOL_ERROR, new IOException(e));
        }
        byte plainTag = decryptedFrame.get();
        if (plainTag != getCosemEventNotificationAPDUTag()) {
            throw DataParseException.ioException(new ProtocolException("Unexpected tag after decrypting an incoming event push notification: " + plainTag + ", expected " + getCosemEventNotificationAPDUTag()));
        }

        parseAPDU(decryptedFrame);
    }

    public DeviceProtocolSecurityPropertySet getSecurityPropertySet() {
        if (securityPropertySet == null) {
            List<SecurityProperty> securityProperties = context.getDeviceProtocolSecurityProperties(deviceIdentifier);
            if (securityProperties != null && !securityProperties.isEmpty()) {
                this.securityPropertySet = new DeviceProtocolSecurityPropertySet() {
                    @Override
                    public int getAuthenticationDeviceAccessLevel() {
                        return securityProperties.size()> 0?securityProperties.get(0).getAuthenticationDeviceAccessLevel().getId():0;
                    }

                    @Override
                    public int getEncryptionDeviceAccessLevel() {
                        return securityProperties.size()> 0?securityProperties.get(0).getAuthenticationDeviceAccessLevel().getId():0;
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        TypedProperties properties = TypedProperties.empty();
                        securityProperties.stream().forEach(securityProperty -> properties.setProperty(securityProperty.getName(), securityProperty.getValue()));
                        return properties;
                    }
                };
            } else {
                throw new CommunicationException(MessageSeeds.COMMAND_NOT_SUPPORTED, deviceIdentifier);
            }
        }
        return this.securityPropertySet;
    }

    protected SecurityContext getSecurityContext() {
        DlmsProperties securityProperties = getNewInstanceOfProperties();
        securityProperties.setSecurityPropertySet(getSecurityPropertySet());
        securityProperties.addProperties(getSecurityPropertySet().getSecurityProperties());

        VoidComChannel dummyComChannel = new VoidComChannel();    //Dummy channel, no bytes will be read/written
        TypedProperties comChannelProperties = TypedProperties.empty();
        comChannelProperties.setProperty(ComChannelType.TYPE, ComChannelType.SOCKET_COM_CHANNEL.getType());
        dummyComChannel.addProperties(comChannelProperties);

        DlmsSession dlmsSession = new DlmsSession(dummyComChannel, securityProperties);
        SecurityContext securityContext = dlmsSession.getAso().getSecurityContext();
        securityContext.getSecurityProvider().setRespondingFrameCounterHandling(new DefaultRespondingFrameCounterHandler());
        return securityContext;
    }

    protected byte getCosemEventNotificationAPDUTag() {
        return DLMSCOSEMGlobals.COSEM_EVENTNOTIFICATIONRESUEST;
    }

    protected byte getCosemDataNotificationAPDUTag() {
        return DLMSCOSEMGlobals.COSEM_DATANOTIFICATIONREQUEST;
    }

    protected void parseRegisters(Structure structure) {
        while (structure.hasMoreElements()) {
            AbstractDataType logicalName = structure.getNextDataType();
            if (!(logicalName instanceof OctetString)) {
                throw DataParseException.ioException(new ProtocolException("Failed to parse the register data from the Data-notification body: Expected an element of type OctetString (~ the logical name of the object), but was an element of type '" + logicalName.getClass().getSimpleName() + "'"));
            }
            AbstractDataType valueData = structure.getNextDataType();
            AbstractDataType scalerUnit = null;
            AbstractDataType eventDate = null;
            if (structure.hasMoreElements() && structure.peekAtNextDataType().isStructure()) {
                scalerUnit = structure.getNextDataType();

                if (structure.hasMoreElements()
                        && structure.peekAtNextDataType().isOctetString()
                        && structure.peekAtNextDataType().getOctetString().getOctetStr().length == AXDRDateTime.SIZE) {
                    eventDate = structure.getNextDataType();
                }
            }

            parseRegisterData(logicalName, valueData, scalerUnit, eventDate);
        }
    }

    private void parseRegisterData(AbstractDataType logicalNameData, AbstractDataType valueData, AbstractDataType scalerUnitData, AbstractDataType eventTimeData) {
        try {
            long value = 0;
            String text = null;
            ScalerUnit scalerUnit = null;
            Date eventTime = null;
            ObisCode obisCode = ObisCode.fromByteArray(((OctetString) logicalNameData).getOctetStr());

            if (valueData.isOctetString()) {
                text = valueData.getOctetString().stringValue();
            } else {
                value = DLMSUtils.parseValue2long(valueData.getBEREncodedByteArray());
            }

            if (scalerUnitData != null) {
                scalerUnit = new ScalerUnit(((Structure) scalerUnitData));
            }

            if (eventTimeData != null) {
                eventTime = parseDateTime((OctetString) eventTimeData);
            }

            addCollectedRegister(obisCode, value, scalerUnit, eventTime, text);
        } catch (IndexOutOfBoundsException | ProtocolException e) {
            throw DataParseException.ioException(new ProtocolException(e, "Failed to parse the register data from the Data-notification body: " + e.getMessage()));
        }
    }

    protected void addCollectedRegister(ObisCode obisCode, long value, ScalerUnit scalerUnit, Date eventTime, String text) {
        CollectedRegister deviceRegister = this.collectedDataFactory.createDefaultCollectedRegister(
                new RegisterDataIdentifierByObisCodeAndDevice(obisCode, obisCode, getDeviceIdentifier()), null);

        if (text == null) {
            deviceRegister.setCollectedData(new Quantity(value, scalerUnit != null ? scalerUnit.getEisUnit() : Unit.getUndefined()));
        } else {
            deviceRegister.setCollectedData(text);
        }

        deviceRegister.setCollectedTimeStamps(Instant.now(), null, Instant.now(), eventTime.toInstant());
        getCollectedRegisters().addCollectedRegister(deviceRegister);
    }

    protected Date parseDateTime(OctetString octetString) {
        try {
            return new AXDRDateTime(octetString.getBEREncodedByteArray(), 0, TimeZone.getDefault()).getValue().getTime(); // Make sure to pass device TimeZone, as deviation info is unspecified
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }
    }


    public CollectedRegisterList getCollectedRegisters() {
        if (this.collectedRegisters == null) {
            this.collectedRegisters = this.collectedDataFactory.createCollectedRegisterList(getDeviceIdentifier());
        }
        return this.collectedRegisters;
    }

    protected ComChannel getComChannel() {
        return comChannel;
    }

}