package com.elster.jupiter.issue.rest.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.rest.util.properties.PredefinedPropertyValuesInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertySelectionMode;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;

public class PropertyUtils {
    
    private final Thesaurus thesaurus;
    
    @Inject
    public PropertyUtils(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    private String getTranslatedPropertyName(PropertySpec propertySpec) {
        return thesaurus.getStringBeyondComponent(propertySpec.getName(), propertySpec.getName());
    }

    private PropertyInfoFactory propertyInfoFactory = new PropertyInfoFactory();

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(List<PropertySpec> propertySpecs) {
        return convertPropertySpecsToPropertyInfos(propertySpecs, null);//no initial values for properties
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(List<PropertySpec> propertySpecs, Map<String, Object> values) {
        List<PropertyInfo> propertyInfos = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            PropertyInfo propertyInfo = createPropertyInfo(propertySpec, values);
            propertyInfos.add(propertyInfo);
        }
        return propertyInfos;
    }

    private PropertyInfo createPropertyInfo(PropertySpec propertySpec, Map<String, Object> values) {
        PropertyValueInfo<?> propertyValueInfo = getPropertyValueInfo(propertySpec, values);
        PropertyType propertyType = PropertyType.getTypeFrom(propertySpec.getValueFactory());
        PropertyTypeInfo propertyTypeInfo = getPropertyTypeInfo(propertySpec, propertyType);
        return new PropertyInfo(getTranslatedPropertyName(propertySpec), propertySpec.getName(), propertyValueInfo, propertyTypeInfo, propertySpec.isRequired());
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

        PropertySelectionMode selectionMode = PropertySelectionMode.UNSPECIFIED;
        if (PropertyType.IDWITHNAME == propertyType) {
            selectionMode = PropertySelectionMode.COMBOBOX;
        }

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
        if (Objects.equals(propertySpec.getValueFactory().getValueType(), ListValue.class)) {
            ListValue<HasIdAndName> listValue = new ListValue<>();
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                for (Object listItem : list) {
                    listValue.addValue(parseListValueInfo(propertySpec, listItem));
                }
                return listValue;
            }
        }
        if (Objects.equals(propertySpec.getValueFactory().getValueType(), Boolean.class)) {
            if (Boolean.class.isAssignableFrom(value.getClass())) {
                return value;
            }
        }
        return propertySpec.getValueFactory().fromStringValue(value.toString());
    }

    private ListValue<HasIdAndName> parseListValueInfo(PropertySpec propertySpec, Object value) {
        String stringValue = value.toString();
        Object obj = propertySpec.getValueFactory().fromStringValue(stringValue);
        return (ListValue<HasIdAndName>) obj;
    }
}