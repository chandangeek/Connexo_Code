/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;

@XmlRootElement
public class ReadingTypeInfo {

    public String mRID;
    public String aliasName;
    public String name;
    public boolean active;
    public String macroPeriod;
    public String aggregate;
    public String measuringPeriod;
    public String accumulation;
    public String flowDirection;
    public String commodity;
    public boolean isGasRelated;
    public String measurementKind;
    public long interHarmonicNumerator;
    public long interHarmonicDenominator;
    public long argumentNumerator;
    public long argumentDenominator;
    public int tou;
    public int cpp;
    public int consumptionTier;
    public String phases;
    public int metricMultiplier;
    public String unit;
    public String currency;
    public long version;
    public boolean isCumulative;

    /* Both the fullAliasName and the separate names are provided to the FrontEnd to do smart dynamic adjustments to the alias */
    public ReadingTypeNames names;
    public String fullAliasName;

    public ReadingTypeInfo() {
    }

    ReadingTypeInfo(ReadingType readingType) {
        this.mRID = readingType.getMRID();
        this.aliasName = readingType.getAliasName();
        this.name = readingType.getName();
        this.active = readingType.isActive();
        this.isCumulative = readingType.isCumulative();
        this.macroPeriod = readingType.getMacroPeriod().getDescription();
        this.aggregate = readingType.getAggregate().getDescription();
        this.measuringPeriod = readingType.getMeasuringPeriod().getDescription();
        this.accumulation = readingType.getAccumulation().getDescription();
        this.flowDirection = readingType.getFlowDirection().getDescription();
        this.commodity = readingType.getCommodity().getDescription();
        this.isGasRelated = readingType.getCommodity().equals(Commodity.NATURALGAS);
        this.measurementKind = readingType.getMeasurementKind().getDescription();
        this.interHarmonicNumerator = readingType.getInterharmonic().getNumerator();
        this.interHarmonicDenominator = readingType.getInterharmonic().getDenominator();
        this.argumentNumerator = readingType.getArgument().getNumerator();
        this.argumentDenominator = readingType.getArgument().getDenominator();
        this.tou = readingType.getTou();
    	this.cpp = readingType.getCpp();
    	this.consumptionTier = readingType.getConsumptionTier();
    	this.phases = readingType.getPhases().getBaseDescription();
    	this.metricMultiplier = readingType.getMultiplier().getMultiplier();
    	this.unit = readingType.getUnit().getSymbol();
    	this.currency = readingType.getCurrency().getSymbol();
        this.version = readingType.getVersion();
        this.names = new ReadingTypeNames(readingType);
        this.fullAliasName = readingType.getFullAliasName();
    }

    public static class ReadingTypeNames {
    	
    	public String timeOfUse;
    	public String timeAttribute;
    	public String unitOfMeasure;
        public String phase;
    	
     	ReadingTypeNames() {
    	}
    	
    	ReadingTypeNames(ReadingType readingType) {
    		this.timeOfUse = readingType.getTou() == 0 ? "" : "ToU " + readingType.getTou();
            if(!readingType.getMeasuringPeriod().equals(TimeAttribute.NOTAPPLICABLE)){
                this.timeAttribute = readingType.getMeasuringPeriod().getDescription();
            } else if(readingType.getMacroPeriod().equals(MacroPeriod.DAILY) || readingType.getMacroPeriod().equals(MacroPeriod.MONTHLY)){
                this.timeAttribute = readingType.getMacroPeriod().getDescription();
            }
    		this.unitOfMeasure = readingType.getMultiplier().getSymbol() + readingType.getUnit().getSymbol();
            if (!readingType.getPhases().equals(Phase.NOTAPPLICABLE)) {
                this.phase = readingType.getPhases().getDescription();
            } else {
                this.phase = "";
            }
    	}
    }
}
