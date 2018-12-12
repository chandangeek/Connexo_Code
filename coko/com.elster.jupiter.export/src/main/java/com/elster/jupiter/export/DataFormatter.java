/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import aQute.bnd.annotation.ConsumerType;

import java.util.logging.Logger;
import java.util.stream.Stream;

@ConsumerType
public interface DataFormatter {

    void startExport(DataExportOccurrence occurrence, Logger logger);

    FormattedData processData(Stream<ExportData> data);

    void endExport();
}
