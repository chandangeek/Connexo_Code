/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SlidingKWDemands.java
 *
 * Created on 18 mei 2005, 16:01
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class SlidingKWDemands extends AbstractCommand {

    private static final int DEBUG=0;
    private static final String COMMAND="RS*";
    private static final Unit UNIT = Unit.get("kW");
    private static final int NR_OF_CHANNELS=8;
    private static final int NR_OF_5_MINUTE_INTERVALS=12; // 5*12 = 60 minutes, 0 = most recent interval

    Quantity[][] quantities = new Quantity[NR_OF_CHANNELS][NR_OF_5_MINUTE_INTERVALS];

    /** Creates a new instance of SlidingKWDemands */
    public SlidingKWDemands(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("AllEnergy:\n");
        for (int interval = 0; interval < NR_OF_5_MINUTE_INTERVALS; interval++) {
           strBuff.append("interval "+interval+": ");
           for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
               strBuff.append("ch "+channel+": "+getQuantity(channel, interval)+", ");
           }
           strBuff.append("\n");
        }
        return strBuff.toString();
    }

    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    protected void parse(byte[] data) throws ConnectionException, IOException {
        if (DEBUG>=1)
           System.out.println(new String(data));

        CommandParser cp = new CommandParser(data);

        for (int interval=0;interval<NR_OF_5_MINUTE_INTERVALS;interval++) {
            // Generation 1: tag = LINE-1, LINE-2, ...
            // Generation 2: tag contains timestamp: 0440-1 (04h:40m, line 1), 0435-2 (04h 35m, line 2), ...
            List values = cp.getValuesForTagEndingWith("-"+(interval+1));
            for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
                BigDecimal bd = ez7CommandFactory.getMeterInformation().calculateValue(channel, Long.parseLong((String)values.get(channel),16));
                quantities[channel][interval] = new Quantity(bd,UNIT);
            }
        }

    }

    public Quantity getQuantity(int channel, int interval5min) {
        try {
           return quantities[channel][interval5min];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return null;
        }

    }
}
