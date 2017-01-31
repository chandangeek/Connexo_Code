/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

@ProviderType
public interface IdsService {
	public static final String COMPONENTNAME = "IDS";
	
	Optional<Vault> getVault(String component, long id);
	Optional<RecordSpec> getRecordSpec(String component, long id);
	Optional<TimeSeries> getTimeSeries(long id);
	TimeSeriesDataStorer createUpdatingStorer();
	TimeSeriesDataStorer createOverrulingStorer();
	TimeSeriesDataStorer createNonOverrulingStorer();
	Vault createVault(String component, long id, String name, int slotCount, int textSlotCount, boolean regular);
	RecordSpecBuilder createRecordSpec(String component, long id, String name);
	void purge(Logger logger);
	void extendTo(Instant instant, Logger logger);
}
 