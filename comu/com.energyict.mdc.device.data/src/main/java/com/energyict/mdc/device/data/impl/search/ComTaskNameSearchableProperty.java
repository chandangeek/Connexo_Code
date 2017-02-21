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
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ComTaskNameSearchableProperty extends AbstractSearchableDeviceProperty {
    static final String PROPERTY_NAME = "device.comtask.name";

    private final PropertySpecService propertySpecService;
    private final TaskService taskService;
    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;

    @Inject
    public ComTaskNameSearchableProperty(PropertySpecService propertySpecService, TaskService taskService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
        this.taskService = taskService;
    }

    ComTaskNameSearchableProperty init(SearchDomain searchDomain, SearchablePropertyGroup parentGroup) {
        this.searchDomain = searchDomain;
        this.group = parentGroup;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ComTask;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((ComTask) value).getName();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID IN ");
        sqlBuilder.openBracket();
        sqlBuilder.append("select DEVICECOMCONFIG from DTC_COMTASKENABLEMENT cenabl join CTS_COMTASK comtask on comtask.ID = cenabl.COMTASK where ");
        sqlBuilder.add(toSqlFragment("comtask.ID", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
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
        List<ComTask> comTasks = taskService.findAllComTasks().find();
        return this.propertySpecService
                .referenceSpec(ComTask.class)
                .named(PROPERTY_NAME, getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(comTasks.toArray(new ComTask[comTasks.size()]))
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
        return PropertyTranslationKeys.COMTASK_NAME;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // no refresh
    }
}
