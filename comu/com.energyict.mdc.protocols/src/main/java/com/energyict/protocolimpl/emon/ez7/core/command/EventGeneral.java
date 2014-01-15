/*
 * EventGeneral.java
 *
 * Created on 18 mei 2005, 11:12
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class EventGeneral extends AbstractCommand {

    protected static final int DEBUG=0;
    protected static final String COMMAND="REG";

    protected int NR_OF_CHANNELS;
    protected int NR_OF_LINES;
    protected Date[] meterUnpluggedDates;
    protected int[][] values;

    /** Creates a new instance of EventGeneral */
    public EventGeneral(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
        NR_OF_CHANNELS=8;
        NR_OF_LINES=12;
        values = new int[NR_OF_CHANNELS][NR_OF_LINES];
        meterUnpluggedDates = new Date[NR_OF_CHANNELS];
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
        for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
            if (meterUnpluggedDates[channel] != null)
               strBuff.append("ch "+channel+" meterUnpluggedDates="+meterUnpluggedDates[channel]+", ");
        }
        return strBuff.toString();
    }

    public List toMeterEvents() {
        List meterEvents = new ArrayList();
        for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
            if (getMeterUnpluggedDate(channel) != null) {
                meterEvents.add(new MeterEvent(getMeterUnpluggedDate(channel),MeterEvent.OTHER,"Event general Meter "+(channel+1)+" Off-line (power failure) at "+getMeterUnpluggedDate(channel)));
            }
        }
        return meterEvents;
    }

    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    private void parse(byte[] data) {

        if (DEBUG>=1)
            System.out.println(new String(data));

        Calendar calCurrent = ProtocolUtils.getCalendar(ez7CommandFactory.getEz7().getTimeZone());
        CommandParser cp = new CommandParser(data);

        for (int line = 0; line < NR_OF_LINES; line++) {
           List vals = cp.getValues("FLAG-"+(line+1));
           for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
               values[channel][line] = Integer.parseInt((String)vals.get(channel),16);
           }
        }

        List valuesMMDD = cp.getValues("FLAG-8");
        List valuesHHMM = cp.getValues("FLAG-9");
        for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
            int valueHHMM = Integer.parseInt((String)valuesHHMM.get(channel));
            int valueMMDD = Integer.parseInt((String)valuesMMDD.get(channel));

            if (valueMMDD==0)
                meterUnpluggedDates[channel] = null;
            else {
                Calendar cal = ProtocolUtils.getCalendar(ez7CommandFactory.getEz7().getTimeZone());
                int month = (valueMMDD/100)-1;
                cal.set(Calendar.MONTH,(valueMMDD/100)-1);
                cal.set(Calendar.DAY_OF_MONTH,(valueMMDD%100));
                cal.set(Calendar.HOUR_OF_DAY,valueHHMM/100);
                cal.set(Calendar.MINUTE,valueHHMM%100);
                cal.set(Calendar.SECOND,0);
                cal.set(Calendar.MILLISECOND,0);
                // no year indication is given in the timestamp information...
                // so, if cal > currentCal, year=year-1
                if (cal.getTime().after(calCurrent.getTime()))
                    cal.add(Calendar.YEAR,-1);
                meterUnpluggedDates[channel] = cal.getTime();
            }
        }
    }

    /**
     * Getter for property meterUnpluggedDate.
     * @return Value of property meterUnpluggedDate.
     */
    private java.util.Date getMeterUnpluggedDate(int channel) {
        return this.meterUnpluggedDates[channel];
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
