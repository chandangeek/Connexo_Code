/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search.enddevice;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Exposes the master resource identifier (mRID)
 * of a {@link com.elster.jupiter.metering.UsagePoint}
 * as a {@link SearchableProperty}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (15:03)
 */
public class MasterResourceIdentifierSearchableProperty implements SearchableEndDeviceProperty {

    private final EndDeviceSearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private static final String FIELDNAME = "mRID";

    public MasterResourceIdentifierSearchableProperty(EndDeviceSearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public EndDeviceSearchDomain getDomain() {
        return domain;
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
        return Visibility.STICKY;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.ENDDEVICE_MRID.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (!this.valueCompatibleForDisplay(value)) {
            throw new IllegalArgumentException("Value not compatible with domain");
        }
        return String.valueOf(value);
    }

    private boolean valueCompatibleForDisplay(Object value) {
        return value instanceof String;
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .stringSpec()
                .named(FIELDNAME, PropertyTranslationKeys.ENDDEVICE_MRID)
                .fromThesaurus(this.thesaurus)
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
    public Condition toCondition(Condition specification) {
        return specification;
    }

}