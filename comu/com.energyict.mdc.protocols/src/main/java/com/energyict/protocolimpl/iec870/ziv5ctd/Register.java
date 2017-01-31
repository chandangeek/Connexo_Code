/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Register.java
 *
 * Created on 27 March 2006, 13:49
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.mdc.common.Quantity;

/** @author fbo */

class Register {

    Integer address;
    Quantity quantity;
    int status;

    /** Creates a new instance of Register */
    public Register( int address, Quantity quantity ){
        this( new Integer(address), quantity );
    }

    /** Creates a new instance of Register */
    public Register(Integer address, Quantity quantity) {
        this.address = address;
        this.quantity = quantity;
    }

    public Quantity getQuantity( ){
        return quantity;
    }

    public String toString(){
        return "Register[ addr:" + address + ", quantity:" + quantity + " ]";
    }

}
