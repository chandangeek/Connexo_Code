package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;

import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 */

public class Beacon3100SecurityEventLog extends Beacon3100AbstractEventLog {

    public Beacon3100SecurityEventLog(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    @Override
    protected String getLogBookName() {
        return "Security event log";
    }
    
}
