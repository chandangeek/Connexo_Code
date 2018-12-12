/*
 * FlagsStatus.java
 *
 * Created on 18 mei 2005, 11:31
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class FlagsStatus extends AbstractCommand {

    private static final int DEBUG=0;
    private static final String COMMAND="RF";
    private static final int NR_OF_STATUS_REGISTERS=8;
    private static final int NR_OF_LINES=4;

    private int[][] values = new int[NR_OF_STATUS_REGISTERS][NR_OF_LINES];
    private Date lastPowerFailure;

    public FlagsStatus(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FlagsStatus:\n");
        for (int line = 0; line < NR_OF_LINES; line++) {
           builder.append("line ").append(line).append(": ");
           for (int status=0;status<NR_OF_STATUS_REGISTERS;status++) {
                   builder.append("ch ").append(status).append(": 0x").append(Integer.toHexString(getValue(status, line))).append(", ");
           }
           builder.append("\n");
        }
        return builder.toString();
    }

    public List<MeterEvent> toMeterEvents(Date from, Date to) {
        List<MeterEvent> meterEvents = new ArrayList<>();
        int temp;
        Date date = ProtocolUtils.getCalendar(ez7CommandFactory.getEz7().getTimeZone()).getTime();
        // LINE-1
        temp=getValue(1,0);
        if (temp != 0x0101) {
            meterEvents.add(new MeterEvent(date, MeterEvent.HARDWARE_ERROR, "LINE-1, status 2/8 (0x" + Integer.toHexString(temp) + "), Hardware error"));
        }
        temp=getValue(2,0);
        if (temp != 0x0000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.REGISTER_OVERFLOW, "LINE-1, status 3/8 (0x" + Integer.toHexString(temp) + "), Meter overload"));
        }
        temp=getValue(3,0);
        if (temp != 0x0000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.REGISTER_OVERFLOW, "LINE-1, status 4/8 (0x" + Integer.toHexString(temp) + "), Data register overload"));
        }
        temp=getValue(4,0);
        if (temp != 0x1170) {
            meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, "LINE-1, status 5/8 (0x" + Integer.toHexString(temp) + "), Scheduling error"));
        }
        temp=getValue(7,0);
        if (temp != 0x0000) {
            meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, "LINE-1, status 8/8 (0x" + Integer.toHexString(temp) + "), Checksum error"));
        }

        // Power failure information
        if (lastPowerFailure != null && lastPowerFailure.after(from) && lastPowerFailure.before(to)) {
            meterEvents.add(new MeterEvent(lastPowerFailure, MeterEvent.OTHER, "Power failure at " + lastPowerFailure));
        }
        return meterEvents;
    }

    @Override
    public void build() throws IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    private void parse(byte[] data) {
        if (DEBUG>=1) {
            System.out.println(new String(data));
        }
        Calendar calCurrent = ProtocolUtils.getCalendar(ez7CommandFactory.getEz7().getTimeZone());
        CommandParser cp = new CommandParser(data);

        for (int line = 0; line < NR_OF_LINES; line++) {
           List vals = cp.getValues("LINE-"+(line+1));
           for (int status=0;status<NR_OF_STATUS_REGISTERS;status++) {
               values[status][line] = Integer.parseInt((String)vals.get(status),16);
           }
        }

        // Parse power failure info
        int valueMMDD = Integer.parseInt((String) cp.getValues("LINE-2").get(2));
        int valueHHMM = Integer.parseInt((String) cp.getValues("LINE-2").get(3));
        if (valueMMDD != 0 || valueHHMM != 0) {
            Calendar cal = ProtocolUtils.getCalendar(ez7CommandFactory.getEz7().getTimeZone());
            cal.set(Calendar.MONTH, (valueMMDD / 100) - 1);
            cal.set(Calendar.DAY_OF_MONTH, (valueMMDD % 100));
            cal.set(Calendar.HOUR_OF_DAY, valueHHMM / 100);
            cal.set(Calendar.MINUTE, valueHHMM % 100);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            // no year indication is given in the timestamp information...
            // so, if cal > current meter time, year=year-1
            Date currentMeterTime = calCurrent.getTime();
            try {
                currentMeterTime = ez7CommandFactory.getRTC().getDate();
            } catch (IOException e) {
            }
            if (cal.getTime().after(currentMeterTime)) {
                cal.add(Calendar.YEAR, -1);
            }
            lastPowerFailure = cal.getTime();
        }
    }

    @Override
    public int getValue(int status, int line) {
        try {
            return values[status][line];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

}