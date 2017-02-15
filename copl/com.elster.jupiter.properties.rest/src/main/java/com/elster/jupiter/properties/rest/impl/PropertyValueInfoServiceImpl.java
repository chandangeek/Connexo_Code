/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.rest.PredefinedPropertyValuesInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.properties.rest.SimplePropertyType;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by mbarinov on 17.08.2016.
 */
@Component(name = "com.elster.jupiter.properties.rest.propertyvalueinfoservice", immediate = true, service = PropertyValueInfoService.class)
public class PropertyValueInfoServiceImpl implements PropertyValueInfoService {

    private static PropertyValueConverter DEFAULT_CONVERTER = new DefaultPropertyValueConverter();
    private final List<PropertyValueConverter> converters = new CopyOnWriteArrayList<>();

    public PropertyValueInfoServiceImpl() {

    }

    @Activate
    public void activate() {
        this.addPropertyValueInfoConverter(new BooleanPropertyValueConverter());
        this.addPropertyValueInfoConverter(new NumberPropertyValueConverter());
        this.addPropertyValueInfoConverter(new InstantPropertyValueConverter());
        this.addPropertyValueInfoConverter(new LongPropertyValueConverter());
        this.addPropertyValueInfoConverter(new NullableBooleanPropertyValueConverter());
        this.addPropertyValueInfoConverter(new IdWithNamePropertyValueConverter());
        this.addPropertyValueInfoConverter(new RelativePeriodPropertyValueConverter());
        this.addPropertyValueInfoConverter(new ListPropertyValueConverter());
        this.addPropertyValueInfoConverter(new QuantityPropertyValueConverter());
    }

    @Override
    public void addPropertyValueInfoConverter(PropertyValueConverter converter) {
        this.converters.add(converter);
    }

    @Override
    public void removePropertyValueInfoConverter(PropertyValueConverter converter) {
        this.converters.remove(converter);
    }

    @Override
    public PropertyValueConverter getConverter(PropertySpec propertySpec) {
        return this.converters.stream()
                .filter(converter -> converter.canProcess(propertySpec))
                .findAny()
                .orElse(DEFAULT_CONVERTER);
    }

    @Override
    public PropertyInfo getPropertyInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider) {
        PropertyType propertyType = getConverter(propertySpec).getPropertyType(propertySpec);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo(propertyType, null, getPredefinedPropertyValueInfo(propertySpec, propertyType), null);
        PropertyValueInfo propertyValueInfo = getPropertyValueInfo(propertySpec, propertyValueProvider);
        return new PropertyInfo(propertySpec.getDisplayName(), propertySpec.getName(), propertyValueInfo, propertyTypeInfo, propertySpec.isRequired());
    }

    @Override
    public List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs) {
        return propertySpecs
                .stream()
                .map(propertySpec -> getPropertyInfo(propertySpec, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs, Map<String, Object> propertyValues) {
        return propertySpecs
                .stream()
                .map(propertySpec -> getPropertyInfo(propertySpec, propertyValues::get))
                .collect(Collectors.toList());
    }

    @Override
    public Object findPropertyValue(PropertySpec propertySpec, List<PropertyInfo> propertyInfos) {
        for (PropertyInfo propertyInfo : propertyInfos) {
            if (propertySpec.getName().equals(propertyInfo.key) && hasValue(propertyInfo)) {
                return getConverter(propertySpec).convertInfoToValue(propertySpec, propertyInfo.propertyValueInfo.getValue());
            }
        }
        return null;
    }

    @Override
    public PropertyValueInfoService getEmptyPropertyValueInfoService() {
        return new PropertyValueInfoServiceImpl();
    }

    private PropertyValueInfo getPropertyValueInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider) {
        Object propertyValue = getPropertyValue(propertySpec, propertyValueProvider);
        Object defaultValue = getDefaultValue(propertySpec);
        return new PropertyValueInfo<>(propertyValue, defaultValue);
    }

    private Object getPropertyValue(PropertySpec propertySpec, Function<String, Object> propertyValueProvider) {
        if (propertyValueProvider == null) {
            return null;
        }
        return getConverter(propertySpec).convertValueToInfo(propertySpec, propertyValueProvider.apply(propertySpec.getName()));
    }

    private Object getDefaultValue(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        Object defaultValue = possibleValues.getDefault();
        if (defaultValue != null) {
            return getConverter(propertySpec).convertValueToInfo(propertySpec, defaultValue);
        } else {
            return null;
        }
    }

    private PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec propertySpec, PropertyType propertyType) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        if (possibleValues.getAllValues().size() <= 1
                && propertyType != SimplePropertyType.DEVICECONFIGURATIONLIST
                && propertyType != SimplePropertyType.QUANTITY
                && propertyType != SimplePropertyType.SELECTIONGRID
                && propertyType != SimplePropertyType.ENDDEVICEEVENTTYPE
                && propertyType != SimplePropertyType.LIFECYCLESTATUSINDEVICETYPE
                && propertyType != SimplePropertyType.RAISEEVENTPROPS
                ) {
            // this means we have a default value, so no predefinedPropertyValues necessary in frontend.
            return null;
        }
        Object[] possibleObjects = new Object[possibleValues.getAllValues().size()];
        PropertyValueConverter converter = getConverter(propertySpec);
        if (converter != null) {
            for (int i = 0; i < possibleValues.getAllValues().size(); i++) {
                if (propertyType == SimplePropertyType.SELECTIONGRID || propertyType == SimplePropertyType.LISTREADINGQUALITY || propertyType == SimplePropertyType.DEVICECONFIGURATIONLIST ||
                        propertyType == SimplePropertyType.ENDDEVICEEVENTTYPE || propertyType == SimplePropertyType.LIFECYCLESTATUSINDEVICETYPE) {
                    possibleObjects[i] = possibleValues.getAllValues().get(i);
                } else if (propertyType == SimplePropertyType.IDWITHNAME) {
                    HasIdAndName idWithName = (HasIdAndName) possibleValues.getAllValues().get(i);
                    possibleObjects[i] = asInfo(idWithName.getId(), idWithName.getName());
                } else {
                    possibleObjects[i] = converter.convertValueToInfo(propertySpec, possibleValues.getAllValues().get(i));
                }
            }
        }

        return new PredefinedPropertyValuesInfo<>(possibleObjects, possibleValues.getSelectionMode(), propertySpec.getPossibleValues().isExhaustive(), propertySpec.getPossibleValues().isEditable());
    }

    private boolean hasValue(PropertyInfo propertyInfo) {
        PropertyValueInfo<?> propertyValueInfo = propertyInfo.getPropertyValueInfo();
        return propertyValueInfo != null && propertyValueInfo.getValue() != null && !"".equals(propertyValueInfo.getValue());
    }

    private Object asInfo(Object id, String name) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", id);
        info.put("name", name);
        return info;
    }
}
