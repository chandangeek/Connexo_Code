package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.MeterEvent;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.AbstractEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Deprecated
public class ESMR50CommunicationSessionLog extends AbstractEvent {

    private static final int EVENT_EVENT_LOG_CLEARED = 255;
    private static final int EVENT_METROLOGICAL_MAINTENANCE = 71;
    private static final int EVENT_TECHNICAL_MAINTENANCE = 72;
    private static final int EVENT_RETRIEVE_METER_READINGS_E = 73;
    private static final int EVENT_RETRIEVE_METER_READINGS_G = 74;
    private static final int EVENT_RETRIEVE_INTERVAL_DATA_E = 75;
    private static final int EVENT_RETRIEVE_INTERVAL_DATA_G = 76;

    /**
     * @param dc            the DataContainer, containing all the eventData
     * @param deviationType the interpretation type of the DataTime
     */
    public ESMR50CommunicationSessionLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        buildMeterEventWithCounter(meterEvents, eventTimeStamp, eventId, "");
    }

    @Override
    public List<MeterEvent> getMeterEvents() throws ProtocolException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();

        for (int i = 0; i <= (size - 1); i++) {
            DataStructure structure = this.dcEvents.getRoot().getStructure(i);
            String counter = null;
            int eventId = -1;
            Date eventTimeStamp = null;

            if (structure.getNrOfElements()==2){
                eventId = (int) structure.getValue(1) & 0xFF; // To prevent negative values
                if (isOctetString(structure.getElement(0))) {
                    eventTimeStamp = new AXDRDateTime(OctetString.fromByteArray(structure.getOctetString(0).getArray()), this.deviationType).getValue().getTime();
                }
            }

            if (structure.getNrOfElements()==3){
                eventId = (int) structure.getValue(1) & 0xFF; // To prevent negative values
                if (isOctetString(structure.getElement(0))) {
                    eventTimeStamp = new AXDRDateTime(OctetString.fromByteArray(structure.getOctetString(0).getArray()), this.deviationType).getValue().getTime();
                }
                counter = structure.getElement(2).toString();
            }

            if (eventTimeStamp != null && eventId>0) {
                if (counter==null) {
                    buildMeterEvent(meterEvents, eventTimeStamp, eventId);
                } else {
                    buildMeterEventWithCounter(meterEvents, eventTimeStamp, eventId, counter);
                }
            }
        }
        return meterEvents;
    }

    /**
     * Adds a new event to the meter event list, using extra information provided by the device;
     *
     * @param meterEvents
     * @param eventTimeStamp
     * @param eventId
     */
    protected void buildMeterEventWithCounter(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId, String counter) {
        switch (eventId) {
            case EVENT_EVENT_LOG_CLEARED: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId, "Communication session log profile cleared"));
            }
            break;
            case EVENT_METROLOGICAL_MAINTENANCE: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Metrological maintenance ["+counter+"]"));
            }
            break;
            case EVENT_TECHNICAL_MAINTENANCE: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Technical maintenance ["+counter+"]"));
            }
            break;
            case EVENT_RETRIEVE_METER_READINGS_E: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Retrieve meter readings E ["+counter+"]"));
            }
            break;
            case EVENT_RETRIEVE_METER_READINGS_G: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Retrieve meter readings G ["+counter+"]"));
            }
            break;
            case EVENT_RETRIEVE_INTERVAL_DATA_E: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Retrieve interval data E ["+counter+"]"));
            }
            break;
            case EVENT_RETRIEVE_INTERVAL_DATA_G: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Retrieve interval data G ["+counter+"]"));
            }
            break;
            default:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}
