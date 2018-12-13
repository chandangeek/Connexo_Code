package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;

import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 */

public class Beacon3100StandardEventLog extends Beacon3100AbstractEventLog {
    public static String LOGBOOK_NAME = "Standard event log";
    public Beacon3100StandardEventLog(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    @Override
    protected String getLogBookName() {
        return LOGBOOK_NAME;
    }
    
}
