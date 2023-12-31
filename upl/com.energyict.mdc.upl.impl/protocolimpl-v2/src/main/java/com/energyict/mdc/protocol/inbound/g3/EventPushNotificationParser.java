/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.DeviceIdentifierBySystemTitle;
import com.energyict.mdc.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.mdc.identifiers.LogBookIdentifierByObisCodeAndDevice;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.event.PowerUp;
import com.energyict.mdc.protocol.inbound.idis.DataPushNotificationParser;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityContextV2EncryptionHandler;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.EventPushNotificationConfig;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.idis.am540.events.MeterAlarmParser;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks.Beacon3100StandardEventLog;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;

public class EventPushNotificationParser extends DataPushNotificationParser {

    /**
     * The default obiscode of the logbook to store the received events in
     */
    private static final ObisCode DEFAULT_OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    private static final ObisCode ALARM_EVENTOBISCODE = ObisCode.fromString("0.0.97.98.20.255");
    private static final ObisCode ALARM_1_EVENTOBISCODE = ObisCode.fromString("1.0.0.97.98.20");
    private static final ObisCode ALARM_2_EVENTOBISCODE = ObisCode.fromString("0.0.97.98.0.255");
    private static final ObisCode EVENT_NOTIFICATION_OBISCODE = ObisCode.fromString("0.0.128.0.12.255");

    private static final int EVENT_NOTIFICATION_ATTRIBUTE_NUMBER = 2;
    private static final int LAST_EVENT_ATTRIBUTE_NUMBER = 3;

    private static final String IP_ADDRESS_PROPERTY_NAME = "host";
    private static final String PORT_NUMBER_PROPERTY_NAME = "portNumber";

    private static final int DROP = 0;
    private static final int PASSTHROUGH = 1;
    private static final int ADD_ORIGIN_HEADER = 2;
    private static final int WRAP_AS_SERVER_EVENT = 3;
    private static final int RELAYED_EVENT = 4;
    private static final int INTERNAL_EVENT = 5;
    //private static final int EVENT_NOTIFICATION = 6;

    private static final byte TAG_EVENT_NOTIFICATION_REQUEST = (byte) (194);
    private static final String GATEWAY_LOGICAL_DEVICE_PREFIX = "ELS-UGW-";
    private static final int MAC_ADDRESS_LENGTH = 8;
    private static final int ATTRIBUTE_UNKNOWN = 255;
    private static final String UTF_8 = "UTF-8";

    protected ObisCode logbookObisCode;
    protected CollectedLogBook collectedLogBook;
    private List<CollectedDeviceInfo> collectedDeviceInfoList;
    private ComChannel comChannel;
    private DeviceProtocolSecurityPropertySet securityPropertySet;
    private DeviceIdentifier originDeviceId;
    private int sourceSAP = 0;
    private int destinationSAP = 0;
    private int notificationType = 0;

