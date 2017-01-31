/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.rest;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.RangeInstantBuilder;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomPropertySetInfoFactory {

    private final Thesaurus thesaurus;
    private final PropertyValueInfoService propertyValueInfoService;
    private final Clock clock;

    @Inject
    public CustomPropertySetInfoFactory(Thesaurus thesaurus, Clock clock, PropertyValueInfoService propertyValueInfoService) {
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.propertyValueInfoService = propertyValueInfoService;
    }

    private CustomPropertySetInfo getGeneralInfo(RegisteredCustomPropertySet rcps) {
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        if (rcps != null) {
            CustomPropertySet<?, ?> cps = rcps.getCustomPropertySet();
            info.id = rcps.getId();
            info.viewPrivileges = rcps.getViewPrivileges();
            info.editPrivileges = rcps.getEditPrivileges();
            info.isEditable = rcps.isEditableByCurrentUser();

            info.customPropertySetId = cps.getId();
            info.name = cps.getName();
            info.domainNameUntranslated = cps.getDomainClass().getName();
            info.domainName = cps.getDomainClassDisplayName();

            info.isVersioned = cps.isVersioned();
            info.isRequired = cps.isRequired();
            info.defaultViewPrivileges = cps.defaultViewPrivileges();
            info.defaultEditPrivileges = cps.defaultEditPrivileges();
        }
        return info;
    }

    public CustomPropertySetInfo getGeneralAndPropertiesInfo(RegisteredCustomPropertySet rcps) {
        CustomPropertySetInfo info = getGeneralInfo(rcps);
        if (rcps != null) {
            CustomPropertySet<?, ?> cps = rcps.getCustomPropertySet();
            info.properties = cps.getPropertySpecs()
                    .stream()
                    .map(this::getPropertyInfo)
                    .collect(Collectors.toList());
        }
        return info;
    }


    public CustomPropertySetInfo getFullInfo(RegisteredCustomPropertySet rcps, CustomPropertySetValues customPropertySetValue) {
        CustomPropertySetInfo info = getGeneralInfo(rcps);
        if (rcps != null) {
            if (info.isVersioned) {
                addTimeSliceCustomPropertySetInfo(info, customPropertySetValue);
            }
            CustomPropertySet<?, ?> cps = rcps.getCustomPropertySet();
            info.properties = cps.getPropertySpecs().stream()
                    .map(propertySpec -> getPropertyInfo(propertySpec, key -> customPropertySetValue != null ? customPropertySetValue
                            .getProperty(key) : null))
                    .collect(Collectors.toList());
        }
        return info;
    }

    private void addTimeSliceCustomPropertySetInfo(CustomPropertySetInfo info, CustomPropertySetValues customPropertySetValue) {
        if (customPropertySetValue != null) {
            Range<Instant> effective = customPropertySetValue.getEffectiveRange();
            info.versionId = effective.hasLowerBound() ? effective.lowerEndpoint().toEpochMilli() : 0;
            info.startTime = effective.hasLowerBound() ? effective.lowerEndpoint().toEpochMilli() : null;
            info.endTime = effective.hasUpperBound() ? effective.upperEndpoint().toEpochMilli() : null;
            info.isActive = !customPropertySetValue.isEmpty() && effective.contains(this.clock.instant());
        } else {
            info.isActive = false;
        }
    }

    private CustomPropertySetAttributeInfo getPropertyInfo(PropertySpec propertySpec) {
        return getPropertyInfo(propertySpec, null);
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

    public CustomPropertySetValues getCustomPropertySetValues(CustomPropertySetInfo<?> info, List<PropertySpec> propertySpecs) {
        CustomPropertySetValues values;
        if (info.isVersioned) {
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

    public ValuesRangeConflictInfo getValuesRangeConflictInfo(ValuesRangeConflict valueRangeConflict) {
        ValuesRangeConflictInfo info = new ValuesRangeConflictInfo();
        if (valueRangeConflict != null) {
            info.conflictType = valueRangeConflict.getType().name();
            info.message = valueRangeConflict.getMessage();
            info.conflictAtStart = valueRangeConflict.getType()
                    .equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_START);
            info.conflictAtEnd = valueRangeConflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END);
            info.editable = valueRangeConflict.getType().equals(ValuesRangeConflictType.RANGE_INSERTED);
        }
        return info;
    }
}