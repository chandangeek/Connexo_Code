/*
 * BillingValue.java
 *
 * Created on 12 oktober 2004, 15:11
 */

package com.energyict.protocolimpl.dlms.iskrame37x;

import com.energyict.dlms.ScalerUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.protocol.NoSuchRegisterException;

import java.math.BigDecimal;
import java.util.Date;
/**
 *
 * @author  Koen
 */
public class BillingValue {

    Date captureDateTime;
    Quantity quantityR1;
    Quantity quantityR2;
    Quantity quantityTotalPlus;
    Quantity quantityTotalMinus;
    int status;
    ObisCode obisCode;
    long value;
    ScalerUnit scalerUnit;
    Quantity quantity;

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

    public BillingValue(Date captureDateTime, int state, long R1, long R2, long plus, long minus){
    	//unit is the same, eg. WattHour
    	scalerUnit = new ScalerUnit(0,30);
    	this.captureDateTime = captureDateTime;
    	this.quantityR1 = new Quantity(BigDecimal.valueOf(R1), scalerUnit.getEisUnit());
    	this.quantityR2 = new Quantity(BigDecimal.valueOf(R2), scalerUnit.getEisUnit());
    	this.quantityTotalPlus = new Quantity(BigDecimal.valueOf(plus), scalerUnit.getEisUnit());
    	this.quantityTotalMinus = new Quantity(BigDecimal.valueOf(minus), scalerUnit.getEisUnit());
    	this.status = state;
    }


	public long getRegisterValue(int register) throws NoSuchRegisterException{

    	switch(register){
    		case 0:
    			return quantityR1.longValue();

    		case 1:
    			return quantityR2.longValue();

    		case 2:
    			return quantityTotalPlus.longValue();

    		case 3:
    			return quantityTotalMinus.longValue();

    		default:
    			throw new NoSuchRegisterException("No such register supported!");
    	}
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
