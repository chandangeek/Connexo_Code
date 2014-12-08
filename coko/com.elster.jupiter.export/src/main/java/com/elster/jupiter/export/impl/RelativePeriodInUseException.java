package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.HasName;

import java.util.List;
import java.util.stream.Collectors;

public class RelativePeriodInUseException extends LocalizedException {
    protected RelativePeriodInUseException(Thesaurus thesaurus, List<ReadingTypeDataExportTask> using) {
        super(thesaurus, MessageSeeds.RELATIVE_PERIOD_USED, asString(using));
    }

    private static String asString(List<ReadingTypeDataExportTask> using) {
        return using.stream()
                .map(HasName::getName)
                .collect(Collectors.joining(", "));
    }
}
