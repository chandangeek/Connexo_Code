package com.elster.jupiter.ids;

public interface IdsService {	
	Vault getVault(String component , long id);
	RecordSpec getRecordSpec(String component, long id);
	TimeSeries getTimeSeries(long id);
	TimeSeriesDataStorer createStorer(boolean overrules);
	Vault newVault(String component, long id, String name, int slotCount,boolean regular);	
	RecordSpec newRecordSpec(String component , long id,String name);	
}
