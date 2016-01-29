package com.elster.jupiter.cps.rest;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CustomPropertySetInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public CustomPropertySetInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public List<CustomPropertySetInfo> from(Iterable<? extends RegisteredCustomPropertySet> registeredCustomPropertySets) {
        return StreamSupport.stream(registeredCustomPropertySets.spliterator(), false)
                .map(this::from)
                .collect(Collectors.toList());
    }

    public CustomPropertySetInfo from(RegisteredCustomPropertySet registeredCustomPropertySet){
        return new CustomPropertySetInfo(registeredCustomPropertySet,
                getAttributes(registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs()),
                thesaurus.getStringBeyondComponent(registeredCustomPropertySet.getCustomPropertySet().getDomainClass().getName(),
                        registeredCustomPropertySet.getCustomPropertySet().getDomainClass().getName()));
    }

    public List<CustomPropertySetDomainExtensionNameInfo> from(Set<String> domainExtensions) {
        return domainExtensions.stream().map(domainExtension -> new CustomPropertySetDomainExtensionNameInfo(domainExtension,
                thesaurus.getStringBeyondComponent(domainExtension, domainExtension))).collect(Collectors.toList());
    }

    private List<CustomPropertySetAttributeInfo> getAttributes(List<PropertySpec> propertySpecs) {
        List<CustomPropertySetAttributeInfo> customPropertySetAttributeInfos = new ArrayList<>();
        propertySpecs.stream().forEach(attribute -> customPropertySetAttributeInfos.add(new CustomPropertySetAttributeInfo(attribute, thesaurus)));
        return customPropertySetAttributeInfos;
    }
}