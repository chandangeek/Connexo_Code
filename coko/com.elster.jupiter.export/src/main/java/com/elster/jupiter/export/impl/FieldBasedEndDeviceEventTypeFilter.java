/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.cbo.HasNumericCode;
import com.elster.jupiter.export.EndDeviceEventTypeFilter;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class FieldBasedEndDeviceEventTypeFilter implements EndDeviceEventTypeFilter, PersistenceAware {

    private final MeteringService meteringService;

    private Reference<EventSelectorConfig> dataSelector = ValueReference.absent();
    private String code;

    private transient EndDeviceType type;
    private transient EndDeviceDomain domain;
    private transient EndDeviceSubDomain subDomain;
    private transient EndDeviceEventOrAction eventOrAction;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    FieldBasedEndDeviceEventTypeFilter(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    FieldBasedEndDeviceEventTypeFilter init(EventSelectorConfig selector, EndDeviceType type, EndDeviceDomain domain, EndDeviceSubDomain subDomain, EndDeviceEventOrAction eventOrAction) {
        this.dataSelector.set(selector);
        this.type = type;
        this.domain = domain;
        this.subDomain = subDomain;
        this.eventOrAction = eventOrAction;

        code = Stream.<HasNumericCode>of(type, domain, subDomain, eventOrAction)
                .map(hasNumericCode -> Optional.ofNullable(hasNumericCode)
                        .map(HasNumericCode::getCode)
                        .map(String::valueOf)
                        .orElse("*")
                )
                .collect(Collectors.joining("."));
        return this;
    }

    static FieldBasedEndDeviceEventTypeFilter from(DataModel dataModel, EventSelectorConfig selector, EndDeviceType type, EndDeviceDomain domain, EndDeviceSubDomain subDomain, EndDeviceEventOrAction eventOrAction) {
        return dataModel.getInstance(FieldBasedEndDeviceEventTypeFilter.class).init(selector, type, domain, subDomain, eventOrAction);
    }

    static FieldBasedEndDeviceEventTypeFilter from(DataModel dataModel, EventSelectorConfig selector, String code) {
        FieldBasedEndDeviceEventTypeFilter filter = dataModel.getInstance(FieldBasedEndDeviceEventTypeFilter.class);
        filter.code = code;
        filter.dataSelector.set(selector);
        filter.postLoad();
        return filter;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public Predicate<EndDeviceEventType> asEndDeviceEventTypePredicate() {
        return matchesType()
                .and(matchesDomain())
                .and(matchesSubDomain())
                .and(matchesEventOrAction());
    }

    @Override
    public Predicate<String> asEndDeviceEventTypeCodePredicate() {
        return code -> meteringService.getEndDeviceEventType(code)
                .map(asEndDeviceEventTypePredicate()::test)
                .orElse(false);
    }

    private Predicate<EndDeviceEventType> matchesType() {
        return eventType -> type == null || type.equals(eventType.getType());
    }

    private Predicate<EndDeviceEventType> matchesDomain() {
        return eventType -> domain == null || domain.equals(eventType.getDomain());
    }

    private Predicate<EndDeviceEventType> matchesSubDomain() {
        return eventType -> subDomain == null || subDomain.equals(eventType.getSubDomain());
    }

    private Predicate<EndDeviceEventType> matchesEventOrAction() {
        return eventType -> eventOrAction == null || eventOrAction.equals(eventType.getEventOrAction());
    }

    @Override
    public void postLoad() {
        String[] fields = code.split("\\.");
        List<Consumer<String>> setters = ImmutableList.of(
                s -> type = EndDeviceType.get(Integer.parseInt(s)),
                s -> domain = EndDeviceDomain.get(Integer.parseInt(s)),
                s -> subDomain = EndDeviceSubDomain.get(Integer.parseInt(s)),
                s -> eventOrAction = EndDeviceEventOrAction.get(Integer.parseInt(s))
        );
        IntStream.range(0, fields.length)
                .filter(i -> fields[i].charAt(0) != '*')
                .forEach(i -> setters.get(i).accept(fields[i]));
    }
}
