/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MainRegister.java
 *
 * Created on 14 juni 2004, 13:05
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.common.Quantity;

/**
 *
 * @author  Koen
 */
public class MainRegister {

    Quantity quantity=null;
    HistoricalValueSetInfo historicalValueSetInfo=new HistoricalValueSetInfo();

    /** Creates a new instance of MainRegister */
    public MainRegister() {
    }
    public MainRegister(Quantity quantity) {
        this.quantity=quantity;
    }

    // implementing BillingRegister
    public void setHistoricalValueSetInfo(HistoricalValueSetInfo historicalValueSetInfo) {
        this.historicalValueSetInfo=historicalValueSetInfo;
    }
    public HistoricalValueSetInfo getHistoricalValueSetInfo() {
        return historicalValueSetInfo;
    }

    /**
     * Getter for property quantity.
     * @return Value of property quantity.
     */
    public Quantity getQuantity() {
        return quantity;
    }

    /**
     * Setter for property quantity.
     * @param quantity New value of property quantity.
     */
    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

}
