package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.EnumFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Exposes the service kind enumeration
 * of a {@link com.elster.jupiter.metering.UsagePoint}
 * as a {@link SearchableProperty}.
 *
 * @author Anton Fomchenko
 * @since 2015-08-12
 */
public class ServiceCategorySearchableProperty implements SearchableUsagePointProperty {

    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    static final String FIELDNAME = "SERVICEKIND";

    public ServiceCategorySearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public SearchDomain getDomain() {
        return domain;
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
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_SERVICECATEGORY.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (!this.valueCompatibleForDisplay(value)) {
            throw new IllegalArgumentException("Value not compatible with domain");
        }
        return this.toDisplayAfterValidation(value);
    }

    private boolean valueCompatibleForDisplay(Object value) {
        return value instanceof Enum;
    }

    protected String toDisplayAfterValidation(Object value) {
        ServiceKind serviceKind = (ServiceKind) value;
        return this.thesaurus.getStringBeyondComponent(serviceKind.getKey(), serviceKind.getDefaultFormat());
    }
    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new EnumFactory(ServiceKind.class))
                .named(FIELDNAME, PropertyTranslationKeys.USAGEPOINT_SERVICECATEGORY)
                .fromThesaurus(this.thesaurus)
                .addValues(ServiceKind.values())
                .markExhaustive()
                .finish();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
    }

    @Override
    public Condition toCondition(Condition specification) {
        return specification;
    }

}