package com.energyict.mdc.protocol.inbound.mbus.factory.events;

import com.energyict.mdc.identifiers.LogBookIdentifierByDeviceAndObisCode;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.factory.AbstractMerlinFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.mappings.StatusEventMapping;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StatusEventsFactory extends AbstractMerlinFactory {

    public StatusEventsFactory(Telegram telegram, InboundContext inboundContext) {
        super(telegram, inboundContext);
    }

    @Override
    public ObisCode getObisCode() {
        return ObisCode.fromString(STANDARD_LOGBOOK);
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
            getInboundContext().getLogger().info("Status event info " + mapping.toString());
        } else {
            // normal event
            getInboundContext().getLogger().info("Status event: " + mapping.toString());
        }


        return createEvent(mapping.getMeterEvent(), mapping.getMessage());
    }

}
