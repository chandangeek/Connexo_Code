/*
 * DateValuePair.java
 *
 * Created on 21 oktober 2004, 15:52
 */

package com.energyict.protocolimpl.iec1107.vdew;

import java.util.Date;

import com.energyict.cbo.Quantity;

/**
 *
 * @author  Koen
 */
public class DateQuantityPair {

    
    Date date;
    Quantity quantity;
    /** Creates a new instance of DateValuePair */
    public DateQuantityPair(Date date, Quantity quantity) {
        this.date = date;
        this.quantity = quantity;
    }
    
    /**
     * Getter for property value.
     * @return Value of property value.
     */
    public Quantity getQuantity() {
        return quantity;
    }
    
    /**
     * Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(Quantity quantity) {
        this.quantity = quantity;
    }
    
    /**
     * Getter for property date.
     * @return Value of property date.
     */
    public java.util.Date getDate() {
        return date;
    }
    
    /**
     * Setter for property date.
     * @param date New value of property date.
     */
    public void setDate(java.util.Date date) {
        this.date = date;
    }
    
    public String toString() {
        return getQuantity()+", "+getDate();
    }
}
