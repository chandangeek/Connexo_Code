package com.energyict.mdc.device.data.impl.search;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.DeviceFields;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class YearOfCertificationSearchableProperty extends AbstractSearchableDeviceProperty {

    private DeviceSearchDomain domain;
    private SearchablePropertyGroup group;
    private final PropertySpecService propertySpecService;
    private static final int YEARS_IN_LIST_NUMBER = 20;
    private Long[] years = new Long [YEARS_IN_LIST_NUMBER];

    @Inject
    public YearOfCertificationSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
        for (int i=0; i < YEARS_IN_LIST_NUMBER; i++) {
            this.years[i] = Long.valueOf(String.valueOf(ZonedDateTime.now().getYear() - i));
        }
    }

    YearOfCertificationSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
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
        return Optional.of(group);
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
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.DEVICE_CERT_YEAR;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof Long;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return String.valueOf(value);
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .longSpec()
                .named(DeviceFields.CERT_YEAR.fieldName(), this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(this.years)
                .markExhaustive()
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
        return this.toSqlFragment("dev.CERTIF_YEAR", condition, now);
    }
}
