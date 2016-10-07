package com.energyict.protocols.mdc.inbound.general.frames.parsing;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Class that parses a received string (containing event information) into a MeterEvent object.
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/06/12
 * Time: 15:22
 * Author: khe
 */
public class EventInfo {

    private static final int UNKNOWN = 0;
    private String info;

    public EventInfo(String info) {
        this.info = info;
    }

    public Optional<MeterProtocolEvent> parse(Thesaurus thesaurus, MeteringService meteringService) {
        String[] eventInfos = info.split(" ");
        if (eventInfos.length == 3) {
            int eventCode = Integer.parseInt(eventInfos[0]);
            String eventDescription = thesaurus.getFormat(MessageSeeds.EVENT_VALUE).format() + ": " + eventInfos[1];
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date eventTimeStamp;
            try {
                eventTimeStamp = formatter.parse(eventInfos[2]);
            } catch (ParseException e) {
                return null;
            }
            return EndDeviceEventTypeMapping
                    .getEventTypeCorrespondingToEISCode(MeterEvent.OTHER, meteringService)
                    .map(endDeviceEventType -> new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventCode, endDeviceEventType, eventDescription, UNKNOWN, UNKNOWN));
        }
        return Optional.empty();
    }

}