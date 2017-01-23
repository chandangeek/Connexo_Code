/*
 * BillingValue.java
 *
 * Created on 12 oktober 2004, 15:11
 */

package com.energyict.protocolimpl.dlms.actarisace6000;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.ScalerUnit;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.util.Date;
/**
 *
 * @author  Koen
 */
public class BillingValue {

    Date captureDateTime;
    Quantity quantity;
    ObisCode obisCode;
    long value;
    ScalerUnit scalerUnit;

    /** Creates a new instance of BillingValue */
    public BillingValue(Date captureDateTime, long value, ScalerUnit scalerUnit, ObisCode obisCode) {
        this.captureDateTime=captureDateTime;
        this.obisCode=obisCode;
        this.value=value;
        this.scalerUnit=scalerUnit;
        quantity = new Quantity(BigDecimal.valueOf(value),scalerUnit.getEisUnit());
    }


    public BillingValue(Date captureDateTime, Quantity quantity, ObisCode obisCode) {
        this.captureDateTime=captureDateTime;
        this.quantity=quantity;
        this.obisCode=obisCode;
        value = quantity.longValue();
        scalerUnit = new ScalerUnit(quantity.getUnit().getScale(),quantity.getUnit().getDlmsCode());
    }

    public String toString() {
        return "ObisCode="+obisCode+", quantity="+(quantity)+(captureDateTime!=null?(", capture datetime="+captureDateTime):"");
    }

    /**
     * Getter for property captureDateTime.
     * @return Value of property captureDateTime.
     */
    public java.util.Date getCaptureDateTime() {
        return captureDateTime;
    }

    /**
     * Setter for property captureDateTime.
     * @param captureDateTime New value of property captureDateTime.
     */
    public void setCaptureDateTime(java.util.Date captureDateTime) {
        this.captureDateTime = captureDateTime;
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
    public com.energyict.dlms.ScalerUnit getScalerUnit() {
        return scalerUnit;
    }

    /**
     * Setter for property scalerUnit.
     * @param scalerUnit New value of property scalerUnit.
     */
    public void setScalerUnit(com.energyict.dlms.ScalerUnit scalerUnit) {
        this.scalerUnit = scalerUnit;
    }

}
