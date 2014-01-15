/*
 * SCTMProfileMultipleBufferFCL.java
 *
 * Created on 15 december 2004, 10:35
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.common.interval.IntervalStateBits;

import java.util.List;

/**
 *
 * @author  Koen
 */
public class SCTMProfileMultipleBufferFCL extends SCTMProfileMultipleBuffer {

    /** Creates a new instance of SCTMProfileMultipleBufferFCL */
    public SCTMProfileMultipleBufferFCL(List frames, ChannelMap channelMap, List bufferStructures) {
        super(frames,channelMap,bufferStructures);

    }

    protected int getEIStatusFromMeteringValueStatus(int status, int deviceStatus) {
         int eiStatus=0;
         if ((status & CORRUPTEDVALUE) != 0) {
             if (((deviceStatus&U_BIT)==0) && ((deviceStatus&A_BIT)==0))
                 eiStatus|= IntervalStateBits.CORRUPTED;
         }
         return eiStatus;
     }

     protected int getEIStatusFromDeviceStatus(int status) {
        int eiStatus=0;
        if ((status&T_BIT) != 0) eiStatus |= IntervalData.SHORTLONG;
        if ((status&U_BIT) != 0) eiStatus |= IntervalData.SHORTLONG;
        if ((status&M_BIT) != 0) eiStatus |= IntervalData.CONFIGURATIONCHANGE;
        if ((status&A_BIT) != 0) eiStatus |= IntervalData.OTHER;
        if ((status&NP_BIT) != 0) eiStatus |= IntervalData.MISSING;
        if ((status&F_BIT) != 0) eiStatus |= IntervalData.CORRUPTED;
        return eiStatus;
    }

}
