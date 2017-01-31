/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DateValuePair.java
 *
 * Created on 21 oktober 2004, 15:52
 */

package com.energyict.protocolimpl.iec1107.vdew;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author  Koen
 */
public class DateValuePair {


    Date date;
    BigDecimal value;
    /** Creates a new instance of DateValuePair */
    public DateValuePair(Date date,BigDecimal value) {
        this.date = date;
        this.value = value;
    }

    /**
     * Getter for property value.
     * @return Value of property value.
     */
    public java.math.BigDecimal getValue() {
        return value;
    }

    /**
     * Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(java.math.BigDecimal value) {
        this.value = value;
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
        return getValue()+", "+getDate();
    }
}
