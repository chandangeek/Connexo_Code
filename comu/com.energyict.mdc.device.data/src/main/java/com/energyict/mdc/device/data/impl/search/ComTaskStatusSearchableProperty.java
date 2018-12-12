/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.impl.SearchHelperValueFactory;
import com.energyict.mdc.device.data.impl.TaskStatusTranslationKeys;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionFilterSqlBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComTaskStatusSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.comtask.status";

    private final PropertySpecService propertySpecService;
    private final Clock clock;

    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;

    @Inject
    public ComTaskStatusSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus, Clock clock) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
        this.clock = clock;
    }

    ComTaskStatusSearchableProperty init(SearchDomain searchDomain, SearchablePropertyGroup parentGroup) {
        this.searchDomain = searchDomain;
        this.group = parentGroup;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ComTaskStatusInfo;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((ComTaskStatusInfo) value).getName();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        if (!(condition instanceof Contains)) {
            throw new IllegalArgumentException("Condition must be IN or NOT IN");
        }
        Contains contains = (Contains) condition;
        Collection<?> collection = contains.getCollection();
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = collection.stream()
                .map(ComTaskStatusInfo.class::cast)
                .map(ComTaskStatusInfo::getStatus)
                .collect(Collectors.toSet());
        filter.restrictedDeviceStates = Collections.emptySet();//no restrictions on device states
        ComTaskExecutionFilterSqlBuilder comTaskExecutionSqlBuilder = new ComTaskExecutionFilterSqlBuilder(filter, clock, null);//null because we will not use device groups in filter
        SqlFragment devicesWithSuchComTasksSql = comTaskExecutionSqlBuilder.build(new SqlBuilder("select cte.device from DDC_COMTASKEXEC cte"), "cte").asBuilder();

        SqlBuilder sqlBuilder = new SqlBuilder(JoinClauseBuilder.Aliases.DEVICE + ".id ");
        sqlBuilder.append(contains.getOperator().getSymbol());
        sqlBuilder.openBracket();
        sqlBuilder.add(devicesWithSuchComTasksSql);
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public SearchDomain getDomain() {
        return searchDomain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(group);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.COMTASK_STATUS;
    }

    @Override
    public PropertySpec getSpecification() {
        return propertySpecService
                .specForValuesOf(new ComTaskExecutionValueFactory())
                .named(this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(getPossibleValues())
                .markExhaustive()
                .finish();
    }

    private ComTaskStatusInfo[] getPossibleValues() {
        return Stream.of(TaskStatus.values()).map(status -> new ComTaskStatusInfo(this.getThesaurus(), status)).toArray(ComTaskStatusInfo[]::new);
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
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    private class ComTaskExecutionValueFactory extends SearchHelperValueFactory<ComTaskStatusInfo> {

        private ComTaskExecutionValueFactory() {
            super(ComTaskStatusInfo.class);
        }

        @Override
        public ComTaskStatusInfo fromStringValue(String stringValue) {
            return new ComTaskStatusInfo(getThesaurus(), TaskStatus.valueOf(stringValue));
        }

        @Override
        public String toStringValue(ComTaskStatusInfo object) {
            return object.getId();
        }

    }

    static class ComTaskStatusInfo extends HasIdAndName {

        final Thesaurus thesaurus;
        TaskStatus taskStatus;

        ComTaskStatusInfo(Thesaurus thesaurus, TaskStatus taskStatus) {
            this.thesaurus = thesaurus;
            this.taskStatus = taskStatus;
        }

        @Override
        public String getId() {
            return taskStatus.name();
        }

        @Override
        public String getName() {
            return thesaurus.getFormat(TaskStatusTranslationKeys.from(taskStatus)).format();
        }

        public TaskStatus getStatus() {
            return taskStatus;
        }
    }

}