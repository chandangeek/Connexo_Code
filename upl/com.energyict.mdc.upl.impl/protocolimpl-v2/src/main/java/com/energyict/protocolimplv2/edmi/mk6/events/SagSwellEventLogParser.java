/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.events;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.edmi.mk6.profiles.LoadSurveyData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 7/03/2017 - 12:23
 */
public class SagSwellEventLogParser implements EventLogParser {

    public SagSwellEventLogParser() {
    }

    @Override
    public List<MeterProtocolEvent> parseMeterProtocolEvents(LoadSurveyData logSurveyData) throws ProtocolException {
        List<MeterProtocolEvent> meterEvents = new ArrayList<>();
        for (int interval = 0; interval < logSurveyData.getNumberOfRecords(); interval++) {
            Date date = logSurveyData.getChannelValues(interval)[1].getDate();
            int phase = logSurveyData.getChannelValues(interval)[2].getBigDecimal().intValue();
            float voltageMagnitude = logSurveyData.getChannelValues(interval)[2].getBigDecimal().floatValue();
            float durationInSeconds = logSurveyData.getChannelValues(interval)[3].getBigDecimal().floatValue();

            if (date != null) {
                String message = ProtocolTools.format(
                        "Voltage sag/swell (value = {0}) on phase {1} for a duration of {2} seconds.",
                        new Object[]{voltageMagnitude, mapPhase(phase), durationInSeconds}
                );
                meterEvents.add(
                        new MeterProtocolEvent(
                                date,
                                MeterEvent.VOLTAGE_SWELL,
                                0, // No protocol code
                                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.VOLTAGE_SWELL),
                                message,
                                0,
                                0
                        )

                );
            }
        }
        return meterEvents;
    }

    private String mapPhase(int phase) {
        if (phase == 0) {
            return "A";
        } else if (phase == 1) {
            return "B";
        } else if (phase == 2) {
            return "C";
        }
        return Integer.toString(phase);
    }
}