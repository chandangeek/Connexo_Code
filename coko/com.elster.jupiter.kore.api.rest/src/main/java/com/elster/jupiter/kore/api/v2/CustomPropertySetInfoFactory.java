/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyValueInfoService;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.RangeInstantBuilder;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class CustomPropertySetInfoFactory extends SelectableFieldFactory<CustomPropertySetInfo, RegisteredCustomPropertySet> {

    private final Clock clock;
    private final PropertyValueInfoService propertyValueInfoService;
    private final Thesaurus thesaurus;

    @Inject
    public CustomPropertySetInfoFactory(Clock clock, PropertyValueInfoService propertyValueInfoService, Thesaurus thesaurus) {
        this.clock = clock;
        this.propertyValueInfoService = propertyValueInfoService;
        this.thesaurus = thesaurus;
    }

    public LinkInfo asLink(RegisteredCustomPropertySet registeredCustomPropertySet, Relation relation, UriInfo uriInfo) {
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        copySelectedFields(info, registeredCustomPropertySet, uriInfo, Arrays.asList("id", "version"));
        info.link = link(registeredCustomPropertySet, relation, uriInfo);
        return info;
    }

    private Link link(RegisteredCustomPropertySet registeredCustomPropertySet, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Custom property set")
                .build(registeredCustomPropertySet.getCustomPropertySet().getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(CustomPropertySetResource.class)
                .path(CustomPropertySetResource.class, "getCustomPropertySet");
    }

    public CustomPropertySetInfo from(RegisteredCustomPropertySet registeredCustomPropertySet, UriInfo uriInfo, Collection<String> fields) {
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        copySelectedFields(info, registeredCustomPropertySet, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<CustomPropertySetInfo, RegisteredCustomPropertySet>> buildFieldMap() {
        Map<String, PropertyCopier<CustomPropertySetInfo, RegisteredCustomPropertySet>> map = new HashMap<>();
        map.put("id", (customPropertySetInfo, registeredCustomPropertySet, uriInfo) -> customPropertySetInfo.id = registeredCustomPropertySet
                .getId());
        map.put("link", ((customPropertySetInfo, registeredCustomPropertySet, uriInfo) ->
                customPropertySetInfo.link = link(registeredCustomPropertySet, Relation.REF_SELF, uriInfo)));
        map.put("name", (customPropertySetInfo, registeredCustomPropertySet, uriInfo) -> customPropertySetInfo.name = registeredCustomPropertySet
                .getCustomPropertySet()
                .getName());
        map.put("domainDomainName", (customPropertySetInfo, registeredCustomPropertySet, uriInfo) -> customPropertySetInfo.domainClass = registeredCustomPropertySet
                .getCustomPropertySet()
                .getDomainClass()
                .getName());
        map.put("isRequired", (customPropertySetInfo, registeredCustomPropertySet, uriInfo) -> customPropertySetInfo.isRequired = registeredCustomPropertySet
                .getCustomPropertySet()
                .isRequired());
        map.put("isVersioned", (customPropertySetInfo, registeredCustomPropertySet, uriInfo) -> customPropertySetInfo.isVersioned = registeredCustomPropertySet
                .getCustomPropertySet()
                .isVersioned());
        map.put("properties", (customPropertySetInfo, registeredCustomPropertySet, uriInfo) -> {
            CustomPropertySet<UsagePoint, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();

            customPropertySetInfo.properties =
                    customPropertySet.getPropertySpecs().stream()
                            .map(cps -> this.getPropertyInfo(cps,  null))
                            .collect(toList());
        });
        return map;
    }

    public CustomPropertySetAttributeInfo getPropertyInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider) {
        CustomPropertySetAttributeInfo info = new CustomPropertySetAttributeInfo();
        if (propertySpec != null) {
            PropertyInfo propertyInfo = propertyValueInfoService.getPropertyInfo(propertySpec, propertyValueProvider);
            info.key = propertyInfo.key;
            info.name = propertyInfo.name;
            info.required = propertyInfo.required;
            info.description = propertySpec.getDescription();
            PropertyTypeInfo propertyTypeInfo = propertyInfo.propertyTypeInfo;
            CustomPropertySetAttributeTypeInfo customPropertySetAttributeTypeInfo = new CustomPropertySetAttributeTypeInfo();
            customPropertySetAttributeTypeInfo.type = propertySpec.getValueFactory().getValueType().getName();
            customPropertySetAttributeTypeInfo.typeSimpleName = thesaurus.getString(customPropertySetAttributeTypeInfo.type, customPropertySetAttributeTypeInfo.type);
            if (propertyTypeInfo != null) {
                customPropertySetAttributeTypeInfo.simplePropertyType = propertyTypeInfo.simplePropertyType;
                customPropertySetAttributeTypeInfo.predefinedPropertyValuesInfo = propertyTypeInfo.predefinedPropertyValuesInfo;
            }
            info.propertyTypeInfo = customPropertySetAttributeTypeInfo;
            info.propertyValueInfo = propertyInfo.getPropertyValueInfo();
        }
        return info;
    }


}
