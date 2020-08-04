/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.ReadingType;

import java.util.List;

public interface CompletionOptionsBuilder {
    CompletionOptionsBuilder filterReadingTypes(List<ReadingType> readingTypes);

    CompletionOptions build();
}
