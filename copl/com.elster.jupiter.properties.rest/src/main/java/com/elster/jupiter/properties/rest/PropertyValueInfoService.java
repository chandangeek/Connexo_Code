/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest;

import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by mbarinov on 17.08.2016.
 */
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

    List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs);

    List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs, Map<String, Object> propertyValues);

    Object findPropertyValue(PropertySpec propertySpec, List<PropertyInfo> propertyInfos);

    PropertyValueInfoService getEmptyPropertyValueInfoService();

}
