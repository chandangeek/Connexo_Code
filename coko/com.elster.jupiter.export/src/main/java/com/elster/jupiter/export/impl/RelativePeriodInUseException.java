/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.List;
import java.util.stream.Collectors;

public class RelativePeriodInUseException extends LocalizedException {
    protected RelativePeriodInUseException(Thesaurus thesaurus, List<ExportTask> using) {
        super(thesaurus, MessageSeeds.RELATIVE_PERIOD_USED, asString(using));
    }

    private static String asString(List<ExportTask> using) {
        return using.stream()
                .map(ExportTask::getName)
                .collect(Collectors.joining(", "));
    }
}
