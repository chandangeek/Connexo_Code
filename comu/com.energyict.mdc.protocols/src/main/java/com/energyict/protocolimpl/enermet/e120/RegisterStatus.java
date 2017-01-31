/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.enermet.e120;

import com.energyict.mdc.common.interval.IntervalStateBits;

import java.util.ArrayList;

class RegisterStatus {

    final static RegisterStatus TIME_CHANGE =
        new RegisterStatus( (byte)0x1, IntervalStateBits.SHORTLONG,
            "Time has been changed during the measuring period");

    final static RegisterStatus ERROR =
        new RegisterStatus( (byte)0x2, IntervalStateBits.OTHER,
            "Internal/External error during the measuring period");

    final static RegisterStatus POWER_FAILURE =
        new RegisterStatus( (byte)0x4, IntervalStateBits.POWERDOWN,
            "Power failure during the measuring period");

    /** This flag is set on intervals in the future.  The meter returns
     * requested intervals, while they should not even exists. */
    final static RegisterStatus ILLEGAL_VALUE =
        new RegisterStatus( (byte)0x8, IntervalStateBits.CORRUPTED,
            "Illegal value request");

    private byte flag;
    private int intervalStateBit;
    private String description;

    private RegisterStatus(byte flag, int intervalStateBit, String descr ){
        this.flag = flag;
        this.intervalStateBit = intervalStateBit;
        this.description = descr;
    }

    int getIntervalStateBit(){
        return intervalStateBit;
    }

    static ArrayList getStatuses(byte aByte){
        ArrayList result = new ArrayList();
        if( (aByte & 0x01) > 0 ) result.add(TIME_CHANGE);
        if( (aByte & 0x02) > 0 ) result.add(ERROR);
        if( (aByte & 0x04) > 0 ) result.add(POWER_FAILURE);
        if( (aByte & 0x08) > 0 ) result.add(ILLEGAL_VALUE);
        return result;
    }

    boolean isIllegal(){
        return ILLEGAL_VALUE.equals(this);
    }

    public String toString( ){
        return
            new StringBuffer()
                .append( "RegisterStatus [" )
                .append( flag ).append( ", " )
                .append( intervalStateBit ).append( ", " )
                .append( description ).append( "]" )
                .toString();
    }

}
