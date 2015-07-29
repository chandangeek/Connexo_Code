package com.elster.jupiter.metering.impl;

import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

import com.elster.jupiter.metering.PurgeConfiguration;

public class DataPurger {

	private final PurgeConfiguration purgeConfiguration;
	private final ServerMeteringService meteringService;

	DataPurger(PurgeConfiguration purgeConfiguration, ServerMeteringService meteringService) {
		this.purgeConfiguration = purgeConfiguration;
		this.meteringService = meteringService;
	}

	void purge() {
		purgeConfiguration.eventRetention().ifPresent(this::purgeEvents);
		purgeConfiguration.readingQualityRetention().ifPresent(this::purgeReadingQualities);
	}

	private void purgeEvents(Period period) {
		Instant limit = Instant.now().minus(period).truncatedTo(ChronoUnit.DAYS);
		logger().info("Removing device events up to " + limit);
		meteringService.getDataModel().dataDropper(TableSpecs.MTR_ENDDEVICEEVENTRECORD.name(),logger()).drop(limit);
	}

	private void purgeReadingQualities(Period period) {
		Instant limit = Instant.now().minus(period).truncatedTo(ChronoUnit.DAYS);
		logger().info("Removing reading qualities up to " + limit);
		meteringService.getDataModel().dataDropper(TableSpecs.MTR_READINGQUALITY.name(),logger()).drop(limit);
	}

	private Logger logger() {
		return purgeConfiguration.getLogger();
	}

}
