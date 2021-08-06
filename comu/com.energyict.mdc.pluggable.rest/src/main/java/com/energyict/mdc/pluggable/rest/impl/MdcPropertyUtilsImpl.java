/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.pki.SecurityAccessorType;
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
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;
import com.energyict.mdc.pluggable.rest.PropertyValuesResourceProvider;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;
import com.energyict.mdc.upl.TypedProperties;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

@Component(name = "com.elster.jupiter.properties.rest.mdcpropertyutils", immediate = true, service = MdcPropertyUtils.class)
public class MdcPropertyUtilsImpl implements MdcPropertyUtils {
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile MeteringGroupsService meteringGroupsService;

    //osgi
    public MdcPropertyUtilsImpl() {

    }

    @Activate
    public void activate(BundleContext context) {

    }

    @Inject
    public MdcPropertyUtilsImpl(PropertyValueInfoService propertyValueInfoService, MeteringGroupsService meteringGroupsService) {
        setPropertyValueInfoService(propertyValueInfoService);
        setMeteringGroupsService(meteringGroupsService);
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    public void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, Collection<PropertySpec> propertySpecs, TypedProperties properties, List<PropertyInfo> propertyInfoList) {
        convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, properties, propertyInfoList, SHOW_VALUES, WITHOUT_PRIVILEGES);
    }

    public void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, Collection<PropertySpec> propertySpecs, TypedProperties properties, List<PropertyInfo> propertyInfoList,
                                                    MdcPropertyUtils.ValueVisibility showValue, MdcPropertyUtils.PrivilegePresence privilegePresence) {
        if (!propertySpecs.isEmpty()) {
            for (PropertySpec propertySpec : propertySpecs) {
                PropertyInfo propertyInfo = propertyValueInfoService.getPropertyInfo(propertySpec, properties.getLocalValue(propertySpec.getName()) != null ? properties::getLocalValue : null);
                modifyPropertyValueInfo(propertyInfo, propertySpec, properties, showValue, privilegePresence);
                modifyPropertyTypeInfo(propertyInfo, propertySpec, uriInfo, (PropertyDefaultValuesProvider) null);
                propertyInfoList.add(propertyInfo);
            }
        }
    }

    private void modifyPropertyValueInfo(PropertyInfo propertyInfo, PropertySpec propertySpec, TypedProperties properties, MdcPropertyUtils.ValueVisibility showValue, MdcPropertyUtils.PrivilegePresence privilegePresence) {
        PropertyValueInfo propertyValueInfo = propertyInfo.getPropertyValueInfo();
        Boolean propertyHasValue = true;
        Object inheritedValue = properties.getInheritedValue(propertySpec.getName());
        Object inheritedProperty = inheritedValue != null ? propertyValueInfoService.getConverter(propertySpec).convertValueToInfo(propertySpec, inheritedValue) : null;
        if (isNull(propertyValueInfo.getValue()) && (isNull(inheritedProperty)) && (isNull(propertyValueInfo.defaultValue))) {
            propertyHasValue = false;
        }
        if (HIDE_VALUES.equals(showValue)) {
            propertyValueInfo.value = null;
            inheritedProperty = null;
            propertyValueInfo.defaultValue = null;
        }
        if (WITHOUT_PRIVILEGES.equals(privilegePresence)) {
            propertyHasValue = null;
        }
        propertyValueInfo.inheritedValue = inheritedProperty;
        propertyValueInfo.propertyHasValue = propertyHasValue;
    }

    private void modifyPropertyTypeInfo(PropertyInfo propertyInfo, PropertySpec propertySpec, final UriInfo uriInfo, PropertyDefaultValuesProvider valuesProvider) {
        PropertyTypeInfo propertyTypeInfo = propertyInfo.propertyTypeInfo;
        if (propertyTypeInfo.propertyValidationRule == null) {
            propertyTypeInfo.propertyValidationRule = getPropertyValidationRule(propertySpec);
        }
        propertyTypeInfo.predefinedPropertyValuesInfo = getPredefinedPropertyValueInfo(propertySpec, valuesProvider);
        propertyTypeInfo.referenceUri = getReferenceUri(uriInfo, propertySpec, propertyTypeInfo.simplePropertyType);
    }

    private void modifyPropertyTypeInfo(PropertyInfo propertyInfo, PropertySpec propertySpec, PropertyValuesResourceProvider valuesProvider) {
        PropertyTypeInfo propertyTypeInfo = propertyInfo.propertyTypeInfo;
        if (propertyTypeInfo.propertyValidationRule == null) {
            propertyTypeInfo.propertyValidationRule = getPropertyValidationRule(propertySpec);
        }
        propertyTypeInfo.propertyValuesResource = new PropertyValuesResourceInfo();
        if (valuesProvider.getPropertiesValuesResource(propertySpec).isPresent()) {
            propertyTypeInfo.propertyValuesResource.possibleValuesURI = valuesProvider.getPropertiesValuesResource(propertySpec)
                    .get()
                    .toString();
        }
        propertyTypeInfo.referenceUri = getReferenceUri(null, propertySpec, propertyTypeInfo.simplePropertyType);
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties) {
        return this.convertPropertySpecsToPropertyInfos(propertySpecs, properties, SHOW_VALUES, WITHOUT_PRIVILEGES);
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties, MdcPropertyUtils.ValueVisibility showValue, MdcPropertyUtils.PrivilegePresence privilegePresence) {
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            PropertyInfo propertyInfo = propertyValueInfoService.getPropertyInfo(propertySpec, properties.getLocalValue(propertySpec.getName()) != null ? properties::getLocalValue : null);
            modifyPropertyValueInfo(propertyInfo, propertySpec, properties, showValue, privilegePresence);
            modifyPropertyTypeInfo(propertyInfo, propertySpec, null, (PropertyDefaultValuesProvider) null);
            propertyInfoList.add(propertyInfo);
        }
        return propertyInfoList;
    }

    public PropertyInfo convertPropertySpecToPropertyInfo(PropertySpec propertySpec, Object propertyValue) {
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(propertySpec.getName(), propertyValue);
        List<PropertyInfo> propertyInfoList = convertPropertySpecsToPropertyInfos(Collections.singletonList(propertySpec), properties);
        return !propertyInfoList.isEmpty() ? propertyInfoList.get(0) : null; // Safety measure, but should never be the case
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties, Device device) {
        PropertyDefaultValuesProvider provider = (propertySpec, propertyType) -> {
            List<?> possibleValues = null;
            if (propertyType instanceof SimplePropertyType) {
                SimplePropertyType simplePropertyType = (SimplePropertyType) propertyType;
                if (SimplePropertyType.LOADPROFILE.equals(simplePropertyType)) {
                    possibleValues = device.getLoadProfiles();
                } else if (SimplePropertyType.LOGBOOK.equals(simplePropertyType)) {
                    possibleValues = device.getLogBooks();
                } else if (SimplePropertyType.REGISTER.equals(simplePropertyType)) {
                    possibleValues = device.getRegisters();
                } else if (SimplePropertyType.REFERENCE.equals(simplePropertyType)) {
                    PropertySpecPossibleValues possibleValuesFromSpec = propertySpec.getPossibleValues();
                    if (possibleValuesFromSpec != null) {
                        possibleValues = possibleValuesFromSpec.getAllValues();
                    }
                }
            } else if (propertyType instanceof com.elster.jupiter.properties.rest.SimplePropertyType) {
                com.elster.jupiter.properties.rest.SimplePropertyType simplePropertyType = (com.elster.jupiter.properties.rest.SimplePropertyType) propertyType;
                if ((propertySpec.isReference() && (SecurityAccessorType.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())))) {
                    possibleValues = device.getDeviceType().getSecurityAccessorTypes();
                } else if (com.elster.jupiter.properties.rest.SimplePropertyType.IDWITHNAME.equals(simplePropertyType)) {
                    possibleValues = device.getDeviceType().getDeviceMessageFiles();
                }
            }
            return possibleValues;
        };
        return convertPropertySpecsToPropertyInfos(propertySpecs, properties, provider);
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties, PropertyDefaultValuesProvider valuesProvider) {
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            PropertyInfo propertyInfo = propertyValueInfoService.getPropertyInfo(propertySpec, properties.getLocalValue(propertySpec.getName()) != null ? properties::getLocalValue : null);
            modifyPropertyValueInfo(propertyInfo, propertySpec, properties, SHOW_VALUES, WITHOUT_PRIVILEGES);
            modifyPropertyTypeInfo(propertyInfo, propertySpec, null, valuesProvider);
            propertyInfoList.add(propertyInfo);
        }
        return propertyInfoList;
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs,
                                                                  TypedProperties properties,
                                                                  PropertyValuesResourceProvider valuesResourceProvider,
                                                                  PropertyDefaultValuesProvider valuesProvider) {
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            PropertyInfo propertyInfo = propertyValueInfoService.getPropertyInfo(propertySpec, properties.getLocalValue(propertySpec.getName()) != null ? properties::getLocalValue : null);
            modifyPropertyValueInfo(propertyInfo, propertySpec, properties, SHOW_VALUES, WITHOUT_PRIVILEGES);
            if (valuesResourceProvider.getPropertiesValuesResource(propertySpec).isPresent()) {
                modifyPropertyTypeInfo(propertyInfo, propertySpec, valuesResourceProvider);
            } else {
                modifyPropertyTypeInfo(propertyInfo, propertySpec, null, valuesProvider);
            }
            propertyInfoList.add(propertyInfo);
        }
        return propertyInfoList;
    }

    private boolean isNull(Object propertyValue) {
        return propertyValue == null || "".equals(propertyValue);
    }

    private URI getReferenceUri(final UriInfo uriInfo, PropertySpec propertySpec, PropertyType propertyType) {
        if (propertyType instanceof SimplePropertyType && ((SimplePropertyType) propertyType).isReference()) {
            return getReferenceUriFor(uriInfo, propertySpec.getValueFactory().getValueType());
        } else {
            return null;
        }
    }

    private PropertyValidationRule getPropertyValidationRule(PropertySpec propertySpec) {
        if (BoundedBigDecimalPropertySpec.class.isAssignableFrom(propertySpec.getClass())) {
            BoundedBigDecimalPropertySpec boundedBigDecimalPropertySpec = (BoundedBigDecimalPropertySpec) propertySpec;
            return createBoundedBigDecimalValidationRules(boundedBigDecimalPropertySpec);
        } else {
            return null;
        }
    }

    private PropertyValidationRule createBoundedBigDecimalValidationRules(BoundedBigDecimalPropertySpec boundedBigDecimalPropertySpec) {
        NumberValidationRules<BigDecimal> bigDecimalNumberValidationRules = new NumberValidationRules<>();
        bigDecimalNumberValidationRules.setAllowDecimals(true);
        bigDecimalNumberValidationRules.setMaximumValue(boundedBigDecimalPropertySpec.getUpperLimit());
        bigDecimalNumberValidationRules.setMinimumValue(boundedBigDecimalPropertySpec.getLowerLimit());
        return bigDecimalNumberValidationRules;
    }

    private PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec propertySpec, PropertyDefaultValuesProvider valuesProvider) {
        List<?> possibleValues = null;
        boolean isExhaustive = true;
        boolean editable = false;
        PropertyValueConverter converter = propertyValueInfoService.getConverter(propertySpec);
        if (valuesProvider != null) {
            possibleValues = valuesProvider.getPropertyPossibleValues(propertySpec, converter.getPropertyType(propertySpec));
        } else if (propertySpec.getValueFactory() != null && propertySpec.getValueFactory().getValueType() != null) {
            if (EndDeviceGroup.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
                possibleValues = meteringGroupsService.findEndDeviceGroups();
            }
        }

        if (propertySpec.getPossibleValues() != null && possibleValues == null) {
            possibleValues = propertySpec.getPossibleValues().getAllValues();
            isExhaustive = propertySpec.getPossibleValues().isExhaustive();
            editable = propertySpec.getPossibleValues().isEditable();
        }

        if ((possibleValues == null || possibleValues.isEmpty()) && !propertySpec.isReference()) {
            return null;
        } else if (possibleValues == null) {
            possibleValues = new ArrayList<>();
        }

        Object[] possibleObjects = new Object[possibleValues.size()];
        for (int i = 0; i < possibleValues.size(); i++) {
            possibleObjects[i] = converter.convertValueToInfo(propertySpec, possibleValues.get(i));
        }
        PropertySelectionMode selectionMode = PropertySelectionMode.COMBOBOX;

        return new PredefinedPropertyValuesInfo<>(
                possibleObjects,
                selectionMode,
                isExhaustive,
                editable);
    }

    @Override
    public Object findPropertyValue(PropertySpec propertySpec, Collection<PropertyInfo> propertyInfos) {
        return propertyValueInfoService.findPropertyValue(propertySpec, propertyInfos);
    }

    @Override
    public Object findPropertyValue(PropertySpec propertySpec, PropertyInfo[] propertyInfos) {
        return propertyValueInfoService.findPropertyValue(propertySpec, Arrays.asList(propertyInfos));
    }

    public enum ValueVisibility {
        SHOW_VALUES, HIDE_VALUES
    }

    public enum PrivilegePresence {
        WITH_PRIVILEGES, WITHOUT_PRIVILEGES
    }

    /**
     * Creates a proper URI to fetch the <i>full</i> list of the BusinessObjects of the given class
     *
     * @param uriInfo           the URI info which was used for the REST call
     * @param propertyClassType the classTypeName of the object
     * @return the uri to fetch the list of objects
     */
    private URI getReferenceUriFor(UriInfo uriInfo, Class propertyClassType) {
        URI uri = null;
        if (TimeZoneInUse.class.isAssignableFrom(propertyClassType)) {
            // The TimeZoneInUse values are provided as possibleValues
        } else if (Calendar.class.isAssignableFrom(propertyClassType)) {
            uri = uriInfo.getBaseUriBuilder().path(CalendarResource.class).path("/").build();
        } else if (LoadProfileType.class.isAssignableFrom(propertyClassType)) {
            uri = uriInfo.getBaseUriBuilder().path(LoadProfileTypeResource.class).path("/").build();
        } else if (com.energyict.mdc.upl.properties.DeviceGroup.class.isAssignableFrom(propertyClassType)) {

        }
        return uri;
    }
}
