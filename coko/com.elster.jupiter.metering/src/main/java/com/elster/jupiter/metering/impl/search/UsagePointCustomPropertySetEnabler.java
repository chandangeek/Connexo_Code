/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetSearchEnabler;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.metering.impl.search.UsagePointCustomPropertySetEnabler", service = CustomPropertySetSearchEnabler.class, immediate = true)
public class UsagePointCustomPropertySetEnabler implements CustomPropertySetSearchEnabler {
    private MeteringService meteringService;

    @Override
    public Class getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public boolean enableWhen(CustomPropertySet customPropertySet, List<SearchablePropertyConstriction> constrictions) {
        return constrictions
                .stream()
                .anyMatch(candidate -> cpsIsAPartOfServiceCategory(customPropertySet, candidate)
                        || cpsIsAPartOfMetrologyConfiguration(customPropertySet, candidate));
    }

    private boolean cpsIsAPartOfServiceCategory(CustomPropertySet customPropertySet, SearchablePropertyConstriction constriction) {
        return constriction.getConstrainingProperty().hasName(ServiceCategorySearchableProperty.FIELD_NAME) && constriction.getConstrainingValues()
                .stream()
                .map(ServiceKind.class::cast)
                .map(kind -> this.meteringService.getServiceCategory(kind))
                .filter(Optional::isPresent)
                .flatMap(sc -> sc.get().getCustomPropertySets().stream())
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .map(CustomPropertySet::getId)
                .anyMatch(id -> id.equals(customPropertySet.getId()));
    }

    private boolean cpsIsAPartOfMetrologyConfiguration(CustomPropertySet customPropertySet, SearchablePropertyConstriction constriction) {
        return constriction.getConstrainingProperty().hasName(MetrologyConfigurationSearchableProperty.FIELD_NAME) && constriction.getConstrainingValues()
                .stream()
                .map(MetrologyConfiguration.class::cast)
                .flatMap(mc -> mc.getCustomPropertySets().stream())
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .map(CustomPropertySet::getId)
                .anyMatch(id -> id.equals(customPropertySet.getId()));
    }

    @Override
    public List<SearchableProperty> getConstrainingProperties(CustomPropertySet customPropertySet, List<SearchablePropertyConstriction> constrictions) {
        return constrictions
                .stream()
                .filter(candidate -> cpsIsAPartOfServiceCategory(customPropertySet, candidate)
                        || cpsIsAPartOfMetrologyConfiguration(customPropertySet, candidate))
                .map(SearchablePropertyConstriction::getConstrainingProperty)
                .collect(Collectors.toList());
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
}
