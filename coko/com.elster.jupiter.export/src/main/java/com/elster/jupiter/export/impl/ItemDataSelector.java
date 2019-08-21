/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;

import java.time.Instant;
import java.util.Optional;

interface ItemDataSelector {

    Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, ReadingTypeDataExportItem item);

    Optional<MeterReadingData> selectDataForUpdate(DataExportOccurrence occurrence, ReadingTypeDataExportItem item, Instant since);
}
