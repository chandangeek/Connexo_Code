package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
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
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.g3.DummyComChannel;
import com.energyict.mdc.upl.InboundDAO;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.SecurityProperty;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130Properties;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;

/**
 * Parser class for the IDIS DataPush<br/>
 * This parser class can parse/handle a pushed DLMS Data-notification message and extract the relevant information
 * - being the deviceIdentifier and some register data - out of it.
 *
 * @author sva
 * @since 13/04/2015 - 16:45
 */
public class DataPushNotificationParser {

    private static final ObisCode DEFAULT_OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode EVENT_NOTIFICATION_OBISCODE = ObisCode.fromString("0.0.128.0.12.255");
    protected final ObisCode logbookObisCode;
    private final InboundDiscoveryContext context;
    protected final CollectedDataFactory collectedDataFactory;
    public InboundDAO inboundDAO;
    protected DeviceIdentifier deviceIdentifier;
    protected CollectedLogBook collectedLogBook;
    private CollectedRegisterList collectedRegisters;
    private ComChannel comChannel;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    private TimeZone timeZone;

    public DataPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        this.comChannel = comChannel;
        this.collectedDataFactory = context.getCollectedDataFactory();
        this.inboundDAO = context.getInboundDAO();
        this.logbookObisCode = DEFAULT_OBIS_STANDARD_EVENT_LOG;
        this.context = context;
    }

    protected InboundDiscoveryContext getContext() {
        return context;
    }

    protected CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    protected void log(String message) {
        log(message, Level.INFO);
    }

    protected void log(String message, Level level) {
        getContext().getLogger().log(level, message);
    }

    protected DeviceIdentifier getDeviceIdentifierBasedOnSystemTitle(byte[] systemTitle) {
        String serverSystemTitle = ProtocolTools.getHexStringFromBytes(systemTitle, "");
        serverSystemTitle = serverSystemTitle.replace("454C53", "ELS-");      // Replace HEX 454C53 by its ASCII 'ELS'
        return new DialHomeIdDeviceIdentifier(serverSystemTitle);
    }

    protected DlmsProperties getNewInstanceOfProperties() {
        return new AM130Properties();
    }

    protected byte getCosemNotificationAPDUTag() {
        return DLMSCOSEMGlobals.COSEM_DATA_NOTIFICATION;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void parseInboundFrame() {
        ByteBuffer inboundFrame = readInboundFrame();
        parseFrame(inboundFrame, true, this.deviceIdentifier);
    }

    protected void parseFrame(ByteBuffer frame, boolean expectHeader, DeviceIdentifier originDeviceIdentified) {
        byte[] header = new byte[8];
        if (expectHeader) {
            frame.get(header);
        }
        byte tag = frame.get();
        if (tag == getCosemNotificationAPDUTag()) {
            parseAPDU(frame);
        } else if (tag == DLMSCOSEMGlobals.GENERAL_GLOBAL_CIPHERING) {
            parseEncryptedFrame(frame, originDeviceIdentified);
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


    protected void parseAPDU(ByteBuffer inboundFrame) {
        parseDataNotificationAPDU(inboundFrame);
    }

    /**
     * Data-Notification ::= SEQUENCE
     * - long-invoke-id-and-priority
     * - date-time (OCTET STRING)
     * - notification-body
     */
    protected void parseDataNotificationAPDU(ByteBuffer inboundFrame) {
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

        parseNotificationBody(structure);
    }

    protected void parseNotificationBody(Structure structure) {
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
        parseEncryptedFrame(inboundFrame, this.deviceIdentifier);
    }

    protected ByteBuffer getDecryptedPayload(ByteBuffer inboundFrame, DeviceIdentifier originDeviceIdentifier) {
        int systemTitleLength = inboundFrame.get() & 0xFF;
        byte[] systemTitle = new byte[systemTitleLength];
        inboundFrame.get(systemTitle);
        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(systemTitle);

        int remainingLength = inboundFrame.get() & 0xFF;
        int securityPolicy = inboundFrame.get() & 0xFF;

        if (getSecurityPropertySet(originDeviceIdentifier).getEncryptionDeviceAccessLevel() != (securityPolicy / 16)) {
            throw DataParseException.ioException(new ProtocolException(
                    "Security mismatch: received incoming event push notification encrypted with security policy " + (securityPolicy / 16) + ", but device in EIServer is configured with security level " + getSecurityPropertySet().getEncryptionDeviceAccessLevel()));
        }

        SecurityContext securityContext = getSecurityContext(originDeviceIdentifier);
        securityContext.setResponseSystemTitle(systemTitle);

        ByteBuffer decryptedFrame;
        byte[] cipherFrame = new byte[inboundFrame.remaining()];
        inboundFrame.get(cipherFrame);
        byte[] fullCipherFrame = ProtocolTools.concatByteArrays(new byte[]{(byte) 0x00, (byte) remainingLength, (byte) securityPolicy}, cipherFrame);
        log("Decoding ciphered frame: " + ProtocolTools.getHexStringFromBytes(fullCipherFrame));
        try {
            byte[] decryptedFrameBytes = SecurityContextV2EncryptionHandler.dataTransportDecryption(securityContext, fullCipherFrame);
            log(" > deciphered frame is: " + ProtocolTools.getHexStringFromBytes(decryptedFrameBytes));

            decryptedFrame = ByteBuffer.wrap(decryptedFrameBytes);
        } catch (DLMSConnectionException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
        }

        return decryptedFrame;
    }

    protected void parseEncryptedFrame(ByteBuffer inboundFrame, DeviceIdentifier originDeviceIdentifier) {
        ByteBuffer decryptedFrame = getDecryptedPayload(inboundFrame, originDeviceIdentifier);
        byte plainTag = decryptedFrame.get();
        if (plainTag != getCosemEventNotificationAPDUTag()) {
            throw DataParseException.ioException(new ProtocolException("Unexpected tag after decrypting an incoming event push notification: " + plainTag + ", expected " + getCosemEventNotificationAPDUTag()));
        }

        parseAPDU(decryptedFrame);
    }

    public DeviceProtocolSecurityPropertySet getSecurityPropertySet() {
        if (securityPropertySet == null) {
            getSecurityPropertySet(this.deviceIdentifier);
        }
        return this.securityPropertySet;
    }

    private DeviceProtocolSecurityPropertySet getSecurityPropertySet(DeviceIdentifier deviceIdentifier) {
        List<? extends SecurityProperty> securityProperties =
                context
                        .getProtocolSecurityProperties(deviceIdentifier)
                        .orElseThrow(() -> CommunicationException.notConfiguredForInboundCommunication(deviceIdentifier));
        if (!securityProperties.isEmpty()) {
            return new DeviceProtocolSecurityPropertySetImpl(securityProperties);
        } else {
            throw CommunicationException.notConfiguredForInboundCommunication(deviceIdentifier);
        }
    }

    protected SecurityContext getSecurityContext() {
        return getSecurityContext(this.deviceIdentifier);
    }

    protected SecurityContext getSecurityContext(DeviceIdentifier originDeviceIdentified) {
        DlmsProperties securityProperties = getNewInstanceOfProperties();
        securityProperties.setSecurityPropertySet(getSecurityPropertySet(originDeviceIdentified));
        securityProperties.addProperties(getSecurityPropertySet(originDeviceIdentified).getSecurityProperties());

        DummyComChannel dummyComChannel = new DummyComChannel();    //Dummy channel, no bytes will be read/written
        DlmsSession dlmsSession = getNewInstanceOfDlmsSession(securityProperties, dummyComChannel);
        SecurityContext securityContext = dlmsSession.getAso().getSecurityContext();
        securityContext.getSecurityProvider().setRespondingFrameCounterHandling(new DefaultRespondingFrameCounterHandler());
        return securityContext;
    }

    /**
     * Subclasses can override this implementation
     */
    protected DlmsSession getNewInstanceOfDlmsSession(DlmsProperties securityProperties, DummyComChannel dummyComChannel) {
        return new DlmsSession(dummyComChannel, securityProperties);
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
                new RegisterDataIdentifierByObisCodeAndDevice(obisCode, getDeviceIdentifier())
        );

        if (text == null) {
            deviceRegister.setCollectedData(new Quantity(value, scalerUnit != null ? scalerUnit.getEisUnit() : Unit.getUndefined()));
        } else {
            deviceRegister.setCollectedData(text);
        }

        deviceRegister.setCollectedTimeStamps(new Date(), null, new Date(), eventTime);
        getCollectedRegisters().addCollectedRegister(deviceRegister);
    }

    protected Date parseDateTime(OctetString octetString) {
        try {
            return new AXDRDateTime(octetString.getBEREncodedByteArray(), 0, getDeviceTimeZone()).getValue().getTime(); // Make sure to pass device TimeZone, as deviation info is unspecified
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }
    }

    protected TimeZone getDeviceTimeZone() {
        if (timeZone == null) {
            if (getDeviceIdentifier() == null) {
                timeZone = TimeZone.getTimeZone(DEFAULT_TIMEZONE);
            } else {
                TypedProperties deviceProtocolProperties = getInboundDAO().getDeviceProtocolProperties(getDeviceIdentifier());
                if (deviceProtocolProperties == null) {
                    timeZone = TimeZone.getTimeZone(DEFAULT_TIMEZONE);
                } else {
                    return deviceProtocolProperties.getTypedProperty(TIMEZONE, TimeZone.getTimeZone(DEFAULT_TIMEZONE));
                }
            }
        }
        return timeZone;
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

    protected InboundDAO getInboundDAO() {
        return inboundDAO;
    }
}