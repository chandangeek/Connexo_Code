package com.energyict.mdc.pluggable.rest;

import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.FieldValidationException;
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
import com.energyict.mdc.pluggable.rest.impl.properties.MdcPropertyReferenceInfoFactory;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdw.UserFileService;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

/**
 * Serves as a utility class to create proper PropertyInfo objects for a set of Properties
 * and their corresponding PropertySpecs
 */
public class MdcPropertyUtils {

    private final UserFileService userFileService;

    @Inject
    public MdcPropertyUtils(UserFileService userFileService) {
        this.userFileService = userFileService;
    }

    public void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, List<PropertySpec> propertySpecs, TypedProperties properties, List<PropertyInfo> propertyInfoList) {
        for (PropertySpec<?> propertySpec : propertySpecs) {
            PropertyInfo propertyInfo = createPropertyInfo(uriInfo, properties, propertySpec);
            propertyInfoList.add(propertyInfo);
        }
    }

    private PropertyInfo createPropertyInfo(UriInfo uriInfo, TypedProperties properties, PropertySpec<?> propertySpec) {
        PropertyValueInfo<?> propertyValueInfo = getThePropertyValueInfo(properties, propertySpec);
        SimplePropertyType simplePropertyType = getSimplePropertyType(propertySpec);
        PropertyTypeInfo propertyTypeInfo = getPropertyTypeInfo(uriInfo, propertySpec, simplePropertyType);
        return new PropertyInfo(propertySpec.getName(), propertyValueInfo, propertyTypeInfo, propertySpec.isRequired());
    }

    private PropertyValueInfo<Object> getThePropertyValueInfo(TypedProperties properties, PropertySpec<?> propertySpec) {
        Object propertyValue = getPropertyValue(properties, propertySpec);
        Object inheritedProperty = getInheritedProperty(properties, propertySpec);
        Object defaultValue = getDefaultValue(propertySpec);
        if (propertyValue == null && inheritedProperty == null && defaultValue == null) {
            return null;
        }
        return new PropertyValueInfo<>(propertyValue, inheritedProperty, defaultValue);
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
        try {
            for (PropertyInfo propertyInfo : propertyInfos) {
                if (propertyInfo.key.equals(propertySpec.getName())) {
                    if (propertyInfo.getPropertyValueInfo() != null && propertyInfo.getPropertyValueInfo().getValue()!= null) {
                        return convertPropertyInfoValueToPropertyValue(propertySpec, propertyInfo.getPropertyValueInfo().getValue());
                    } else {
                        return null;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new FieldValidationException(e.getLocalizedMessage(), propertySpec.getName());
        }
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
            return new TimeDuration(""+count+" "+timeUnit);
        } else if (propertySpec.getValueFactory().getValueType() == String.class) {
            return value;
        } else if (propertySpec.getValueFactory().getValueType() == UserFile.class) {
            return userFileService.findUSerFileById((Integer) ((LinkedHashMap<String, Object>) value).get("userFileReferenceId"));
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