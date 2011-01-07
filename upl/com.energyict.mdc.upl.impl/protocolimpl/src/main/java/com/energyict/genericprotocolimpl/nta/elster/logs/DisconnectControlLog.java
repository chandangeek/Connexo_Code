package com.energyict.genericprotocolimpl.nta.elster.logs;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * <p>
 * Copyrights EnergyICT
 * Date: 4-jun-2010
 * Time: 10:51:11
 * </p>
 */
public class DisconnectControlLog extends com.energyict.genericprotocolimpl.nta.eventhandling.DisconnectControlLog {

    public DisconnectControlLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp = null;
        for (int i = 0; i <= (size - 1); i++) {
            int eventId = (int) this.dcEvents.getRoot().getStructure(i).getValue(1) & 0xFF; // To prevent negative values
            String threshold = "Unknown";
            if (this.dcEvents.getRoot().getStructure(i).getElements().length == 3) {
                threshold = Integer.toString(this.dcEvents.getRoot().getStructure(i).getInteger(2));
            }
            if (isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))) {
                eventTimeStamp = dcEvents.getRoot().getStructure(i).getOctetString(0).toCalendar(timeZone).getTime();
            }
            if (eventTimeStamp != null) {
                buildMeterEvent(meterEvents, eventTimeStamp, eventId, threshold);
            }
        }
        return meterEvents;
    }
}
