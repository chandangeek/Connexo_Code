package com.elster.jupiter.metering.impl.search;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.SearchableProperty.SelectionMode;
import com.elster.jupiter.search.SearchableProperty.Visibility;
import com.elster.jupiter.util.conditions.Condition;

public class NameSearchableProperty implements SearchableUsagePointProperty {
   
    private final UsagePointSearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private static final String FIELDNAME = "name";

    public NameSearchableProperty(UsagePointSearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public UsagePointSearchDomain getDomain() {
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
        return PropertyTranslationKeys.USAGEPOINT_NAME.getDisplayName(this.thesaurus);
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
        return this.propertySpecService.basicPropertySpec(
                FIELDNAME,
                false,
                new StringFactory());
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
