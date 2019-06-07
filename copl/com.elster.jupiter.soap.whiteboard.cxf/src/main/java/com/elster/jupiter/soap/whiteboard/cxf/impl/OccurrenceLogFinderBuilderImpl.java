package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.OccurrenceLogFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.util.conditions.Condition;

import static com.elster.jupiter.util.conditions.Where.where;

public class OccurrenceLogFinderBuilderImpl implements OccurrenceLogFinderBuilder {

    private DataModel dataModel;
    private Condition condition;

    OccurrenceLogFinderBuilderImpl(DataModel dataModel, Condition condition) {
        this.dataModel = dataModel;
        this.condition = condition;
    }

    @Override
    public OccurrenceLogFinderBuilder withEndPointConfiguration(EndPointConfiguration epc){

        this.condition = this.condition.and(where("endPointConfiguration").isEqualTo(epc));

        return this;
        /*return DefaultFinder.of(EndPointLog.class,
                Where.where(EndPointLogImpl.Fields.endPointConfiguration.fieldName())
                        .isEqualTo(this), dataModel).sorted(EndPointLogImpl.Fields.timestamp.fieldName(), false);*/


    }

    @Override
    public OccurrenceLogFinderBuilder withEmptyOccurrence(){
        this.condition = this.condition.and(where("occurrence").isNull());
        return this;

    };

    @Override
    public OccurrenceLogFinderBuilder withOccurrenceId(WebServiceCallOccurrence epoc){
        this.condition = this.condition.and(where("occurrence").isEqualTo(epoc));
        return this;

    };

    public Finder<EndPointLog> build(){
        return DefaultFinder.of(EndPointLog.class,  condition, dataModel)
                .defaultSortColumn("timestamp");
        //return DefaultFinder.of(EndPointLog.class, condition, dataModel/*, ImportSchedule.class*/);
    };
}

