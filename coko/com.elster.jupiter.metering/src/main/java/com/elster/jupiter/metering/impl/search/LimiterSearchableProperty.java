package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.EnumFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LimiterSearchableProperty implements SearchableUsagePointProperty {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    private SearchDomain domain;
    private SearchablePropertyGroup group;
    private Clock clock;
    public static final String FIELD_NAME = "detail.limiter";
    private String uniqueName;

    @Inject
    public LimiterSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    LimiterSearchableProperty init(SearchDomain domain, SearchablePropertyGroup group, Clock clock) {
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
        return true;
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
        return PropertyTranslationKeys.USAGEPOINT_LIMITER.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof YesNoAnswer) {
            String string = value.toString();
            return thesaurus.getString(string, string);
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }


    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new EnumFactory(YesNoAnswer.class))
                .named(uniqueName, PropertyTranslationKeys.USAGEPOINT_LIMITER)
                .fromThesaurus(this.thesaurus)
                .addValues(YesNoAnswer.values())
                .markExhaustive()
                .finish();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(new ServiceCategorySearchableProperty(this.domain, this.propertySpecService, this.thesaurus));
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
