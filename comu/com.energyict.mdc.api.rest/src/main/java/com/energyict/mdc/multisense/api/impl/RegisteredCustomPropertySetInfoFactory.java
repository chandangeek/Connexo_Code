/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.upl.TypedProperties;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegisteredCustomPropertySetInfoFactory
        extends SelectableFieldFactory<RegisteredCustomPropertySetInfo, RegisteredCustomPropertySetTypeInfo> {

    private static final String TITLE = "Custom property set";

    private final CustomPropertySetService customPropertySetService;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public RegisteredCustomPropertySetInfoFactory(CustomPropertySetService customPropertySetService,
                                                  MdcPropertyUtils mdcPropertyUtils) {
        this.customPropertySetService = customPropertySetService;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public RegisteredCustomPropertySetInfo asLink(RegisteredCustomPropertySetTypeInfo registeredCustomPropertySetTypeInfo,
                                                  Relation relation, UriInfo uriInfo) {
        RegisteredCustomPropertySetInfo info = new RegisteredCustomPropertySetInfo();
        copySelectedFields(info, registeredCustomPropertySetTypeInfo, uriInfo, Arrays.asList("id", "version"));
        info.link = link(registeredCustomPropertySetTypeInfo, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<RegisteredCustomPropertySetTypeInfo> registeredCustomPropertySetTypeInfos,
                                 Relation relation, UriInfo uriInfo) {
        return registeredCustomPropertySetTypeInfos.stream()
                .map(i -> asLink(i, relation, uriInfo))
                .collect(Collectors.toList());
    }

    private Link link(RegisteredCustomPropertySetTypeInfo registeredCustomPropertySetTypeInfo, Relation relation,
                      UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(registeredCustomPropertySetTypeInfo.getRegisteredCustomPropertySet(),
                uriInfo))
                .rel(relation.rel())
                .title(TITLE)
                .build(registeredCustomPropertySetTypeInfo.getParentId(),
                        registeredCustomPropertySetTypeInfo.getRegisteredCustomPropertySet().getId());
    }

    private UriBuilder getUriBuilder(RegisteredCustomPropertySet registeredCustomPropertySet, UriInfo uriInfo) {
        return RegisteredCustomPropertySetResourseType
                .fromClass(registeredCustomPropertySet.getCustomPropertySet().getDomainClass())
                .map(type -> uriInfo.getBaseUriBuilder()
                        .path(type.getResourceClass())
                        .path(type.getResourceClass(), type.getMethodName()))
                .orElse(uriInfo.getBaseUriBuilder());
    }

    public RegisteredCustomPropertySetInfo from(RegisteredCustomPropertySetTypeInfo registeredCustomPropertySetTypeInfo,
                                                UriInfo uriInfo, List<String> fields) {
        RegisteredCustomPropertySetInfo customPropertySetInfo = new RegisteredCustomPropertySetInfo();
        copySelectedFields(customPropertySetInfo, registeredCustomPropertySetTypeInfo, uriInfo, fields);
        return customPropertySetInfo;
    }

    @Override
    protected Map<String, PropertyCopier<RegisteredCustomPropertySetInfo, RegisteredCustomPropertySetTypeInfo>> buildFieldMap() {
        Map<String, PropertyCopier<RegisteredCustomPropertySetInfo, RegisteredCustomPropertySetTypeInfo>> map =
                new HashMap<>();
        map.put("id", (info, rcpsInfo, uriInfo) -> info.id = rcpsInfo.getRegisteredCustomPropertySet().getId());
        map.put("link", (info, rcpsInfo, uriInfo) -> info.link = link(rcpsInfo, Relation.REF_SELF, uriInfo));
        map.put("version", (info, rcpsInfo, uriInfo) -> info.version = rcpsInfo.getVersion());
        map.put("name", (info, rcpsInfo, uriInfo) -> info.name =
                rcpsInfo.getRegisteredCustomPropertySet().getCustomPropertySet().getName());
        map.put("customAttributes", (info, rcpsInfo, uriInfo) -> info.customAttributes =
                getCustomPropertySetValues(rcpsInfo.getParent(), rcpsInfo.getRegisteredCustomPropertySet()));
        return map;
    }

    @SuppressWarnings("unchecked")
    private List<PropertyInfo> getCustomPropertySetValues(HasId businessObject, RegisteredCustomPropertySet rcps) {
        return mdcPropertyUtils.convertPropertySpecsToPropertyInfos(rcps.getCustomPropertySet().getPropertySpecs(),
                getCustomProperties(customPropertySetService.getUniqueValuesFor(rcps.getCustomPropertySet(),
                        businessObject)));
    }

    private TypedProperties getCustomProperties(CustomPropertySetValues customPropertySetValues) {
        TypedProperties typedProperties = TypedProperties.empty();
        customPropertySetValues.propertyNames().forEach(propertyName ->
                typedProperties.setProperty(propertyName, customPropertySetValues.getProperty(propertyName)));
        return typedProperties;
    }
}