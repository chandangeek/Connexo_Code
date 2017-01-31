/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
public class AM540MbusLog extends AM130MBusEventLog {

    public AM540MbusLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }
}