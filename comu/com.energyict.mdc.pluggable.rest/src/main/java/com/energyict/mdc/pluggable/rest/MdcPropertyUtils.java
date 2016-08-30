package com.energyict.mdc.pluggable.rest;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.BoundedBigDecimalPropertySpec;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.rest.NumberValidationRules;
import com.elster.jupiter.properties.rest.PredefinedPropertyValuesInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValidationRule;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.FieldValidationException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.pluggable.rest.impl.properties.MdcPropertyReferenceInfoFactory;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;

import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.PrivilegePresence.WITHOUT_PRIVILEGES;
import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.ValueVisibility.HIDE_VALUES;
import static com.energyict.mdc.pluggable.rest.MdcPropertyUtils.ValueVisibility.SHOW_VALUES;

/**
 * Serves as a utility class to create proper PropertyInfo objects for a set of Properties
 * and their corresponding PropertySpecs
 */
public class MdcPropertyUtils {

    public void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, Collection<PropertySpec> propertySpecs, TypedProperties properties, List<PropertyInfo> propertyInfoList) {
        convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, properties, propertyInfoList, SHOW_VALUES, WITHOUT_PRIVILEGES);
    }

    public void convertPropertySpecsToPropertyInfos(final UriInfo uriInfo, Collection<PropertySpec> propertySpecs, TypedProperties properties, List<PropertyInfo> propertyInfoList, ValueVisibility showValue, PrivilegePresence privilegePresence) {
        for (PropertySpec propertySpec : propertySpecs) {
            PropertyValueInfo<?> propertyValueInfo = getThePropertyValueInfo(properties, propertySpec, showValue, privilegePresence);
            SimplePropertyType simplePropertyType = getSimplePropertyType(propertySpec);
            PropertyTypeInfo propertyTypeInfo = getPropertyTypeInfo(uriInfo, propertySpec, simplePropertyType, null);
            PropertyInfo propertyInfo = new PropertyInfo(getTranslatedPropertyName(propertySpec), propertySpec.getName(), propertyValueInfo, propertyTypeInfo, propertySpec.isRequired());
            propertyInfoList.add(propertyInfo);
        }
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties) {
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            PropertyValueInfo<?> propertyValueInfo = getThePropertyValueInfo(properties, propertySpec, SHOW_VALUES, WITHOUT_PRIVILEGES);
            SimplePropertyType simplePropertyType = getSimplePropertyType(propertySpec);
            PropertyTypeInfo propertyTypeInfo = getPropertyTypeInfo(null, propertySpec, simplePropertyType, null);
            PropertyInfo propertyInfo = new PropertyInfo(getTranslatedPropertyName(propertySpec), propertySpec.getName(), propertyValueInfo, propertyTypeInfo, propertySpec.isRequired());
            propertyInfoList.add(propertyInfo);
        }
        return propertyInfoList;
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties, Device device) {
        PropertyDefaultValuesProvider provider = (propertySpec, propertyType) -> {
            List<?> possibleValues = null;
            if (propertyType instanceof SimplePropertyType) {
                SimplePropertyType simplePropertyType = (SimplePropertyType) propertyType;
                if (SimplePropertyType.LOADPROFILE.equals(simplePropertyType)) {
                    possibleValues = device.getLoadProfiles();
                } else if (SimplePropertyType.LOGBOOK.equals(simplePropertyType)) {
                    possibleValues = device.getLogBooks();
                } else if (SimplePropertyType.REGISTER.equals(simplePropertyType)) {
                    possibleValues = device.getRegisters();
                } else if (SimplePropertyType.REFERENCE.equals(simplePropertyType)) {
                    PropertySpecPossibleValues possibleValuesFromSpec = propertySpec.getPossibleValues();
                    if (possibleValuesFromSpec != null) {
                        possibleValues = possibleValuesFromSpec.getAllValues();
                    }
                }
            }
            return possibleValues;
        };
        return convertPropertySpecsToPropertyInfos(propertySpecs, properties, provider);
    }

    public List<PropertyInfo> convertPropertySpecsToPropertyInfos(Collection<PropertySpec> propertySpecs, TypedProperties properties, PropertyDefaultValuesProvider valuesProvider) {
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        for (PropertySpec propertySpec : propertySpecs) {
            PropertyValueInfo<?> propertyValueInfo = getThePropertyValueInfo(properties, propertySpec, SHOW_VALUES, WITHOUT_PRIVILEGES);
            SimplePropertyType simplePropertyType = getSimplePropertyType(propertySpec);
            PropertyTypeInfo propertyTypeInfo = getPropertyTypeInfo(null, propertySpec, simplePropertyType, valuesProvider);
            PropertyInfo propertyInfo = new PropertyInfo(getTranslatedPropertyName(propertySpec), propertySpec.getName(), propertyValueInfo, propertyTypeInfo, propertySpec.isRequired());
            propertyInfoList.add(propertyInfo);
        }
        return propertyInfoList;
    }

    private String getTranslatedPropertyName(PropertySpec propertySpec) {
        return propertySpec.getDisplayName();
    }

    private PropertyValueInfo<Object> getThePropertyValueInfo(TypedProperties properties, PropertySpec propertySpec, ValueVisibility valueVisibility, PrivilegePresence privilegePresence) {
        Object propertyValue = getPropertyValue(properties, propertySpec);
        Boolean propertyHasValue = true;
        Object inheritedProperty = this.getInheritedPropertyValue(properties, propertySpec);
        Object defaultValue = getDefaultValue(propertySpec);
        if (isNull(propertyValue) && (isNull(inheritedProperty)) && (isNull(defaultValue))) {
            propertyHasValue = false;
        }
        if (HIDE_VALUES.equals(valueVisibility)) {
            propertyValue = null;
            inheritedProperty = null;
            defaultValue = null;
        }
        if (WITHOUT_PRIVILEGES.equals(privilegePresence)) {
            propertyHasValue = null;
        }
        return new PropertyValueInfo<>(propertyValue, inheritedProperty, defaultValue, propertyHasValue);
    }

    private boolean isNull(Object propertyValue) {
        return propertyValue == null || "".equals(propertyValue);
    }

    private SimplePropertyType getSimplePropertyType(PropertySpec propertySpec) {
        SimplePropertyType simplePropertyType = SimplePropertyType.getTypeFrom(propertySpec.getValueFactory());
        if (simplePropertyType.equals(SimplePropertyType.UNKNOWN)) {
            return MdcPropertyReferenceInfoFactory.getReferencedSimplePropertyType(propertySpec, simplePropertyType);
        } else {
            return simplePropertyType;
        }
    }

    private PropertyTypeInfo getPropertyTypeInfo(UriInfo uriInfo, PropertySpec propertySpec, SimplePropertyType simplePropertyType, PropertyDefaultValuesProvider valuesProvider) {
        return new PropertyTypeInfo(simplePropertyType, getPropertyValidationRule(propertySpec), getPredefinedPropertyValueInfo(propertySpec, simplePropertyType, valuesProvider), getReferenceUri(uriInfo, propertySpec, simplePropertyType));
    }

    private URI getReferenceUri(final UriInfo uriInfo, PropertySpec propertySpec, SimplePropertyType simplePropertyType) {
        if (simplePropertyType.isReference()) {
            return MdcPropertyReferenceInfoFactory.getReferenceUriFor(uriInfo, propertySpec.getValueFactory().getValueType());
        } else {
            return null;
        }
    }

    private PropertyValidationRule getPropertyValidationRule(PropertySpec propertySpec) {
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

    private Object getPropertyValue(TypedProperties properties, PropertySpec propertySpec) {
        return MdcPropertyReferenceInfoFactory.asInfoObject(properties.getLocalValue(propertySpec.getName()));
    }

    private Object getInheritedPropertyValue(TypedProperties properties, PropertySpec propertySpec) {
        return MdcPropertyReferenceInfoFactory.asInfoObject(properties.getInheritedValue(propertySpec.getName()));
    }

    private PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec propertySpec, SimplePropertyType simplePropertyType, PropertyDefaultValuesProvider valuesProvider) {
        List<?> possibleValues = null;
        boolean isExchaustive = true;
        boolean editable = false;
        if (valuesProvider != null) {
            possibleValues = valuesProvider.getPropertyPossibleValues(propertySpec, simplePropertyType);
        }

        if (propertySpec.getPossibleValues() != null) {
            possibleValues = propertySpec.getPossibleValues().getAllValues();
            isExchaustive = propertySpec.getPossibleValues().isExhaustive();
            editable = propertySpec.getPossibleValues().isEditable();
        }

        if (possibleValues == null || possibleValues.isEmpty()) {
            return null;
        }

        Object[] possibleObjects = new Object[possibleValues.size()];
        for (int i = 0; i < possibleValues.size(); i++) {
            possibleObjects[i] = MdcPropertyReferenceInfoFactory.asInfoObject(possibleValues.get(i));
        }
        PropertySelectionMode selectionMode = PropertySelectionMode.COMBOBOX;

        return new PredefinedPropertyValuesInfo<>(
                possibleObjects,
                selectionMode,
                isExchaustive,
                editable);
    }

    private <T> Object getDefaultValue(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        return MdcPropertyReferenceInfoFactory.asInfoObject(possibleValues.getDefault());
    }

    public Object findPropertyValue(PropertySpec propertySpec, Collection<PropertyInfo> propertyInfos) {
        return findPropertyValue(propertySpec, propertyInfos.toArray(new PropertyInfo[propertyInfos.size()]));
    }

    //find propertyValue in info
    public Object findPropertyValue(PropertySpec propertySpec, PropertyInfo[] propertyInfos) {
        for (PropertyInfo propertyInfo : propertyInfos) {
            if (propertyInfo.key.equals(propertySpec.getName())) {
                if (propertyInfo.getPropertyValueInfo() != null && propertyInfo.getPropertyValueInfo().getValue() != null && !"".equals(propertyInfo.getPropertyValueInfo().getValue())) {
                    return convertPropertyInfoValueToPropertyValue(propertySpec, propertyInfo.getPropertyValueInfo().getValue());
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private Object convertPropertyInfoValueToPropertyValue(PropertySpec propertySpec, Object value) {
        //SimplePropertyType simplePropertyType = getSimplePropertyType(propertySpec);
        if (Objects.equals(propertySpec.getValueFactory().getValueType(), Password.class)) {
            return new Password(value.toString());
        } else if (Objects.equals(propertySpec.getValueFactory().getValueType(), Date.class)) {
            return new Date((long) value);
        } else if (Objects.equals(propertySpec.getValueFactory().getValueType(), TimeDuration.class)) {
            Integer count = (Integer) ((LinkedHashMap<String, Object>) value).get("count");
            String timeUnit = (String) ((LinkedHashMap<String, Object>) value).get("timeUnit");
            try {
                return new TimeDuration("" + count + " " + timeUnit);
            }
            catch (LocalizedFieldValidationException e) {
                throw new LocalizedFieldValidationException(e.getMessageSeed(), propertySpec.getName() + "." + e.getViolatingProperty(), e.getArgs());
            }
        } else if (Objects.equals(propertySpec.getValueFactory().getValueType(), String.class)) {
            return value;
        } else if (Objects.equals(propertySpec.getValueFactory().getValueType(), Boolean.class)) {
            if (Boolean.class.isAssignableFrom(value.getClass())) {
                return value;
            } else {
                throw new FieldValidationException("Not a boolean", getTranslatedPropertyName(propertySpec));
            }
        }
        return propertySpec.getValueFactory().fromStringValue(value.toString());
    }

    public enum ValueVisibility {
        SHOW_VALUES, HIDE_VALUES
    }

    public enum PrivilegePresence {
        WITH_PRIVILEGES, WITHOUT_PRIVILEGES
    }

}