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
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ComTaskSecuritySettingSearchableProperty extends AbstractSearchableDeviceProperty {
    static final String PROPERTY_NAME = "device.comtask.security";

    private final PropertySpecService propertySpecService;

    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;
    private SearchableProperty parent;
    private SecurityPropertySet[] possibleValues = new SecurityPropertySet[0];

    @Inject
    public ComTaskSecuritySettingSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    ComTaskSecuritySettingSearchableProperty init(SearchDomain searchDomain, SearchablePropertyGroup parentGroup, SearchableProperty parent) {
        this.searchDomain = searchDomain;
        this.group = parentGroup;
        this.parent = parent;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof SecurityPropertySet;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        SecurityPropertySet securityPropertySet = (SecurityPropertySet) value;
        return securityPropertySet.getName()
                + " (" + securityPropertySet.getDeviceConfiguration().getDeviceType().getName()
                + " / " + securityPropertySet.getDeviceConfiguration().getName() + ")";
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID IN ");
        sqlBuilder.openBracket();
        sqlBuilder.append("select DEVICECOMCONFIG from DTC_COMTASKENABLEMENT where ");
        sqlBuilder.add(this.toSqlFragment("SECURITYPROPERTYSET ", condition, now));
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
        return this.propertySpecService
                .referenceSpec(SecurityPropertySet.class)
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(this.possibleValues)
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
        return PropertyTranslationKeys.COMTASK_SECURITY_SETTING;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(this.parent);
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        if (constrictions.size() != 1) {
            throw new IllegalArgumentException("Expecting a constraint on the device type");
        }
        this.refreshWithConstrictions(constrictions.get(0));
    }

    private void refreshWithConstrictions(SearchablePropertyConstriction constriction) {
        if (constriction.getConstrainingProperty().hasName(DeviceTypeSearchableProperty.PROPERTY_NAME)) {
            this.refreshWithConstrictionValues(constriction.getConstrainingValues());
        } else {
            throw new IllegalArgumentException("Unknown or unexpected constriction, was expecting the constraining property to be the device type");
        }
    }

    private void refreshWithConstrictionValues(List<Object> deviceTypes) {
        this.validateObjectsType(deviceTypes);
        this.possibleValues = deviceTypes.stream()
                .map(DeviceType.class::cast)
                .flatMap(deviceType -> deviceType.getConfigurations().stream())
                .flatMap(deviceConfiguration ->deviceConfiguration.getSecurityPropertySets().stream())
                .sorted(Comparator.comparing(SecurityPropertySet::getName, String.CASE_INSENSITIVE_ORDER))
                .toArray(SecurityPropertySet[]::new);
    }

    private void validateObjectsType(List<Object> objectsForValidation) {
        objectsForValidation.stream()
                .filter(Predicates.not(DeviceType.class::isInstance))
                .findAny()
                .ifPresent(badObj -> {
                    throw new IllegalArgumentException("Parents are expected to be of type " + DeviceType.class.getName());
                });
    }
}
