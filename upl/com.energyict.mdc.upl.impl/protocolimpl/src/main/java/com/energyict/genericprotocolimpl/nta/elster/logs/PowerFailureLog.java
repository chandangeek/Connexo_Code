package com.energyict.genericprotocolimpl.nta.elster.logs;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
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
 * Time: 11:00:58
 * </p>
 */
public class PowerFailureLog extends com.energyict.genericprotocolimpl.nta.eventhandling.PowerFailureLog {

    public PowerFailureLog(TimeZone timeZone, DataContainer dc) {
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
			long duration = this.dcEvents.getRoot().getStructure(i).getValue(1);
			if(duration < 0){
				duration += 0xFFFF;
				duration++;
			}
            if (isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))) {
                eventTimeStamp = dcEvents.getRoot().getStructure(i).getOctetString(0).toCalendar(timeZone).getTime();
            }

            if (eventTimeStamp != null) {
                buildMeterEvent(meterEvents, eventTimeStamp, duration);
            }
        }
        return meterEvents;
    }
}
