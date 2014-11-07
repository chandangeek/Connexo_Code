package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ReadingTypeDataExportItem;

import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 6/11/2014
 * Time: 19:04
 */
public interface IReadingTypeDataExportItem extends ReadingTypeDataExportItem {
    void updateLastRunAndLastExported(Instant lastRun, Instant lastExported);

    void update();
}
