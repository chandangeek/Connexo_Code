package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.cim.EndDeviceEventType;
import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.identifiers.LogBookIdentifierByDeviceAndObisCode;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EventFactory extends AbstractMerlinFactory{

    private static final ObisCode STANDARD_LOGBOOK = ObisCode.fromString("0.0.99.98.0.255");

    public EventFactory(Telegram telegram, InboundContext inboundContext) {
        super(telegram, inboundContext);
    }

    @Override
    public ObisCode getObisCode() {
        return STANDARD_LOGBOOK;
    }

    public CollectedLogBook extractEventsFromStatus() {
        LogBookIdentifier standardLogBook = new LogBookIdentifierByDeviceAndObisCode(getDeviceIdentifier(), getObisCode());
        CollectedLogBook statusLogBookEvents = getCollectedDataFactory().createCollectedLogBook(standardLogBook);

        extractTelegramDateTime();

        String statusRaw = getTelegram().getBody().getBodyHeader().getStatusField();
        int status = Integer.parseInt(statusRaw, 16);

        List<MeterProtocolEvent> meterEvents = Arrays.stream(StatusEventMapping.values())
                .map(mapping -> extractMapping(mapping, status))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        statusLogBookEvents.addCollectedMeterEvents(meterEvents);
        return statusLogBookEvents;

    }

    private MeterProtocolEvent extractMapping(StatusEventMapping mapping, int status) {
        if( ((status & mapping.getMask()) >> mapping.getShift()) != mapping.getExpectedValue()) {
            return null;
        }

        if (!mapping.isError()) {
            // just an info
            getInboundContext().getLogger().log("Status event info " + mapping.toString());
        } else {
            // normal event
            getInboundContext().getLogger().log("Status event: " + mapping.toString());
        }


        Date eventTime = getDateOnDeviceTimeZoneFromTelegramTime();
        int eiCode = mapping.getMeterEvent();
        int protocolCode = 0;
        EndDeviceEventType eventType =  EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(eiCode);
        String message = mapping.getMessage();
        int meterEventLogId = 0;
        int deviceEventId = 0;

        return new MeterProtocolEvent(eventTime, eiCode, protocolCode, eventType, message, meterEventLogId, deviceEventId);
    }

}
