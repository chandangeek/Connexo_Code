/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest;

import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@ProviderType
public interface PropertyValueInfoService {

    void addPropertyValueInfoConverter(PropertyValueConverter converter);

    void removePropertyValueInfoConverter(PropertyValueConverter converter);

    PropertyValueConverter getConverter(PropertySpec propertySpec);

    PropertyInfo getPropertyInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider);

    PropertyInfo getPropertyInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider, Function<String, Object> inheritedPropertyValueProvider);

    List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs);

    List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs, Map<String, Object> propertyValues);

    List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs, Map<String, Object> propertyValues, Map<String, Object> inheritedPropertyValues);

    Object findPropertyValue(PropertySpec propertySpec, List<PropertyInfo> propertyInfos);

    PropertyValueInfoService getEmptyPropertyValueInfoService();

}
