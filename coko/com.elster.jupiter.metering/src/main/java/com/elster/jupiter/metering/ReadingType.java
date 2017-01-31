/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

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

import java.time.temporal.TemporalAmount;
import java.util.Currency;
import java.util.Optional;

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

	Optional<TemporalAmount> getIntervalLength();

	Optional<ReadingType> getBulkReadingType();

	boolean isBulkQuantityReadingType(ReadingType readingType);

	Optional<ReadingType> getCalculatedReadingType();

	boolean isRegular();

	default boolean isCumulative() {
		return getAccumulation().isCumulative();
	}

	long getVersion();

	void setDescription(String description);

	void setAliasName(String alias);

	boolean isActive();

	void activate();

	void deactivate();

	void update();

	/**
	 * Using the {@link #getAliasName()} and some of the attributes of a ReadingType a more detailed aliasName is constructed.
	 *
	 * @return the full alias name
	 * @since v1.1
	 */
	String getFullAliasName();
}
