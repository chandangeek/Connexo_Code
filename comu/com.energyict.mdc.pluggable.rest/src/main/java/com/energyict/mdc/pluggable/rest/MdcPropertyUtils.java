/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.properties.BoundedBigDecimalPropertySpec;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.rest.NumberValidationRules;
import com.elster.jupiter.properties.rest.PredefinedPropertyValuesInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValidationRule;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.properties.rest.PropertyValuesResourceInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.pluggable.rest.impl.CalendarResource;
import com.energyict.mdc.pluggable.rest.impl.LoadProfileTypeResource;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.DeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;

import aQute.bnd.annotation.ProviderType;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.PrivilegePresence.WITHOUT_PRIVILEGES;
import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.ValueVisibility.HIDE_VALUES;
import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.ValueVisibility.SHOW_VALUES;

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