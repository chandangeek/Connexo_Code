/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public abstract class AbstractReadingTypeTimeOfUseSearchableProperty<T> extends AbstractSearchableDeviceProperty {

    private Class<T> implClass;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private DeviceSearchDomain domain;
    private SearchablePropertyGroup group;

    public AbstractReadingTypeTimeOfUseSearchableProperty(Class<T> clazz, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.implClass = clazz;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    T init(DeviceSearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
        return this.implClass.cast(this);
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof Long;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return value.toString();
    }

    @Override
    public final void appendJoinClauses(JoinClauseBuilder builder) {
        // nothing to join to device table
    }

    /**
     * @return join clause for MDS_MEASUREMENTTYPE
     */
    public abstract String getMeasurementTypeJoinSql();

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        if (!(condition instanceof Contains)) {
            throw new IllegalAccessError("Condition must be IN or NOT IN");
        }
        Contains contains = (Contains) condition;
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID");
        if (contains.getOperator() == ListOperator.NOT_IN) {
            sqlBuilder.append(" NOT");
        }
        sqlBuilder.append(" IN (");
        sqlBuilder.append("select DEVICECONFIGID " +
                "from ");
        sqlBuilder.append(getMeasurementTypeJoinSql());
        sqlBuilder.append(" where ");
        sqlBuilder.append(contains.getCollection().stream()
                .map(Long.class::cast)
                .map(tou -> "MDS_MEASUREMENTTYPE.readingtype like '%.%.%.%.%.%.%.%.%.%.%." + tou + ".%.%.%.%.%.%' ")
                .collect(Collectors.joining(" OR ")));
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
        return Optional.of(group);
    }

    @Override
    public abstract String getName();

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .longSpec()
                .named(getName(), PropertyTranslationKeys.READING_TYPE_TOU)
                .fromThesaurus(this.getThesaurus())
                .addValues(LongStream.rangeClosed(0, 255).mapToObj(Long::valueOf).toArray(Long[]::new))
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
        return PropertyTranslationKeys.READING_TYPE_TOU;
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
