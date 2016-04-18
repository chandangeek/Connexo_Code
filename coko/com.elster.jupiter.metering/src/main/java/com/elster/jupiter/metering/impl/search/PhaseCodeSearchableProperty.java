package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.EnumFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PhaseCodeSearchableProperty implements SearchableUsagePointProperty {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;


    private SearchablePropertyGroup group;
    private SearchDomain domain;
    private static final String FIELDNAME = "detail.phaseCode";

    @Inject
    public PhaseCodeSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    PhaseCodeSearchableProperty init(SearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
        return this;
    }
    @Override
    public SearchDomain getDomain() {
        return domain;
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
                .specForValuesOf(new EnumFactory(PhaseCode.class))
                .named(FIELDNAME, PropertyTranslationKeys.USAGEPOINT_PHASECODE)
                .fromThesaurus(this.thesaurus)
                .addValues(PhaseCode.values())
                .markExhaustive()
                .finish();
    }

    @Override
    public SearchableProperty.Visibility getVisibility() {
        return SearchableProperty.Visibility.REMOVABLE;
    }

    @Override
    public SearchableProperty.SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_PHASECODE.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof PhaseCode) {
            return ((PhaseCode) value).getValue();
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(new ServiceCategorySearchableProperty(this.domain, this.propertySpecService, this.thesaurus));
    }

    private boolean valueCompatibleForDisplay(Object value) {
        return value instanceof Enum;
    }

    private String toDisplayAfterValidation(Object value) {
        PhaseCode phaseCodes = (PhaseCode) value;
        return phaseCodes.getValue();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {

    }

    @Override
    public Condition toCondition(Condition specification) {
        return specification.and(Where.where("detail.interval").isEffective(Instant.now()));
    }
}
