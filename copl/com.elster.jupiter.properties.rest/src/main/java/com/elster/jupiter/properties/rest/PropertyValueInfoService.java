/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest;

import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@ProviderType
public interface PropertyValueInfoService {

    void addPropertyValueInfoConverter(PropertyValueConverter converter);

    /**
     * Add a 'dedicated' {@link PropertyValueConverter}. This will be the converter that will be used
     * for converting values of {@link PropertySpec}s with the given name. Converters added this way will first be 'visited' to
     * check they can convert a given PropertySpec.
     * @param converter to use
     * @param propertyName the name of the propertyspecs to convert values from/to
     */
    void addPropertyValueInfoConverter(PropertyValueConverter converter, String propertyName);

    void removePropertyValueInfoConverter(PropertyValueConverter converter);

    PropertyValueConverter getConverter(PropertySpec propertySpec);

    PropertyInfo getPropertyInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider);

    PropertyInfo getPropertyInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider, Function<String, Object> inheritedPropertyValueProvider);

    List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs);

    List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs, Map<String, Object> propertyValues);

    List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs, Map<String, Object> propertyValues, Map<String, Object> inheritedPropertyValues);

    /**
     * @deprecated Please use {@link #findPropertyValue(PropertySpec, Collection)}
     */
    @Deprecated
    Object findPropertyValue(PropertySpec propertySpec, List<PropertyInfo> propertyInfos);

    Object findPropertyValue(PropertySpec propertySpec, Collection<PropertyInfo> propertyInfos);

    Map<String, Object> findPropertyValues(Collection<PropertySpec> propertySpecs, Collection<PropertyInfo> propertyInfos);

    PropertyValueInfoService getEmptyPropertyValueInfoService();

}
