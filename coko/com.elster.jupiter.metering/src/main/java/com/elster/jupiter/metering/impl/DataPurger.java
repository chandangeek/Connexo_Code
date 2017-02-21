/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.PurgeConfiguration;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

public class DataPurger {

	private final PurgeConfiguration purgeConfiguration;
	private final ServerMeteringService meteringService;
	private final Clock clock;

	DataPurger(PurgeConfiguration purgeConfiguration, ServerMeteringService meteringService, Clock clock) {
		this.purgeConfiguration = purgeConfiguration;
		this.meteringService = meteringService;
		this.clock = clock;
	}

	void purge() {
		purgeConfiguration.eventRetention().ifPresent(this::purgeEvents);
		purgeConfiguration.readingQualityRetention().ifPresent(this::purgeReadingQualities);
	}

	private void purgeEvents(Period period) {
		Instant limit = Instant.now(clock).minus(period).truncatedTo(ChronoUnit.DAYS);
		logger().info("Removing device events up to " + limit);
		meteringService.getDataModel().dataDropper(TableSpecs.MTR_ENDDEVICEEVENTRECORD.name(),logger()).drop(limit);
	}

	private void purgeReadingQualities(Period period) {
		Instant limit = Instant.now(clock).minus(period).truncatedTo(ChronoUnit.DAYS);
		logger().info("Removing reading qualities up to " + limit);
		meteringService.getDataModel().dataDropper(TableSpecs.MTR_READINGQUALITY.name(),logger()).drop(limit);
	}

	private Logger logger() {
		return purgeConfiguration.getLogger();
	}

}