    public EventPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        super(comChannel, context);
        this.comChannel = comChannel;
        this.inboundDAO = context.getInboundDAO();
        this.logbookObisCode = DEFAULT_OBIS_STANDARD_EVENT_LOG;
    }

    public EventPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context, ObisCode logbookObisCode) {
        super(comChannel, context);
        this.comChannel = comChannel;
        this.inboundDAO = context.getInboundDAO();
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
     * - ded-event-notification-request (tag 0xD2 DED_EVENTNOTIFICATION_REQUEST) because we don't have a dedicated session key available
     * - glo-event-notification-request (tag 0xCA GLO_EVENTNOTIFICATION_REQUEST) because it does not contain a system-title to identify the device
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
            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
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
            throw ConnectionCommunicationException.unExpectedProtocolError(new NestedIOException(e));
        }
        deviceIdentifier = getDeviceIdentifierBasedOnSystemTitle(securityContext.getResponseSystemTitle());

        //Now parse the resulting APDU again, it could be a plain or a ciphered APDU.
        readAndParseInboundFrame(decryptedFrame);
    }

    private void parseGeneralSigningFrame(ByteBuffer inboundFrame) {
        byte[] systemTitle = initializeDeviceIdentifier(inboundFrame.asReadOnlyBuffer());
        SecurityContext securityContext = getSecurityContext();
        securityContext.setResponseSystemTitle(systemTitle);

        byte[] remaining = new byte[inboundFrame.remaining()];
        inboundFrame.get(remaining);
        byte[] generalSignedResponse = ProtocolTools.concatByteArrays(new byte[]{DLMSCOSEMGlobals.GENERAL_SIGNING}, remaining);

        ByteBuffer decryptedFrame = ByteBuffer.wrap(SecurityContextV2EncryptionHandler.unwrapGeneralSigning(securityContext, generalSignedResponse));
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
        short classId;
        byte[] obisCodeBytes;
        int attributeNumber;
        ObisCode obisCode;
        byte attributeValue;
        Date dateTime = null;

        // Check notification type by source SAP
        if (getNotificatioType() == INTERNAL_EVENT || getNotificatioType() == RELAYED_EVENT) {
            byte dateLength = inboundFrame.get();
            byte[] octetString = new byte[dateLength];
            inboundFrame.get(octetString);
            /*dateTime = parseDateTime(new OctetString(octetString));*/

            classId = inboundFrame.getShort();
            if ((classId != DLMSClassId.EVENT_NOTIFICATION.getClassId()) &&
                    (classId != DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP.getClassId()) &&
                    (classId != DLMSClassId.DATA.getClassId())) // EVN uses
            {
                throw DataParseException.ioException(new ProtocolException("Expected push event notification from object with class ID '" + DLMSClassId.EVENT_NOTIFICATION.getClassId() + "' but was '" + classId + "'"));
            }
            obisCodeBytes = new byte[6];
            inboundFrame.get(obisCodeBytes);
            obisCode = ObisCode.fromByteArray(obisCodeBytes);
            attributeNumber = inboundFrame.get() & 0xFF;
            validateCosemAttributeDescriptorOriginatingFromGateway(classId, obisCode, attributeNumber);
        } else {
            classId = inboundFrame.getShort();

            obisCodeBytes = new byte[6];
            inboundFrame.get(obisCodeBytes);
            obisCode = ObisCode.fromByteArray(obisCodeBytes);

            attributeNumber = inboundFrame.get() & 0xFF;
            attributeValue = inboundFrame.get();
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
        } else if (nrOfDataTypes == 3) {
            //This is the case for the Beacon gateway/DC with firmware >= 1.4.0
            parseNotificationWith3Elements(structure, classId, obisCode, attributeNumber);
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected a structure with 3, 4 or 5 elements, but received a structure with " + nrOfDataTypes + " element(s)"));
        }
    }

    /**
     * It will initialize the deviceIdentifier object based on the system title then return the system title
     *
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
     * STRUCTURE {
     * Equipment-Identifier  OCTET STRING,  (RTU serial number)
     * Logical-Device-Id     Unsigned16     (Originating logical device (fixed to 1))
     * Event-Payload ::= STRUCTURE {
     * TimeStamp         COSEM DATE TIME   ( Timestamp of event )
     * Event-Code        Unsigned16        ( Event code )
     * Device-Code       Unsigned16        ( Device code )
     * Event-Message     OCTET-STRING      ( Additional message )
     * }
     * }
     */
    protected void parsePlainDataAPDU(ByteBuffer inboundFrame) {

        /* long-invoke-id-and-priority*/
        byte[] invokeIdAndPriority = new byte[4];   // 32-bits long format used
        inboundFrame.get(invokeIdAndPriority);

        /* date-time (length and value)*/
        int dateTimeAxdrLength = DLMSUtils.getAXDRLength(inboundFrame.array(), inboundFrame.position());
        inboundFrame.get(new byte[DLMSUtils.getAXDRLengthOffset(dateTimeAxdrLength)]); // Increment ByteBuffer position
        byte[] octetString = new byte[dateTimeAxdrLength];
        inboundFrame.get(octetString);
        Date dateTime = parseDateTime(new OctetString(octetString));

        /* notification-body*/
        Structure structure = null;
        try {
            structure = AXDRDecoder.decode(inboundFrame.array(), inboundFrame.position(), 1, Structure.class);
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }

        int nrOfDataTypes = structure.nrOfDataTypes();

        if (nrOfDataTypes != 3) {
            throw DataParseException.ioException(new ProtocolException("Expected a structure with 3 elements, but received a structure with " + nrOfDataTypes + " element(s)"));
        }

        parseWrappedMeterEvent(structure, dateTime);
    }

    private boolean isPowerUpWithJsonPayload(int eventCode, String eventDescription) {
        return eventCode == 2
                && eventDescription.startsWith("{")
                && eventDescription.endsWith("}")
                && eventDescription.contains("transport")
                && eventDescription.contains("address")
                && eventDescription.contains("port");
    }

    private void parseWrappedMeterEvent(Structure structure, Date dateTime) {
        OctetString equipmentIdentifier = structure.getDataType(0).getOctetString();
        if (equipmentIdentifier == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (equipment identifier) to be of type OctetString"));
        }
        deviceIdentifier = new DeviceIdentifierBySerialNumber(equipmentIdentifier.stringValue());

        Unsigned16 logicalDeviceId = structure.getDataType(1).getUnsigned16();
        if (logicalDeviceId == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (logical device id) to be of type Unsigned16"));
        }
        Structure eventPayload = structure.getDataType(2).getStructure();
        if (eventPayload.nrOfDataTypes() == 4) {
            if (getNotificatioType() == INTERNAL_EVENT) {
                Date dateTime1 = parseDateTime(eventPayload.getDataType(0).getOctetString());
                Unsigned16 eventCode = eventPayload.getDataType(1).getUnsigned16();
                Unsigned16 deviceCode = eventPayload.getDataType(2).getUnsigned16();
                String description = parseDescriptionFromOctetString(eventPayload.getDataType(3).getOctetString());
                createCollectedLogBook(dateTime1, eventCode.getValue(), deviceCode.getValue(), description);

                if (isPowerUpWithJsonPayload(eventCode.getValue(), description)) {
                    try {
                        PowerUp event = PowerUp.fromJson(description);
                        collectedDeviceInfoList = new ArrayList<>();
                        Map<String, Object> properties = new HashMap<>();
                        properties.put(IP_ADDRESS_PROPERTY_NAME, event.address);
                        properties.put(PORT_NUMBER_PROPERTY_NAME, event.port);
                        collectedDeviceInfoList.add(getCollectedDataFactory().createDeviceConnectionProperties(deviceIdentifier, properties));
                        //this.getCollectedDataFactory().createDeviceConnectionProperty(deviceIdentifier, event.transport.toString(), "TRANSPORT?");
                    } catch (final IOException ex) {
                        throw DataParseException.ioException(new ProtocolException("Received POWER_UP event but failed parsing payload " + description));
                    }
                }
            } else {
                AbstractDataType dataType = eventPayload.getNextDataType();
                if (dataType instanceof OctetString) {
                    ObisCode obisCode = ObisCode.fromByteArray(((OctetString) dataType).getOctetStr());
                    if (!obisCode.equalsIgnoreBChannel(EventPushNotificationConfig.getDefaultObisCode())) {
                        throw DataParseException.ioException(new ProtocolException("The first element of the Data-notification body should contain the obiscode of the Push Setup IC, but was unexpected obis '" + obisCode
                                .toString() + "'"));
                    }
                } else {
                    throw DataParseException.ioException(new ProtocolException("The first element of the Data-notification body should contain the obiscode of the Push Setup IC, but was an element of type '" + dataType
                            .getClass()
                            .getSimpleName() + "'"));
                }

                dataType = eventPayload.getNextDataType();
                if (dataType instanceof Structure) {
                    parseRegisters(eventPayload);
                } else if (dataType instanceof OctetString) {
                    OctetString originalAPDU = OctetString.fromByteArray(((OctetString) dataType).getOctetStr());
                }
            }
        } else if (eventPayload.nrOfDataTypes() == 6) { // WRAP_AS_SERVER_EVENT event notification
            parseNotificationWith6Elements(eventPayload);
        } else if (eventPayload.nrOfDataTypes() == 7) { // WRAP_AS_SERVER_EVENT data notification
            parseNotificationWith7Elements(eventPayload);
        } else if (eventPayload.nrOfDataTypes() == 2) {
            parseNotificationWith2Elements(eventPayload);
        }
    }

    private void parseNotificationWith2Elements(Structure eventPayload) {
        OctetString equipmentIdentifier = eventPayload.getDataType(0).getOctetString();
        if (equipmentIdentifier == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (equipment identifier) to be of type OctetString"));
        }

        originDeviceId = getOriginDeviceIdentification(equipmentIdentifier);

        log("Frame is relayed from " + originDeviceId);

        if (eventPayload.peekAtNextDataType().isOctetString()) {
            OctetString relayedFrame = eventPayload.getDataType(1).getOctetString();
            parseFrame(ByteBuffer.wrap(relayedFrame.toByteArray()), false, originDeviceId);
        } else if (eventPayload.peekAtNextDataType().isStructure()) {
            Structure s1 = eventPayload.getNextDataType().getStructure();
            Array array = s1.getDataType(0).getArray();

            OctetString payload = array.getDataType(0).getOctetString();

            createCollectedLogBook(new Date(), 0, 0, payload.toString());
        } else {
            parseEvent(eventPayload);
        }
    }

    @Override
    protected void parseEncryptedFrame(ByteBuffer inboundFrame, DeviceIdentifier originDeviceIdentifier) {
        ByteBuffer decryptedFrame = getDecryptedPayload(inboundFrame, originDeviceIdentifier);
        byte plainTag = decryptedFrame.get();
        if (plainTag == DLMSCOSEMGlobals.COSEM_DATANOTIFICATIONREQUEST) {
            parseDataNotificationAPDU(decryptedFrame);
        } else {
            super.parseEncryptedFrame(inboundFrame, originDeviceIdentifier);
        }
    }


    @Override
    protected void parseNotificationBody(Structure structure) {
        int nrOfDataTypes = structure.nrOfDataTypes();

        if (nrOfDataTypes == 3) {
            Date dateTime = parseDateTime(structure.getDataType(0).getOctetString(), getDeviceTimeZone(originDeviceId));
            Unsigned32 eventCode = structure.getDataType(1).getUnsigned32();
            Unsigned32 deviceCode = structure.getDataType(2).getUnsigned32();
            String description = "Event code=" + eventCode.getValue() + ", deviceCode=" + deviceCode.getValue() +
                    ", relayed by " + deviceIdentifier + ", from " + originDeviceId + " at " + dateTime;
            log(description);
            logbookObisCode = DEFAULT_OBIS_STANDARD_EVENT_LOG;

            // force running of inbound task on the meter, not gateway
            deviceIdentifier = originDeviceId;

            createCollectedLogBook(MeterAlarmParser.parseAlarmCode(dateTime, eventCode.getValue(), getAlarmRegister(ALARM_EVENTOBISCODE)));

        }
    }

    private DeviceIdentifier getOriginDeviceIdentification(OctetString equipmentIdentifier) {
        byte[] logicalDeviceNameBytes = equipmentIdentifier.getOctetStr();

        if (hasLogicalDevicePrefix(logicalDeviceNameBytes, GATEWAY_LOGICAL_DEVICE_PREFIX)) {
            byte[] logicalNameMacBytes = ProtocolTools.getSubArray(logicalDeviceNameBytes, GATEWAY_LOGICAL_DEVICE_PREFIX.length(), GATEWAY_LOGICAL_DEVICE_PREFIX.length() + MAC_ADDRESS_LENGTH);
            final String macAddress = ProtocolTools.getHexStringFromBytes(logicalNameMacBytes, "");
            return new DialHomeIdDeviceIdentifier(macAddress);
        } else {
            return new DeviceIdentifierBySerialNumber(equipmentIdentifier.stringValue());
        }
    }

    private boolean hasLogicalDevicePrefix(byte[] logicalDeviceNameBytes, String expectedPrefix) {
        byte[] actualPrefixBytes = ProtocolTools.getSubArray(logicalDeviceNameBytes, 0, expectedPrefix.length());
        String actualPrefix = new String(actualPrefixBytes, Charset.forName(UTF_8));
        return actualPrefix.equals(expectedPrefix);
    }

    private void parseNotificationWith6Elements(Structure eventPayload) {
        OctetString equipmentIdentifier = eventPayload.getDataType(0).getOctetString();
        if (equipmentIdentifier == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (equipment identifier) to be of type OctetString"));
        }
        originDeviceId = new DeviceIdentifierBySerialNumber(equipmentIdentifier.stringValue());

        if (eventPayload.peekAtNextDataType().isOctetString()) {
            OctetString originDeviceIdData = eventPayload.getDataType(1).getOctetString();
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected the second element(Origin Device ID) of the received structure to be of type OctetString"));
        }

        if (eventPayload.peekAtNextDataType().isUnsigned16()) {
            Unsigned16 originalClientID = eventPayload.getDataType(2).getUnsigned16();
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected the third element(Client ID of the original event) of the received structure to be of type OctetString"));
        }

        if (eventPayload.peekAtNextDataType().isUnsigned8()) {
            Unsigned8 originalSecurityControlField = eventPayload.getDataType(3).getUnsigned8();
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected the forth element(Security control field) of the received structure to be of type OctetString"));
        }

        if (eventPayload.peekAtNextDataType().isTypeEnum()) {
            TypeEnum originalType = eventPayload.getDataType(4).getTypeEnum();
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected the fifth element(Original event type) of the received structure to be of type OctetString"));
        }

        // data notification
        if (eventPayload.peekAtNextDataType().isStructure()) {
            Structure eventHeaderAndBody = eventPayload.getDataType(5).getStructure();
            if (eventHeaderAndBody.peekAtNextDataType().isStructure()) {
                Structure eventBody = eventHeaderAndBody.getDataType(0).getStructure();

                // Cosem-Attribute-Descriptor
                short classId = (short) eventBody.getDataType(0).getUnsigned16().getValue();
                if (classId != DLMSClassId.DATA.getClassId()) {
                    throw DataParseException.ioException(new ProtocolException("Expected DATA notification from object with class ID '" + DLMSClassId.EVENT_NOTIFICATION.getClassId() + "' but was '" + classId + "'"));
                }

                AbstractDataType obisCodeBytes = eventBody.getNextDataType();
                ObisCode obisCodeDescriptor = ObisCode.fromByteArray(((OctetString) obisCodeBytes).getOctetStr());

                AbstractDataType attributeNumberData = eventBody.getNextDataType();
                int attributeNumber = attributeNumberData.getInteger8().getValue();

                // ClassId_1 Data
                if (eventHeaderAndBody.peekAtNextDataType().isOctetString()) {
                    // Logical Name(OBIS Code) + data value
                    parseRegisters(eventHeaderAndBody);
                } else if (eventHeaderAndBody.peekAtNextDataType().isNumerical()) {
                    // just data value
                    AbstractDataType dataType = eventHeaderAndBody.getNextDataType();
                    long value = dataType.getUnsigned32().getValue();
                    Date dateTime = Calendar.getInstance().getTime();
                    /*addCollectedRegister(obisCode, value, null, dateTime, null);*/
                }
            }
        }
    }

    private void parseNotificationWith7Elements(Structure eventPayload) {
        OctetString equipmentIdentifier = eventPayload.getDataType(0).getOctetString();
        if (equipmentIdentifier == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure (equipment identifier) to be of type OctetString"));
        }
        originDeviceId = new DeviceIdentifierBySerialNumber(equipmentIdentifier.stringValue());

        if (eventPayload.peekAtNextDataType().isOctetString()) {
            OctetString originDeviceIdData = eventPayload.getDataType(1).getOctetString();
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected the second element(Origin Device ID) of the received structure to be of type OctetString"));
        }

        if (eventPayload.peekAtNextDataType().isUnsigned16()) {
            Unsigned16 originalClientID = eventPayload.getDataType(2).getUnsigned16();
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected the third element(Client ID of the original event) of the received structure to be of type OctetString"));
        }

        if (eventPayload.peekAtNextDataType().isUnsigned16()) {
            Unsigned8 originalSecurityControlField = eventPayload.getDataType(3).getUnsigned8();
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected the forth element(Security control field) of the received structure to be of type OctetString"));
        }

        if (eventPayload.peekAtNextDataType().isTypeEnum()) {
            TypeEnum originalType = eventPayload.getDataType(4).getTypeEnum();
        } else {
            throw DataParseException.ioException(new ProtocolException("Expected the fifth element(Original event type) of the received structure to be of type OctetString"));
        }

        // data notification
        if (eventPayload.peekAtNextDataType().isOctetString()) {
            OctetString dataBlob = eventPayload.getDataType(5).getOctetString();
            if (eventPayload.peekAtNextDataType().isStructure()) {
                Structure notificationBody = eventPayload.getDataType(6).getStructure();
                Unsigned32 originalInvokeId = notificationBody.getDataType(0).getUnsigned32();

                // ClassId_1 Data
                AbstractDataType obisCodeBytes = notificationBody.getNextDataType();
                ObisCode dataObisCode = ObisCode.fromByteArray(((OctetString) obisCodeBytes).getOctetStr());
                AbstractDataType valueData = notificationBody.getNextDataType();
                long value = 0;
                String text = null;

                try {
                    if (valueData.isOctetString()) {
                        text = valueData.getOctetString().stringValue();
                    } else if (valueData.isNumerical()) {
                        value = DLMSUtils.parseValue2long(valueData.getBEREncodedByteArray());
                    }
                    Date dateTime = Calendar.getInstance().getTime();
                    addCollectedRegister(dataObisCode, value, null, dateTime, text);
                } catch (IndexOutOfBoundsException | ProtocolException e) {
                    throw DataParseException.ioException(new ProtocolException(e, "Failed to parse the register data from the Data-notification body: " + e.getMessage()));
                }
            }
        }
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
        int numberOfElements = structure.nrOfDataTypes();

        if (numberOfElements == 2) {
            Date dateTime = new Date();
            Unsigned32 protocolCode = structure.getDataType(1).getUnsigned32();
            logbookObisCode = DEFAULT_OBIS_STANDARD_EVENT_LOG;
            createCollectedLogBook(MeterAlarmParser.parseAlarmCode(dateTime, protocolCode.getValue(), getAlarmRegister(ALARM_EVENTOBISCODE)));
        } else {
            Date dateTime = parseDateTime(structure);
            int eiCode = structure.getDataType(2).intValue();
            int protocolCode = structure.getDataType(3).intValue();
            String description = parseDescription(structure);
            createCollectedLogBook(dateTime, eiCode, protocolCode, description);
        }
    }

    protected DeviceIdentifier getDeviceIdentifierBasedOnSystemTitle(byte[] systemTitle) {
        String serverSystemTitle = null;
        byte[] mc = ProtocolTools.getSubArray(systemTitle, 0, 3);
        String manufacturerCode = ProtocolTools.getAsciiFromBytes(mc);
        byte[] remainingBytes = ProtocolTools.getSubArray(systemTitle, 3);
        String remainingData = ProtocolTools.getHexStringFromBytes(remainingBytes, "");
        serverSystemTitle = manufacturerCode + remainingData;
        return new DeviceIdentifierBySystemTitle(serverSystemTitle);
    }

    protected DlmsProperties getNewInstanceOfProperties() {
        return new G3GatewayProperties();
    }

    protected Boolean getInboundComTaskOnHold() {
        return getContext()
                .isInboundOnHold(deviceIdentifier)
                .orElse(Boolean.FALSE); // Avoid NPE down the line that may naively unbox in if-then constructs
    }

    public DeviceProtocolSecurityPropertySet getSecurityPropertySet() {
        if (securityPropertySet == null) {
            this.securityPropertySet = getContext()
                    .getDeviceProtocolSecurityPropertySet(deviceIdentifier)
                    .orElseThrow(() -> CommunicationException.notConfiguredForInboundCommunication(deviceIdentifier));
        }
        return this.securityPropertySet;
    }

    public ByteBuffer readInboundFrame() {
        byte[] header = new byte[8];
        getComChannel().startReading();
        final int readBytes = getComChannel().read(header);

        Supplier<String> message2 = () -> "Received frame header [" + readBytes + "]: " + ProtocolTools.getHexStringFromBytes(header);
        getContext().getLogger().info(message2);

        if (readBytes != 8) {
            throw DataParseException.ioException(new ProtocolException("Attempted to read out 8 header bytes but received " + readBytes + " bytes instead..."));
        }

        setSourceSAP(ProtocolTools.getIntFromBytes(header, 2, 2));
        setDestinationSAP(ProtocolTools.getIntFromBytes(header, 4, 2));
        Supplier<String> message1 = () -> " - sourceSAP=" + getSourceSAP() + ", destinationSAP:" + getDestinationSAP();
        getContext().getLogger().info(message1);
        if (getSourceSAP() == 1) {
            setNotificatioType(INTERNAL_EVENT);
            Supplier<String> message = () -> " - this frame is an internal event";
            getContext().getLogger().info(message);
        } else {
            setNotificatioType(RELAYED_EVENT);
            Supplier<String> message = () -> " - this frame is relayed";
            getContext().getLogger().info(message);
        }

        int length = ProtocolTools.getIntFromBytes(header, 6, 2);

        byte[] frame = new byte[length];
        final int moreReadBytes = getComChannel().read(frame);

        Supplier<String> message = () -> "Received frame [" + moreReadBytes + "]: " + ProtocolTools.getHexStringFromBytes(frame);
        getContext().getLogger().info(message);

        if (moreReadBytes != length) {
            throw DataParseException.ioException(new ProtocolException("Attempted to read out full frame (" + length + " bytes), but received " + moreReadBytes + " bytes instead..."));
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
            if (classId != DLMSClassId.DATA.getClassId()) {
                if (classId != DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP.getClassId()) {
                    throw DataParseException.ioException(new ProtocolException("Expected push event notification from object with class ID '" + DLMSClassId.EVENT_NOTIFICATION.getClassId() +
                            "' or with classId '" + DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP.getClassId() +
                            "' or with classId '" + DLMSClassId.DATA.getClassId() +
                            "' but was '" + classId + "'"));
                }
            }
        }

        if (attributeNumber != ATTRIBUTE_UNKNOWN) {
            if (attributeNumber != LAST_EVENT_ATTRIBUTE_NUMBER) {
                if (attributeNumber != EVENT_NOTIFICATION_ATTRIBUTE_NUMBER) {
                    throw DataParseException.ioException(new ProtocolException("Expected push event notification attribute '" + LAST_EVENT_ATTRIBUTE_NUMBER + "' or '" + EVENT_NOTIFICATION_ATTRIBUTE_NUMBER + "' but was '" + attributeNumber + "'"));
                }
            }
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
        Supplier<String> message = () -> " - this notification is relayed by " + deviceIdentifier.toString();
        getContext().getLogger().info(message);
        if (getNotificatioType() == RELAYED_EVENT) {
            Unsigned16 logicalDeviceId = eventWrapper.getDataType(1).getUnsigned16();
            if (logicalDeviceId == null) {
                throw DataParseException.ioException(new ProtocolException("Expected the second element of the received structure (logical device id) to be of type Unsigned16"));
            }
        }

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
            Date dateTime1 = parseDateTime(eventPayLoad.getDataType(0).getOctetString());
            int eiCode = eventPayLoad.getDataType(1).intValue();
            int protocolCode = eventPayLoad.getDataType(2).intValue();
            String description = parseDescription(eventPayLoad.getDataType(3).getOctetString());
            createCollectedLogBook(dateTime1, eiCode, protocolCode, description);
        } else if (nrOfDataTypes == 6) { // WRAP_AS_SERVER_EVENT event notification
            parseNotificationWith6Elements(eventPayLoad);
        } else if (nrOfDataTypes == 7) { // WRAP_AS_SERVER_EVENT data notification
            parseNotificationWith7Elements(eventPayLoad);
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
            throw DataParseException.ioException(new ProtocolException("Expected the second element of the received structure (logical device name) to be of type OctetString"));
        }
        if (logicalDeviceId == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the third element of the received structure (logical device id) to be of type Unsigned16"));
        }

        Date dateTime = Calendar.getInstance().getTime();
        deviceIdentifier = new DialHomeIdDeviceIdentifier(logicalDeviceName.toString());

        createCollectedLogBook(MeterAlarmParser.parseAlarmCode(dateTime, attributeValue.getValue(), 1));
    }

    private void parseGatewayRelayedEventWith2Elements(Structure eventPayLoad, int alarmRegister) {
        OctetString logicalDeviceName = eventPayLoad.getDataType(0).getOctetString();
        Unsigned32 attributeValue = eventPayLoad.getDataType(1).getUnsigned32();// alarm descriptor value

        if (alarmRegister != 0) {
            byte[] logicalDeviceNameBytes = logicalDeviceName.getOctetStr();
            byte[] logicalNameMacBytes = ProtocolTools.getSubArray(logicalDeviceNameBytes, GATEWAY_LOGICAL_DEVICE_PREFIX.length(), GATEWAY_LOGICAL_DEVICE_PREFIX.length() + MAC_ADDRESS_LENGTH);
            String macAddress = ProtocolTools.getHexStringFromBytes(logicalNameMacBytes, "");
            Supplier<String> message = () -> " - event is from device with MAC " + macAddress;
            getContext().getLogger().info(message);
            deviceIdentifier = new DialHomeIdDeviceIdentifier(macAddress);
        }
        Date dateTime = Calendar.getInstance().getTime();//TODO: see what timezone should be used
        createCollectedLogBook(MeterAlarmParser.parseAlarmCode(dateTime, attributeValue.getValue(), alarmRegister));
    }

    protected void createCollectedLogBook(List<MeterEvent> meterEvents) {
        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
        for (MeterEvent meterEvent : meterEvents) {
            meterProtocolEvents.add(MeterEvent.mapMeterEventToMeterProtocolEvent(meterEvent));
        }
        createCollectedLogBookFromProtocolEvents(meterProtocolEvents);
    }

    private void createCollectedLogBook(Date dateTime, int eiCode, int protocolCode, String description) {
        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
        MeterProtocolEvent meterProtocolEvent = MeterEvent.mapMeterEventToMeterProtocolEvent(Beacon3100StandardEventLog.buildMeterEvent(dateTime, eiCode, protocolCode, description, Beacon3100StandardEventLog.LOGBOOK_NAME));
        meterProtocolEvents.add(meterProtocolEvent);
        createCollectedLogBookFromProtocolEvents(meterProtocolEvents);
    }

    protected void createCollectedLogBookFromProtocolEvents(List<MeterProtocolEvent> meterProtocolEvents) {
        collectedLogBook = collectedDataFactory.createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, logbookObisCode));
        collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
    }

    public CollectedLogBook getCollectedLogBook() {
        return collectedLogBook;
    }

    public List<CollectedDeviceInfo> getCollectedDeviceInfoList() {
        return collectedDeviceInfoList;
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

    protected Date parseDateTime(OctetString octetString) {
        return parseDateTime(octetString, getDeviceTimeZone());
    }

    protected Date parseDateTime(OctetString octetString, TimeZone tz) {
        if (octetString == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the second element of the received structure to be an OctetString"));
        }
        try {
            return new AXDRDateTime(octetString.getBEREncodedByteArray(), 0, tz).getValue().getTime(); // Make sure to pass device TimeZone, as deviation info is unspecified
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }
    }

    private int getAlarmRegister(ObisCode obisCode) {
        if (obisCode.equals(ALARM_EVENTOBISCODE)) {
            return 1;
        } else if (obisCode.equals(ObisCode.fromString("0.0.97.98.21.255"))) {
            return 2;
        }
        return 0;
    }

    public int getSourceSAP() {
        return sourceSAP;
    }

    public void setSourceSAP(int logicalDeviceID) {
        sourceSAP = logicalDeviceID;
    }

    public int getDestinationSAP() {
        return destinationSAP;
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
