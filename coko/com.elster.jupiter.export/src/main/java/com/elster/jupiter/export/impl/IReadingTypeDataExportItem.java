/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ReadingTypeDataExportItem;

import java.time.Instant;

interface IReadingTypeDataExportItem extends ReadingTypeDataExportItem {

    void setLastRun(Instant lastRun);

    void setLastExportedDate(Instant lastExportedDate);

    void update();

    void activate();

    void deactivate();
}
