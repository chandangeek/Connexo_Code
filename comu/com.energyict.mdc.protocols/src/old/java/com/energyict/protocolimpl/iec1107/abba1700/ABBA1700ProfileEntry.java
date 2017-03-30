/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ABBA1700ProfileEntry.java
 *
 * Created on 30 april 2003, 10:45
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocols.util.Calculate;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class ABBA1700ProfileEntry {
    private static final int DEBUG=0;

    // type attribute:
    // markers
    public static final int EXTERNALDATAENTRY=0xE2; // KV 20122004
    public static final int POWERUP=0xE5;
    public static final int CONFIGURATIONCHANGE=0xE8;
    public static final int POWERDOWN=0xE6;
    public static final int NEWDAY=0xE4;
    public static final int TIMECHANGE=0xEA;
    public static final int DAYLIGHTSAVING=0xED;
    public static final int LOADPROFILECLEARED=0xEB;
    public static final int FORCEDENDOFDEMAND=0xE9;
    // when last packet does not contain 64 (normal mode) or 256 (DS mode) bytes
    public static final int ENDOFDATA=0xFF;

    public static final int INFO_CHANNELMASK=0;
    public static final int INFO_INTEGRATIONPERIOD=1;
    public static final int INFO_STATUS=2;
    public static final int INFO_NROFINTERVALS_EXTERNAL=3;

    long date;
    int type;
    int[] info = new int[4];
    long[] values;

    List intervalValues = null;

    private ABBA1700ProfileEntry(ByteArrayInputStream bai, int nrOfChannels) throws IOException {
        date = 0;
        fillAttributes(bai,nrOfChannels);
    }
    private ABBA1700ProfileEntry(int nrOfChannels) throws IOException {
        this(null,nrOfChannels);
    }

    private void initValues(int nrOfChannels) {
        values = new long[nrOfChannels];
        for (int i=0; i<nrOfChannels; i++) {
            values[i] =0;
        }
        if (intervalValues==null)
            intervalValues = new ArrayList();
        intervalValues.add(values);
    }

    public static ABBA1700ProfileEntry getInstance(ByteArrayInputStream bai, int nrOfChannels) throws IOException {
        return new ABBA1700ProfileEntry(bai,nrOfChannels);
    }

    public static ABBA1700ProfileEntry getCleanInstance(int nrOfChannels) throws IOException {
        return new ABBA1700ProfileEntry(nrOfChannels);
    }

    public void add(ABBA1700ProfileEntry pe) {
        int channels = getValues().length;
        if (pe.getValues().length < getValues().length)
            channels = pe.getValues().length;
        if (pe.getValues().length > getValues().length)
            changeNrOfChannels(pe.getValues().length);
        for (int i=0; i<channels; i++) {
            getValues()[i] += pe.getValues()[i];
        }
        // KV 21062004
        int status = getStatus() | pe.getStatus();
        setStatus(status);
    }

    private void changeNrOfChannels(int newNrOfChannels) {
        long[] newValues = new long[newNrOfChannels];
        long[] currentValues = getLastValues();
        for (int i = 0;i<currentValues.length;i++)
            newValues[i] = currentValues[i];
        intervalValues.set(intervalValues.size()-1, newValues);
    }


    public boolean isMarker() {
        return ((type == POWERUP) || (type == CONFIGURATIONCHANGE) || (type == POWERDOWN) ||
                (type == NEWDAY) || (type == TIMECHANGE) || (type == DAYLIGHTSAVING) ||
                (type == LOADPROFILECLEARED) || (type == FORCEDENDOFDEMAND) || (type == ENDOFDATA));
    }

    private void fillAttributes(ByteArrayInputStream bai,int nrOfChannels) throws IOException {

        if (bai==null) {
            initValues(nrOfChannels);
            return;
        }

        type = ProtocolUtils.getVal(bai);

        if (type == EXTERNALDATAENTRY) {
            info[INFO_NROFINTERVALS_EXTERNAL] = (((int)ProtocolUtils.getShortLE(bai)&0xFFFF)-4)/(nrOfChannels*3);
            info[INFO_STATUS] = 0;
            if (nrOfChannels ==0) throw new IOException("ABBA1700ProfileEntry, fillAttributes, INFO_STATUS received with nrofchannels == 0 (external data)!");
            for (int interval=0;interval<info[INFO_NROFINTERVALS_EXTERNAL];interval++) {
                initValues(nrOfChannels);
                for (int i=0;i<nrOfChannels;i++) {
                    long val = (int)Long.parseLong(Long.toHexString(ProtocolUtils.getLong(bai,3)));
                    values[i] = (val/10) * Calculate.exp(val % 10);
                }
            }
            ProtocolUtils.getVal(bai); // skip closing EXTERNALDATAENTRY marker
            return;
        } // if (type == EXTERNALDATAENTRY)

        if (type == ENDOFDATA) {
            return;
        } // if (type == ENDOFDATA)

        if (!isMarker()) {
            info[INFO_STATUS] = type;
            if (nrOfChannels ==0) throw new IOException("ABBA1700ProfileEntry, fillAttributes, INFO_STATUS received with nrofchannels == 0 !");
            initValues(nrOfChannels);
            for (int i=0;i<nrOfChannels;i++) {
                long val = (int)Long.parseLong(Long.toHexString(ProtocolUtils.getLong(bai,3)));
                values[i] = (val/10) * Calculate.exp(val%10);
            }
        }
        else {
            date = (long)ProtocolUtils.getIntLE(bai)&0xFFFFFFFFL;
            if ((type == NEWDAY) || (type == CONFIGURATIONCHANGE)) {
                info[INFO_CHANNELMASK] = (int)ProtocolUtils.getShort(bai)&0xFFFF;
                info[INFO_INTEGRATIONPERIOD] = ProtocolUtils.getVal(bai);
            }
        }

    } // private void fillAttributes(ByteArrayInputStream bai,int nrOfChannels) throws IOException



    protected int getType() {
        return type;
    }
    // integrationPeriod attribute:
    final int[] INTEGRATIONPERIODS={1,2,3,4,5,6,10,15,20,30,60};
    protected int getIntegrationPeriod() {
        return INTEGRATIONPERIODS[info[INFO_INTEGRATIONPERIOD]&0x0F]*60;
    }

    protected int getChannelmask() {
        return ((info[INFO_CHANNELMASK]&0x7F00)>>1)|(info[INFO_CHANNELMASK]&0x7F);
    }
    protected int getNumberOfChannels() {
        int nrOfChannels=0;
        int channelMask = getChannelmask();
        for (long i=1; i!=0x10000 ; i<<=1)
        if ((channelMask & i) != 0) nrOfChannels++;
        return nrOfChannels;
    }

    public boolean isExternalData() {
        return (type == EXTERNALDATAENTRY);
    }

    public int getNrOfIntervals() {
        return intervalValues.size();
    }

    protected boolean isDST() {
        return ((info[INFO_CHANNELMASK] & 0x0080)>0);
    }
    protected int getStatus() {
        return info[INFO_STATUS];
    }

    private void setStatus(int status) {
        info[INFO_STATUS]=status;
    }

    protected long[] getValues() {
        return getValues(0);
    }

    protected long[] getValues(int index) {
        return (long[])intervalValues.get(index);
    }

    protected long[] getLastValues() {
        return (long[])intervalValues.get(intervalValues.size()-1);
    }


    protected long getTime() {
        return date;
    }

    public String toString(TimeZone timeZone, boolean dst) {
        StringBuffer strBuff = new StringBuffer();
        if (type == EXTERNALDATAENTRY) {
           strBuff.append("-----------> External data entries: "+info[INFO_NROFINTERVALS_EXTERNAL]+" intervals");
        }
        else if (!isMarker()) {
           strBuff.append("Demanddata:\n");
           strBuff.append("   Status: 0x"+Integer.toHexString(info[INFO_STATUS])+"\n");
           for(int i=0;i<values.length;i++) {
              strBuff.append("   Channel "+i+": "+values[i]+"\n");
           }
        }
        else if (type == ENDOFDATA) {
            strBuff.append("End of data\n");
        }
        else {
            Calendar cal = ProtocolUtils.getCalendar(timeZone,date);

            strBuff.append("Marker: 0x"+Integer.toHexString(type)+" at "+cal.getTime()+" "+date+"\n");

            if ((type == NEWDAY) || (type == CONFIGURATIONCHANGE)) {
                strBuff.append("   ChannelMask: 0x"+Integer.toHexString(getChannelmask()));
                strBuff.append("   IntegrationTime: "+Integer.toString(getIntegrationPeriod()));
                strBuff.append("   dst: "+isDST());
            }
        }

        return strBuff.toString();
    }

    private int bit(int index) {
        return 0x1 << index;
    }
} // public class ABBA1700ProfileEntry
