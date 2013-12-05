package com.elster.jupiter.metering.impl;

import java.util.Currency;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;


public class ReadingTypeCodeBuilder {
	private MacroPeriod macroPeriod = MacroPeriod.NOTAPPLICABLE;
	private Aggregate aggregate = Aggregate.NOTAPPLICABLE;
	private TimeAttribute measuringPeriod = TimeAttribute.NOTAPPLICABLE;
	private Accumulation accumulation = Accumulation.NOTAPPLICABLE;
	private FlowDirection flowDirection = FlowDirection.NOTAPPLICABLE;
	private Commodity commodity = Commodity.NOTAPPLICABLE;
	private MeasurementKind measurementKind = MeasurementKind.NOTAPPLICABLE;
	private RationalNumber interharmonic = RationalNumber.NOTAPPLICABLE;
	private RationalNumber argument = RationalNumber.NOTAPPLICABLE;
	private int tou = 0;
	private int cpp = 0; 
	private int consumptionTier = 0;
	private Phase phases = Phase.NOTAPPLICABLE;
	private MetricMultiplier multiplier = MetricMultiplier.ZERO;
	private ReadingTypeUnit unit = ReadingTypeUnit.NOTAPPLICABLE;
	private Currency currency = ReadingTypeImpl.getCurrency(0);
	
	private ReadingTypeCodeBuilder() {
	}
	
	static ReadingTypeCodeBuilder of(Commodity commodity) {
		return new ReadingTypeCodeBuilder().commodity(commodity);
	}
	
	ReadingTypeCodeBuilder period(MacroPeriod macroPeriod) {
		this.macroPeriod = macroPeriod;
		return this;
	}
	
	ReadingTypeCodeBuilder aggregate(Aggregate aggregate) {
		this.aggregate = aggregate;
		return this;
	}
	
	ReadingTypeCodeBuilder period(TimeAttribute timeAttribute) {
		this.measuringPeriod = timeAttribute;
		return this;
	}
	
	ReadingTypeCodeBuilder accumulate(Accumulation accumulation) {
		this.accumulation = accumulation;
		return this;
	}
	
	ReadingTypeCodeBuilder flow(FlowDirection flowDirection) {
		this.flowDirection = flowDirection;
		return this;
	}
	
	public ReadingTypeCodeBuilder commodity(Commodity commodity) {
		this.commodity = commodity;
		return this;
	}
	
	public ReadingTypeCodeBuilder measure(MeasurementKind meaurementKind) {
		this.measurementKind = meaurementKind;
		return this;
	}
	
	public ReadingTypeCodeBuilder harmonic(int numerator , int denominator) {
		this.interharmonic = new RationalNumber(numerator, denominator);
		return this;
	}
	
	public ReadingTypeCodeBuilder argument(int numerator , int denominator) {
		this.argument = new RationalNumber(numerator, denominator);
		return this;
	}
	
	public ReadingTypeCodeBuilder tou(int tou) {
		this.tou = tou;
		return this;
	}
	
	public ReadingTypeCodeBuilder cpp(int cpp) {
		this.cpp = cpp;
		return this;
	}
	
	public ReadingTypeCodeBuilder tier(int consumptionTier) {
		this.consumptionTier = consumptionTier;
		return this;
	}
	
	public ReadingTypeCodeBuilder phase(Phase phases) {
		this.phases = phases;
		return this;
	}
	
	public ReadingTypeCodeBuilder in (MetricMultiplier multiplier , ReadingTypeUnit unit) {
		this.multiplier = multiplier;
		this.unit = unit;
		return this;		
	}
	
	public ReadingTypeCodeBuilder in (ReadingTypeUnit unit) {
		return in(MetricMultiplier.ZERO,unit);
	}
	
	String code() {
		return 
			"" +
			macroPeriod.getId() + "." + 
			aggregate.getId() + "." + 
			measuringPeriod.getId() + "." + 
			accumulation.getId() + "." + 
			flowDirection.getId() + "." +
			commodity.getId() + "." + 
			measurementKind.getId() + "." + 
			interharmonic.getNumerator() + "." + 
			interharmonic.getDenominator() + "." + 
			argument.getNumerator() + "." + 
			argument.getDenominator() + "." + 
			tou + "." + 
			cpp + "." + 
			consumptionTier + "." + 
			phases.getId() + "." + 
			multiplier.getId() + "." + 
			unit.getId() + "." + 
			ReadingTypeImpl.getCurrencyId(currency);
	}
}
