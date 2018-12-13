/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SharedScheduleSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.shared.schedule";

    private DeviceSearchDomain domain;
    private final SchedulingService schedulingService;
    private final PropertySpecService propertySpecService;

    @Inject
    public SharedScheduleSearchableProperty(SchedulingService schedulingService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.schedulingService = schedulingService;
        this.propertySpecService = propertySpecService;
    }

    SharedScheduleSearchableProperty init(DeviceSearchDomain domain) {
        this.domain = domain;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ComSchedule;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((ComSchedule) value).getName();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".id IN ");
        sqlBuilder.openBracket();
        sqlBuilder.append("select DEVICE from DDC_COMTASKEXEC where ");
        sqlBuilder.add(this.toSqlFragment("DDC_COMTASKEXEC.comschedule", condition, now));
        sqlBuilder.append(" AND DDC_COMTASKEXEC.obsolete_date is null");
        sqlBuilder.closeBracket();
        return sqlBuilder;
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
        return Optional.empty();
    }

    @Override
    public PropertySpec getSpecification() {
        List<ComSchedule> comSchedules = this.schedulingService.findAllSchedules().find();
        return propertySpecService
                .referenceSpec(ComSchedule.class)
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(comSchedules.toArray(new ComSchedule[comSchedules.size()]))
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
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.SHARED_SCHEDULE;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }
}
