/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.LogBookIdentifierByObisCodeAndDevice;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.g3.EventPushNotificationParser;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import com.energyict.cim.EndDeviceType;
import com.energyict.dlms.DataContainer;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class AcudGatewayDataPushNotificationParser extends EventPushNotificationParser {
    private DeviceIdentifierBySerialNumber slaveSerialNumberIdentifier;

    private TimeZone timeZone;

    public AcudGatewayDataPushNotificationParser(TimeZone timeZone, ComChannel comChannel, InboundDiscoveryContext context) {
        super(comChannel, context);
        this.timeZone = timeZone;
    }

    @Override
    public void readAndParseInboundFrame() {
        ByteBuffer inboundFrame = readInboundFrame();
        byte[] header = new byte[17];
        inboundFrame.get(header);

        int deviceIdentifierFieldLength = ProtocolTools.getIntFromBytes(header, 16, 1);
        byte[] deviceIdentifierField = new byte[deviceIdentifierFieldLength];
        inboundFrame.get(deviceIdentifierField);

        String fullDeviceIdentifierField = ProtocolTools.getAsciiFromBytes(deviceIdentifierField);
        String[] deviceIP = fullDeviceIdentifierField.split(",");

        this.deviceIdentifier = new DeviceIdentifierBySerialNumber(deviceIP[0]);
        this.slaveSerialNumberIdentifier = new DeviceIdentifierBySerialNumber(deviceIP[1]);
        readAndParseInboundFrame(inboundFrame);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    /**
     * [0]           -> 7E, frame start
     * [1]           -> frame type(F) + segmentation bit(S) + length upper byte(L) -> (FFFFSLLL)
     * [2]           -> length lower byte
     * [3][4][5][6]  -> Server device address ( logical + physical) 4 bytes addressing
     * [7]           -> client address (Client SAP)
     * [8]           -> Frame control byte
     * [9][10]       -> Header check sum
     * [11][12][13]                  -> LLC bytes
     * [14]                          -> Gateway request tag
     * [15]                          -> Network ID
     * [16]                          -> No of bytes in address field
     * [17][18][19][20][21][22][23][24]    -> gateway serial number
     * [25]                                -> Comma separator
     * [26][27][28][29][30][31][32][33]    -> Meter serial number
     * // Event notification (ex)
     * C2 00 00 07 00 00 63 62 05 FF 02 02 07 09 0C 07 E6 0A 17 07 17 2C 28 00 80 00 00 11 01 11 C0 12 00 03 12 00 01 12 00 00 12 00 00
     * [length-3] [length-2]         -> Frame check sum
     * [length-1]                    -> 7E, Frame end
     * @return ByteBuffer
     */
    @Override
    public ByteBuffer readInboundFrame() {
        byte[] header = new byte[3];
        getComChannel().startReading();
        final int readBytes = getComChannel().read(header);

        getContext().getLogger().info(() -> "Received header [" + readBytes + "]: " + ProtocolTools.getHexStringFromBytes(header));

        if (readBytes != 3) {
            throw DataParseException.ioException(new ProtocolException("Attempted to read out 3 bytes but received " + readBytes + " bytes instead..."));
        }

        int secondByte = ProtocolTools.getIntFromBytes(header, 1, 1);
        boolean isSegmented = ProtocolTools.isBitSet(secondByte, 3);
        if (isSegmented) {
            throw DataParseException.ioException(new ProtocolException("Segmentation bit is set, while the inbound protocol doesn't support segmentation."));
        }
        int length = ProtocolTools.getIntFromBytes(header, 2, 1);
        boolean isLengthUpperBytesUsed = ProtocolTools.isBitSet(secondByte, 2) || ProtocolTools.isBitSet(secondByte, 1) || ProtocolTools.isBitSet(secondByte, 0);
        if (isLengthUpperBytesUsed) {
            byte[] lengthBytes = new byte[2];
            lengthBytes[0] = (byte) (header[1] & 0x07);
            lengthBytes[1] = (byte) (header[2] & 0xFF);
            length = ProtocolTools.getIntFromBytes(lengthBytes);
        }

        byte[] frame = new byte[length - 1];
        final int moreReadBytes = getComChannel().read(frame);

        getContext().getLogger().info(() -> "Received frame [" + moreReadBytes + "]: " + ProtocolTools.getHexStringFromBytes(frame));

        if (moreReadBytes != length - 1) {
            throw DataParseException.ioException(new ProtocolException("Attempted to read out full frame (" + length + " bytes), but received " + moreReadBytes + " bytes instead..."));
        }
        return ByteBuffer.wrap(ProtocolTools.concatByteArrays(header, frame));
    }

    /**
     * DataNotificationRequest ::= SEQUENCE

     * Something         Unsigned8        (00)
     * Logbook-Id        9 bytes           (ClassId_2_bytes + Obis_6_bytes + Attribute_number_byte)
     * Event-Payload ::= STRUCTURE {
     * TimeStamp         COSEM DATE TIME   ( Timestamp of event )
     * Event-Code        Unsigned8         ( Event code )
     * Device-Code       Unsigned8         ( Device code  )
     *                   Unsigned16
     *                   Unsigned16
     *                   Unsigned16
     *                   Unsigned16
     * }
     */
    @Override
    protected void parsePlainEventAPDU(ByteBuffer inboundFrame) {
        short something = inboundFrame.get();
        int classId = inboundFrame.getShort();

        byte[] obisCodeBytes = new byte[6];
        inboundFrame.get(obisCodeBytes);
        logbookObisCode = ObisCode.fromByteArray(obisCodeBytes);

        int attributeNumber = inboundFrame.get() & 0xFF;

        byte[] eventData = new byte[inboundFrame.remaining()];
        inboundFrame.get(eventData);
        byte[] outerStructure = {0x02, 0x01};
        byte[] data = ProtocolTools.concatByteArrays(outerStructure, eventData);

        DataContainer dataContainer = new DataContainer();
        dataContainer.parseObjectList(data, null);

        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
        if (AcudElectricLogBookFactory.isLogBookSupported(logbookObisCode)) {
            meterProtocolEvents = AcudElectricLogBookFactory.parseElectricityEvents(this.timeZone, dataContainer, logbookObisCode, EndDeviceType.ELECTRICMETER);
        } else if (AcudGasLogBookFactory.isLogBookSupported(logbookObisCode)) {
            meterProtocolEvents = AcudGasLogBookFactory.parseGasEvents(this.timeZone, dataContainer, logbookObisCode, EndDeviceType.GASMETER);
        } else if (AcudWaterLogBookFactory.isLogBookSupported(logbookObisCode)) {
            meterProtocolEvents = AcudWaterLogBookFactory.parseWaterEvents(this.timeZone, dataContainer, logbookObisCode, EndDeviceType.WATERMETER);
        } else {
            throw DataParseException.ioException(new ProtocolException("Unsupported logbook: " + logbookObisCode));
        }
        createCollectedLogBookFromProtocolEvents(meterProtocolEvents);
    }

    @Override
    protected void createCollectedLogBookFromProtocolEvents(List<MeterProtocolEvent> meterProtocolEvents) {
        collectedLogBook = collectedDataFactory.createCollectedLogBook(new LogBookIdentifierByObisCodeAndDevice(slaveSerialNumberIdentifier, logbookObisCode));
        collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
    }
}
