/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.events;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimplv2.edmi.mk6.profiles.LoadSurveyData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 7/03/2017 - 12:23
 */
public class StandardEventLogParser implements EventLogParser {

    public StandardEventLogParser() {
    }

    @Override
    public List<MeterProtocolEvent> parseMeterProtocolEvents(LoadSurveyData logSurveyData) throws ProtocolException {
        List<MeterProtocolEvent> meterEvents = new ArrayList<>();
        for (int interval = 0; interval < logSurveyData.getNumberOfRecords(); interval++) {
            Date date = logSurveyData.getChannelValues(interval)[1].getDate();
            String message = logSurveyData.getChannelValues(interval)[2].getString();
            int eiCode = mapEventLogMessage2MeterEventEICode(message);
            meterEvents.add(
                    new MeterProtocolEvent(
                            date,
                            eiCode,
                            0, // No protocol code
                            EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(eiCode),
                            message,
                            0,
                            0
                    )
            );
        }
        return meterEvents;
    }

    private int mapEventLogMessage2MeterEventEICode(String message) {
        if (message.contains("Power Off")) {
            return MeterEvent.POWERDOWN;
        } else if (message.contains("Power On")) {
            return MeterEvent.POWERUP;
        } else if (message.contains("Changing System Time")) {
            return MeterEvent.SETCLOCK_BEFORE;
        } else if (message.contains("System Time Changed")) {
            return MeterEvent.SETCLOCK_AFTER;
        } else if (message.contains("Firmware")) {   // Firmware vX.XX changed to vY.YY
            return MeterEvent.FIRMWARE_ACTIVATED;
        } else if (message.contains("Billing Reset")) { // Automatic or manual billing reset
            return MeterEvent.BILLING_ACTION;
        } else if (message.contains("TOU Cleared")) {
            return MeterEvent.CONFIGURATIONCHANGE;
        } else if (message.contains("Buffer Limit Reached")) {
            return MeterEvent.MEASUREMENT_SYSTEM_ERROR;
        } else {
            return MeterEvent.OTHER; // All other events map to EIS code 'OTHER'
        }
    }
}