/*
 * LogicalID.java
 *
 * Created on 3 november 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

/**
 *
 * @author Koen
 */
public class LogicalID {

	private String description;
	private long id;
	private Unit unit;
	private ObisCode obisCode;

	/** Creates a new instance of LogicalID */
	public LogicalID(String description, long id, ObisCode obisCode, Unit unit) {
		this.description = description;
		this.id = id;
		this.unit = unit;
		this.obisCode = obisCode;
	}

	public String toString() {
		return this.description + ", 0x" + Long.toHexString(this.id) + ", " + this.unit + ", " + this.obisCode;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Unit getUnit() {
		return this.unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public ObisCode getObisCode() {
		return this.obisCode;
	}

	public void setObisCode(ObisCode obisCode) {
		this.obisCode = obisCode;
	}

}
