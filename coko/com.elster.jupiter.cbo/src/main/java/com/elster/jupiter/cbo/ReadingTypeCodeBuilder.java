/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import com.google.common.base.Joiner;

import java.util.Currency;

public final class ReadingTypeCodeBuilder {
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
	private Currency currency = ReadingTypeCodeBuilder.getCurrency(0);
	
	private ReadingTypeCodeBuilder() {
	}
	
	public static Currency getCurrency(int isoCode) {
		if (isoCode == 0) {
			isoCode = 999;
		} 
		for (Currency each : Currency.getAvailableCurrencies()) {
			if (each.getNumericCode() == isoCode) {
				return each;
			}
		}
		throw new IllegalArgumentException("Invalid currency code " + isoCode);		
	}
	
	public static int getCurrencyId(Currency currency) {
		int result = currency.getNumericCode();
		return result == 999 ? 0 : result;
	}
	
	public static ReadingTypeCodeBuilder of(Commodity commodity) {
		return new ReadingTypeCodeBuilder().commodity(commodity);
	}
	
	public ReadingTypeCodeBuilder period(MacroPeriod macroPeriod) {
		this.macroPeriod = macroPeriod;
		return this;
	}
	
	public ReadingTypeCodeBuilder aggregate(Aggregate aggregate) {
		this.aggregate = aggregate;
		return this;
	}
	
	public ReadingTypeCodeBuilder period(TimeAttribute timeAttribute) {
		this.measuringPeriod = timeAttribute;
		return this;
	}
	
	public ReadingTypeCodeBuilder accumulate(Accumulation accumulation) {
		this.accumulation = accumulation;
		return this;
	}
	
	public ReadingTypeCodeBuilder flow(FlowDirection flowDirection) {
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
		if (numerator == 0 && denominator == 0) {
			this.interharmonic = RationalNumber.NOTAPPLICABLE;
		} else {
			this.interharmonic = new RationalNumber(numerator, denominator);
		} 
		return this;
	}
	
	public ReadingTypeCodeBuilder argument(int numerator , int denominator) {
		if (numerator == 0 && denominator == 0) {
			this.argument = RationalNumber.NOTAPPLICABLE;
		} else {
			this.argument = new RationalNumber(numerator, denominator);
		}
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

    public ReadingTypeCodeBuilder in (MetricMultiplier multiplier) {
        this.multiplier = multiplier;
        return this;
    }

	public ReadingTypeCodeBuilder in (ReadingTypeUnit unit) {
		return in(MetricMultiplier.ZERO,unit);
	}
	
	public ReadingTypeCodeBuilder currency(Currency currency) {
		this.currency = currency;
		return this;
	}
	
	public String code() {
		return Joiner.on(".").join(
			macroPeriod.getId(), 
			aggregate.getId(), 
			measuringPeriod.getId(), 
			accumulation.getId(), 
			flowDirection.getId(),
			commodity.getId(), 
			measurementKind.getId(), 
			interharmonic.getNumerator(), 
			interharmonic.getDenominator(), 
			argument.getNumerator(), 
			argument.getDenominator(), 
			tou, 
			cpp,
			consumptionTier,
			phases.getId(), 
			multiplier.getMultiplier(),
			unit.getId(), 
			ReadingTypeCodeBuilder.getCurrencyId(currency));
	}

    public Accumulation getCurrentAccumulation() {
        return accumulation;
    }

    public TimeAttribute getCurrentTimeAttribute() {
        return measuringPeriod;
    }

    public Integer getCurrentTimeOfUseAttribute() {
        return tou;
    }

    public Phase getCurrentPhase() {
        return phases;
    }

    public MetricMultiplier getCurrentMetricMultiplier() {
        return multiplier;
    }
}
