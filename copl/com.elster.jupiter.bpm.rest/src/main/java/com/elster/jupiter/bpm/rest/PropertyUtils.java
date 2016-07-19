package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.rest.util.properties.PredefinedPropertyValuesInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PropertyUtils {

    private PropertyInfoFactory propertyInfoFactory = new PropertyInfoFactory();

    private String getTranslatedPropertyName(PropertySpec propertySpec) {
        return propertySpec.getDisplayName();
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(List<PropertySpec> propertySpecs) {
        return convertPropertySpecsToPropertyInfos(propertySpecs, null);//no initial values for properties
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(List<PropertySpec> propertySpecs, Map<String, Object> values) {
        return propertySpecs
                .stream()
                .map(propertySpec -> this.createPropertyInfo(propertySpec, values))
                .collect(Collectors.toList());
    }

    public Map<String, Object> convertPropertyInfosToProperties(List<PropertySpec> propertySpecs, List<PropertyInfo> properties) {
        Map<String, Object> propertyValues = new LinkedHashMap<>();
        for (PropertySpec propertySpec : propertySpecs) {
            Object value = findPropertyValue(propertySpec, properties);
            if (value != null) {
                propertyValues.put(propertySpec.getName(), value);
            }
        }
        return propertyValues;
    }

    private PropertyInfo createPropertyInfo(PropertySpec propertySpec, Map<String, Object> values) {
        PropertyValueInfo<?> propertyValueInfo = getPropertyValueInfo(propertySpec, values);
        PropertyType propertyType = PropertyType.getTypeFrom(propertySpec.getValueFactory());
        PropertyTypeInfo propertyTypeInfo = getPropertyTypeInfo(propertySpec, propertyType);
        return new PropertyInfo(getTranslatedPropertyName(propertySpec), propertySpec.getName(), propertyValueInfo, propertyTypeInfo, propertySpec
                .isRequired());
    }

    private PropertyValueInfo<Object> getPropertyValueInfo(PropertySpec propertySpec, Map<String, Object> values) {
        Object propertyValue = getPropertyValue(propertySpec, values);
        Object defaultValue = getDefaultValue(propertySpec);
        return new PropertyValueInfo<>(propertyValue, defaultValue);
    }

    private Object getPropertyValue(PropertySpec propertySpec, Map<String, Object> values) {
        if (values == null) {
            return null;
        }
        return propertyInfoFactory.asInfoObject(values.get(propertySpec.getName()));
    }

    private Object getDefaultValue(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        return propertyInfoFactory.asInfoObject(possibleValues.getDefault());
    }

    private PropertyTypeInfo getPropertyTypeInfo(PropertySpec propertySpec, PropertyType propertyType) {
        return new PropertyTypeInfo(propertyType, null, getPredefinedPropertyValueInfo(propertySpec, propertyType), null);
    }

    private PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec propertySpec, PropertyType propertyType) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null || possibleValues.getAllValues().isEmpty()) {
            return null;
        }
        Object[] possibleObjects = new Object[possibleValues.getAllValues().size()];
        for (int i = 0; i < possibleValues.getAllValues().size(); i++) {
            possibleObjects[i] = propertyInfoFactory.asInfoObjectForPredifinedValues(possibleValues.getAllValues()
                    .get(i));
        }

        PropertySelectionMode selectionMode = propertySpec.getPossibleValues().getSelectionMode();

        return new PredefinedPropertyValuesInfo<>(possibleObjects, selectionMode, propertySpec.getPossibleValues()
                .isExhaustive(), propertySpec.getPossibleValues().isEditable());
    }

    public Object findPropertyValue(PropertySpec propertySpec, List<PropertyInfo> propertyInfos) {
        for (PropertyInfo propertyInfo : propertyInfos) {
            if (matches(propertyInfo, propertySpec) && hasValue(propertyInfo)) {
                return convertPropertyInfoValueToPropertyValue(propertySpec, propertyInfo.propertyValueInfo.getValue());
            }
        }
        return null;
    }

    private boolean matches(PropertyInfo propertyInfo, PropertySpec propertySpec) {
        return propertySpec.getName().equals(propertyInfo.key);
    }

    private boolean hasValue(PropertyInfo propertyInfo) {
        PropertyValueInfo<?> propertyValueInfo = propertyInfo.getPropertyValueInfo();
        return propertyValueInfo != null && propertyValueInfo.getValue() != null && !"".equals(propertyValueInfo.getValue());
    }

    @SuppressWarnings("unchecked")
    private Object convertPropertyInfoValueToPropertyValue(PropertySpec propertySpec, Object value) {
        if (Objects.equals(propertySpec.getValueFactory().getValueType(), Boolean.class)) {
            if (Boolean.class.isAssignableFrom(value.getClass())) {
                return value;
            }
        }
        // Check for List values
        if (propertySpec.getValueFactory() instanceof ListValueFactory && value instanceof List) {
            List<Object> valueList = (List<Object>) value;
            ListValueFactory listValueFactory = (ListValueFactory) propertySpec.getValueFactory();
            return listValueFactory.fromValues(valueList);
        }
        return propertySpec.getValueFactory().fromStringValue(value.toString());
    }
}