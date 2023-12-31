/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.inbound.nfc;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.g3.DummyComChannel;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

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
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.mdc.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.mdc.identifiers.LogBookIdentifierByObisCodeAndDevice;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Parser class for the NFC DataPush<br/>
 * This parser class can parse/handle a pushed DLMS Data-notification message and extract the relevant information
 */
public class NFCDataPushNotificationParser {

    CollectedRegisterList collectedRegisters;
    CollectedLogBook collectedEvents;
    private ComChannel comChannel;
    private final InboundDiscoveryContext context;
    private final CollectedDataFactory collectedDataFactory;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    protected CollectedLogBook collectedLogBook;
    protected DeviceIdentifier deviceIdentifier;

    public NFCDataPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        this.comChannel = comChannel;
        this.context = context;
        this.collectedDataFactory = context.getCollectedDataFactory();
    }

    protected InboundDiscoveryContext getContext(){
        return context;
    }

    protected void log(String message){
        log(message, Level.INFO);
    }

    protected void log(String message, Level level){
        getContext().getLogger().log(level, message);
    }

    protected DeviceIdentifier getDeviceIdentifierBasedOnSystemTitle(byte[] systemTitle) {
        String serverSystemTitle = ProtocolTools.getHexStringFromBytes(systemTitle, "");
        return new DialHomeIdDeviceIdentifier(serverSystemTitle);
    }

    protected DlmsProperties getNewInstanceOfProperties() {
        return new DlmsProperties();
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

        // 2. system title
        byte systemTitleType = inboundFrame.get();
        if (systemTitleType != 0x09) { //octet-string
            throw DataParseException.ioException(new ProtocolException("System title should be octet-string!"));
        }

        int systemTitleLength = DLMSUtils.getAXDRLength(inboundFrame.array(), inboundFrame.position());
        inboundFrame.get(new byte[DLMSUtils.getAXDRLengthOffset(systemTitleLength)]); // Increment ByteBuffer position

        byte[] systemTitle = new byte[systemTitleLength];
        inboundFrame.get(systemTitle);
        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(systemTitle);

        // 3. date-time
        byte dateTimeType = inboundFrame.get();
        if (dateTimeType != 0x09) { //octet-string
            throw DataParseException.ioException(new ProtocolException("Date/time should be octet-string!"));
        }

        int dateTimeAxdrLength = DLMSUtils.getAXDRLength(inboundFrame.array(), inboundFrame.position());
        inboundFrame.get(new byte[DLMSUtils.getAXDRLengthOffset(dateTimeAxdrLength)]); // Increment ByteBuffer position

        byte[] octetString = new byte[dateTimeAxdrLength];
        inboundFrame.get(octetString);
        Date dateTime = parseDateTime(new OctetString(octetString));

        // 4. notification-body
        Structure structure;
        try {
            structure = AXDRDecoder.decode(inboundFrame.array(), inboundFrame.position(), Structure.class);
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }

        parsePayload(structure);
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

        SecurityContext securityContext = getSecurityContext(deviceIdentifier);
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

    public DeviceProtocolSecurityPropertySet getSecurityPropertySet() {
        if (this.securityPropertySet == null) {
            this.securityPropertySet = getSecurityPropertySet(this.deviceIdentifier);
        }

        return this.securityPropertySet;
    }

    private DeviceProtocolSecurityPropertySet getSecurityPropertySet(DeviceIdentifier deviceIdentifier) {
        return context
                .getDeviceProtocolSecurityPropertySet(deviceIdentifier)
                .orElseThrow(() -> CommunicationException.notConfiguredForInboundCommunication(deviceIdentifier));
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

    protected DlmsSession getNewInstanceOfDlmsSession(DlmsProperties securityProperties, DummyComChannel dummyComChannel) {
        return new DlmsSession(dummyComChannel, securityProperties);
    }

    protected byte getCosemEventNotificationAPDUTag() {
        return DLMSCOSEMGlobals.COSEM_EVENTNOTIFICATIONRESUEST;
    }

    protected void parsePayload(Structure structure){
        if(structure.nrOfDataTypes() != 2){
            throw DataParseException.ioException(new ProtocolException("Failed to parse the register data from the Data-notification body: Expected payload of 2 structures - registers and events"));
        }

        Structure registers = structure.getNextDataType().getStructure();
        if(registers == null || registers.nrOfDataTypes() != 2){
            throw DataParseException.ioException(new ProtocolException("Failed to parse the register data from the Data-notification body: Expected register set of 2 structures - OBIS and registers"));
        }

        AbstractDataType logicalNameRegisters = registers.getNextDataType();
        if (!(logicalNameRegisters instanceof OctetString)) {
            throw DataParseException.ioException(new ProtocolException("Failed to parse the register data from the Data-notification body: Expected OBIS code for register set"));
        }
        // TODO: establish the OBIS code for data push register set and verify it here

        //parseRegisters(ObisCode.fromByteArray(((OctetString) logicalNameRegisters).getOctetStr()), registers.getNextDataType().getStructure());
        parseRegisters(registers.getNextDataType().getStructure());

        Structure events = structure.getNextDataType().getStructure();
        if(events == null || events.nrOfDataTypes() != 2){
            throw DataParseException.ioException(new ProtocolException("Failed to parse the register data from the Data-notification body: Expected event set of 2 structures - OBIS and registers"));
        }

        AbstractDataType logicalNameEvents = events.getNextDataType();
        if (!(logicalNameEvents instanceof OctetString)) {
            throw DataParseException.ioException(new ProtocolException("Failed to parse the register data from the Data-notification body: Expected OBIS code for event set"));
        }
        // TODO: establish the OBIS code for data push event set and verify it here

        parseEvents(ObisCode.fromByteArray(((OctetString) logicalNameEvents).getOctetStr()), events.getNextDataType().getStructure());
    }

    //protected void parseRegisters(ObisCode registersObis, Structure structure) {
    protected void parseRegisters(Structure structure) {
        if(structure == null){
            throw DataParseException.ioException(new ProtocolException("Failed to parse the register data from the Data-notification body: Expected list of registers"));
        }
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

    protected void parseEvents(ObisCode eventsObis, Structure structure) {
        if(structure == null){
            throw DataParseException.ioException(new ProtocolException("Failed to parse the register data from the Data-notification body: Expected list of events"));
        }

        List<MeterEvent> events = new ArrayList<>();
        while (structure.hasMoreElements()) {
            if(! (structure.peekAtNextDataType().isOctetString()
                    && structure.peekAtNextDataType().getOctetString().getOctetStr().length == AXDRDateTime.SIZE)) {
                throw DataParseException.ioException(new ProtocolException("Failed to parse the register data from the Data-notification body: Expected event timestamp"));
            }
            AbstractDataType eventDate = structure.getNextDataType();

            if(! (structure.peekAtNextDataType().isInteger16())) {
                throw DataParseException.ioException(new ProtocolException("Failed to parse the register data from the Data-notification body: Expected event code"));
            }
            AbstractDataType eventCode = structure.getNextDataType();

            getCollectedEvent(eventsObis, eventCode.intValue()&0x0000FFFF, parseDateTime((OctetString) eventDate)).ifPresent(events::add);
        }

        createCollectedLogbook(eventsObis).setCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(events));
    }

    protected void addCollectedRegister(ObisCode obisCode, long value, ScalerUnit scalerUnit, Date eventTime, String text) {
        //ReadingType readingType = this.readingTypeUtilService.getReadingTypeFrom(obisCode, scalerUnit.getEisUnit());

        CollectedRegister deviceRegister = this.collectedDataFactory.createDefaultCollectedRegister(
                new RegisterDataIdentifierByObisCodeAndDevice(obisCode, getDeviceIdentifier()));

        if (text == null) {
            deviceRegister.setCollectedData(new Quantity(value, scalerUnit != null ? scalerUnit.getEisUnit() : Unit.getUndefined()));
        } else {
            deviceRegister.setCollectedData(text);
        }

        deviceRegister.setCollectedTimeStamps(new Date(), null, new Date(), eventTime);
        getCollectedRegisters().addCollectedRegister(deviceRegister);
    }

    protected Optional<MeterEvent> getCollectedEvent(ObisCode obisCode, int eventCode, Date eventTime){
        return NFCDataPushEvent.parseEventCode(eventTime, eventCode);
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

    private CollectedLogBook createCollectedLogbook(ObisCode logbookObisCode) {
        if (this.collectedEvents == null) {
            this.collectedEvents = collectedDataFactory.createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(getDeviceIdentifier(), logbookObisCode));
        }
        return this.collectedEvents;
    }

    public CollectedLogBook getCollectedEvents(){
        return this.collectedEvents;
    }

    protected ComChannel getComChannel() {
        return comChannel;
    }

}