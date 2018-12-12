/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Finder;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Created by Lucian on 6/2/2015.
 */
@ProviderType
public interface FileImportOccurrenceFinderBuilder {

    FileImportOccurrenceFinderBuilder withStatusIn(List<Status> statuses);
    FileImportOccurrenceFinderBuilder withImportServiceIn(List<Long> importServicesIds);
    FileImportOccurrenceFinderBuilder withStartDateIn(Range<Instant> interval);

    FileImportOccurrenceFinderBuilder withEndDateIn(Range<Instant> interval);

    Finder<FileImportOccurrence> build();
}
