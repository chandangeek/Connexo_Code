/*
 * OperatingSetUpBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class OperatingSetUpBasePage extends AbstractBasePage {

    /*
     Bit 0 should be set if the external line frequency is 60 Hz. It should be cleared if the
     line frequency is 50 Hz.

     If bit 1 is set, the real time clock and demand intervals will be synchronized to the
     external line frequency. If bit 1 is clear, the real time clock and demand intervals
     will be synchronized to the microprocessor time keeping.

     If bit 2 is set, energy will only be accumulated if it is delivered. If it is clear, energy
     will be added regardless of whether it is delivered or received.

     If bit 3 is set, the meter will run as a thermal demand meter. If bit 3 is clear, the
     meter will run as a sliding or block demand meter.

     Bit 4 is set for Canadian meters so that the software can restrict download access
     for certain parameters.

     If bit 5 is set, the meter will use Daylight Savings Time changes; if it is clear, DST
     records in the TOU schedule will be ignored.

     If bit 6 is set, a season change will not occur until a demand reset is performed. If it
     is clear, the season change will occur at 12:00 on the date specified in the TOU
     schedule.

     If bit 7 is set, the WDE segments will light up to show the current TOU day type:
     weekday, Saturday, Sunday, holiday (from left to right, 5th position unused). If it is
     clear, the WDE segments will operate normally.
     */

    private int flags;

    private boolean dstEnabled; // bit 4

    /** Creates a new instance of OperatingSetUpBasePage */
    public OperatingSetUpBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("OperatingSetUpBasePage:\n");
        strBuff.append("   dstEnabled="+isDstEnabled()+"\n");
        strBuff.append("   flags=0x"+Integer.toHexString(getFlags())+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x2196, 1);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setFlags((int)data[0]&0xff);
        setDstEnabled((getFlags() & 0x20) == 0x20);
    }

    public boolean isDstEnabled() {
        return dstEnabled;
    }

    private void setDstEnabled(boolean dstEnabled) {
        this.dstEnabled = dstEnabled;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }


} // public class RealTimeBasePage extends AbstractBasePage
