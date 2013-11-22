package com.energyict.mdc.rest.impl.properties;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecPossibleValues;
import com.energyict.cpo.TypedProperties;
import com.energyict.dynamicattributes.AttributeValueSelectionMode;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

public class MdcPropertyUtils {

    public static void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, List<PropertySpec> optionalProperties, TypedProperties properties, List<PropertyInfo> propertyInfoList) {
        for (PropertySpec propertySpec : optionalProperties) {
            PropertyValueInfo propertyValueInfo = getThePropertyValueInfo(properties, propertySpec);
            SimplePropertyType simplePropertyType = getSimplePropertyType(propertySpec);
            PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo(simplePropertyType, getPropertyValidationRule(), getPredefinedPropertyValueInfo(propertySpec), getReferenceUri(uriInfo, propertySpec, simplePropertyType));
            propertyInfoList.add(new PropertyInfo(propertySpec.getName(), propertyValueInfo, propertyTypeInfo, false));
        }
    }

    private static PropertyValueInfo<Object> getThePropertyValueInfo(TypedProperties properties, PropertySpec propertySpec) {
        Object propertyValue = getPropertyValue(properties, propertySpec);
        Object inheritedProperty = getInheritedProperty(properties, propertySpec);
        Object defaultValue = getDefaultValue(propertySpec);
        if(propertyValue == null && inheritedProperty == null && defaultValue == null){
            return null;
        }
        return new PropertyValueInfo<>(propertyValue, inheritedProperty, defaultValue);
    }

    private static URI getReferenceUri(final UriInfo uriInfo, PropertySpec propertySpec, SimplePropertyType simplePropertyType) {
        if(simplePropertyType == SimplePropertyType.REFERENCE){
            return MdcPropertyValueInfoFactory.getReferenceUriFor(uriInfo, propertySpec.getDomain().getValueType());
        } else {
            return null;
        }
    }

    private static PropertyValidationRule getPropertyValidationRule() {
        return null;
    }

    private static SimplePropertyType getSimplePropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.getTypeFrom(propertySpec.getDomain().getValueType());
    }

    private static Object getPropertyValue(TypedProperties properties, PropertySpec propertySpec) {
        return MdcPropertyValueInfoFactory.asInfo(properties.getProperty(propertySpec.getName()));
    }

    private static PredefinedPropertyValuesInfo getPredefinedPropertyValueInfo(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        } else {
            Object[] possibleObjects = new Object[possibleValues.getAllValues().size()];
            for (int i = 0; i < possibleValues.getAllValues().size(); i++) {
                possibleObjects[i] = MdcPropertyValueInfoFactory.asInfo(possibleValues.getAllValues().get(i));
            }
            PropertySelectionMode selectionMode = mapPropertySelectionMode(propertySpec.getSelectionMode());
            if(selectionMode.equals(PropertySelectionMode.UNSPECIFIED) && possibleObjects.length > 1){
                /*
                We set the selectionMode to ComboBox if we more than 1 possible value (otherwise it can be it is the default value)
                and the current selectionMode is not specified (otherwise the frontEnd will not be able to show them)
                 */
                selectionMode = PropertySelectionMode.COMBOBOX;
            }
            return new PredefinedPropertyValuesInfo<>(
                    possibleObjects,
                    selectionMode,
                    propertySpec.getPossibleValues().isExhaustive());
        }
    }

    private static  Object getDefaultValue(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        return MdcPropertyValueInfoFactory.asInfo(possibleValues.getDefault());
    }

    private static  Object getInheritedProperty(TypedProperties properties, PropertySpec propertySpec) {
        TypedProperties inheritedProperties = properties.getInheritedProperties();
        if (inheritedProperties == null) {
            return null;
        }
        return getPropertyValue(inheritedProperties, propertySpec);
    }

    private static  PropertySelectionMode mapPropertySelectionMode(AttributeValueSelectionMode selectionMode) {
        return PropertySelectionMode.values()[selectionMode.ordinal()];
    }
}