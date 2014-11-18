package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.properties.BoundedBigDecimalPropertySpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.rest.util.properties.NumberValidationRules;
import com.elster.jupiter.rest.util.properties.PredefinedPropertyValuesInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertySelectionMode;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValidationRule;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.FieldValidationException;
import com.energyict.mdc.pluggable.rest.impl.MessageSeeds;
import com.energyict.mdc.pluggable.rest.impl.properties.MdcPropertyReferenceInfoFactory;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import javax.ws.rs.core.UriInfo;

/**
 * Serves as a utility class to create proper PropertyInfo objects for a set of Properties
 * and their corresponding PropertySpecs
 */
public class MdcPropertyUtils {

    public void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, Collection<PropertySpec> propertySpecs, TypedProperties properties, List<PropertyInfo> propertyInfoList) {
        convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, properties, propertyInfoList, true, false);
    }

    public void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, Collection<PropertySpec> propertySpecs, TypedProperties properties, List<PropertyInfo> propertyInfoList, boolean showValue, boolean withPrivileges) {
        for (PropertySpec<?> propertySpec : propertySpecs) {
            PropertyValueInfo<?> propertyValueInfo = getThePropertyValueInfo(properties, propertySpec, showValue, withPrivileges);
            SimplePropertyType simplePropertyType = getSimplePropertyType(propertySpec);
            PropertyTypeInfo propertyTypeInfo = getPropertyTypeInfo(uriInfo, propertySpec, simplePropertyType);
            PropertyInfo propertyInfo = new PropertyInfo(propertySpec.getName(), propertyValueInfo, propertyTypeInfo, propertySpec.isRequired());
            propertyInfoList.add(propertyInfo);
        }
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties) {
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        for (PropertySpec<?> propertySpec : propertySpecs) {
            PropertyValueInfo<?> propertyValueInfo = getThePropertyValueInfo(properties, propertySpec, true, false);
            SimplePropertyType simplePropertyType = getSimplePropertyType(propertySpec);
            PropertyTypeInfo propertyTypeInfo = getPropertyTypeInfo(null, propertySpec, simplePropertyType);
            PropertyInfo propertyInfo = new PropertyInfo(propertySpec.getName(), propertyValueInfo, propertyTypeInfo, propertySpec.isRequired());
            propertyInfoList.add(propertyInfo);
        }
        return propertyInfoList;
    }

    private PropertyValueInfo<Object> getThePropertyValueInfo(TypedProperties properties, PropertySpec<?> propertySpec, boolean showValue, boolean withPrivileges) {
        Object propertyValue = getPropertyValue(properties, propertySpec);
        boolean propertyHasValue = true;
        Object inheritedProperty = getInheritedProperty(properties, propertySpec);
        Object defaultValue = getDefaultValue(propertySpec);
        if ((propertyValue == null || propertyValue.equals("")) && (inheritedProperty == null || inheritedProperty.equals("")) && (defaultValue == null || defaultValue.equals(""))) {
            propertyHasValue = false;
        }
        if (!showValue) {
            propertyValue = null;
            inheritedProperty = null;
            defaultValue = null;
        }
        if (!withPrivileges) {
            propertyHasValue = false;
        }
        return new PropertyValueInfo<>(propertyValue, inheritedProperty, defaultValue, propertyHasValue);
    }

    private SimplePropertyType getSimplePropertyType(PropertySpec<?> propertySpec) {
        SimplePropertyType simplePropertyType = SimplePropertyType.getTypeFrom(propertySpec.getValueFactory());
        if (simplePropertyType.equals(SimplePropertyType.UNKNOWN)) {
            return MdcPropertyReferenceInfoFactory.getReferencedSimplePropertyType(propertySpec, simplePropertyType);
        } else {
            return simplePropertyType;
        }
    }

    private PropertyTypeInfo getPropertyTypeInfo(UriInfo uriInfo, PropertySpec<?> propertySpec, SimplePropertyType simplePropertyType) {
        return new PropertyTypeInfo(simplePropertyType, getPropertyValidationRule(propertySpec), getPredefinedPropertyValueInfo(propertySpec), getReferenceUri(uriInfo, propertySpec, simplePropertyType));
    }

    private URI getReferenceUri(final UriInfo uriInfo, PropertySpec<?> propertySpec, SimplePropertyType simplePropertyType) {
        if (simplePropertyType.isReference()) {
            return MdcPropertyReferenceInfoFactory.getReferenceUriFor(uriInfo, propertySpec.getValueFactory().getValueType());
        } else {
            return null;
        }
    }

    private PropertyValidationRule getPropertyValidationRule(PropertySpec<?> propertySpec) {
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

    private Object getPropertyValue(TypedProperties properties, PropertySpec<?> propertySpec) {
        return MdcPropertyReferenceInfoFactory.asInfoObject(properties.getPropertyValue(propertySpec.getName()));
    }

    private PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec<?> propertySpec) {
        PropertySpecPossibleValues<?> possibleValues = propertySpec.getPossibleValues();
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

    private <T> Object getDefaultValue(PropertySpec<T> propertySpec) {
        PropertySpecPossibleValues<T> possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        return MdcPropertyReferenceInfoFactory.asInfoObject(possibleValues.getDefault());
    }

    public Object findPropertyValue(PropertySpec<?> propertySpec, Collection<PropertyInfo> propertyInfos) {
        return findPropertyValue(propertySpec, propertyInfos.toArray(new PropertyInfo[propertyInfos.size()]));
    }

    //find propertyValue in info
    public Object findPropertyValue(PropertySpec<?> propertySpec, PropertyInfo[] propertyInfos) {
        for (PropertyInfo propertyInfo : propertyInfos) {
            if (propertyInfo.key.equals(propertySpec.getName())) {
                if (propertyInfo.getPropertyValueInfo() != null && propertyInfo.getPropertyValueInfo().getValue()!= null && !propertyInfo.getPropertyValueInfo().getValue().equals("")) {
                    return convertPropertyInfoValueToPropertyValue(propertySpec, propertyInfo.getPropertyValueInfo().getValue());
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private Object convertPropertyInfoValueToPropertyValue(PropertySpec<?> propertySpec, Object value) {
        //SimplePropertyType simplePropertyType = getSimplePropertyType(propertySpec);
        if (propertySpec.getValueFactory().getValueType() == Password.class){
            return new Password(value.toString());
        } else if (propertySpec.getValueFactory().getValueType() == Date.class){
            return new Date((long)value);
        } else if (propertySpec.getValueFactory().getValueType() == TimeDuration.class) {
            Integer count = (Integer) ((LinkedHashMap<String, Object>) value).get("count");
            String timeUnit = (String) ((LinkedHashMap<String, Object>) value).get("timeUnit");
            if (!TimeDuration.isValidTimeUnitDescription(timeUnit)) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, propertySpec.getName());
            }
            return new TimeDuration(""+count+" "+timeUnit);
        } else if (propertySpec.getValueFactory().getValueType() == String.class) {
            return value;
        } else if (propertySpec.getValueFactory().getValueType() == Boolean.class) {
            if (Boolean.class.isAssignableFrom(value.getClass())) {
                return value;
            } else {
                throw new FieldValidationException("Not a boolean", propertySpec.getName());
            }
        }
        return propertySpec.getValueFactory().fromStringValue(value.toString());
    }


    private Object getInheritedProperty(TypedProperties properties, PropertySpec<?> propertySpec) {
        TypedProperties inheritedProperties = properties.getInheritedProperties();
        if (inheritedProperties == null) {
            return null;
        }
        return getPropertyValue(inheritedProperties, propertySpec);
    }

}