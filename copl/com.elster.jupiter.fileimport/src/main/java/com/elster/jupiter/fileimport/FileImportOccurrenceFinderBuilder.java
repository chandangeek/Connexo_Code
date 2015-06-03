package com.elster.jupiter.fileimport;

import com.elster.jupiter.domain.util.Finder;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Created by Lucian on 6/2/2015.
 */
public interface FileImportOccurrenceFinderBuilder {

    FileImportOccurrenceFinderBuilder withStatusIn(List<Status> statuses);
    FileImportOccurrenceFinderBuilder withImportServiceIn(List<Long> importServicesIds);
    FileImportOccurrenceFinderBuilder withStartDateIn(Range<Instant> interval);

    FileImportOccurrenceFinderBuilder withEndDateIn(Range<Instant> interval);

    Finder<FileImportOccurrence> build();
}
