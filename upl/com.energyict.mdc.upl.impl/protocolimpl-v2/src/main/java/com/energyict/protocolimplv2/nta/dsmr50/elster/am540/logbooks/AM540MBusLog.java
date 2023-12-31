package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130MBusEventLog;

import java.util.TimeZone;

/**
 * The MbBusLog of AM540 device is based on IDIS Package 2 spec, just like AM130MBusEventLog
 *
 * @author sva
 * @since 20/02/2015 - 16:06
 */
public class AM540MBusLog extends AM130MBusEventLog {

    public AM540MBusLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc, false);
    }
}
