/*
 * LoadSurveyChannel.java
 *
 * Created on 31 maart 2006, 14:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimplv2.edmi.mk10.profiles;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;


/**
 *
 * @author koen
 */
public class LoadSurveyChannel {

	private int width;
	private Unit unit;
	private int decimalPointPositionScaling;
	private BigDecimal scalingFactor;
	private boolean isStatusChannel;
	private ObisCode obisCode;

	/**
	 * Creates a new instance of LoadSurveyChannel
	 */
	public LoadSurveyChannel() {
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public int getDecimalPointPositionScaling() {
		return decimalPointPositionScaling;
	}

	public void setDecimalPointPositionScaling(int decimalPointPositionScaling) {
		this.decimalPointPositionScaling = decimalPointPositionScaling;
	}

	public BigDecimal getScalingFactor() {
		return scalingFactor;
	}

	public void setScalingFactor(BigDecimal scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

    public boolean isStatusChannel() {
        return isStatusChannel;
    }

    public void markAsStatusChannel() {
        isStatusChannel = true;
    }

	public ObisCode getObisCode() {
		return obisCode;
	}

	public void setObisCode(ObisCode obisCode) {
		this.obisCode = obisCode;
	}
}