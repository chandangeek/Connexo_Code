/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import java.util.Date;

/** @author  Koen */

public class MainRegister {

    Quantity quantity=null;
    HistoricalRegister historicalValues = new HistoricalRegister();

    /** Creates a new instance of MainRegister */
    public MainRegister() { }

    public MainRegister(Quantity quantity) {
        this.quantity=quantity;
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

    public HistoricalRegister getHistoricalValues() {
        return historicalValues;
    }

    public void setHistoricalValues(HistoricalRegister historicalValues) {
        this.historicalValues = historicalValues;
    }

    /** Return the Register as a RegisterValue.
     * @param obisCode that identifies the register
     * @return registerValue
     */
    public RegisterValue toRegisterValue( ObisCode obisCode ){
        Date date = historicalValues.getBillingDate();
        return new RegisterValue( obisCode, quantity, date, date );
    }

}
