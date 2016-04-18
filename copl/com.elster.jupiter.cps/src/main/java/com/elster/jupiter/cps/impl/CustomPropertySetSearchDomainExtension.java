package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.List;
import java.util.stream.Collectors;

public class CustomPropertySetSearchDomainExtension implements SearchDomainExtension {
    private final CustomPropertySetServiceImpl customPropertySetService;
    private final CustomPropertySet<?, ?> customPropertySet;

    public CustomPropertySetSearchDomainExtension(CustomPropertySetServiceImpl customPropertySetService, CustomPropertySet<?, ?> customPropertySet) {
        this.customPropertySetService = customPropertySetService;
        this.customPropertySet = customPropertySet;
    }

    @Override
    public boolean isExtensionFor(SearchDomain domain, List<SearchablePropertyConstriction> constrictions) {
        return domain.supports(this.customPropertySet.getDomainClass())
                && (this.customPropertySet.isSearchableByDefault()
                || this.customPropertySetService.isSearchEnabledForCustomPropertySet(this.customPropertySet, constrictions));
    }

    @Override
    public List<SearchableProperty> getProperties() {
        CustomPropertySetSearchableGroup searchablePropertyGroup = new CustomPropertySetSearchableGroup(this.customPropertySet);
        return this.customPropertySet.getPropertySpecs()
                .stream()
                .map(propertySpec -> new CustomPropertySetSearchablePropertyImpl(this.customPropertySet, propertySpec, searchablePropertyGroup))
                .collect(Collectors.toList());
    }

    private static class CustomPropertySetSearchableGroup implements SearchablePropertyGroup {
        private final CustomPropertySet<?, ?> customPropertySet;

        CustomPropertySetSearchableGroup(CustomPropertySet<?, ?> customPropertySet) {
            this.customPropertySet = customPropertySet;
        }

        @Override
        public String getId() {
            return customPropertySet.getId();
        }

        @Override
        public String getDisplayName() {
            return customPropertySet.getName();
        }
    }
}
