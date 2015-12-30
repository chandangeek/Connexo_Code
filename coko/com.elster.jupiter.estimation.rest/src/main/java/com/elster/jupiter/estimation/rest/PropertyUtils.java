package com.elster.jupiter.estimation.rest;

import com.elster.jupiter.estimation.AdvanceReadingsSettings;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.estimation.rest.impl.EstimationApplication;
import com.elster.jupiter.estimation.rest.impl.PropertyInfoFactory;
import com.elster.jupiter.estimation.rest.impl.PropertyType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.rest.util.properties.PredefinedPropertyValuesInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.time.RelativePeriod;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PropertyUtils {

    private final Thesaurus thesaurus;

    private PropertyInfoFactory propertyInfoFactory = new PropertyInfoFactory();

    @Inject
    public PropertyUtils(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(EstimationApplication.COMPONENT_NAME, Layer.REST);
    }

    private String getTranslatedPropertyName(PropertySpec propertySpec) {
        return thesaurus.getStringBeyondComponent(propertySpec.getName(), propertySpec.getName());
    }

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
        if (propertyValue == null && defaultValue == null) {
            return null;
        }
        if (Objects.equals(propertySpec.getValueFactory().getValueType(), AdvanceReadingsSettings.class)) {
            Map <String, Boolean> defaultValueMap = new HashMap<>();
            if (defaultValue != null) {
                defaultValueMap.put(defaultValue.toString(), true);
            }
            if (propertyValue != null && !(propertyValue instanceof ReadingTypeAdvanceReadingsSettings)){
                Map <String, Boolean> propertyValueMap = new HashMap<>();
                propertyValueMap.put(propertyValue.toString(), true);
                propertyValue = propertyValueMap;
            }
            return new PropertyValueInfo<>(propertyValue, defaultValueMap);
        }
        if (Objects.equals(propertySpec.getValueFactory().getValueType(), RelativePeriod.class)) {
            Map <String, Integer> defaultValueMap = new HashMap<>();
            if (defaultValue != null) {
                defaultValueMap.put("id", 0);
                if (propertyValue != null && propertyValue.toString().equals(defaultValue.toString())) {
                    propertyValue = defaultValueMap;
                }
            }
            return new PropertyValueInfo<>(propertyValue, defaultValueMap);
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

    private PropertyTypeInfo getPropertyTypeInfo(PropertySpec propertySpec, PropertyType propertyType) {
        return new PropertyTypeInfo(propertyType, null, getPredefinedPropertyValueInfo(propertySpec, propertyType), null);
    }

    private PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec propertySpec, PropertyType propertyType) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        if (possibleValues.getDefault() != null) {
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
            return propertySpec.getValueFactory().fromStringValue("" + ((Map) value).get("id"));
        }
        if (Objects.equals(propertySpec.getValueFactory().getValueType(), AdvanceReadingsSettings.class)) {
            Map map = (Map) value;
            String advanceSettings = NoneAdvanceReadingsSettings.NONE_ADVANCE_READINGS_SETTINGS;
            Object bulkProperty = map.get(BulkAdvanceReadingsSettings.BULK_ADVANCE_READINGS_SETTINGS);
            if ((bulkProperty != null) && ((Boolean) bulkProperty)) {
                advanceSettings = BulkAdvanceReadingsSettings.BULK_ADVANCE_READINGS_SETTINGS;
            } else if (map.get("readingType") != null) {
                advanceSettings = ((Map) map.get("readingType")).containsKey("mRID") ?
                        (String)((Map) map.get("readingType")).get("mRID") : (String)((Map) map.get("readingType")).get("mrid");
            }
            return  propertySpec.getValueFactory().fromStringValue(advanceSettings);
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