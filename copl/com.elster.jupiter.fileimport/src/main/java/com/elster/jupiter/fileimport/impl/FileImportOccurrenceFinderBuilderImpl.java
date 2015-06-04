package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImportOccurrenceFinderBuilder;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.Status;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Created by Lucian on 6/2/2015.
 */
public class FileImportOccurrenceFinderBuilderImpl implements FileImportOccurrenceFinderBuilder {
    private DataModel dataModel;
    private Condition condition;

    public FileImportOccurrenceFinderBuilderImpl() {
    }

    public FileImportOccurrenceFinderBuilderImpl(DataModel dataModel, Condition condition) {
        this.dataModel = dataModel;
        this.condition = condition;
    }


    @Override
    public FileImportOccurrenceFinderBuilder withStatusIn(List<Status> statuses) {
        if(statuses.size()>0)
            this.condition = this.condition.and(where("status").in(statuses));
        return this;
    }

    @Override
    public FileImportOccurrenceFinderBuilder withImportServiceIn(List<Long> importServicesIds) {
        if(importServicesIds.size()>0)
            this.condition = this.condition.and(where("importScheduleId").in(importServicesIds));
        return this;
    }


    @Override
    public FileImportOccurrenceFinderBuilder withStartDateIn(Range<Instant> interval) {

        this.condition = this.condition.and(where("startDate").in(interval));
        return this;
    }

    @Override
    public FileImportOccurrenceFinderBuilder withEndDateIn(Range<Instant> interval) {

        this.condition = this.condition.and(where("endDate").in(interval));
        return this;
    }

    @Override
    public Finder<FileImportOccurrence> build() {
        return DefaultFinder.of(FileImportOccurrence.class, condition, dataModel, ImportSchedule.class);
    }
}
