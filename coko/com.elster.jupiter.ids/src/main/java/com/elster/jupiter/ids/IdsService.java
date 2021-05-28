/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

import com.elster.jupiter.util.Pair;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.ResultWrapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@ProviderType
public interface IdsService {

    String COMPONENTNAME = "IDS";

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

    List<TimeSeriesEntry> getEntries(List<Pair<TimeSeries, Instant>> scope);

    ResultWrapper<String> extendTo(Instant instant, Logger logger, boolean dryRun);
}
