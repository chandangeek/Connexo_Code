/*
 * EventGeneral.java
 *
 * Created on 18 mei 2005, 11:12
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EventGeneral specific for Generation2 devices,
 * who have a slightly different response format (response doesn't contain power failure info).
 *
 * @author sva
 */
public class EventGeneralGeneration2 extends EventGeneral {

    /**
     * Creates a new instance of EventGeneralGeneration2
     */
    public EventGeneralGeneration2(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
        NR_OF_CHANNELS=8;
        NR_OF_LINES=8;
        values = new int[NR_OF_CHANNELS][NR_OF_LINES];
    }

    @Override
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventLoad:\n");
        for (int line = 0; line < NR_OF_LINES; line++) {
            strBuff.append("line " + line + ": ");
            for (int channel = 0; channel < NR_OF_CHANNELS; channel++) {
                strBuff.append("ch " + channel + ": 0x" + Integer.toHexString(getValue(channel, line)) + ", ");
            }
            strBuff.append("\n");
        }
        return strBuff.toString();
    }

    @Override
    public List toMeterEvents() {
        return new ArrayList();     // No power failure information available, so no meterEvents to create.
    }

    @Override
    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    private void parse(byte[] data) {
        if (DEBUG >= 1) {
            System.out.println(new String(data));
        }

        CommandParser cp = new CommandParser(data);

        for (int line = 0; line < NR_OF_LINES; line++) {
            List vals = cp.getValues("LINE-" + (line + 1));
            for (int channel = 0; channel < NR_OF_CHANNELS; channel++) {
                values[channel][line] = Integer.parseInt((String) vals.get(channel), 16);
            }
        }
    }
}