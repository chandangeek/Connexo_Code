/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface TimeSeriesJournalEntry extends TimeSeriesEntry {

    Instant getJournalTime();

    String getUserName();

    Boolean getActive();
}
