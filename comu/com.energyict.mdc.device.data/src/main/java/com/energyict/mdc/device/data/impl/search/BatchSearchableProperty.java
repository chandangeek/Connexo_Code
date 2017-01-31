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
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BatchSearchableProperty extends AbstractSearchableDeviceProperty {

    private DeviceSearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public BatchSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    BatchSearchableProperty init(DeviceSearchDomain domain) {
        this.domain = domain;
        return this;
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
        return Optional.empty();
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
        return PropertyTranslationKeys.BATCH;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof Batch;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((Batch)value).getName();
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .stringSpec()
                .named(DeviceFields.BATCH.fieldName(), this.getNameTranslationKey())
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
        builder.addBatch();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return this.toSqlFragment("bch.NAME", condition, now);
    }
}

