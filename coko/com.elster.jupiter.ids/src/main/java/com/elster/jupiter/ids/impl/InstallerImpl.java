package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpecBuilder;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.orm.DataModel;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

public class InstallerImpl {
	
	private final IdsService idsService;
	private final DataModel dataModel;
	private final Clock clock;

	public InstallerImpl(DataModel dataModel, IdsService idsService, Clock clock) {
		this.idsService = idsService;
		this.dataModel = dataModel;
		this.clock = clock;
	}

    private static final int DEFAULT_SLOT_COUNT = 8;

    public void install(boolean executeDdl, boolean createMasterData) {
		dataModel.install(executeDdl,true);
		if (createMasterData) {
            createMasterData();
        }
	}
	
	private void createMasterData() {
		createVaults();
		createRecordSpecs();
	}

	private void createVaults() {
		Vault newVault = idsService.createVault(IdsService.COMPONENTNAME, 1, "Regular TimeSeries Default ", DEFAULT_SLOT_COUNT, 0, true);
		Instant start = Instant.now(clock).truncatedTo(ChronoUnit.DAYS);
		newVault.activate(start);		
		newVault.extendTo(start.plus(360, ChronoUnit.DAYS), Logger.getLogger(getClass().getPackage().getName()));		
	}
	
	private void createRecordSpecs() {
		RecordSpecBuilder builder = idsService.createRecordSpec(IdsService.COMPONENTNAME, 1, "Simple");
		builder.addFieldSpec("value", FieldType.NUMBER);
		builder.create();
	}
	
}
