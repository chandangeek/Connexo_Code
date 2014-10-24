package com.elster.jupiter.metering.rest;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;

@XmlRootElement
public class ReadingTypeInfo {

    public String mRID;
    public String aliasName;
    public String name;
    public String macroPeriod;
    public String aggregate;
    public String measuringPeriod;
    public String accumulation;
    public String flowDirection;
    public String commodity;
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
    public ReadingTypeNames names;

    public ReadingTypeInfo() {
    }

    public ReadingTypeInfo(ReadingType readingType) {
        this.mRID = readingType.getMRID();
        this.aliasName = readingType.getAliasName();
        this.name = readingType.getName();
        this.macroPeriod = readingType.getMacroPeriod().getDescription();
        this.aggregate = readingType.getAggregate().getDescription();
        this.measuringPeriod = readingType.getMeasuringPeriod().getDescription();
        this.accumulation = readingType.getAccumulation().getDescription();
        this.flowDirection = readingType.getFlowDirection().getDescription();
        this.commodity = readingType.getCommodity().getDescription();
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
    }
    
    public static class ReadingTypeNames {
    	
    	public String timeOfUse;
    	public String timeAttribute;
    	public String unitOfMeasure;
    	
     	ReadingTypeNames() {
    	}
    	
    	ReadingTypeNames(ReadingType readingType) {
    		this.timeOfUse = readingType.getTou() == 0 ? "" : "ToU " + readingType.getTou();
    		this.timeAttribute = readingType.getMeasuringPeriod().equals(TimeAttribute.NOTAPPLICABLE) ? "" : readingType.getMeasuringPeriod().getDescription();
    		this.unitOfMeasure = readingType.getMultiplier().getSymbol() + readingType.getUnit().getSymbol();
    	}
    }
}
