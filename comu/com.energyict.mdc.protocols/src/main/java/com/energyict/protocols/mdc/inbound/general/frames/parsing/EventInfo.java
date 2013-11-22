package com.energyict.protocols.mdc.inbound.general.frames.parsing;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class that parses a received string (containing event information) into a MeterEvent object
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/06/12
 * Time: 15:22
 * Author: khe
 */
public class EventInfo {

    private static final int UNKNOWN = 0;
    private static final String PROTOCOL_EVENTVALUE = EnvironmentImpl.getDefault().getTranslation("protocol.eventvalue");
    private String info;
    private int logBookId;

    public EventInfo(String info, int logBookId) {
        this.info = info;
        this.logBookId = logBookId;
    }

    public MeterProtocolEvent parse() {
        String[] eventInfos = info.split(" ");
        if (eventInfos.length == 3) {
            int eventCode = Integer.parseInt(eventInfos[0]);
            String eventDescription = PROTOCOL_EVENTVALUE + ": " + eventInfos[1];
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date eventTimeStamp;
            try {
                eventTimeStamp = formatter.parse(eventInfos[2]);
            } catch (ParseException e) {
                return null;
            }
            return new MeterProtocolEvent(eventTimeStamp, MeterEvent.OTHER, eventCode, EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.OTHER), eventDescription, UNKNOWN, UNKNOWN);
        }
        return null;
    }
}