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
import com.energyict.mdc.device.config.DeviceConfigurationService;
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
 * Exposes the {@link }
 * of a {@link Device} as a {@link SearchableProperty}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (15:41)
 */
public class DeviceTypeSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = DeviceFields.DEVICETYPE.fieldName();

    private DeviceSearchDomain domain;
    private final DeviceConfigurationService deviceConfigurationService;
    private final PropertySpecService mdcPropertySpecService;

    @Inject
    public DeviceTypeSearchableProperty(DeviceConfigurationService deviceConfigurationService, PropertySpecService mdcPropertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.mdcPropertySpecService = mdcPropertySpecService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    DeviceTypeSearchableProperty init(DeviceSearchDomain domain) {
        this.domain = domain;
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
        return PropertyTranslationKeys.DEVICE_TYPE;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof DeviceType;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((DeviceType) value).getName();
    }

    @Override
    public PropertySpec getSpecification() {
        List<DeviceType> deviceTypes = this.deviceConfigurationService.findAllDeviceTypes().find();
        return this.mdcPropertySpecService
                .referenceSpec(DeviceType.class)
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(deviceTypes.toArray(new DeviceType[deviceTypes.size()]))
                .markExhaustive()
                .finish();
    }

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
        // No join clauses required
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return this.toSqlFragment("devicetype", condition, now);
    }

}