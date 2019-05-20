package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointOccurrence;
import com.elster.jupiter.util.conditions.Condition;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class EndPointConfigurationOccurrenceFinderBuilderImpl  implements EndPointConfigurationOccurrenceFinderBuilder {
    private DataModel dataModel;
    private Condition condition;

    EndPointConfigurationOccurrenceFinderBuilderImpl(DataModel dataModel, Condition condition) {
        this.dataModel = dataModel;
        this.condition = condition;
    }

    @Override
    public EndPointConfigurationOccurrenceFinderBuilder withApplicationName(String applicationName){
        if (!applicationName.isEmpty())
        {
            this.condition = this.condition.and(where("applicationName").isEqualTo(applicationName));
        }
        return this;
    }

    @Override
    /*public EndPointConfigurationOccurrenceFinderBuilder withStatusIn(List<Status> statuses) {*/
    /* XROMVYU Change to enum statuses */
    public EndPointConfigurationOccurrenceFinderBuilder withStatusIn(List<String> statuses) {
        if (!statuses.isEmpty()) {
            this.condition = this.condition.and(where("status").in(statuses));
        }
        return this;
    }

    @Override
    public EndPointConfigurationOccurrenceFinderBuilder withWebServiceName(String webServiceName) {
        if (!webServiceName.isEmpty()) {
            this.condition = this.condition.and(where("importScheduleId").isEqualTo(webServiceName));
        }
        return this;
    }

    @Override
    public EndPointConfigurationOccurrenceFinderBuilder withStartTimeIn(Range<Instant> interval) {

        this.condition = this.condition.and(where("startTime").in(interval));
        return this;
    }

    @Override
    public EndPointConfigurationOccurrenceFinderBuilder withEndTimeIn(Range<Instant> interval) {

        this.condition = this.condition.and(where("endTime").in(interval));
        return this;
    }

    @Override
    public Finder<EndPointOccurrence> build() {
        return DefaultFinder.of(FileImportOccurrence.class, condition, dataModel, ImportSchedule.class);
    }
}
