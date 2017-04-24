/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.JanzC280.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class QualityEventLog extends AbstractEvent {

    public QualityEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    /**
     * @return the MeterEvent List
     * @throws java.io.IOException
     */
    @Override
    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();

        for (int i = 0; i <= (size - 1); i++) {
            int eventDefHighByte = (byte) this.dcEvents.getRoot().getStructure(i).getValue(0);
            int eventDefLowByte = (byte) this.dcEvents.getRoot().getStructure(i).getValue(1);
            int durationOfFault = (byte) this.dcEvents.getRoot().getStructure(i).getValue(2);

            Calendar cal = Calendar.getInstance(this.timeZone);         // Received timestamp is in the device timezone!
            cal.setTimeInMillis((946684800 + this.dcEvents.getRoot().getStructure(i).getValue(3)) * 1000);    // Number of seconds [1970 - 2000] + number of seconds [2000 - meter time]
            if (cal.getTime() != null) {
                buildMeterEvent(meterEvents, cal.getTime(), eventDefHighByte, eventDefLowByte, durationOfFault);
            }
        }
        return meterEvents;
    }

    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventDefHighByte, int eventDefLowByte, int durationOfFault) {
        int eventId = Integer.parseInt(String.valueOf(eventDefHighByte)
                + (eventDefLowByte < 10 ? "0" : "")
                + String.valueOf(eventDefLowByte));

        switch (eventDefHighByte) {
            case 0:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Not available"));
                break;
            case 7:
                switch (eventDefLowByte) {
                    case 4:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Imperfection of the phase of the single-phase meter - Duration of fault: " + durationOfFault + " min"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Imperfection of the phase of the single-phase meter"));
                        break;
                }
                break;
            case 21:
                switch (eventDefLowByte) {
                    case 1:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Error in the Quality Event - Duration of fault: " + durationOfFault + " min"));
                        break;
                    case 2:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "Cleaning of Quality Events - Duration of fault: " + durationOfFault + " min"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Event Quality of Service"));
                        break;
                }
                break;
            case 22:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Not applicable"));
                break;
            case 23:
                switch (eventDefLowByte) {
                    case 1:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Variation of Tension Statistical - Duration of fault: " + durationOfFault + " min"));
                        break;
                    case 2:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Variation of Instantaneous Tension - Duration of fault: " + durationOfFault + " min"));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Variation of Tension Event"));
                        break;
                }
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode:" + eventId));
                break;
        }
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        throw new UnsupportedOperationException();
    }
}