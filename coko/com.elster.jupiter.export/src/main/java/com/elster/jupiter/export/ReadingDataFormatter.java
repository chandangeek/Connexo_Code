/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface ReadingDataFormatter extends DataFormatter {

    void startItem(ReadingTypeDataExportItem item);

    void endItem(ReadingTypeDataExportItem item);
}
