/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import java.util.logging.Logger;
import java.util.stream.Stream;

public interface DataFormatter {

    void startExport(DataExportOccurrence occurrence, Logger logger);

    FormattedData processData(Stream<ExportData> data);

    void endExport();
}
