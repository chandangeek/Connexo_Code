package com.energyict.mdc.rest.impl.properties;

import com.energyict.cbo.HexString;
import com.energyict.cpo.BoundedBigDecimalPropertySpec;
import com.energyict.cpo.FixedLengthHexStringPropertySpec;
import com.energyict.cpo.FixedLengthStringPropertySpec;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecPossibleValues;
import com.energyict.cpo.TypedProperties;
import com.energyict.dynamicattributes.AttributeValueSelectionMode;
import com.energyict.mdc.rest.impl.properties.validators.NumberValidationRules;
import com.energyict.mdc.rest.impl.properties.validators.StringValidationRules;

import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

public class MdcPropertyUtils {

    public static void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, List<PropertySpec> optionalProperties, TypedProperties properties, List<PropertyInfo> propertyInfoList) {
        for (PropertySpec propertySpec : optionalProperties) {
            PropertyInfo propertyInfo = createPropertyInfo(uriInfo, properties, propertySpec);
            propertyInfoList.add(propertyInfo);
        }
    }

    private static PropertyInfo createPropertyInfo(UriInfo uriInfo, TypedProperties properties, PropertySpec propertySpec) {
        PropertyValueInfo propertyValueInfo = getThePropertyValueInfo(properties, propertySpec);
        SimplePropertyType simplePropertyType = getSimplePropertyType(propertySpec);
        PropertyTypeInfo propertyTypeInfo = getPropertyTypeInfo(uriInfo, propertySpec, simplePropertyType);
        return new PropertyInfo(propertySpec.getName(), propertyValueInfo, propertyTypeInfo, false);
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

    private static SimplePropertyType getSimplePropertyType(PropertySpec propertySpec) {
        if(HexString.class.isAssignableFrom(propertySpec.getDomain().getValueType())){
            return SimplePropertyType.TEXT;
        } else {
            return SimplePropertyType.getTypeFrom(propertySpec.getDomain().getValueType());
        }
    }

    private static PropertyTypeInfo getPropertyTypeInfo(UriInfo uriInfo, PropertySpec propertySpec, SimplePropertyType simplePropertyType) {
        return new PropertyTypeInfo(simplePropertyType, getPropertyValidationRule(propertySpec), getPredefinedPropertyValueInfo(propertySpec), getReferenceUri(uriInfo, propertySpec, simplePropertyType));
    }

    private static URI getReferenceUri(final UriInfo uriInfo, PropertySpec propertySpec, SimplePropertyType simplePropertyType) {
        if(simplePropertyType == SimplePropertyType.REFERENCE){
            return MdcPropertyValueInfoFactory.getReferenceUriFor(uriInfo, propertySpec.getDomain().getValueType());
        } else {
            return null;
        }
    }

    private static PropertyValidationRule getPropertyValidationRule(PropertySpec propertySpec) {
        if(BoundedBigDecimalPropertySpec.class.isAssignableFrom(propertySpec.getClass())){
            BoundedBigDecimalPropertySpec boundedBigDecimalPropertySpec = (BoundedBigDecimalPropertySpec) propertySpec;
            return createBoundedBigDecimalValidationRules(boundedBigDecimalPropertySpec);
        } else if (FixedLengthStringPropertySpec.class.isAssignableFrom(propertySpec.getClass())){
            FixedLengthStringPropertySpec fixedLengthStringPropertySpec = (FixedLengthStringPropertySpec) propertySpec;
            return createFixedLengthStringValidationRules(fixedLengthStringPropertySpec);
        } else if(FixedLengthHexStringPropertySpec.class.isAssignableFrom(propertySpec.getClass())){
            FixedLengthHexStringPropertySpec fixedLengthHexStringPropertySpec = (FixedLengthHexStringPropertySpec) propertySpec;
            return createFixedLengthHexStringValidationRules(fixedLengthHexStringPropertySpec);
        }
        return null;
    }

    private static PropertyValidationRule createFixedLengthHexStringValidationRules(FixedLengthHexStringPropertySpec fixedLengthHexStringPropertySpec) {
        StringValidationRules stringValidationRules = new StringValidationRules();
        stringValidationRules.setEnforceMaxLength(Boolean.TRUE);
        stringValidationRules.setMaxLength(fixedLengthHexStringPropertySpec.getLength() * 2); // the given length is the number of bytes
        stringValidationRules.setRegex(StringValidationRules.HEX_CHARACTERS_REGEX);
        return stringValidationRules;
    }

    private static PropertyValidationRule createFixedLengthStringValidationRules(FixedLengthStringPropertySpec fixedLengthStringPropertySpec) {
        StringValidationRules stringValidationRules = new StringValidationRules();
        stringValidationRules.setEnforceMaxLength(Boolean.TRUE);
        stringValidationRules.setMaxLength(fixedLengthStringPropertySpec.getLength());
        return stringValidationRules;
    }

    private static PropertyValidationRule createBoundedBigDecimalValidationRules(BoundedBigDecimalPropertySpec boundedBigDecimalPropertySpec) {
        NumberValidationRules<BigDecimal> bigDecimalNumberValidationRules = new NumberValidationRules<>();
        bigDecimalNumberValidationRules.setAllowDecimals(true);
        bigDecimalNumberValidationRules.setMaximumValue(boundedBigDecimalPropertySpec.getUpperLimit());
        bigDecimalNumberValidationRules.setMinimumValue(boundedBigDecimalPropertySpec.getLowerLimit());
        return bigDecimalNumberValidationRules;
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
                We set the selectionMode to ComboBox if we have more than 1 possible value (otherwise it can be it is the default value)
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