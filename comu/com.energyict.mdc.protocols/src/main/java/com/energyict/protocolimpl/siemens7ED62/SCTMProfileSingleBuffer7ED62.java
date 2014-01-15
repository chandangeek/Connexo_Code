/*
 * SCTMProfileMetcom2.java
 *
 * Created on 6 februari 2003, 11:45
 */

package com.energyict.protocolimpl.siemens7ED62;

import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.common.interval.IntervalStateBits;

/**
 *
 * @author  Koen
 */
public class SCTMProfileSingleBuffer7ED62 extends SCTMProfileSingleBuffer {



    /** Creates a new instance of SCTMProfile */
    public SCTMProfileSingleBuffer7ED62(byte[] frame) {
        super(frame);
    }

     protected int getMeterType() {
         return SIEMENS7ED62;
     }

    public int getEIStatusFromDeviceStatus(int status) {
       /*
        *  statusbytes
        *  bit15 tijd is gezet tijdens interval   8000
        *  bit14 verkorte meetperiode (powerfail) 4000
        *  bit13 parameter gewijzigd              2000
        *  bit12 alarm                            1000
        *  bit11 DST active                       0800
        *  bit10 -                                0400
        *  bit9 ganse meetperiode spanningsloos   0200
        *  bit8 Error duringselftest              0100
        *  bit7 crc fout in meetperiode           0080
        *  bit6 version                           0040
        *  bit5 version                           0020
        *  bit 4..0 -                             001F
        *
        */
        int eiStatus=0;
        if ((status&0x8000) != 0) eiStatus |= IntervalData.SHORTLONG;
        if ((status&0x4000) != 0) eiStatus |= (IntervalData.SHORTLONG|IntervalData.POWERDOWN|IntervalData.POWERUP);
        if ((status&0x2000) != 0) eiStatus |= IntervalData.CONFIGURATIONCHANGE;
        if ((status&0x1000) != 0) eiStatus |= IntervalData.CORRUPTED;
        if ((status&0x200) != 0) eiStatus |= IntervalData.CORRUPTED;
        if ((status&0x100) != 0) eiStatus |= IntervalData.CORRUPTED;
        if ((status&0x80) != 0) eiStatus |= IntervalData.CORRUPTED;
        return eiStatus;
    }

     protected int getEIStatusFromMeteringValueStatus(int status, int deviceStatus) {
         int eiStatus=0;
         if ((status & MODIFIEDVALUE) != 0)
             eiStatus|= IntervalStateBits.MODIFIED;
         if ((status & CORRUPTEDVALUE) != 0)
             eiStatus|=IntervalStateBits.CORRUPTED;
         if ((status & VALUEOVERFLOW) != 0)
             eiStatus|=IntervalStateBits.OVERFLOW;
         return eiStatus;
     }

} // public class SCTMProfileMetcom2FBC
