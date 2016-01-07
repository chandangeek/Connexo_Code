package com.elster.jupiter.validation.rest;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.rest.util.properties.PredefinedPropertyValuesInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.time.RelativePeriod;

import java.util.ArrayList;
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

    private PropertyInfo createPropertyInfo(PropertySpec propertySpec, Map<String, Object> values) {
        PropertyValueInfo<?> propertyValueInfo = getPropertyValueInfo(propertySpec, values);
        BasicPropertyTypes propertyType = BasicPropertyTypes.getTypeFrom(propertySpec.getValueFactory());
        PropertyTypeInfo propertyTypeInfo = getPropertyTypeInfo(propertySpec, propertyType);
        return new PropertyInfo(getTranslatedPropertyName(propertySpec), propertySpec.getName(), propertyValueInfo, propertyTypeInfo, propertySpec.isRequired());
    }

    private PropertyValueInfo<Object> getPropertyValueInfo(PropertySpec propertySpec, Map<String, Object> values) {
        Object propertyValue = getPropertyValue(propertySpec, values);
        Object defaultValue = getDefaultValue(propertySpec);
        if (propertyValue == null && defaultValue == null) {
            return null;
        }
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

    private PropertyTypeInfo getPropertyTypeInfo(PropertySpec propertySpec, BasicPropertyTypes propertyType) {
        return new PropertyTypeInfo(propertyType, null, getPredefinedPropertyValueInfo(propertySpec), null);
    }

    private PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        if (possibleValues.getAllValues().size() <= 1) {
            // this means we have a default value, so no predefinedPropertyValues necessary in frontend.
            return null;
        }
        Object[] possibleObjects = new Object[possibleValues.getAllValues().size()];
        for (int i = 0; i < possibleValues.getAllValues().size(); i++) {
            possibleObjects[i] = propertyInfoFactory.asInfoObjectForPredifinedValues(possibleValues.getAllValues().get(i));
        }

        PropertySelectionMode selectionMode = possibleValues.getSelectionMode();

        return new PredefinedPropertyValuesInfo<>(possibleObjects, selectionMode, propertySpec.getPossibleValues().isExhaustive());
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

    private Object convertPropertyInfoValueToPropertyValue(PropertySpec propertySpec, Object value) {
        if (HasIdAndName.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
            List<HasIdAndName> listValue = new ArrayList<>();
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                for (Object listItem : list) {
                    listValue.add(parseListValueInfo(propertySpec, listItem));
                }
                return listValue;
            }
        }
        if (Objects.equals(propertySpec.getValueFactory().getValueType(), RelativePeriod.class)) {
            return propertySpec.getValueFactory().fromStringValue("" + ((Map)value).get("id"));
        }
        if (Objects.equals(propertySpec.getValueFactory().getValueType(), Boolean.class)) {
            if (Boolean.class.isAssignableFrom(value.getClass())) {
                return value;
            }
        }
        return propertySpec.getValueFactory().fromStringValue(value.toString());
    }

    private HasIdAndName parseListValueInfo(PropertySpec propertySpec, Object value) {
        String stringValue = (String) value;
        return (HasIdAndName) propertySpec.getValueFactory().fromStringValue(stringValue);
    }

}