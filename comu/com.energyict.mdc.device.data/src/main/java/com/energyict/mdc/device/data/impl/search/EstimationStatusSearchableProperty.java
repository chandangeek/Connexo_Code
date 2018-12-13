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
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EstimationStatusSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.estimation.status";

    private DeviceSearchDomain domain;
    private SearchablePropertyGroup group;

    private final PropertySpecService propertySpecService;

    @Inject
    public EstimationStatusSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    EstimationStatusSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof DeviceDataStatusSearchWrapper;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return getThesaurus().getFormat(((DeviceDataStatusSearchWrapper) value)
                .getDeviceDataStatusContainer().getTranslation()).format();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return this.toSqlFragment("dev.ESTIMATION_ACTIVE", condition, now);
    }

    @Override
    public void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException {
        Boolean result = ((DeviceDataStatusSearchWrapper) value).getDeviceDataStatusContainer()
                .getTranslation()
                .getDefaultFormat()
                .equals(DeviceDataStatusContainer.ACTIVE.getTranslation().getDefaultFormat());
        statement.setString(bindPosition, result ? "Y" : "N");
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
        return Optional.of(this.group);
    }

    @Override
    public PropertySpec getSpecification() {
        Stream<DeviceDataStatusSearchWrapper> estimations =
                Arrays.stream(DeviceDataStatusContainer.values()).map(DeviceDataStatusSearchWrapper::new);
        return this.propertySpecService
                .specForValuesOf(new DeviceDataStatusValueFactory())
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(estimations.toArray(DeviceDataStatusSearchWrapper[]::new))
                .markExhaustive()
                .finish();
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
        return PropertyTranslationKeys.ESTIMATION_STATUS;
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