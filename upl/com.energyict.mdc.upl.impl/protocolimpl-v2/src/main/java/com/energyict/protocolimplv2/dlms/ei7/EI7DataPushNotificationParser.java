/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.mdc.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

public class EI7DataPushNotificationParser {
    
    private ComChannel comChannel;
    protected DeviceIdentifier deviceIdentifier;
    private final InboundDiscoveryContext context;
    private final CollectedDataFactory collectedDataFactory;
    private DeviceProtocolSecurityPropertySet securityPropertySet;

    public EI7DataPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        this.comChannel = comChannel;
        this.context = context;
        this.collectedDataFactory = context.getCollectedDataFactory();
    }

    protected InboundDiscoveryContext getContext(){
        return context;
    }

    protected DeviceIdentifier getDeviceIdentifierBasedOnSystemTitle(byte[] systemTitle) {
        String serverSystemTitle = ProtocolTools.getHexStringFromBytes(systemTitle, "");
        return new DialHomeIdDeviceIdentifier(serverSystemTitle);
    }

    public void parseInboundFrame() {
        ByteBuffer inboundFrame = readInboundFrame();
        byte[] header = new byte[8];
        inboundFrame.get(header);
        byte tag = inboundFrame.get();
        parseAPDU(inboundFrame);
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


    protected Date parseDateTime(OctetString octetString) {
        try {
            return new AXDRDateTime(octetString.getBEREncodedByteArray(), 0, TimeZone.getDefault()).getValue().getTime(); // Make sure to pass device TimeZone, as deviation info is unspecified
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }
    }

    protected void parsePayload(Structure structure){
    }

    protected ComChannel getComChannel() {
        return comChannel;
    }

    protected DlmsProperties getNewInstanceOfProperties() {
        return new DlmsProperties();
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    protected void log(String message){
        log(message, Level.INFO);
    }

    protected void log(String message, Level level){
        getContext().getLogger().log(level, message);
    }
}