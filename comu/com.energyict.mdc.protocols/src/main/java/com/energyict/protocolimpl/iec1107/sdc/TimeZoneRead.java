/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TimeZoneRead.java
 *
 * Created on 2 november 2004, 14:40
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.protocolimpl.base.DataParser;

import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class TimeZoneRead extends AbstractDataReadingCommand {

    int timeZoneInSec=-1;

    /** Creates a new instance of TimeZoneRead */
    public TimeZoneRead(DataReadingCommandFactory drcf) {
        super(drcf);
    }

    public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException {
        DataParser dp = new DataParser();
        timeZoneInSec = Integer.parseInt(dp.parseBetweenBrackets(new String(data)));
    }

    public TimeZone getGMTTimeZone() throws java.io.IOException {
        if (timeZoneInSec == -1)
            retrieve("TMZ");
        String strTimeZone = timeZoneInSec>0?"GMT+"+Integer.toString(timeZoneInSec/3600):"GMT"+Integer.toString(timeZoneInSec/3600);
        return TimeZone.getTimeZone(strTimeZone);
    }

}
