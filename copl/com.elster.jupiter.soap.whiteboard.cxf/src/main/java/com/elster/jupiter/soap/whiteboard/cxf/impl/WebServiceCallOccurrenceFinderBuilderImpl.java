package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.util.conditions.Condition;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;

public class WebServiceCallOccurrenceFinderBuilderImpl implements WebServiceCallOccurrenceFinderBuilder {
    private DataModel dataModel;
    private Condition condition;

    WebServiceCallOccurrenceFinderBuilderImpl(DataModel dataModel, Condition condition) {
        this.dataModel = dataModel;
        this.condition = condition;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withApplicationName(Set<String> applicationNames){
        if (!applicationNames.isEmpty())
        {
            List<String> namesList = new ArrayList<>(applicationNames);
            this.condition = this.condition.and(where("applicationName").in(namesList));
        }
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withStatusIn(List<WebServiceCallOccurrenceStatus> statuses) {
        if (!statuses.isEmpty()) {
            this.condition = this.condition.and(where("status").in(statuses));
        }
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withWebServiceName(String webServiceName) {
        if (!webServiceName.isEmpty()) {
            this.condition = this.condition.and(where("importScheduleId").isEqualTo(webServiceName));
        }
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withEndPointConfiguration(EndPointConfiguration epc) {
        this.condition = this.condition.and(where("endPointConfiguration").isEqualTo(epc));
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withStartTimeIn(Range<Instant> interval) {
        this.condition = this.condition.and(where("startTime").in(interval));
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withEndTimeIn(Range<Instant> interval) {
        this.condition = this.condition.and(where("endTime").in(interval));
        return this;
    }

    @Override
    public Finder<WebServiceCallOccurrence> build() {
        return DefaultFinder.of(WebServiceCallOccurrence.class, this.condition, this.dataModel)
                .defaultSortColumn("startTime");
    }
}