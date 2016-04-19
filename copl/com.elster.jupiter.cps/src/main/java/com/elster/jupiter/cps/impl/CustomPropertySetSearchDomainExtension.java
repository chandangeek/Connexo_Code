package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import java.util.List;
import java.util.stream.Collectors;

public class CustomPropertySetSearchDomainExtension implements SearchDomainExtension {
    private final CustomPropertySetServiceImpl customPropertySetService;
    private final CustomPropertySet<?, ?> customPropertySet;
    private final DataModel dataModel;

    public CustomPropertySetSearchDomainExtension(CustomPropertySetServiceImpl customPropertySetService, ActiveCustomPropertySet activeCustomPropertySet) {
        this.customPropertySetService = customPropertySetService;
        this.customPropertySet = activeCustomPropertySet.getCustomPropertySet();
        this.dataModel = activeCustomPropertySet.getDataModel();
    }

    @Override
    public boolean isExtensionFor(SearchDomain domain, List<SearchablePropertyConstriction> constrictions) {
        return domain.getDomainClass().isAssignableFrom(this.customPropertySet.getDomainClass())
                && (this.customPropertySet.isSearchableByDefault()
                || this.customPropertySetService.isSearchEnabledForCustomPropertySet(this.customPropertySet, constrictions));
    }

    @Override
    public List<SearchableProperty> getProperties() {
        CustomPropertySetSearchablePropertyGroup searchablePropertyGroup = new CustomPropertySetSearchablePropertyGroup(this.customPropertySet);
        return this.customPropertySet.getPropertySpecs()
                .stream()
                .map(propertySpec -> new CustomPropertySetSearchableProperty(this.customPropertySet, propertySpec, searchablePropertyGroup))
                .collect(Collectors.toList());
    }

    @Override
    public SqlFragment asFragment(List<SearchablePropertyCondition> conditions) {
        return this.dataModel.query(customPropertySet.getPersistenceSupport().persistenceClass())
                .asFragment(Condition.TRUE, customPropertySet.getPersistenceSupport().domainFieldName());
    }
}
