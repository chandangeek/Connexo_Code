/*
 * DateValuePair.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew;

import com.energyict.cbo.Unit;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author  Koen
 */
public class DateValuePair {


    Date date;
    BigDecimal value;
    Unit unit;
    String text;

    /** Creates a new instance of DateValuePair */
    public DateValuePair(Date date,BigDecimal value, Unit unit, String text) {
        this.date = date;
        this.value = value;
        this.unit = unit;
        this.text = text;
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

    public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public String toString() {
        return getValue()+", "+getDate();
    }

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
