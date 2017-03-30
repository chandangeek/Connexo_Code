/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EventGeneral.java
 *
 * Created on 18 mei 2005, 11:12
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;

import java.io.IOException;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class EventLoad extends AbstractCommand {

    private static final int DEBUG=0;
    private static final String COMMAND="REL";
    private static final int NR_OF_CHANNELS=8;
    private static final int NR_OF_LINES=8;

    int[][] values = new int[NR_OF_CHANNELS][NR_OF_LINES];

    /** Creates a new instance of FlagsStatus */
    public EventLoad(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }


    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventLoad:\n");
        for (int line = 0; line < NR_OF_LINES; line++) {
           strBuff.append("line "+line+": ");
           for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
                   strBuff.append("ch "+channel+": 0x"+Integer.toHexString(getValue(channel, line))+", ");
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

    private void parse(byte[] data) {
        if (DEBUG>=1)
            System.out.println(new String(data));

        CommandParser cp = new CommandParser(data);

        for (int line = 0; line < NR_OF_LINES; line++) {
           List vals = cp.getValues("FLAG-"+(line+1));
           for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
               values[channel][line] = Integer.parseInt((String)vals.get(channel),16);
           }
        }
    }

    public int getValue(int channel, int line) {
        try {
            return values[channel][line];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

}