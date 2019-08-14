/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.util.HasId;

import java.time.Instant;

public interface IReadingTypeDataExportItem extends ReadingTypeDataExportItem, HasId {

    void setLastRun(Instant lastRun);

    void setLastExportedDate(Instant lastExportedDate);

    void update();

    void activate();

    void deactivate();

    void clearCachedReadingContainer();
}
