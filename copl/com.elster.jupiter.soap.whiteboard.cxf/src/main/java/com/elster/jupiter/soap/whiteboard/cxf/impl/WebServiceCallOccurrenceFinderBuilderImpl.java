package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttribute;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttributeBinding;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;

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
            condition = condition.and(where(WebServiceCallOccurrenceImpl.Fields.APPLICATION_NAME.fieldName()).in(applicationNames));
        }
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withStatuses(Set<WebServiceCallOccurrenceStatus> statuses) {
        if (!statuses.isEmpty()) {
            condition = condition.and(where(WebServiceCallOccurrenceImpl.Fields.STATUS.fieldName()).in(statuses));
        }
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withWebServiceNames(Set<String> webServiceNames) {
        if (!webServiceNames.isEmpty()) {
            condition = condition.and(where(WebServiceCallOccurrenceImpl.Fields.ENDPOINT_CONFIGURATION.fieldName() + '.' + EndPointConfigurationImpl.Fields.WEB_SERVICE_NAME.fieldName())
                    .in(webServiceNames));
        }
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withEndPointConfigurations(Set<EndPointConfiguration> endPointConfigurations) {
        if (!endPointConfigurations.isEmpty()) {
            condition = condition.and(where(WebServiceCallOccurrenceImpl.Fields.ENDPOINT_CONFIGURATION.fieldName()).in(endPointConfigurations));
        }
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withStartTimeIn(Range<Instant> interval) {
        condition = condition.and(where(WebServiceCallOccurrenceImpl.Fields.START_TIME.fieldName()).in(interval));
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withEndTimeIn(Range<Instant> interval) {
        condition = condition.and(where(WebServiceCallOccurrenceImpl.Fields.END_TIME.fieldName()).in(interval));
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder onlyInbound() {
        condition = condition.and(where(WebServiceCallOccurrenceImpl.Fields.ENDPOINT_CONFIGURATION.fieldName() + '.' + Column.TYPEFIELDNAME)
                .isEqualTo(EndPointConfigurationImpl.INBOUND_WEBSERVICE_DISCRIMINATOR));
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder onlyOutbound() {
        condition = condition.and(where(WebServiceCallOccurrenceImpl.Fields.ENDPOINT_CONFIGURATION.fieldName() + '.' + Column.TYPEFIELDNAME)
                .isEqualTo(EndPointConfigurationImpl.OUTBOUND_WEBSERVICE_DISCRIMINATOR));
        return this;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder withRelatedAttribute(WebServiceCallRelatedAttribute relatedObject){
        Subquery subquery = dataModel.query(WebServiceCallRelatedAttributeBinding.class)
                .asSubquery(this.subCondition.and(where(WebServiceCallRelatedAttributeBindingImpl.Fields.ATTRIBUTE.fieldName()).isEqualTo(relatedObject)),
                        WebServiceCallRelatedAttributeBindingImpl.Fields.OCCURRENCE.fieldName());
        condition = condition.and(ListOperator.IN.contains(subquery, WebServiceCallRelatedAttributeBindingImpl.Fields.ID.fieldName()));
        return this;
    }

    @Override
    public Finder<WebServiceCallOccurrence> build() {
        return DefaultFinder.of(WebServiceCallOccurrence.class, condition, dataModel, EndPointConfiguration.class)
                .defaultSortColumn(WebServiceCallOccurrenceImpl.Fields.START_TIME.fieldName());
    }
}
