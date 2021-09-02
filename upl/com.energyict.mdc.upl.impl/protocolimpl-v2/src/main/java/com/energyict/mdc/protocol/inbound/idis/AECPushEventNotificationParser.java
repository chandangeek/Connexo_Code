package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.g3.EventPushNotificationParser;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimplv2.dlms.idis.am540.events.MeterAlarmParser;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

public class AECPushEventNotificationParser extends EventPushNotificationParser {
    /**
     * The default obiscode of the logbook to store the received events in
     */
    private static final ObisCode DEFAULT_OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");

    public AECPushEventNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        super(comChannel, context);
        this.inboundDAO = context.getInboundDAO();
        this.logbookObisCode = DEFAULT_OBIS_STANDARD_EVENT_LOG;
    }

    @Override
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

        if (nrOfDataTypes != 4) {
            throw DataParseException.ioException(new ProtocolException("Expected a structure with 4 elements, but received a structure with " + nrOfDataTypes + " element(s)"));
        }

        parseWrappedMeterEvent(structure, dateTime);
    }

    private void parseWrappedMeterEvent(Structure structure, Date dateTime) {
        OctetString equipmentIdentifier = structure.getDataType(0).getOctetString();
        if (equipmentIdentifier == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the 1st element of the received structure (equipment identifier) to be of type OctetString"));
        }
        deviceIdentifier = new DeviceIdentifierBySerialNumber(equipmentIdentifier.stringValue());

        final OctetString clock = structure.getDataType(1).getOctetString();
        if (clock == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the 2nd element of the received structure (clock) to be of type OctetString"));
        }

        final Unsigned32 alarmDescriptor1 = structure.getDataType(2).getUnsigned32();
        if (alarmDescriptor1 == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the 3rd element of the received structure (alarm descriptor 1) to be of type Integer32"));
        }

        final Unsigned32 alarmDescriptor2 = structure.getDataType(3).getUnsigned32();
        if (alarmDescriptor2 == null) {
            throw DataParseException.ioException(new ProtocolException("Expected the 4th element of the received structure (alarm descriptor 2) to be of type Integer32"));
        }

        final Date pushDate = parseDateTime(clock);
        final long alarmDescriptor1Value = alarmDescriptor1.getValue();
        final long alarmDescriptor2Value = alarmDescriptor2.getValue();

        List<MeterEvent> meterAlarmEvents = MeterAlarmParser.parseAlarmCode(pushDate, alarmDescriptor1Value, 1);
        meterAlarmEvents.addAll(MeterAlarmParser.parseAlarmCode(pushDate, alarmDescriptor2Value, 2) );

        createCollectedLogBook(meterAlarmEvents);
    }
}
