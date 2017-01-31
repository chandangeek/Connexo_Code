/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SCTMProfileMetcom2.java
 *
 * Created on 6 februari 2003, 11:45
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.IntervalData;

import com.energyict.protocolimpl.siemens7ED62.SCTMProfileSingleBuffer;
/**
 *
 * @author  Koen
 */
public class SCTMProfileSingleBufferMetcom2 extends SCTMProfileSingleBuffer {

    public SCTMProfileSingleBufferMetcom2(byte[] frame) {
        super(frame);
    }

     protected int getEIStatusFromMeteringValueStatus(int status, int deviceStatus) {
         int eiStatus=0;

         if (getIntervalStatusBehaviour()==1) {
             if (((deviceStatus&U_BIT)==U_BIT) && ((deviceStatus&T_BIT)==0)) {
                 eiStatus|= IntervalStateBits.POWERDOWN; // changes for EDP 23092005
                 eiStatus|=IntervalStateBits.POWERUP; // changes for EDP 23092005
             }
         }
         else {
             if ((status & CORRUPTEDVALUE) != 0) {
                 eiStatus|=IntervalStateBits.POWERDOWN; // changes for EDP 23092005
                 eiStatus|=IntervalStateBits.POWERUP; // changes for EDP 23092005

                 // changes for EDP 23092005
                 //if (((deviceStatus&U_BIT)==0)) {
                 //    eiStatus|=IntervalStateBits.CORRUPTED;
                 //}
             }
         }

         if ((status & VALUEOVERFLOW) != 0)
             eiStatus|=IntervalStateBits.OVERFLOW;

         return eiStatus;
     }

     protected int getMeterType() {
         return METCOM2;
     }

     protected int getEIStatusFromDeviceStatus(int status) {
        int eiStatus=0;
        if ((status&T_BIT) != 0) {
            eiStatus |= IntervalData.SHORTLONG;
        }
        if ((status&U_BIT) != 0) {
            eiStatus |= IntervalData.SHORTLONG;
        }
        if ((status&M_BIT) != 0) {
            eiStatus |= IntervalData.CONFIGURATIONCHANGE;
        }
        //if ((status&A_BIT) != 0) eiStatus |= IntervalData.OTHER;  changes for EDP 23092005
        if ((status&NP_BIT) != 0) {
            eiStatus |= IntervalData.MISSING;
        }
        if ((status&AL_BIT) != 0) {
            eiStatus |= IntervalData.CORRUPTED;
        }
        if ((status&F_BIT) != 0) {
            eiStatus |= IntervalData.CORRUPTED;
        }
        return eiStatus;
    }

}
