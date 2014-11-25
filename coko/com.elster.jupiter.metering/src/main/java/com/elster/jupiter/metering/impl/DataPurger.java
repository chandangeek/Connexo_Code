package com.elster.jupiter.metering.impl;

import java.time.Instant;

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
	
	void purgeRegister(Instant limit) {
		meteringService.registerVaults().forEach(vault -> vault.purge(limit));
	}
	
	void purgeInterval(Instant limit) {
		meteringService.intervalVaults().forEach(vault -> vault.purge(limit));
	}
	
	void purgeDaily(Instant limit) {
		meteringService.dailyVaults().forEach(vault -> vault.purge(limit));
	}
	
	void  purgeEvents(Instant limit) {
	}
	
}
