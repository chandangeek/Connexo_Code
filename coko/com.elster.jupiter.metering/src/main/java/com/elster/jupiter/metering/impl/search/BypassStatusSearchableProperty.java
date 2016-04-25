package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.EnumFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BypassStatusSearchableProperty implements SearchableUsagePointProperty {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    private SearchDomain domain;
    private SearchablePropertyGroup group;
    private Clock clock;
    private static final String FIELD_NAME = "detail.bypassStatus";
    private String uniqueName;

    @Inject
    public BypassStatusSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    BypassStatusSearchableProperty init(SearchDomain domain, SearchablePropertyGroup group, Clock clock) {
        this.domain = domain;
        this.group = group;
        this.clock = clock;
        this.uniqueName = FIELD_NAME.concat(".").concat(group.getId());
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
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_BYPASS_STATUS.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof BypassStatus) {
            return ((BypassStatus) value).getDisplayValue(this.thesaurus);
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new EnumFactory(BypassStatus.class))
                .named(uniqueName, PropertyTranslationKeys.USAGEPOINT_BYPASS_STATUS)
                .fromThesaurus(this.thesaurus)
                .addValues(BypassStatus.values())
                .markExhaustive()
                .finish();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(new BypassSearchableProperty(this.propertySpecService, this.thesaurus)
                .init(this.domain, this.group, this.clock));
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    @Override
    public Condition toCondition(Condition specification) {
        List<Object> values = new ArrayList<>(((Contains) specification).getCollection());
        return Where.where(FIELD_NAME).in(values).and(Where.where("detail.interval").isEffective(this.clock.instant()));
    }
}
