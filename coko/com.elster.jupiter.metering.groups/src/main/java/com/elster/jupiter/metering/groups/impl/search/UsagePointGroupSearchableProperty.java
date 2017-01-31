/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl.search;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlBuilder;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UsagePointGroupSearchableProperty implements SearchableProperty {

    public static final String PROPERTY_NAME = "usagePointGroup";

    private final PropertySpecService propertySpecService;
    private final MeteringGroupsService meteringGroupsService;
    private final Thesaurus thesaurus;

    @Inject
    public UsagePointGroupSearchableProperty(PropertySpecService propertySpecService, MeteringGroupsService meteringGroupsService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.meteringGroupsService = meteringGroupsService;
        this.thesaurus = thesaurus;
    }

    public static Condition toCondition(Condition condition) {
        Contains contains = (Contains) condition;
        SqlBuilder sqlBuilder = new SqlBuilder();
        groupsAsSql(sqlBuilder, (Collection<UsagePointGroup>) contains.getCollection());
        return contains.getOperator().contains(() -> sqlBuilder, "id");
    }

    private static void groupsAsSql(SqlBuilder sqlBuilder, Collection<UsagePointGroup> usagePointGroups) {
        usagePointGroups.stream()
                .map(group -> group.toSubQuery("id").toFragment())
                .reduce((fragment1, fragment2) -> {
                    sqlBuilder.add(fragment1);
                    sqlBuilder.append(" UNION ");
                    return fragment2;
                }).ifPresent(sqlBuilder::add);
    }

    @Override
    public SearchDomain getDomain() {
        return null;
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
        List<UsagePointGroup> usagePointGroups = this.meteringGroupsService.findUsagePointGroups();
        return this.propertySpecService
                .referenceSpec(UsagePointGroup.class)
                .named(PROPERTY_NAME, PropertyTranslationKeys.USAGE_POINT_GROUP)
                .fromThesaurus(this.thesaurus)
                .addValues(usagePointGroups.toArray(new UsagePointGroup[usagePointGroups.size()]))
                .markExhaustive()
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.STICKY;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGE_POINT_GROUP.getDisplayName(thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof UsagePointGroup) {
            return ((UsagePointGroup) value).getName();
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // Nothing to refresh
    }
}
