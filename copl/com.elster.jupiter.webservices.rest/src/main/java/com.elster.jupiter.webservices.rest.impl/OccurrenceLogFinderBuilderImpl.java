package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointOccurrence;
import com.elster.jupiter.util.conditions.Condition;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class OccurrenceLogFinderBuilderImpl implements  OccurrenceLogFinderBuilder{

    private DataModel dataModel;
    private Condition condition;

    OccurrenceLogFinderBuilderImpl(DataModel dataModel, Condition condition) {
        this.dataModel = dataModel;
        this.condition = condition;
    }


    public OccurrenceLogFinderBuilder withOccurrenceId(long occurrenceId){
        /*if (occur)
        {*/
            this.condition = this.condition.and(where("occurrence").isEqualTo(occurrenceId));
        //}
        return this;

    };

    public Finder<EndPointLog> build(){
        return DefaultFinder.of(EndPointLog.class, condition, dataModel/*, ImportSchedule.class*/);
    };
}
