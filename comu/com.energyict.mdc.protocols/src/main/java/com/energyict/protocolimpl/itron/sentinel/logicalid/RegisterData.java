/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterData.java
 *
 * Created on 7 november 2006, 11:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @author Koen
 */
public class RegisterData {

	private LogicalID lid;
	private BigDecimal value;
	private int tariff;
	private Date timestamp;

	public String toString() {
		// Generated code by ToStringBuilder
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("RegisterData:\n");
		strBuff.append("   lid=" + getLid() + "\n");
		strBuff.append("   tariff=" + getTariff() + "\n");
		strBuff.append("   timestamp=" + getTimestamp() + "\n");
		strBuff.append("   value=" + getValue() + "\n");
		return strBuff.toString();
	}

	/** Creates a new instance of RegisterData */
	public RegisterData(LogicalID lid, BigDecimal value) {
		this(lid, value, -1);
	}

	public RegisterData(LogicalID lid, BigDecimal value, int tariff) {
		this(lid, value, tariff, null);
	}

	public RegisterData(LogicalID lid, BigDecimal value, int tariff, Date timestamp) {
		this.lid = lid;
		this.value = value;
		this.tariff = tariff;
		this.timestamp = timestamp;
	}

	public LogicalID getLid() {
		return this.lid;
	}

	public void setLid(LogicalID lid) {
		this.lid = lid;
	}

	public BigDecimal getValue() {
		return this.value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public int getTariff() {
		return this.tariff;
	}

	public void setTariff(int tariff) {
		this.tariff = tariff;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

}
