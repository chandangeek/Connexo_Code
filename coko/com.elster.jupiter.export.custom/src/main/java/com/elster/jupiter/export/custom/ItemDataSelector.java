/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.custom;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.IReadingTypeDataExportItem;
import com.elster.jupiter.export.MeterReadingData;

import java.time.Instant;
import java.util.Optional;

public interface ItemDataSelector {

    Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, IReadingTypeDataExportItem item);

    Optional<MeterReadingData> selectDataForUpdate(DataExportOccurrence occurrence, IReadingTypeDataExportItem item, Instant since);
}
