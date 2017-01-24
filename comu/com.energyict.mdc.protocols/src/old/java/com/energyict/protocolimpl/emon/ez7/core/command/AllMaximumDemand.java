/*
 * AllMaximumDemand.java
 *
 * Created on 17 mei 2005, 11:38
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class AllMaximumDemand extends AbstractCommand {

    private static final int DEBUG=0;
    private static final String MAXDEMANDCOMMAND="RDA8";
    private static final String MAXDEMANDTIMESTAMPCOMMAND="RDD8";
    private static final int NR_OF_CHANNELS=8;
    private static final int NR_OF_TARIFFS=8;
    Quantity[][] quantities = new Quantity[NR_OF_CHANNELS][NR_OF_TARIFFS];
    Date[][] dates = new Date[NR_OF_CHANNELS][NR_OF_TARIFFS];
    /** Creates a new instance of AllMaximumDemand */
    public AllMaximumDemand(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("AllMaximumDemand:\n");
        for (int tariff = 0; tariff < NR_OF_TARIFFS; tariff++) {
           strBuff.append("tariff "+tariff+": ");
           for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
               if (getDate(channel,tariff) != null)
                   strBuff.append("ch "+channel+": "+getQuantity(channel, tariff)+"("+getDate(channel,tariff)+"), ");
           }
           strBuff.append("\n");
        }
        return strBuff.toString();
    }

    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        parseMaximumDemandTimestamp(ez7CommandFactory.getEz7().getEz7Connection().sendCommand(MAXDEMANDTIMESTAMPCOMMAND));
        parseMaximumDemand(ez7CommandFactory.getEz7().getEz7Connection().sendCommand(MAXDEMANDCOMMAND));
    }

    private void parseMaximumDemand(byte[] data) throws ConnectionException, IOException {
        if (DEBUG>=1)
           System.out.println(new String(data));

        CommandParser cp = new CommandParser(data);
        for (int tariff = 0; tariff < NR_OF_TARIFFS; tariff++) {
           List values = cp.getValues("KWDA-"+(tariff+1));
           for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
              int value = Integer.parseInt((String)values.get(channel),16);
              BigDecimal bd = ez7CommandFactory.getMeterInformation().calculateValue(channel, value);
              quantities[channel][tariff] = new Quantity(bd,ez7CommandFactory.getMeterInformation().getUnit(channel, false));
           }
        }
    }

    private void parseMaximumDemandTimestamp(byte[] data) {
        if (DEBUG>=1)
           System.out.println(new String(data));

        Calendar calCurrent = ProtocolUtils.getCalendar(ez7CommandFactory.getEz7().getTimeZone());
        CommandParser cp = new CommandParser(data);
        for (int tariff = 0; tariff < NR_OF_TARIFFS; tariff++) {
           List valuesHHMM = cp.getValues("LINE-"+(tariff+1),0);
           List valuesMMDD = cp.getValues("LINE-"+(tariff+1),1);
           for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
              int valueHHMM = Integer.parseInt((String)valuesHHMM.get(channel));
              int valueMMDD = Integer.parseInt((String)valuesMMDD.get(channel));

              if (valueMMDD==0) {
                  dates[channel][tariff] = null;
              }
              else {
                  Calendar cal = ProtocolUtils.getCalendar(ez7CommandFactory.getEz7().getTimeZone());
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

                  dates[channel][tariff] = cal.getTime();
              }
           }
        }
    }

    public Quantity getQuantity(int channel, int tariff) {
        try {
           return quantities[channel][tariff];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
    public Date getDate(int channel, int tariff) {
        try {
           return dates[channel][tariff];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
