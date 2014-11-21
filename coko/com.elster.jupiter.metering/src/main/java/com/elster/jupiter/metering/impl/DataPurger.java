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
		purgeConfiguration.getRegisterLimit().ifPresent(this::purgeRegisters);
	}
	
	void purgeRegisters(Instant limit) {
		
	}
}
