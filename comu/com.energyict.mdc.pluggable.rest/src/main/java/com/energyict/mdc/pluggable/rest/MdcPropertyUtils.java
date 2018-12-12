/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.upl.TypedProperties;

import aQute.bnd.annotation.ProviderType;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;

/**
 * Serves as a utility class to create proper PropertyInfo objects for a set of Properties
 * and their corresponding PropertySpecs
 */
@ProviderType
public interface MdcPropertyUtils {


    void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, Collection<PropertySpec> propertySpecs, TypedProperties properties, List<PropertyInfo> propertyInfoList);

    void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, Collection<PropertySpec> propertySpecs, TypedProperties properties, List<PropertyInfo> propertyInfoList,
                                             ValueVisibility showValue, PrivilegePresence privilegePresence);


    List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties);

    List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties, ValueVisibility showValue, PrivilegePresence privilegePresence);

    PropertyInfo convertPropertySpecToPropertyInfo(PropertySpec propertySpec, Object propertyValue);

    List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties, Device device);

    List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties, PropertyDefaultValuesProvider valuesProvider);

    List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties, PropertyValuesResourceProvider valuesResourceProvider, PropertyDefaultValuesProvider valuesProvider);

    Object findPropertyValue(PropertySpec propertySpec, Collection<PropertyInfo> propertyInfos);

    Object findPropertyValue(PropertySpec propertySpec, PropertyInfo[] propertyInfos);

    enum ValueVisibility {
        SHOW_VALUES, HIDE_VALUES
    }

    enum PrivilegePresence {
        WITH_PRIVILEGES, WITHOUT_PRIVILEGES
    }
}