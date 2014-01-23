package com.energyict.mdc.rest.impl.properties;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.BoundedBigDecimalPropertySpec;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecPossibleValues;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.rest.impl.properties.validators.NumberValidationRules;

import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

/**
 * Serves as a utility class to create proper PropertyInfo objects for a set of Properties
 * and their corresponding PropertySpecs
 */
public class MdcPropertyUtils {

    public static void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, PropertySpecService propertySpecService, List<PropertySpec> propertySpecs, TypedProperties properties, List<PropertyInfo> propertyInfoList) {
        for (PropertySpec propertySpec : propertySpecs) {
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
        if (propertyValue == null && inheritedProperty == null && defaultValue == null) {
            return null;
        }
        return new PropertyValueInfo<>(propertyValue, inheritedProperty, defaultValue);
    }

    private static SimplePropertyType getSimplePropertyType(PropertySpec propertySpec) {
        SimplePropertyType simplePropertyType = SimplePropertyType.getTypeFrom(propertySpec.getValueFactory());
        if (simplePropertyType.equals(SimplePropertyType.UNKNOWN)) {
            return MdcPropertyReferenceInfoFactory.getReferencedSimplePropertyType(propertySpec, simplePropertyType);
        } else {
            return simplePropertyType;
        }
    }

    private static PropertyTypeInfo getPropertyTypeInfo(UriInfo uriInfo, PropertySpec propertySpec, SimplePropertyType simplePropertyType) {
        return new PropertyTypeInfo(simplePropertyType, getPropertyValidationRule(propertySpec), getPredefinedPropertyValueInfo(propertySpec), getReferenceUri(uriInfo, propertySpec, simplePropertyType));
    }

    private static URI getReferenceUri(final UriInfo uriInfo, PropertySpec propertySpec, SimplePropertyType simplePropertyType) {
        if (simplePropertyType.isReference()) {
            return MdcPropertyReferenceInfoFactory.getReferenceUriFor(uriInfo, propertySpec.getValueFactory().getValueType());
        } else {
            return null;
        }
    }

    private static PropertyValidationRule getPropertyValidationRule(PropertySpec propertySpec) {
        if (BoundedBigDecimalPropertySpec.class.isAssignableFrom(propertySpec.getClass())) {
            BoundedBigDecimalPropertySpec boundedBigDecimalPropertySpec = (BoundedBigDecimalPropertySpec) propertySpec;
            return createBoundedBigDecimalValidationRules(boundedBigDecimalPropertySpec);
        } else {
            return null;
        }
    }

    private static PropertyValidationRule createBoundedBigDecimalValidationRules(BoundedBigDecimalPropertySpec boundedBigDecimalPropertySpec) {
        NumberValidationRules<BigDecimal> bigDecimalNumberValidationRules = new NumberValidationRules<>();
        bigDecimalNumberValidationRules.setAllowDecimals(true);
        bigDecimalNumberValidationRules.setMaximumValue(boundedBigDecimalPropertySpec.getUpperLimit());
        bigDecimalNumberValidationRules.setMinimumValue(boundedBigDecimalPropertySpec.getLowerLimit());
        return bigDecimalNumberValidationRules;
    }

    private static Object getPropertyValue(TypedProperties properties, PropertySpec propertySpec) {
        return MdcPropertyReferenceInfoFactory.asInfoObject(properties.getProperty(propertySpec.getName()));
    }

    private static PredefinedPropertyValuesInfo getPredefinedPropertyValueInfo(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        } else {
            if (possibleValues.getAllValues().size() <= 1) {
                // this means we have a default value, so no predefinedPropertyValues necessary in frontend.
                return null;
            } else {
                Object[] possibleObjects = new Object[possibleValues.getAllValues().size()];
                for (int i = 0; i < possibleValues.getAllValues().size(); i++) {
                    possibleObjects[i] = MdcPropertyReferenceInfoFactory.asInfoObject(possibleValues.getAllValues().get(i));
                }
                PropertySelectionMode selectionMode = PropertySelectionMode.COMBOBOX;

                return new PredefinedPropertyValuesInfo<>(
                        possibleObjects,
                        selectionMode,
                        propertySpec.getPossibleValues().isExhaustive());
            }
        }
    }

    private static Object getDefaultValue(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        return MdcPropertyReferenceInfoFactory.asInfoObject(possibleValues.getDefault());
    }

    //find propertyValue in info
    public static Object findPropertyValue(PropertySpec propertySpec, PropertyInfo[] propertyInfos) {
        for (PropertyInfo propertyInfo : propertyInfos) {
            if (propertyInfo.getKey().equals(propertySpec.getName())) {
                if (propertyInfo.getPropertyValueInfo() != null) {
                    return propertyInfo.getPropertyValueInfo().getValue();
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private static Object getInheritedProperty(TypedProperties properties, PropertySpec propertySpec) {
        TypedProperties inheritedProperties = properties.getInheritedProperties();
        if (inheritedProperties == null) {
            return null;
        }
        return getPropertyValue(inheritedProperties, propertySpec);
    }

}