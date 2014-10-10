package com.elster.jupiter.ids;

import java.util.Optional;

public interface IdsService {
	public static final String COMPONENTNAME = "IDS";
	
	Optional<Vault> getVault(String component, long id);
	Optional<RecordSpec> getRecordSpec(String component, long id);
	Optional<TimeSeries> getTimeSeries(long id);
	TimeSeriesDataStorer createStorer(boolean overrules);
	Vault newVault(String component, long id, String name, int slotCount, int textSlotCount, boolean regular);	
	RecordSpec newRecordSpec(String component , long id,String name);	
}
