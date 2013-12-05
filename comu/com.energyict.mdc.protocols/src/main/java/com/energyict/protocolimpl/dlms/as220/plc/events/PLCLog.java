package com.energyict.protocolimpl.dlms.as220.plc.events;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights
 * Date: 17/06/11
 * Time: 15:21
 */
public class PLCLog extends Array {

    private final TimeZone timeZone;

    public PLCLog(byte[] berEncodedData, TimeZone timeZone) throws IOException {
        super(berEncodedData, 0, 0);
        this.timeZone = timeZone;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public List<PLCEvent> getPLCEvents() {
        List<PLCEvent> plcEvents = new ArrayList<PLCEvent>();
        for (int i = 0; i < nrOfDataTypes(); i++) {
            if ((getDataType(i) != null) && (getDataType(i).isStructure())) {
                byte[] rawData = getDataType(i).getStructure().getBEREncodedByteArray();
                try {
                    PLCEvent plcEvent = new PLCEvent(rawData, getTimeZone());
                    plcEvents.add(plcEvent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return plcEvents;
    }

    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        for (PLCEvent plcEvent : getPLCEvents()) {
            meterEvents.add(plcEvent.getMeterEvent());
        }
        return meterEvents;
    }

}
