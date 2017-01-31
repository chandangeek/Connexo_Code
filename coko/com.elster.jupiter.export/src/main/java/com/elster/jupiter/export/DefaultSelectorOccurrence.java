/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.google.common.collect.Range;

import java.time.Instant;

public interface DefaultSelectorOccurrence {

    Range<Instant> getExportedDataInterval();

}
