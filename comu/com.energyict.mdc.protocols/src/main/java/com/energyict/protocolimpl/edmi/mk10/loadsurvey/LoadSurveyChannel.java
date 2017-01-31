/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LoadSurveyChannel.java
 *
 * Created on 31 maart 2006, 14:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.loadsurvey;

import com.energyict.mdc.common.Unit;

import java.math.BigDecimal;


/**
 *
 * @author koen
 */
public class LoadSurveyChannel {

	private int width;
	private int type; // internal data type
	private Unit unit;
	private String name;
	private int scaling; // DecimalPointScaling (place of the decimal point)
	private BigDecimal scalingFactor; // ScalingFactor (k, M, G, ...)



	/** Creates a new instance of LoadSurveyChannel */
	public LoadSurveyChannel() {
	}

	public String toString() {
		// Generated code by ToStringBuilder
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("LoadSurveyChannel:\n");
		strBuff.append("   name="+getName()+"\n");
		strBuff.append("   scaling="+getScaling()+"\n");
		strBuff.append("   scalingFactor="+getScalingFactor()+"\n");
		strBuff.append("   type="+getType()+"\n");
		strBuff.append("   unit="+getUnit()+"\n");
		strBuff.append("   width="+getWidth()+"\n");
		return strBuff.toString();
	}




	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getScaling() {
		return scaling;
	}

	public void setScaling(int scaling) {
		this.scaling = scaling;
	}

	public BigDecimal getScalingFactor() {
		return scalingFactor;
	}

	public void setScalingFactor(BigDecimal scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

}
