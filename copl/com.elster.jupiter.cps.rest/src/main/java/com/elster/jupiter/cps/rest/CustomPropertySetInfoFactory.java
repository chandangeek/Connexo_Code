package com.elster.jupiter.cps.rest;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.cps.rest.impl.SimplePropertyType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.rest.util.properties.PredefinedPropertyValuesInfo;
import com.elster.jupiter.rest.util.properties.PropertyType;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.RangeInstantBuilder;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomPropertySetInfoFactory {

    private final Thesaurus thesaurus;
    private final PropertyValueInfoService propertyValueInfoService;
    private final Clock clock;

    @Inject
    public CustomPropertySetInfoFactory(Thesaurus thesaurus, Clock clock) {
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.propertyValueInfoService = new PropertyValueInfoService();
        this.propertyValueInfoService.addPropertyValueInfoConverter(new BooleanPropertyValueConverter());
        this.propertyValueInfoService.addPropertyValueInfoConverter(new NumberPropertyValueConverter());
        this.propertyValueInfoService.addPropertyValueInfoConverter(new InstantPropertyValueConverter());
        this.propertyValueInfoService.addPropertyValueInfoConverter(new QuantityPropertyValueConverter());
    }

    private CustomPropertySetInfo getGeneralInfo(RegisteredCustomPropertySet rcps) {
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        if (rcps != null) {
            CustomPropertySet<?, ?> cps = rcps.getCustomPropertySet();
            info.id = rcps.getId();
            info.viewPrivileges = rcps.getViewPrivileges();
            info.editPrivileges = rcps.getEditPrivileges();
            info.isEditable = rcps.isEditableByCurrentUser();

            info.customPropertySetId = cps.getId();
            info.name = cps.getName();
            info.domainNameUntranslated = cps.getDomainClass().getName();
            info.domainName = thesaurus.getStringBeyondComponent(info.domainNameUntranslated, info.domainNameUntranslated);

            info.isVersioned = cps.isVersioned();
            info.isRequired = cps.isRequired();
            info.defaultViewPrivileges = cps.defaultViewPrivileges();
            info.defaultEditPrivileges = cps.defaultEditPrivileges();
        }
        return info;
    }

    public CustomPropertySetInfo getGeneralAndPropertiesInfo(RegisteredCustomPropertySet rcps) {
        CustomPropertySetInfo info = getGeneralInfo(rcps);
        if (rcps != null) {
            CustomPropertySet<?, ?> cps = rcps.getCustomPropertySet();
            info.properties = cps.getPropertySpecs()
                    .stream()
                    .map(this::getPropertyInfo)
                    .collect(Collectors.toList());
        }
        return info;
    }


    public CustomPropertySetInfo getFullInfo(RegisteredCustomPropertySet rcps, CustomPropertySetValues customPropertySetValue) {
        CustomPropertySetInfo info = getGeneralInfo(rcps);
        if (rcps != null) {
            if (info.isVersioned) {
                addTimeSliceCustomPropertySetInfo(info, customPropertySetValue);
            }
            CustomPropertySet<?, ?> cps = rcps.getCustomPropertySet();
            info.properties = cps.getPropertySpecs().stream()
                    .map(propertySpec -> getPropertyInfo(propertySpec, key -> customPropertySetValue != null ? customPropertySetValue
                            .getProperty(key) : null))
                    .collect(Collectors.toList());
        }
        return info;
    }

    private void addTimeSliceCustomPropertySetInfo(CustomPropertySetInfo info, CustomPropertySetValues customPropertySetValue) {
        if (customPropertySetValue != null) {
            Range<Instant> effective = customPropertySetValue.getEffectiveRange();
            info.versionId = effective.hasLowerBound() ? effective.lowerEndpoint().toEpochMilli() : 0;
            info.startTime = effective.hasLowerBound() ? effective.lowerEndpoint().toEpochMilli() : null;
            info.endTime = effective.hasUpperBound() ? effective.upperEndpoint().toEpochMilli() : null;
            info.isActive = !customPropertySetValue.isEmpty() && effective.contains(this.clock.instant());
        } else {
            info.isActive = false;
        }
    }

    private CustomPropertySetAttributeInfo getPropertyInfo(PropertySpec propertySpec) {
        return getPropertyInfo(propertySpec, null);
    }

    public CustomPropertySetAttributeInfo getPropertyInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider) {
        CustomPropertySetAttributeInfo info = new CustomPropertySetAttributeInfo();
        if (propertySpec != null) {
            info.key = propertySpec.getName();
            info.name = propertySpec.getDisplayName();
            info.required = propertySpec.isRequired();
            info.propertyTypeInfo = getPropertyTypeInfo(propertySpec);
            info.propertyValueInfo = getPropertyValueInfo(propertySpec, propertyValueProvider);
            info.description = propertySpec.getDescription();
        }
        return info;
    }

    private CustomPropertySetAttributeTypeInfo getPropertyTypeInfo(PropertySpec propertySpec) {
        CustomPropertySetAttributeTypeInfo info = new CustomPropertySetAttributeTypeInfo();
        if (propertySpec != null) {
            info.type = propertySpec.getValueFactory().getValueType().getName();
            info.typeSimpleName = thesaurus.getString(info.type, info.type);
            info.simplePropertyType = SimplePropertyType.getTypeFrom(propertySpec.getValueFactory());
            info.predefinedPropertyValuesInfo = getPredefinedPropertyValueInfo(propertySpec);
        }
        return info;
    }

    private PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null || possibleValues.getAllValues().isEmpty()) {
            return null;
        }
        PropertyValueInfoConverter converter = this.propertyValueInfoService.getConverter(propertySpec);
        Object[] possibleObjects = new Object[possibleValues.getAllValues().size()];
        for (int i = 0; i < possibleValues.getAllValues().size(); i++) {
            possibleObjects[i] = converter.convertValueToInfo(possibleValues.getAllValues().get(i), propertySpec);
        }
        PropertySelectionMode selectionMode = propertySpec.getPossibleValues().getSelectionMode();
        PredefinedPropertyValuesInfo<Object> predefinedPropertyValuesInfo = new PredefinedPropertyValuesInfo<>();
        predefinedPropertyValuesInfo.possibleValues = possibleObjects;
        predefinedPropertyValuesInfo.selectionMode = selectionMode;
        predefinedPropertyValuesInfo.exhaustive = propertySpec.getPossibleValues().isExhaustive();
        return predefinedPropertyValuesInfo;
    }

    private PropertyValueInfo<?> getPropertyValueInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider) {
        Object propertyValue = getPropertyValue(propertySpec, propertyValueProvider);
        Object defaultValue = getDefaultValue(propertySpec);
        return new PropertyValueInfo<>(propertyValue, defaultValue);
    }

    private Object getPropertyValue(PropertySpec propertySpec, Function<String, Object> valueProvider) {
        if (valueProvider == null) {
            return null;
        }
        return this.propertyValueInfoService.getConverter(propertySpec)
                .convertValueToInfo(valueProvider.apply(propertySpec.getName()), propertySpec);
    }

    private Object getDefaultValue(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        return this.propertyValueInfoService.getConverter(propertySpec)
                .convertValueToInfo(possibleValues.getDefault(), propertySpec);
    }

    public CustomPropertySetValues getCustomPropertySetValues(CustomPropertySetInfo<?> info, List<PropertySpec> propertySpecs) {
        CustomPropertySetValues values;
        if (info.isVersioned) {
            values = CustomPropertySetValues.emptyDuring(Interval.of(RangeInstantBuilder.closedOpenRange(info.startTime, info.endTime)));
        } else {
            values = CustomPropertySetValues.empty();
        }
        if (info.properties != null && propertySpecs != null) {
            Map<String, PropertySpec> propertySpecMap = propertySpecs
                    .stream()
                    .collect(Collectors.toMap(PropertySpec::getName, Function.identity()));
            for (CustomPropertySetAttributeInfo property : info.properties) {
                PropertySpec propertySpec = propertySpecMap.get(property.key);
                if (propertySpec != null && property.propertyValueInfo != null && property.propertyValueInfo.value != null) {
                    values.setProperty(property.key, this.propertyValueInfoService.getConverter(propertySpec)
                            .convertInfoToValue(property.propertyValueInfo.value, propertySpec));
                }
            }
        }
        return values;
    }

    public ValuesRangeConflictInfo getValuesRangeConflictInfo(ValuesRangeConflict valueRangeConflict) {
        ValuesRangeConflictInfo info = new ValuesRangeConflictInfo();
        if (valueRangeConflict != null) {
            info.conflictType = valueRangeConflict.getType().name();
            info.message = valueRangeConflict.getMessage();
            info.conflictAtStart = valueRangeConflict.getType()
                    .equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_START);
            info.conflictAtEnd = valueRangeConflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END);
            info.editable = valueRangeConflict.getType().equals(ValuesRangeConflictType.RANGE_INSERTED);
        }
        return info;
    }

    interface PropertyValueInfoConverter {

        boolean canProcess(PropertySpec propertySpec);

        PropertyType getPropertyType(PropertySpec propertySpec);

        Object convertValueToInfo(Object domainValue, PropertySpec propertySpec);

        Object convertInfoToValue(Object infoValue, PropertySpec propertySpec);
    }

    //TODO https://jira.eict.vpdc/browse/COMU-3291 extract to common bundle
    static class PropertyValueInfoService {
        private static PropertyValueInfoConverter DEFAULT_CONVERTER = new DefaultPropertyValueConverter();
        private final List<PropertyValueInfoConverter> converters = new CopyOnWriteArrayList<>();

        public void addPropertyValueInfoConverter(PropertyValueInfoConverter converter) {
            this.converters.add(converter);
        }

        public void removePropertyValueInfoConverter(PropertyValueInfoConverter converter) {
            this.converters.remove(converter);
        }

        public PropertyValueInfoConverter getConverter(PropertySpec propertySpec) {
            return this.converters.stream()
                    .filter(converter -> converter.canProcess(propertySpec))
                    .findAny()
                    .orElse(DEFAULT_CONVERTER);
        }
    }

    static class BooleanPropertyValueConverter implements PropertyValueInfoConverter {
        @Override
        public boolean canProcess(PropertySpec propertySpec) {
            return propertySpec != null && propertySpec.getValueFactory() instanceof BooleanFactory;
        }

        @Override
        public PropertyType getPropertyType(PropertySpec propertySpec) {
            return SimplePropertyType.BOOLEAN;
        }

        @Override
        public Object convertValueToInfo(Object domainValue, PropertySpec propertySpec) {
            return domainValue;
        }

        @Override
        public Object convertInfoToValue(Object infoValue, PropertySpec propertySpec) {
            if (infoValue != null && infoValue instanceof Boolean) {
                return infoValue;
            }
            return Boolean.FALSE;
        }
    }

    static class NumberPropertyValueConverter implements PropertyValueInfoConverter {
        @Override
        public boolean canProcess(PropertySpec propertySpec) {
            return propertySpec != null && propertySpec.getValueFactory().getValueType().equals(BigDecimal.class);
        }

        @Override
        public PropertyType getPropertyType(PropertySpec propertySpec) {
            return SimplePropertyType.NUMBER;
        }

        @Override
        public Object convertValueToInfo(Object domainValue, PropertySpec propertySpec) {
            return domainValue;
        }

        @Override
        public Object convertInfoToValue(Object infoValue, PropertySpec propertySpec) {
            if (infoValue != null && !(infoValue instanceof String && Checks.is((String) infoValue)
                    .emptyOrOnlyWhiteSpace())) {
                return new BigDecimal(infoValue.toString());
            }
            return null;
        }
    }

    static class InstantPropertyValueConverter implements PropertyValueInfoConverter {

        @Override
        public boolean canProcess(PropertySpec propertySpec) {
            return propertySpec != null && propertySpec.getValueFactory().getValueType().equals(Instant.class);
        }

        @Override
        public PropertyType getPropertyType(PropertySpec propertySpec) {
            return SimplePropertyType.TIMESTAMP;
        }

        @Override
        public Object convertValueToInfo(Object domainValue, PropertySpec propertySpec) {
            return domainValue;
        }

        @Override
        public Object convertInfoToValue(Object infoValue, PropertySpec propertySpec) {
            if (infoValue != null && (infoValue instanceof Long)) {
                return Instant.ofEpochMilli((Long) infoValue);
            }
            return null;
        }
    }

    static class QuantityPropertyValueConverter implements PropertyValueInfoConverter {
        static class QuantityInfo {
            public String id;
            public String displayValue;
        }

        @Override
        public boolean canProcess(PropertySpec propertySpec) {
            return propertySpec != null && propertySpec.getValueFactory().getValueType().equals(Quantity.class);
        }

        @Override
        public PropertyType getPropertyType(PropertySpec propertySpec) {
            return SimplePropertyType.QUANTITY;
        }

        @Override
        public Object convertValueToInfo(Object domainValue, PropertySpec propertySpec) {
            QuantityInfo quantityInfo = new QuantityInfo();
            if (domainValue != null && domainValue instanceof Quantity) {
                Quantity value = (Quantity) domainValue;
                quantityInfo.id = new QuantityValueFactory().toStringValue(value);
                String[] valueParts = value.toString().split(" ");
                quantityInfo.displayValue = valueParts[1];
            }
            return quantityInfo;
        }

        @Override
        public Object convertInfoToValue(Object infoValue, PropertySpec propertySpec) {
            if (infoValue != null && infoValue instanceof Map) {
                Map value = (Map) infoValue;
                if (value.get("id") != null) {
                    return new QuantityValueFactory().fromStringValue(value.get("id").toString());
                }
            } else if (infoValue instanceof String) {
                return new QuantityValueFactory().fromStringValue((String) infoValue);
            }

            return null;
        }
    }

    static class DefaultPropertyValueConverter implements PropertyValueInfoConverter {
        @Override
        public boolean canProcess(PropertySpec propertySpec) {
            return true; // it can process any property spec
        }

        @Override
        public PropertyType getPropertyType(PropertySpec propertySpec) {
            return SimplePropertyType.getTypeFrom(propertySpec.getValueFactory());
        }

        @Override
        public Object convertValueToInfo(Object domainValue, PropertySpec propertySpec) {
            return propertySpec.getValueFactory().toStringValue(domainValue);
        }

        @Override
        public Object convertInfoToValue(Object infoValue, PropertySpec propertySpec) {
            if (infoValue != null && infoValue instanceof String && !Checks.is((String) infoValue)
                    .emptyOrOnlyWhiteSpace()) {
                return propertySpec.getValueFactory().fromStringValue((String) infoValue);
            }
            return null;
        }
    }
}