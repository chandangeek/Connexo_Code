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
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Exposes the {@link DeviceConfiguration}
 * of a {@link Device} as a {@link SearchableProperty}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-01 (15:52)
 */
public class DeviceConfigurationSearchableProperty extends AbstractSearchableDeviceProperty {

    public static final String PROPERTY_NAME = DeviceFields.DEVICECONFIGURATION.fieldName();

    private DeviceSearchDomain domain;
    private SearchableProperty parent;
    private final PropertySpecService propertySpecService;
    private DeviceConfiguration[] deviceConfigurations = new DeviceConfiguration[0];

    @Inject
    public DeviceConfigurationSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    DeviceConfigurationSearchableProperty init(DeviceSearchDomain domain, DeviceTypeSearchableProperty parent) {
        this.domain = domain;
        this.parent = parent;
        return this;
    }

    @Override
    public SearchDomain getDomain() {
        return this.domain;
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
    public Visibility getVisibility() {
        return Visibility.STICKY;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.DEVICE_CONFIGURATION;
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .referenceSpec(DeviceConfiguration.class)
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(this.deviceConfigurations)
                .markExhaustive()
                .finish();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(this.parent);
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // We have at most one constraint
        if (constrictions.size() != 1) {
            throw new IllegalArgumentException("Expecting exactly 1 constriction, i.e. the constraint on the device type");
        }
        this.refreshWithConstriction(constrictions.get(0));
    }

    private void refreshWithConstriction(SearchablePropertyConstriction constriction) {
        if (constriction.getConstrainingProperty().hasName(DeviceTypeSearchableProperty.PROPERTY_NAME)) {
            this.refreshWithConstrictionValues(constriction.getConstrainingValues());
        }
        else {
            throw new IllegalArgumentException("Unknown or unexpected constriction, was expecting the constraining property to be the device type");
        }
    }

    private void refreshWithConstrictionValues(List<Object> list) {
        this.validateAllParentsAreDeviceTypes(list);
        this.deviceConfigurations =
            list.stream()
                .map(DeviceType.class::cast)
                .flatMap(each -> each.getConfigurations().stream())
                .toArray(DeviceConfiguration[]::new);
    }

    private void validateAllParentsAreDeviceTypes(List<Object> list) {
        Optional<Object> anyNonDeviceType =
            list.stream()
                .filter(Predicates.not(DeviceType.class::isInstance))
                .findAny();
        if (anyNonDeviceType.isPresent()) {
            throw new IllegalArgumentException("Constricting values are expected to be of type " + DeviceType.class.getName());
        }
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        // No join clauses required
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return this.toSqlFragment("deviceconfigid", condition, now);
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof DeviceConfiguration;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        DeviceConfiguration deviceConfiguration = (DeviceConfiguration) value;
        return deviceConfiguration.getName() + " (" + deviceConfiguration.getDeviceType().getName() + ")";
    }
}