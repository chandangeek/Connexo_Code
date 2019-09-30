/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.metering.impl.ServerCalendarUsage;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CalendarSearchableProperty implements SearchableUsagePointProperty {
    private static final String VALUE_NOT_COMPATIBLE = "Value not compatible with domain";
    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final CalendarService calendarService;
    private final Thesaurus thesaurus;
    private final DataModel dataModel;
    static final String FIELD_NAME = "calendar.timeofuse";


    public CalendarSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService,
            CalendarService calendarService, Thesaurus thesaurus, DataModel dataModel) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.calendarService = calendarService;
        this.thesaurus = thesaurus;
        this.dataModel = dataModel;
    }

    @Override
    public SearchDomain getDomain() {
        return domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return true;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.empty();
    }

    @Override
    public PropertySpec getSpecification() {
        Calendar[] calendars = calendarService.findAllCalendars().stream().toArray(Calendar[]::new);
        return this.propertySpecService
                .referenceSpec(Calendar.class)
                .named(FIELD_NAME, PropertyTranslationKeys.USAGEPOINT_CALENDAR)
                .fromThesaurus(thesaurus)
                .addValues(calendars)
                .markExhaustive()
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_CALENDAR.getDisplayName(thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof Calendar) {
            return ((Calendar) value).getName() + getDescription(value);
        }
        throw new IllegalArgumentException(VALUE_NOT_COMPATIBLE);
    }


    private String getDescription(Object value) {
        String descr = ((Calendar) value).getDescription();
        return descr == null || descr.isEmpty() ? "" : " - " + descr;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    @Override
    public Condition toCondition(Condition specification) {
        return ListOperator.IN.contains(
                dataModel.query(ServerCalendarUsage.class).asSubquery(
                        Where.where("calendar").in(new ArrayList<>(((Contains) specification).getCollection())),
                        "usagePoint"),
                "id");
    }
}
