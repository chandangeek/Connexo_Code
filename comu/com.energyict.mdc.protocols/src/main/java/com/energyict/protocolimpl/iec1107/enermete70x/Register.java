/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Register.java
 *
 * Created on 27 oktober 2004, 18:15
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.mdc.common.Quantity;

import java.util.Date;

/**
 *
 * @author  Koen
 */
public class Register {

    private static final int CUMULATIVE_ENERGY_NO_RESET_AT_BP=2;
    private static final int PERIODIC_ENERGY_RESET_AT_BP=3;
    private static final int PERIODIC_MAX_DEMAND_RESET_AT_BP=4;
    private static final int CUMULATIVE_MAX_DEMAND_RESET_AT_BP=8;

    int type;
    Quantity quantity;
    Date mdTimestamp;
    Date billingTimestamp;

    /** Creates a new instance of Register */
    public Register(int type, Quantity quantity, Date mdTimestamp, Date billingTimestamp) {
        this.type=type;
        this.quantity=quantity;
        this.mdTimestamp=mdTimestamp;
        this.billingTimestamp=billingTimestamp;
    }

    public String toString() {
        return getTypeDescription()+", "+getQuantity()+(mdTimestamp==null?"":", MD:"+mdTimestamp)+(billingTimestamp==null?"":", BP:"+billingTimestamp);
    }

    private String getTypeDescription() {
       switch(type) {
           case CUMULATIVE_ENERGY_NO_RESET_AT_BP:
               return "cumulative energy";
           case PERIODIC_ENERGY_RESET_AT_BP:
               return "periodic energy (reset at bp)";
           case PERIODIC_MAX_DEMAND_RESET_AT_BP:
               return "periodic max demand (reset at bp)";
           case CUMULATIVE_MAX_DEMAND_RESET_AT_BP:
               return "cumulative max demand (reset at bp)";
           default:
               return "unknown registertype "+type;
       }
    }

    /**
     * Getter for property type.
     * @return Value of property type.
     */
    public int getType() {
        return type;
    }

    /**
     * Getter for property quantity.
     * @return Value of property quantity.
     */
    public Quantity getQuantity() {
        return quantity;
    }

    /**
     * Getter for property mdTimestamp.
     * @return Value of property mdTimestamp.
     */
    public java.util.Date getMdTimestamp() {
        return mdTimestamp;
    }

    /**
     * Getter for property billingTimeStamp.
     * @return Value of property billingTimeStamp.
     */
    public java.util.Date getBillingTimestamp() {
        return billingTimestamp;
    }

}
