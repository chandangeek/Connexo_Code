/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
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

public class UsagePointCustomPropertySetInfoFactory extends SelectableFieldFactory<UsagePointCustomPropertySetInfo, UsagePointPropertySet> {

    private final Clock clock;
    private final PropertyValueInfoService propertyValueInfoService;
    private final Thesaurus thesaurus;

    @Inject
    public UsagePointCustomPropertySetInfoFactory(Clock clock, PropertyValueInfoService propertyValueInfoService, Thesaurus thesaurus) {
        this.clock = clock;
        this.propertyValueInfoService = propertyValueInfoService;
        this.thesaurus = thesaurus;
    }

    public LinkInfo asLink(UsagePointPropertySet usagePointCustomPropertySet, Relation relation, UriInfo uriInfo) {
        UsagePointCustomPropertySetInfo info = new UsagePointCustomPropertySetInfo();
        copySelectedFields(info, usagePointCustomPropertySet, uriInfo, Arrays.asList("id", "version"));
        info.link = link(usagePointCustomPropertySet, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<UsagePointPropertySet> usagePointCustomPropertySets, Relation relation, UriInfo uriInfo) {
        return usagePointCustomPropertySets.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(UsagePointPropertySet usagePointCustomPropertySet, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Custom property set")
                .build(usagePointCustomPropertySet.getUsagePoint().getMRID(), usagePointCustomPropertySet.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(UsagePointCustomPropertySetResource.class)
                .path(UsagePointCustomPropertySetResource.class, "getUsagePointCustomPropertySet");
    }

    public UsagePointCustomPropertySetInfo from(UsagePointPropertySet usagePointCustomPropertySet, UriInfo uriInfo, Collection<String> fields) {
        UsagePointCustomPropertySetInfo info = new UsagePointCustomPropertySetInfo();
        copySelectedFields(info, usagePointCustomPropertySet, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<UsagePointCustomPropertySetInfo, UsagePointPropertySet>> buildFieldMap() {
        Map<String, PropertyCopier<UsagePointCustomPropertySetInfo, UsagePointPropertySet>> map = new HashMap<>();
        map.put("id", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> usagePointCustomPropertySetInfo.id = usagePointCustomPropertySet
                .getId());
        map.put("link", ((usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) ->
                usagePointCustomPropertySetInfo.link = link(usagePointCustomPropertySet, Relation.REF_SELF, uriInfo)));
        map.put("version", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) ->
                usagePointCustomPropertySetInfo.version = usagePointCustomPropertySet.getUsagePoint().getVersion());
        map.put("name", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> usagePointCustomPropertySetInfo.name = usagePointCustomPropertySet
                .getCustomPropertySet()
                .getName());
        map.put("domainDomainName", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> usagePointCustomPropertySetInfo.domainClass = usagePointCustomPropertySet
                .getCustomPropertySet()
                .getDomainClass()
                .getName());
        map.put("isRequired", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> usagePointCustomPropertySetInfo.isRequired = usagePointCustomPropertySet
                .getCustomPropertySet()
                .isRequired());
        map.put("isVersioned", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> usagePointCustomPropertySetInfo.isVersioned = usagePointCustomPropertySet
                .getCustomPropertySet()
                .isVersioned());
        map.put("isActive", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> {
            if (usagePointCustomPropertySet.getCustomPropertySet()
                    .isVersioned() && usagePointCustomPropertySet.getValues() != null) {
                usagePointCustomPropertySetInfo.isActive = !usagePointCustomPropertySet.getValues().isEmpty() &&
                        usagePointCustomPropertySet.getValues().isEffectiveAt(clock.instant());
            }
        });
        map.put("startTime", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> {
            if (usagePointCustomPropertySet.getCustomPropertySet()
                    .isVersioned() && usagePointCustomPropertySet.getValues() != null) {
                usagePointCustomPropertySetInfo.startTime = usagePointCustomPropertySet.getValues()
                        .getEffectiveRange()
                        .hasLowerBound() ?
                        usagePointCustomPropertySet.getValues()
                                .getEffectiveRange()
                                .lowerEndpoint()
                                .toEpochMilli() : null;
            }
        });
        map.put("endTime", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> {
            if (usagePointCustomPropertySet.getCustomPropertySet()
                    .isVersioned() && usagePointCustomPropertySet.getValues() != null) {
                usagePointCustomPropertySetInfo.endTime = usagePointCustomPropertySet.getValues()
                        .getEffectiveRange()
                        .hasUpperBound() ?
                        usagePointCustomPropertySet.getValues()
                                .getEffectiveRange()
                                .upperEndpoint()
                                .toEpochMilli() : null;
            }
        });
        map.put("versionId", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> {
            if (usagePointCustomPropertySet.getCustomPropertySet()
                    .isVersioned() && usagePointCustomPropertySet.getValues() != null) {
                usagePointCustomPropertySetInfo.versionId = usagePointCustomPropertySet.getValues()
                        .getEffectiveRange()
                        .hasLowerBound() ?
                        usagePointCustomPropertySet.getValues().getEffectiveRange().lowerEndpoint().toEpochMilli() : 0L;
            }
        });
        map.put("properties", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> {
            CustomPropertySet<UsagePoint, ?> customPropertySet = usagePointCustomPropertySet.getCustomPropertySet();

            usagePointCustomPropertySetInfo.properties =
                    customPropertySet.getPropertySpecs().stream()
                            .map(cps -> this.getPropertyInfo(cps, name -> usagePointCustomPropertySet
                                    .getValues() != null ? usagePointCustomPropertySet.getValues()
                                    .getProperty(name) : null))
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

    public CustomPropertySetValues getValues(UsagePointCustomPropertySetInfo propertySetInfo, UsagePointPropertySet propertySet) {
        UsagePointCustomPropertySetInfo customPropertySetInfo = new UsagePointCustomPropertySetInfo();
        customPropertySetInfo.isVersioned = propertySetInfo.isVersioned;
        customPropertySetInfo.startTime = propertySetInfo.startTime;
        customPropertySetInfo.endTime = propertySetInfo.endTime;
        customPropertySetInfo.properties = propertySetInfo.properties;

        return this.getCustomPropertySetValues(customPropertySetInfo, propertySet.getCustomPropertySet().getPropertySpecs());
    }

    public CustomPropertySetValues getCustomPropertySetValues(UsagePointCustomPropertySetInfo info, List<PropertySpec> propertySpecs) {
        CustomPropertySetValues values;
        if (info.isVersioned != null && info.isVersioned) {
            values = CustomPropertySetValues.emptyDuring(Interval.of(RangeInstantBuilder.closedOpenRange(info.startTime, info.endTime)));
        } else {
            values = CustomPropertySetValues.empty();
        }
        if (info.properties != null && propertySpecs != null) {
            Map<String, PropertySpec> propertySpecMap = propertySpecs
                    .stream()
                    .collect(Collectors.toMap(PropertySpec::getName, Function.identity()));
            for (CustomPropertySetAttributeInfo property : info.properties) {
                PropertySpec propertySpec = propertySpecMap.get(property.key);
                if (propertySpec != null && property.propertyValueInfo != null && property.propertyValueInfo.value != null && this.propertyValueInfoService.getConverter(propertySpec) != null) {
                    values.setProperty(property.key, this.propertyValueInfoService.getConverter(propertySpec)
                            .convertInfoToValue(propertySpec, property.propertyValueInfo.value));
                }
            }
        }
        return values;
    }
}
