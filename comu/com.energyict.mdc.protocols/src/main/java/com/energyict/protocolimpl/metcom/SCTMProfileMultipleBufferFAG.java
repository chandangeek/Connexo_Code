/*
 * SCTMProfileMetcom3FAF.java
 *
 * Created on 15 december 2004, 10:35
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.protocol.device.data.IntervalData;
import com.energyict.mdc.protocol.device.data.IntervalStateBits;

import java.util.List;

/**
 *
 * @author  Koen
 */
public class SCTMProfileMultipleBufferFAG extends SCTMProfileMultipleBuffer {

    /** Creates a new instance of SCTMProfileMetcom3FAF */
    public SCTMProfileMultipleBufferFAG(List frames, ChannelMap channelMap, List bufferStructures) {
        super(frames,channelMap,bufferStructures);

    }

     protected int getEIStatusFromMeteringValueStatus(int status, int deviceStatus) {
         int eiStatus=0;
         if ((status & MODIFIEDVALUE) != 0)
             eiStatus|= IntervalStateBits.MODIFIED;
         if ((status & CORRUPTEDVALUE) != 0) {
             if (((deviceStatus&U_BIT)==0) && ((deviceStatus&NP_BIT)==0) && ((deviceStatus&A_BIT)==0))
                 eiStatus|=IntervalStateBits.CORRUPTED;
         }
         if ((status & VALUEOVERFLOW) != 0)
             eiStatus|=IntervalStateBits.OVERFLOW;
         return eiStatus;
     }

     protected int getEIStatusFromDeviceStatus(int status) {
        int eiStatus=0;
        if ((status&T_BIT) != 0) eiStatus |= IntervalData.SHORTLONG;
        if ((status&U_BIT) != 0) eiStatus |= IntervalData.SHORTLONG;
        if ((status&M_BIT) != 0) eiStatus |= IntervalData.CONFIGURATIONCHANGE;
        if ((status&A_BIT) != 0) eiStatus |= IntervalData.OTHER;
        if ((status&TS_BIT) != 0) eiStatus |= IntervalData.OTHER;
        if ((status&NP_BIT) != 0) eiStatus |= IntervalData.MISSING;
        if ((status&F_BIT) != 0) eiStatus |= IntervalData.CORRUPTED;
        return eiStatus;
    }
}
