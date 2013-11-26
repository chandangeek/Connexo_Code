package com.elster.jupiter.metering;

import java.util.Currency;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;

public interface ReadingType extends IdentifiedObject {
	MacroPeriod getMacroPeriod();
	Aggregate getAggregate();
	TimeAttribute getMeasuringPeriod();
	Accumulation getAccumulation();
	FlowDirection getFlowDirection();
	Commodity getCommodity();
	MeasurementKind getMeasurementKind();
	RationalNumber getInterharmonic();
	RationalNumber getArgument();
	int getTou();
	int getCpp();
	int getConsumptionTier();
	Phase getPhases();
	MetricMultiplier getMultiplier();
	ReadingTypeUnit getUnit();
	Currency getCurrency();
	
	boolean isCumulativeReadingType(ReadingType readingType);
	boolean isRegular();
    long getVersion();

}
