/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.impl.search.AbstractSearchableDeviceProperty;
import com.energyict.mdc.device.data.impl.search.JoinClauseBuilder;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceRegisterSAPInfoCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceRegisterSAPInfoDomainExtension;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LogicalRegisterNumberSearchableProperty extends AbstractSearchableDeviceProperty {

    public static final String PROPERTY_NAME = DeviceRegisterSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName();
    private final PropertySpecService propertySpecService;
    private final OrmService ormService;
    private SearchablePropertyGroup group;

    @Inject
    public LogicalRegisterNumberSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus, OrmService ormService) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
        this.ormService = ormService;
    }

    public LogicalRegisterNumberSearchableProperty init(SearchablePropertyGroup group) {
        this.group = group;
        return this;
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
        return Optional.of(group);
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.LOGICAL_REGISTER_NUMBER;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof String;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return String.valueOf(value);
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .stringSpec()
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .finish();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        if (!constrictions.isEmpty()) {
            throw new IllegalArgumentException("No constraint to refresh");
        }
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        // No join clauses required
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.add(getDataModel(DeviceRegisterSAPInfoCustomPropertySet.MODEL_NAME).query(DeviceRegisterSAPInfoDomainExtension.class).asFragment(condition, DeviceRegisterSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()));
        builder.append(" INTERSECT ");
        builder.add(getDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME).query(DeviceChannelSAPInfoDomainExtension.class).asFragment(condition, DeviceChannelSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()));
        return builder;
    }

    @Override
    public List<String> getAvailableOperators(){
        return Arrays.asList(SearchablePropertyOperator.EQUAL.code(), SearchablePropertyOperator.NOT_EQUAL.code(), SearchablePropertyOperator.IN.code());
    }

    private DataModel getDataModel(String modelName) {
        return ormService.getDataModel(modelName)
                .orElseThrow(() -> new IllegalStateException(DataModel.class.getSimpleName() + ' ' + modelName + " isn't found."));
    }
}