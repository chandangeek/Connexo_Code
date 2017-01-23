/*
 * BillingValue.java
 *
 * Created on 10 januari
 */

package com.energyict.protocolimpl.dlms.flex;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.ScalerUnit;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.util.Date;
/**
 *
 * @author  Koen
 *  <B>@beginchanges</B><BR>
	GN|10012008|Taken over everything from the Iskra protocol and adapted where needed
 * @endchanges
 */
public class BillingValue {

    Date captureDateTime;
    Quantity quantityR1;
    Quantity quantityR2;
    Quantity quantityTotalPlus;
    Quantity quantityTotalMinus;
    int status;
//    ObisCode obisCode;
    long value;
    ScalerUnit scalerUnit;

    private static final int RATE1 = 0;
    private static final int RATE2 = 1;
    private static final int PLUS = 2;
    private static final int MINUS = 3;

    /** Creates a new instance of BillingValue */
    public BillingValue(Date captureDateTime, long value, ScalerUnit scalerUnit, ObisCode obisCode) {
        this.captureDateTime=captureDateTime;
//        this.obisCode=obisCode;
        this.value=value;
        this.scalerUnit=scalerUnit;
//        quantity = new Quantity(BigDecimal.valueOf(value),scalerUnit.getUnit());
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

    public BillingValue(Date captureDateTime, Quantity quantity, ObisCode obisCode) {
        this.captureDateTime=captureDateTime;
//        this.quantity=quantity;
//        this.obisCode=obisCode;
        value = quantity.longValue();
        scalerUnit = new ScalerUnit(quantity.getUnit().getScale(),quantity.getUnit().getDlmsCode());
    }


    public long getRate1(){
    	return quantityR1.longValue();
    }

    public long getRegisterValue(int register) throws NoSuchRegisterException{

    	switch(register){
    		case RATE1:
    			return quantityR1.longValue();

    		case RATE2:
    			return quantityR2.longValue();

    		case PLUS:
    			return quantityTotalPlus.longValue();

    		case MINUS:
    			return quantityTotalMinus.longValue();

    		default:
    			throw new NoSuchRegisterException("No such register supported!");
    	}
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
