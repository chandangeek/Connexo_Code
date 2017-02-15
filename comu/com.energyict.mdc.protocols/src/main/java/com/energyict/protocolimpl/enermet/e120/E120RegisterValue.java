/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.enermet.e120;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

class E120RegisterValue {

    private Quantity quantity;

    /** register status (4 bits) */
    private int rawStatus;
    /** list of RegisterStatus objects */
    private List registerStatus;

    /** register state: true = in use, false = not in use */
    private boolean inUse;

    private Date time;

    E120RegisterValue( ){ }

    /** convert to a Quantity object */
    Quantity toQuantity(){
        return quantity;
    }

    /** convert to a RegisterValue for a certain obis code */
    RegisterValue toRegisterValue(ObisCode obisCode){
        return new RegisterValue(obisCode, quantity, new Date() );
    }

    /** register status as stored by meter. */
    int getRawStatus(){
        return rawStatus;
    }

    E120RegisterValue setRawStatus(int status){
        this.rawStatus = status;
        return this;
    }

    /** register status converted to eiStatus. */
    int getProtocolStatus(){
        int result = 0;
        Iterator i = registerStatus.iterator();
        while( i.hasNext() ){
            RegisterStatus rs = (RegisterStatus)i.next();
            result |= rs.getIntervalStateBit();
        }
        return result;
    }

    /* Illegal records may not to be used for.  Probably is used for records
     * that do not actually exist in the meter */
    boolean isIllegal(){
        Iterator iterator = registerStatus.iterator();
        while( iterator.hasNext() ){
            RegisterStatus status = (RegisterStatus)iterator.next();
            if( status.isIllegal() ) return true;
        }
        return false;
    }

    E120RegisterValue setInUse(boolean inUse) {
        this.inUse = inUse;
        return this;
    }

    E120RegisterValue setQuantity(Quantity quantity) {
        this.quantity = quantity;
        return this;
    }

    E120RegisterValue setRegisterStatus(List registerStatus) {
        this.registerStatus = registerStatus;
        return this;
    }

    E120RegisterValue setTime(Date time) {
        this.time = time;
        return this;
    }

    Date getTime(){
        return time;
    }

    public String toString(){
        return new StringBuffer( )
            .append( "E120registerValue [" )
            .append( quantity ).append( ", " )
            .append( time ).append( ", " )
            .append( registerStatus ).append( ", " )
            .append( "In use: " ).append( inUse )
            .append( "]" )
                .toString();
    }

}
