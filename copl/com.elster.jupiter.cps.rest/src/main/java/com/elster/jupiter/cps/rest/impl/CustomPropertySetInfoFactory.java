package com.elster.jupiter.cps.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomPropertySetInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public CustomPropertySetInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public List<CustomPropertySetInfo> from(Iterable<? extends RegisteredCustomPropertySet> registeredCustomPropertySets) {
        List<CustomPropertySetInfo> customPropertySetInfos = new ArrayList<>();
        for (RegisteredCustomPropertySet registeredCustomPropertySet : registeredCustomPropertySets) {
            customPropertySetInfos.add(new CustomPropertySetInfo(registeredCustomPropertySet,
                    getAttributes(registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs()),
                    thesaurus.getString(registeredCustomPropertySet.getCustomPropertySet().getDomainClass().getName(),
                            registeredCustomPropertySet.getCustomPropertySet().getDomainClass().getName())));
        }
        return customPropertySetInfos;
    }

    public List<CustomPropertySetDomainExtensionNameInfo> from(Set<String> domainExtensions) {
        return domainExtensions.stream().map(domainExtension -> new CustomPropertySetDomainExtensionNameInfo(domainExtension,
                thesaurus.getStringBeyondComponent(domainExtension, domainExtension))).collect(Collectors.toList());
    }

    private List<CustomPropertySetAttributeInfo> getAttributes(List<PropertySpec> propertySpecs) {
        return propertySpecs.stream().map(CustomPropertySetAttributeInfo::new).collect(Collectors.toList());
    }
}