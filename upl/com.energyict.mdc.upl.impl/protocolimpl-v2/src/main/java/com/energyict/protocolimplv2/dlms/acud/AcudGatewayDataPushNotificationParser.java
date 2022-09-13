package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.g3.EventPushNotificationParser;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import com.energyict.cim.EndDeviceType;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;

public class AcudGatewayDataPushNotificationParser extends EventPushNotificationParser {
    private static TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT");

    private TimeZone timeZone;

    public AcudGatewayDataPushNotificationParser(TimeZone timeZone, ComChannel comChannel, InboundDiscoveryContext context) {
        super(comChannel, context);
        this.timeZone = timeZone;
    }

    public void readAndParseInboundFrame() {
        ByteBuffer inboundFrame = readInboundFrame();
        byte[] header = new byte[14];
        inboundFrame.get(header);
        String equipmentIdentifier = "SEE0002021589";
        this.deviceIdentifier = new DeviceIdentifierBySerialNumber(equipmentIdentifier);
        readAndParseInboundFrame(inboundFrame);
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public ByteBuffer readInboundFrame() {
        byte[] header = new byte[3];
        getComChannel().startReading();
        final int readBytes = getComChannel().read(header);

        Supplier<String> message2 = () -> "Received frame header [" + readBytes + "]: " + ProtocolTools.getHexStringFromBytes(header);
        getContext().getLogger().info(message2);

        if (readBytes != 3) {
            throw DataParseException.ioException(new ProtocolException("Attempted to read out 8 header bytes but received " + readBytes + " bytes instead..."));
        }

        int length = ProtocolTools.getIntFromBytes(header, 2, 1);

        byte[] frame = new byte[length-1];
        final int moreReadBytes = getComChannel().read(frame);

        Supplier<String> message = () -> "Received frame [" + moreReadBytes + "]: " + ProtocolTools.getHexStringFromBytes(frame);
        getContext().getLogger().info(message);

        if (moreReadBytes != length-1) {
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
    protected void parsePlainEventAPDU(ByteBuffer inboundFrame) {
        short classId;
        byte[] obisCodeBytes;
        int attributeNumber;
        ObisCode obisCode;
        byte attributeValue;
        Date dateTime = null;

        // Check notification type by source SAP
        /*if (getNotificatioType() == INTERNAL_EVENT || getNotificatioType() == RELAYED_EVENT) {
            byte dateLength = inboundFrame.get();
            byte[] octetString = new byte[dateLength];
            inboundFrame.get(octetString);
            *//*dateTime = parseDateTime(new OctetString(octetString));*//*

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
        } else {*/

        // get first 2 bytes (date?)
            short date = inboundFrame.get();
            classId = inboundFrame.getShort();

            obisCodeBytes = new byte[6];
            inboundFrame.get(obisCodeBytes);
            obisCode = ObisCode.fromByteArray(obisCodeBytes);

            attributeNumber = inboundFrame.get() & 0xFF;
            //attributeValue = inboundFrame.get();
        //}

        byte[] eventData = new byte[inboundFrame.remaining()];
        inboundFrame.get(eventData);

        DataContainer dataContainer = new DataContainer();
        dataContainer.parseObjectList(eventData, null);

        Structure structure;
        try {
            structure = AXDRDecoder.decode(eventData, Structure.class);
        } catch (ProtocolException e) {
            throw DataParseException.ioException(e);
        }


        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
        if (AcudElectricLogBookFactory.getStaticSupportedLogBooks().contains(obisCode)) {
            meterProtocolEvents = AcudElectricLogBookFactory.parseElectricityEvents(this.timeZone, dataContainer, obisCode, EndDeviceType.ELECTRICMETER);
        } else if (AcudGasLogBookFactory.getStaticSupportedLogBooks().contains(obisCode)) {
            meterProtocolEvents = AcudGasLogBookFactory.parseGasEvents(this.timeZone, dataContainer, obisCode, EndDeviceType.GASMETER);
        } else if (AcudWaterLogBookFactory.getStaticSupportedLogBooks().contains(obisCode)) {
            meterProtocolEvents = AcudWaterLogBookFactory.parseWaterEvents(this.timeZone, dataContainer, obisCode, EndDeviceType.WATERMETER);
        } else {
            throw DataParseException.ioException(new ProtocolException("Unsupported logbook: " + obisCode));
        }
        createCollectedLogBookFromProtocolEvents(meterProtocolEvents);
    }
}
