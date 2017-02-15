/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;

import com.energyict.dlms.ScalerUnit;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author  Koen
 */
public class BillingValue {

    Date eventDateTime;
    Quantity quantity;
    ObisCode obisCode;
    long value;
    ScalerUnit scalerUnit;

    /** Creates a new instance of BillingValue */
    public BillingValue(Date eventDateTime, long value, ScalerUnit scalerUnit, ObisCode obisCode) {
        this.eventDateTime =eventDateTime;
        this.obisCode=obisCode;
        this.value=value;
        this.scalerUnit=scalerUnit;
        quantity = new Quantity(BigDecimal.valueOf(value),scalerUnit.getEisUnit());
    }


    public BillingValue(Date eventDateTime, Quantity quantity, ObisCode obisCode) {
        this.eventDateTime =eventDateTime;
        this.quantity=quantity;
        this.obisCode=obisCode;
        value = quantity.longValue();
        scalerUnit = new ScalerUnit(quantity.getUnit().getScale(),quantity.getUnit().getDlmsCode());
    }

    public String toString() {
        return "ObisCode="+obisCode+", quantity="+(quantity)+(eventDateTime !=null?(", event datetime="+ eventDateTime):"");
    }

    /**
     * Getter for property eventDateTime.
     * @return Value of property eventDateTime.
     */
    public Date getEventDateTime() {
        return eventDateTime;
    }

    /**
     * Setter for property eventDateTime.
     * @param eventDateTime New value of property eventDateTime.
     */
    public void setEventDateTime(Date eventDateTime) {
        this.eventDateTime = eventDateTime;
    }



    /**
     * Getter for property obisCode.
     * @return Value of property obisCode.
     */
    public ObisCode getObisCode() {
        return obisCode;
    }

    /**
     * Setter for property obisCode.
     * @param obisCode New value of property obisCode.
     */
    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
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

    /**
     * Getter for property value.
     * @return Value of property value.
     */
    public long getValue() {
        return value;
    }

    /**
     * Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Getter for property scalerUnit.
     * @return Value of property scalerUnit.
     */
    public ScalerUnit getScalerUnit() {
        return scalerUnit;
    }

    /**
     * Setter for property scalerUnit.
     * @param scalerUnit New value of property scalerUnit.
     */
    public void setScalerUnit(ScalerUnit scalerUnit) {
        this.scalerUnit = scalerUnit;
    }
}