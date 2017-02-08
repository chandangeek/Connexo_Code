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

    void removePropertyValueInfoConverter(PropertyValueConverter converter);

    PropertyValueConverter getConverter(PropertySpec propertySpec);

    PropertyInfo getPropertyInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider);

    List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs);

    List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs, Map<String, Object> propertyValues);

    Object findPropertyValue(PropertySpec propertySpec, List<PropertyInfo> propertyInfos);

    PropertyValueInfoService getEmptyPropertyValueInfoService();

}
