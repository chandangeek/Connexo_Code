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
		meteringService.registerVaults().forEach(vault -> vault.purge(limit, logger()));
	}
	
	private void purgeInterval(Instant limit) {
		meteringService.intervalVaults().forEach(vault -> vault.purge(limit, logger()));
	}
	
	private void purgeDaily(Instant limit) {
		meteringService.dailyVaults().forEach(vault -> vault.purge(limit, logger()));
	}
	
	private void  purgeEvents(Instant limit) {
	}
	
	private Logger logger() {
		return purgeConfiguration.getLogger();
	}
}
