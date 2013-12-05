/*
 * TimeDateQualifier.java
 *
 * Created on 4 november 2005, 16:53
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
public class TimeDateQualifier {

    private int timeDataQualBitfield;
    private int dayOfWeek;
    private boolean dstFlag;
    private boolean gmtFlag;
    private boolean timeZoneAppliedFlag;
    private boolean dstAppliedFlag;

    /** Creates a new instance of TimeDateQualifier */
    public TimeDateQualifier(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        setTimeDataQualBitfield(C12ParseUtils.getInt(data,offset));
        setDayOfWeek(getTimeDataQualBitfield()&0x07);
        setDstFlag((getTimeDataQualBitfield() & 0x08) == 0x08);
        setGmtFlag((getTimeDataQualBitfield() & 0x10) == 0x10);
        setTimeZoneAppliedFlag((getTimeDataQualBitfield() & 0x20) == 0x20);
        setDstAppliedFlag((getTimeDataQualBitfield() & 0x40) == 0x40);
    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TimeDateQualifier: timeDataQualBitfield=0x"+Integer.toHexString(getTimeDataQualBitfield()));
        strBuff.append(", dstFlag="+dstFlag+", gmtFlag="+gmtFlag+", timeZoneAppliedFlag="+timeZoneAppliedFlag+", dstAppliedFlag="+dstAppliedFlag+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return 1;
    }

    public int getTimeDataQualBitfield() {
        return timeDataQualBitfield;
    }

    public void setTimeDataQualBitfield(int timeDataQualBitfield) {
        this.timeDataQualBitfield = timeDataQualBitfield;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public boolean isDstFlag() {
        return dstFlag;
    }

    public void setDstFlag(boolean dstFlag) {
        this.dstFlag = dstFlag;
    }

    public boolean isGmtFlag() {
        return gmtFlag;
    }

    public void setGmtFlag(boolean gmtFlag) {
        this.gmtFlag = gmtFlag;
    }

    public boolean isTimeZoneAppliedFlag() {
        return timeZoneAppliedFlag;
    }

    public void setTimeZoneAppliedFlag(boolean timeZoneAppliedFlag) {
        this.timeZoneAppliedFlag = timeZoneAppliedFlag;
    }

    public boolean isDstAppliedFlag() {
        return dstAppliedFlag;
    }

    public void setDstAppliedFlag(boolean dstAppliedFlag) {
        this.dstAppliedFlag = dstAppliedFlag;
    }
}

