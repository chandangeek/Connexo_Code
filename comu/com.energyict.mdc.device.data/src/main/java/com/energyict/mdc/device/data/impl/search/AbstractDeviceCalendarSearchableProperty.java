/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.energyict.mdc.dynamic.PropertySpecService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


public abstract class AbstractDeviceCalendarSearchableProperty extends AbstractSearchableDeviceProperty {

    private DeviceSearchDomain domain;
    private final PropertySpecService mdcPropertySpecService;

    private final CalendarService calendarService;
    private CalendarSearchableGroup calendarSearchableGroup;


    public AbstractDeviceCalendarSearchableProperty(CalendarService calendarService, PropertySpecService mdcPropertySpecService,
            Thesaurus thesaurus) {
        super(thesaurus);
        this.mdcPropertySpecService = mdcPropertySpecService;
        this.calendarService = calendarService;

    }

    AbstractDeviceCalendarSearchableProperty init(DeviceSearchDomain domain,
            CalendarSearchableGroup calendarSearchableGroup) {
        this.calendarSearchableGroup = calendarSearchableGroup;
        this.domain = domain;
        return this;
    }

    @Override
    public SearchDomain getDomain() {
        return this.domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(calendarSearchableGroup);
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
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof Calendar;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((Calendar) value).getName();
    }

    @Override
    public PropertySpec getSpecification() {
        Calendar[] calendars = calendarService.findAllCalendars().stream().toArray(Calendar[]::new);
        return this.mdcPropertySpecService.referenceSpec(Calendar.class)
                .named(getFieldName(), this.getNameTranslationKey()).fromThesaurus(this.getThesaurus())
                .addValues(calendars)
                .markExhaustive()
                .finish();
    }

    protected abstract String getFieldName();

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // Nothing to refresh
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        // Nothing to add
    }

}