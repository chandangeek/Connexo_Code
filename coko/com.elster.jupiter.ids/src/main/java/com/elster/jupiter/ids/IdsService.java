package com.elster.jupiter.ids;

import com.google.common.base.Optional;

public interface IdsService {
	Optional<Vault> getVault(String component, long id);
	Optional<RecordSpec> getRecordSpec(String component, long id);
	Optional<TimeSeries> getTimeSeries(long id);
	TimeSeriesDataStorer createStorer(boolean overrules);
	Vault newVault(String component, long id, String name, int slotCount,boolean regular);	
	RecordSpec newRecordSpec(String component , long id,String name);	
}
