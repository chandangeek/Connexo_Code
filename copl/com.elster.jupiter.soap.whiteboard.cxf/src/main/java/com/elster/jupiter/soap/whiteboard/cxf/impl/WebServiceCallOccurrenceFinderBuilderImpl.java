package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttributeBinding;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttribute;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;

public class WebServiceCallOccurrenceFinderBuilderImpl implements WebServiceCallOccurrenceFinderBuilder {
    private DataModel dataModel;
    private Condition condition;
    private Condition subCondition;

    WebServiceCallOccurrenceFinderBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
        this.condition = Condition.TRUE;
        this.subCondition = Condition.TRUE;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withApplicationNames(Set<String> applicationNames) {
        if (!applicationNames.isEmpty()) {
            this.condition = this.condition.and(where("applicationName").in(ImmutableList.copyOf(applicationNames)));
        }
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withStatuses(Set<WebServiceCallOccurrenceStatus> statuses) {
        if (!statuses.isEmpty()) {
            this.condition = this.condition.and(where("status").in(ImmutableList.copyOf(statuses)));
        }
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withWebServiceNames(Set<String> webServiceNames) {
        if (!webServiceNames.isEmpty()) {
            this.condition = this.condition.and(where("endPointConfiguration.webServiceName").in(ImmutableList.copyOf(webServiceNames)));
        }
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withEndPointConfigurations(Set<EndPointConfiguration> endPointConfigurations) {
        if (!endPointConfigurations.isEmpty()) {
            this.condition = this.condition.and(where("endPointConfiguration").in(ImmutableList.copyOf(endPointConfigurations)));
        }
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
    public WebServiceCallOccurrenceFinderBuilder onlyInbound() {
        this.condition = this.condition.and(ListOperator.IN.contains(dataModel.query(InboundEndPointConfiguration.class)
                .asSubquery(Condition.TRUE, "id"), "endPointConfiguration"));
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder onlyOutbound() {
        this.condition = this.condition.and(ListOperator.IN.contains(dataModel.query(OutboundEndPointConfiguration.class)
                .asSubquery(Condition.TRUE, "id"), "endPointConfiguration"));
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withRelatedAttribute(WebServiceCallRelatedAttribute relatedObject){
        Subquery subquery = dataModel.query(WebServiceCallRelatedAttributeBinding.class)
                .asSubquery(this.subCondition.and(where(WebServiceCallRelatedAttributeBindingImpl.Fields.ATTRIBUTE.fieldName()).isEqualTo(relatedObject)),
                        WebServiceCallRelatedAttributeBindingImpl.Fields.OCCURRENCE.fieldName());
        this.condition = this.condition.and(ListOperator.IN.contains(subquery, WebServiceCallRelatedAttributeBindingImpl.Fields.ID.fieldName()));
        return this;
    }

    @Override
    public Finder<WebServiceCallOccurrence> build() {
        return DefaultFinder.of(WebServiceCallOccurrence.class, this.condition, this.dataModel, EndPointConfiguration.class)
                .defaultSortColumn(WebServiceCallOccurrenceImpl.Fields.START_TIME.fieldName());
    }
}
