/*
 * IntervalSet.java
 *
 * Created on 8 november 2005, 14:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class IntervalSet {
    
    private int[] extendedIntervalStatus; // 8 bit
    
    // COMMON nibble status flag bits
    // bit 0 DST active during or at start of interval
    // bit 1 powerfail within interval
    // bit 2 clock reset forward during interval
    // bit 3 clock reset backwards during interval
    
    // channel nibble status value 0..15
    // bit 0 no status flag
    // bit 1 overflow
    // bit 2 partial interval due to protocolcommon state
    // bit 3 long interval due to protocolcommon state
    // bit 4 skipped interval due to protocolcommon state
    // bit 5 interval contains test mode data
    // bit 6..15 undefined
    
    private IntervalFormat[] intervalData;
    
    private boolean valid;
            
    /** Creates a new instance of IntervalSet */
    public IntervalSet(byte[] data,int offset,TableFactory tableFactory, int set, boolean valid) throws IOException {
        setValid(valid);
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();        
        if (alpt.getLoadProfileSet().isExtendedIntStatusFlag()) {
            setExtendedIntervalStatus(new int[(alpt.getLoadProfileSet().getNrOfChannelsSet()[set]/2)+1]);
            for (int i=0;i<getExtendedIntervalStatus().length;i++) {
                getExtendedIntervalStatus()[i] = C12ParseUtils.getInt(data,offset++);
            }
        }
        
        setIntervalData(new IntervalFormat[alpt.getLoadProfileSet().getNrOfChannelsSet()[set]]);
        for (int i=0;i<getIntervalData().length;i++) {
            getIntervalData()[i] = new IntervalFormat(data, offset, tableFactory, set);
            offset+=IntervalFormat.getSize(tableFactory, set);
        }
        
        
    }
    
    public int getCommonStatus() {
        return extendedIntervalStatus==null?0:(extendedIntervalStatus[0]>>4)&0x0F;
    }
    public int getChannelStatus(int channelId) {
        int index = (channelId+1)/2;
        int nibble = (channelId+1)%2;
        if (nibble==0)
            return extendedIntervalStatus==null?0:(extendedIntervalStatus[index]>>4)&0x0F;
        else
            return extendedIntervalStatus==null?0:extendedIntervalStatus[index]&0x0F;
    }
    
    public boolean isDSTActive() {
        return ((getCommonStatus() & 0x01)==0x01);
    }
    
    public boolean isClockResetBackwards() {
        return ((getCommonStatus() & 0x08)==0x08);
    }
    public boolean isClockResetForward() {
        return ((getCommonStatus() & 0x04)==0x04);
    }
    public boolean isPowerFailWithintheInterval() {
        return ((getCommonStatus() & 0x02)==0x02);
    }
    
    public boolean isPartialDueToCommonState() {
        for (int channelId=0;channelId<intervalData.length;channelId++)
            if (getChannelStatus(channelId) != 2)
                return false;
        
        return true;
    }
            
    
    public int getCommon2EIStatus() {
        int eiStatus=0;
        // powerfail within interval
        if ((getCommonStatus() & 0x02)==0x02)
            eiStatus |= (IntervalStateBits.POWERDOWN | IntervalStateBits.POWERUP);
        // clock reset forward during interval
        if ((getCommonStatus() & 0x04)==0x04)
            eiStatus |= (IntervalStateBits.SHORTLONG);
        // clock reset backwards during interval
        if ((getCommonStatus() & 0x08)==0x08)
            eiStatus |= (IntervalStateBits.SHORTLONG);
        return eiStatus;
    }
    
    public int getCommon2EIStatus(boolean powerStateOn) {
    	//powerStateOn => true, power is on, false, power is off
        int eiStatus=0;
        // powerfail within interval
        if ((getCommonStatus() & 0x02)==0x02) {
        	if (powerStateOn) {
        		eiStatus |= IntervalStateBits.POWERUP;
        	}
        	else {
        		eiStatus |= IntervalStateBits.POWERDOWN;
        	}
        }
        // clock reset forward during interval
        if ((getCommonStatus() & 0x04)==0x04)
            eiStatus |= (IntervalStateBits.SHORTLONG);
        // clock reset backwards during interval
        if ((getCommonStatus() & 0x08)==0x08)
            eiStatus |= (IntervalStateBits.SHORTLONG);
        return eiStatus;
    }
    
    public int getchannel2EIStatus(int channelId) {
        switch(getChannelStatus(channelId)) {
            case 0: // no status
                return 0;
            case 1: // overflow
                return IntervalStateBits.OVERFLOW;
            case 2: // partial interval due to protocolcommon state
                return IntervalStateBits.SHORTLONG;
            case 3: // long interval due to protocolcommon state
                return IntervalStateBits.SHORTLONG;
            case 4: // skipped interval due to protocolcommon state
                return IntervalStateBits.MISSING;
            case 5: // interval contains test mode data
                return IntervalStateBits.TEST;
            default:    
                return IntervalStateBits.OTHER;
        } 
    }
    
    
    public boolean isValid() {
        return valid;
    }
    
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("IntervalSet: ("+valid+") \n");
        
        if (getExtendedIntervalStatus()!=null) {
            for (int i=0;i<getExtendedIntervalStatus().length;i++) {
                strBuff.append("    extendedIntervalStatus["+i+"]=0x"+Integer.toHexString(extendedIntervalStatus[i])+"\n");
            }
        }
        for (int i=0;i<getIntervalData().length;i++) {
            strBuff.append("    intervalData["+i+"]="+intervalData[i]+"\n");
        }
        return strBuff.toString();
        
    }
    
    static public int getSize(TableFactory tableFactory, int set) throws IOException {
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        int size=0;        
        if (alpt.getLoadProfileSet().isExtendedIntStatusFlag()) {
            size+=(alpt.getLoadProfileSet().getNrOfChannelsSet()[set]/2)+1;
        }
        size+=(alpt.getLoadProfileSet().getNrOfChannelsSet()[set]*IntervalFormat.getSize(tableFactory,set));
        return size;
    }      

    public int[] getExtendedIntervalStatus() {
        return extendedIntervalStatus;
    }

    public void setExtendedIntervalStatus(int[] extendedIntervalStatus) {
        this.extendedIntervalStatus = extendedIntervalStatus;
    }

    public IntervalFormat[] getIntervalData() {
        return intervalData;
    }

    public void setIntervalData(IntervalFormat[] intervalData) {
        this.intervalData = intervalData;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
