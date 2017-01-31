/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FormattedData {

    Optional<Instant> lastExported();

    List<FormattedExportData> getData();
}
