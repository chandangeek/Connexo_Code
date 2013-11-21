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
        for (PropertySpec<?> propertySpec : optionalProperties) {
            PropertyValueInfo<?> propertyValueInfo = new PropertyValueInfo<>(getPropertyValue(properties, propertySpec), getInheritedProperty(properties, propertySpec), getDefaultValue(propertySpec));
            SimplePropertyType simplePropertyType = getSimplePropertyType(propertySpec);
            PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo(simplePropertyType, getPropertyValidationRule(), getPredefinedPropertyValueInfo(propertySpec), getReferenceUri(uriInfo, propertySpec, simplePropertyType));
            propertyInfoList.add(new PropertyInfo(propertySpec.getName(), propertyValueInfo, propertyTypeInfo, false));
        }
    }

    private static URI getReferenceUri(final UriInfo uriInfo, PropertySpec<?> propertySpec, SimplePropertyType simplePropertyType) {
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

    static PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec propertySpec) {
        PropertySpecPossibleValues<?> possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        } else {
            Object[] possibleObjects = new Object[possibleValues.getAllValues().size()];
            for (int i = 0; i < possibleValues.getAllValues().size(); i++) {
                possibleObjects[i] = MdcPropertyValueInfoFactory.asInfo(possibleValues.getAllValues().get(i));
            }
            return new PredefinedPropertyValuesInfo<>(
                    possibleObjects,
                    mapPropertySelectionMode(propertySpec.getSelectionMode()),
                    propertySpec.getPossibleValues().isExhaustive());
        }
    }

    static  Object getDefaultValue(PropertySpec<?> propertySpec) {
        PropertySpecPossibleValues<?> possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        return MdcPropertyValueInfoFactory.asInfo(possibleValues.getDefault());
    }

    static  Object getInheritedProperty(TypedProperties properties, PropertySpec<?> propertySpec) {
        TypedProperties inheritedProperties = properties.getInheritedProperties();
        if (inheritedProperties == null) {
            return null;
        }
        return getPropertyValue(inheritedProperties, propertySpec);
    }

    static  PropertySelectionMode mapPropertySelectionMode(AttributeValueSelectionMode selectionMode) {
        return PropertySelectionMode.values()[selectionMode.ordinal()];
    }
}