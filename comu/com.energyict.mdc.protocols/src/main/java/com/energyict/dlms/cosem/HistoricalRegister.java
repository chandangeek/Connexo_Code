/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;

import com.energyict.dlms.ScalerUnit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author khe
 *
 */
public class HistoricalRegister implements CosemObject {

	private Date billingDate		= new Date();
	private Date eventTime		= null;
	private Date captureTime		= new Date();
	private Quantity quantityValue	= new Quantity(0, Unit.getUndefined());
	private ScalerUnit scalerUnit		= new ScalerUnit(Unit.getUndefined());

	public Date getBillingDate() throws IOException {
		return billingDate;
	}

	public void setBillingDate(Date billingDate) {
		this.billingDate = billingDate;
	}

	public Date getCaptureTime() throws IOException {
		return captureTime ;
	}

	public void setCaptureTime(Date captureTime) {
		this.captureTime = captureTime;
	}

	public Quantity getQuantityValue() throws IOException {
		return quantityValue;
	}

	public void setQuantityValue(Quantity quantityValue) {
		this.quantityValue = quantityValue;
	}

	public void setQuantityValue(BigDecimal amount, Unit unit) {
		this.quantityValue = new Quantity(amount, unit);
	}

	public int getResetCounter() {
		return 0;
	}

	public ScalerUnit getScalerUnit() throws IOException {
		return scalerUnit;
	}

	public void setScalerUnit(ScalerUnit scalerUnit) {
		this.scalerUnit  = scalerUnit;
	}

	public String getText() throws IOException {
		return "";
	}

	public long getValue() throws IOException {
		return getQuantityValue().longValue();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("HistoricalRegister [");
		sb.append("billingDate=").append(billingDate);
		sb.append(", captureTime=").append(captureTime);
		sb.append(", quantityValue=").append(quantityValue);
		sb.append(", getResetCounter()=").append(getResetCounter());
		sb.append(", scalerUnit=").append(scalerUnit);
		sb.append(", text=").append("");
		sb.append(", value=").append(0);
		sb.append("]");
		return sb.toString();
	}

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }
}