package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.*;

public class IdsServiceImpl implements IdsService {
	
	@Override
	public Vault getVault(String component, long id) {
		return getOrmClient().getVaultFactory().get(component,id);
	}

	@Override
	public RecordSpec getRecordSpec(String component, long id) {
		return getOrmClient().getRecordSpecFactory().get(component,id);
	}

	@Override
	public TimeSeries getTimeSeries(long id) {
		return getOrmClient().getTimeSeriesFactory().get(id);
	}
	
	@Override
	public TimeSeriesDataStorer createStorer(boolean overrules) {
		return new TimeSeriesDataStorerImpl(overrules);
	}
	
	@Override
	public void install(boolean executeDdl,boolean storeMappings , boolean createMasterData) {
		new InstallerImpl().install(executeDdl, storeMappings, createMasterData);
	}
	
	private OrmClient getOrmClient() {
		return Bus.getOrmClient();
	}

	@Override
	public Vault newVault(String component, long id, String name, int slotCount , boolean regular) {
		return new VaultImpl(component,id,name,slotCount,regular);
	}

	@Override
	public RecordSpec newRecordSpec(String component, long id,String name) {
		return new RecordSpecImpl(component, id, name);
	}


}
