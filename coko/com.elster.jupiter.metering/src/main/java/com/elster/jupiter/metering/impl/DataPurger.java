package com.elster.jupiter.metering.impl;

import java.time.Instant;
import java.util.logging.Logger;

import com.elster.jupiter.metering.PurgeConfiguration;

public class DataPurger {

	private final PurgeConfiguration purgeConfiguration;
	private final MeteringServiceImpl meteringService;
	
	DataPurger(PurgeConfiguration purgeConfiguration, MeteringServiceImpl meteringService) {
		this.purgeConfiguration = purgeConfiguration;
		this.meteringService = meteringService;
	}
	
	void purge() {
		purgeConfiguration.getRegisterLimit().ifPresent(this::purgeRegister);
		purgeConfiguration.getIntervalLimit().ifPresent(this::purgeInterval);
		purgeConfiguration.getDailyLimit().ifPresent(this::purgeDaily);
		purgeConfiguration.getEventLimit().ifPresent(this::purgeEvents);
	}
	
	private void purgeRegister(Instant limit) {
		logger().info("Removing register readings up to " + limit);
		meteringService.registerVaults().forEach(vault -> vault.purge(limit, logger()));
	}
	
	private void purgeInterval(Instant limit) {
		logger().info("Removing interval data up to " + limit);
		meteringService.intervalVaults().forEach(vault -> vault.purge(limit, logger()));
	}
	
	private void purgeDaily(Instant limit) {
		logger().info("Removing daily and monthly readings data up to " + limit);
		meteringService.dailyVaults().forEach(vault -> vault.purge(limit, logger()));
	}
	
	private void  purgeEvents(Instant limit) {
		logger().info("Removing device events up to " + limit);
		//meteringService.getDataModel().dataDropper(TableSpecs.MTR_ENDDEVICEEVENTRECORD.name(),logger()).drop(limit);
	}
	
	private Logger logger() {
		return purgeConfiguration.getLogger();
	}
}
