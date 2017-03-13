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

import com.energyict.cbo.Unit;

import java.math.BigDecimal;


/**
 * @author koen
 */
public class LoadSurveyChannel {

    private int width;
    private Unit unit;
    private int decimalPointPosition; // DecimalPointScaling (place of the decimal point)
    private BigDecimal scalingFactor; // ScalingFactor (k, M, G, ...)
    private boolean isInstantaneousChannel;

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

    public int getDecimalPointPosition() {
        return decimalPointPosition;
    }

    public void setDecimalPointPosition(int decimalPointPosition) {
        this.decimalPointPosition = decimalPointPosition;
    }

    public BigDecimal getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(BigDecimal scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public boolean isInstantaneousChannel() {
        return isInstantaneousChannel;
    }

    public void markAsInstantaneousChannel() {
        isInstantaneousChannel = true;
    }

}
