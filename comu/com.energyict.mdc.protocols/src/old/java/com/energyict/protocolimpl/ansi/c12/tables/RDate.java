/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RDate.java
 *
 * Created on 30 oktober 2005, 2:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;


import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class RDate {

    private int month;

    // month=1..13
    // month = 14
    private int weekday=-1;

    // month=1..13
    private int offset=-1;
    private int day=-1;

    // month = 15
    private int period=-1;
    private int delta=-1;

    /** Creates a new instance of RDate */
    public RDate(byte[] data,int off,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        int temp = C12ParseUtils.getInt(data,off, 2,dataOrder);
        setMonth(temp & 0x000F);
        if ((getMonth()>=1) && (getMonth()<=13)) {
            setOffset((temp >> 4) & 0x000F);
            setWeekday((temp >> 8) & 0x0007);
            setDay((temp >> 11) & 0x001F);
        }
        else if (getMonth() == 14) {
            setWeekday((temp >> 8) & 0x0007);

        }
        else if (getMonth() == 14) {
            setPeriod((temp >> 4) & 0x003F);
            setDelta((temp >> 10) & 0x003F);
        }
    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RDate: month="+getMonth()+", weekday="+getWeekday()+", offset="+getOffset()+", day="+getDay()+", period="+getPeriod()+", delta="+getDelta()+"\n");
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return 2;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }
}
