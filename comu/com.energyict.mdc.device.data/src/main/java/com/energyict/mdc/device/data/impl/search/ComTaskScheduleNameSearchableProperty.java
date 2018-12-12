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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ComTaskScheduleNameSearchableProperty extends AbstractSearchableDeviceProperty {
    static final String PROPERTY_NAME = "device.comtask.schedule.name";

    private final PropertySpecService propertySpecService;
    private final SchedulingService schedulingService;

    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;

    @Inject
    public ComTaskScheduleNameSearchableProperty(PropertySpecService propertySpecService, SchedulingService schedulingService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
        this.schedulingService = schedulingService;
    }

    ComTaskScheduleNameSearchableProperty init(SearchDomain searchDomain, SearchablePropertyGroup parentGroup) {
        this.searchDomain = searchDomain;
        this.group = parentGroup;
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
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID IN ");
        sqlBuilder.openBracket();
        sqlBuilder.append("select DEVICE from DDC_COMTASKEXEC " +
                "join SCH_COMSCHEDULE on DDC_COMTASKEXEC.COMSCHEDULE = SCH_COMSCHEDULE.ID " +
                "where DDC_COMTASKEXEC.OBSOLETE_DATE IS NULL AND ");
        sqlBuilder.add(toSqlFragment("SCH_COMSCHEDULE.NAME", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException {
        statement.setString(bindPosition, toDisplayAfterValidation(value));
    }

    @Override
    public SearchDomain getDomain() {
        return this.searchDomain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(this.group);
    }

    @Override
    public PropertySpec getSpecification() {
        List<ComSchedule> comSchedules = this.schedulingService.getAllSchedules();
        return this.propertySpecService
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
        return PropertyTranslationKeys.COMTASK_SCHEDULE_NAME;
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
