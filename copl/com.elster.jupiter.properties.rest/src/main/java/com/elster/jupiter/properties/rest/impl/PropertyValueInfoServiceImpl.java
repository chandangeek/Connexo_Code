/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.rest.PredefinedPropertyValuesInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.properties.rest.propertyvalueinfoservice", immediate = true, service = PropertyValueInfoService.class)
public class PropertyValueInfoServiceImpl implements PropertyValueInfoService {

    private static PropertyValueConverter DEFAULT_CONVERTER = new DefaultPropertyValueConverter();
    private final List<PropertyValueConverter> converters = new CopyOnWriteArrayList<>();
    private final Map<String, PropertyValueConverter> dedicatedConverters = new ConcurrentHashMap<>();

    private volatile Thesaurus thesaurus;

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TimeService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Activate
    public void activate() {
        this.addPropertyValueInfoConverter(new BooleanPropertyValueConverter());
        this.addPropertyValueInfoConverter(new NumberPropertyValueConverter());
        this.addPropertyValueInfoConverter(new InstantPropertyValueConverter());
        this.addPropertyValueInfoConverter(new LongPropertyValueConverter());
        this.addPropertyValueInfoConverter(new NullableBooleanPropertyValueConverter());
        this.addPropertyValueInfoConverter(new IdWithNamePropertyValueConverter());
        this.addPropertyValueInfoConverter(new RelativePeriodPropertyValueConverter());
        this.addPropertyValueInfoConverter(new ListPropertyValueConverter());
        this.addPropertyValueInfoConverter(new QuantityPropertyValueConverter());
        this.addPropertyValueInfoConverter(new DurationPropertyValueConverter(thesaurus));
        this.addPropertyValueInfoConverter(new TemporalAmountPropertyValueConverter(thesaurus));
        this.addPropertyValueInfoConverter(new TimeDurationPropertyValueConverter(thesaurus));
        this.addPropertyValueInfoConverter(new NoneOrBigDecimalValueConverter());
        this.addPropertyValueInfoConverter(new TwoValuesDifferenceValueConverter());
        this.addPropertyValueInfoConverter(new NoneOrTimeDurationPropertyValueConverter(thesaurus));
        this.addPropertyValueInfoConverter(new IntegerPropertyValueConverter());
    }

    @Deactivate
    public void deactivate() {
        converters.clear();
    }

    @Override
    public void addPropertyValueInfoConverter(PropertyValueConverter converter) {
        this.converters.add(converter);
    }

    @Override
    public void addPropertyValueInfoConverter(PropertyValueConverter converter, String propertyName) {
        dedicatedConverters.put(propertyName, converter);
    }

    @Override
    public void removePropertyValueInfoConverter(PropertyValueConverter converter) {
        this.dedicatedConverters.keySet().forEach(propertyName -> this.cleanupDedicatedConverters(propertyName, converter));
        this.converters.remove(converter);
    }

    @Override
    public PropertyValueConverter getConverter(PropertySpec propertySpec) {
        return Optional.ofNullable(dedicatedConverters.get(propertySpec.getName())).orElseGet(() ->
                this.converters.stream()
                        .filter(converter -> converter.canProcess(propertySpec))
                        .findAny()
                        .orElse(DEFAULT_CONVERTER)
        );
    }

    @Override
    public PropertyInfo getPropertyInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider) {
        return getPropertyInfo(propertySpec, propertyValueProvider, null);
    }

    @Override
    public PropertyInfo getPropertyInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider, Function<String, Object> inheritedPropertyValueProvider) {
        PropertyValueConverter converter = getConverter(propertySpec);
        PropertyType propertyType = converter.getPropertyType(propertySpec);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo(propertyType, converter.getDefaultPropertyValidationRule(), getPredefinedPropertyValueInfo(propertySpec, propertyType), null);
        PropertyValueInfo propertyValueInfo = getPropertyValueInfo(propertySpec, propertyValueProvider, inheritedPropertyValueProvider);
        return new PropertyInfo(propertySpec.getDisplayName(), propertySpec.getName(), propertySpec.getDescription(), propertyValueInfo, propertyTypeInfo, propertySpec.isRequired());
    }

    @Override
    public List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs) {
        return propertySpecs
                .stream()
                .map(propertySpec -> getPropertyInfo(propertySpec, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs, Map<String, Object> propertyValues) {
        return getPropertyInfos(propertySpecs, propertyValues, Collections.emptyMap());
    }

    @Override
    public List<PropertyInfo> getPropertyInfos(List<PropertySpec> propertySpecs, Map<String, Object> propertyValues, Map<String, Object> inheritedPropertyValues) {
        return propertySpecs.stream()
                .map(propertySpec -> getPropertyInfo(propertySpec, propertyValues::get, inheritedPropertyValues::get))
                .collect(Collectors.toList());
    }

    @Deprecated
    @Override
    public Object findPropertyValue(PropertySpec propertySpec, List<PropertyInfo> propertyInfos) {
        return findPropertyValue(propertySpec, (Collection<PropertyInfo>) propertyInfos);
    }

    @Override
    public Object findPropertyValue(PropertySpec propertySpec, Collection<PropertyInfo> propertyInfos) {
        for (PropertyInfo propertyInfo : propertyInfos) {
            if (propertySpec.getName().equals(propertyInfo.key) && hasValue(propertyInfo)) {
                return getConverter(propertySpec).convertInfoToValue(propertySpec, propertyInfo.propertyValueInfo.getValue());
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> findPropertyValues(Collection<PropertySpec> propertySpecs, Collection<PropertyInfo> propertyInfos) {
        Map<String, PropertySpec> propertySpecsByNames = propertySpecs.stream()
                .collect(Collectors.toMap(PropertySpec::getName, Function.identity()));
        Map<String, Object> result = new HashMap<>(propertySpecs.size(), 1);
        for (PropertyInfo propertyInfo : propertyInfos) {
            PropertySpec propertySpec = propertySpecsByNames.get(propertyInfo.key);
            if (propertySpec != null && hasValue(propertyInfo)) {
                result.put(propertyInfo.key, getConverter(propertySpec).convertInfoToValue(propertySpec, propertyInfo.propertyValueInfo.getValue()));
            }
        }
        return result;
    }

    @Override
    public PropertyValueInfoService getEmptyPropertyValueInfoService() {
        return new PropertyValueInfoServiceImpl();
    }

    private void cleanupDedicatedConverters(String propertyName, PropertyValueConverter converter) {
        if (this.dedicatedConverters.get(propertyName).equals(converter)) {
            this.dedicatedConverters.remove(propertyName);
        }
    }

    private PropertyValueInfo getPropertyValueInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider, Function<String, Object> inheritedPropertyProvider) {
        Object propertyValue = getPropertyValue(propertySpec, propertyValueProvider);
        Object inheritedValue = getPropertyValue(propertySpec, inheritedPropertyProvider);
        Object defaultValue = getDefaultValue(propertySpec);
        return new PropertyValueInfo<>(propertyValue, inheritedValue, defaultValue, null);
    }

    private Object getPropertyValue(PropertySpec propertySpec, Function<String, Object> propertyValueProvider) {
        if (propertyValueProvider == null) {
            return null;
        }
        Object domainValue = propertyValueProvider.apply(propertySpec.getName());
        if (domainValue == null) {
            return null;
        }
        return getConverter(propertySpec).convertValueToInfo(propertySpec, domainValue);
    }

    private Object getDefaultValue(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        Object defaultValue = possibleValues.getDefault();
        if (defaultValue != null) {
            return getConverter(propertySpec).convertValueToInfo(propertySpec, defaultValue);
        } else {
            return null;
        }
    }

    private PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec propertySpec, PropertyType propertyType) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        if (possibleValues.getAllValues().size() <= 1
                && propertyType != SimplePropertyType.DEVICECONFIGURATIONLIST
                && propertyType != SimplePropertyType.METROLOGYCONFIGURATIONLIST
                && propertyType != SimplePropertyType.QUANTITY
                && propertyType != SimplePropertyType.SELECTIONGRID
                && propertyType != SimplePropertyType.ENDDEVICEEVENTTYPE
                && propertyType != SimplePropertyType.LIFECYCLESTATUSINDEVICETYPE
                && propertyType != SimplePropertyType.RAISEEVENTPROPS
                && propertyType != SimplePropertyType.BPM_PROCESS
                && propertyType != SimplePropertyType.WEB_SERVICES_ENDPOINT
                && propertyType != SimplePropertyType.IDWITHNAME
                && propertyType != SimplePropertyType.IDWITHNAMELIST
                && propertyType != SimplePropertyType.RADIO_GROUP
                && propertyType != SimplePropertyType.RECURRENCE_SELECTION_PROPS
                && propertyType != SimplePropertyType.LIFECYCLETRANSITION
                && propertyType != SimplePropertyType.TASK
                && propertyType != SimplePropertyType.DEVICEGROUPTYPE
                && propertyType != SimplePropertyType.SERVICE_CALL
                && propertyType != SimplePropertyType.SERVICE_CALL_STATE
                && propertyType != SimplePropertyType.CUSTOM_EVENT_TYPE
                && propertyType != SimplePropertyType.ENDPOINT_CONFIGURATION_LIST
                && propertyType != SimplePropertyType.VALIDATION_RULES_DROPDOWN
        ) {
            // this means we have a default value, so no predefinedPropertyValues necessary in frontend.
            return null;
        }
        Object[] possibleObjects = new Object[possibleValues.getAllValues().size()];
        PropertyValueConverter converter = getConverter(propertySpec);
        if (converter != null) {
            for (int i = 0; i < possibleValues.getAllValues().size(); i++) {
                if (propertyType == SimplePropertyType.SELECTIONGRID || propertyType == SimplePropertyType.LISTREADINGQUALITY || propertyType == SimplePropertyType.DEVICECONFIGURATIONLIST || propertyType == SimplePropertyType.METROLOGYCONFIGURATIONLIST ||
                        propertyType == SimplePropertyType.ENDDEVICEEVENTTYPE || propertyType == SimplePropertyType.LIFECYCLESTATUSINDEVICETYPE ||
                        propertyType == SimplePropertyType.RADIO_GROUP || propertyType == SimplePropertyType.DEVICEGROUPTYPE || propertyType == SimplePropertyType.LIFECYCLETRANSITION || propertyType == SimplePropertyType.RECURRENCE_SELECTION_PROPS ||
                        propertyType == SimplePropertyType.TASK || propertyType == SimplePropertyType.SERVICE_CALL || propertyType == SimplePropertyType.SERVICE_CALL_STATE || propertyType == SimplePropertyType.ENDPOINT_CONFIGURATION_LIST) {
                    possibleObjects[i] = possibleValues.getAllValues().get(i);
                } else if (propertyType == SimplePropertyType.IDWITHNAME
                        || propertyType == SimplePropertyType.IDWITHNAMELIST
                        || propertyType == SimplePropertyType.BPM_PROCESS
                        || propertyType == SimplePropertyType.WEB_SERVICES_ENDPOINT
                        || propertyType == SimplePropertyType.CUSTOM_EVENT_TYPE
                        || propertyType == SimplePropertyType.VALIDATION_RULES_DROPDOWN) {
                    Object idWithName = possibleValues.getAllValues().get(i);
                    possibleObjects[i] = idWithName instanceof HasIdAndName
                            ? asInfo(((HasIdAndName) idWithName).getId(), ((HasIdAndName) idWithName).getName())
                            : asInfo(((HasId) idWithName).getId(), ((HasName) idWithName).getName());
                } else {
                    possibleObjects[i] = converter.convertValueToInfo(propertySpec, possibleValues.getAllValues().get(i));
                }
            }
        }

        return new PredefinedPropertyValuesInfo<>(possibleObjects, possibleValues.getSelectionMode(), propertySpec.getPossibleValues()
                .isExhaustive(), propertySpec.getPossibleValues().isEditable());
    }

    private boolean hasValue(PropertyInfo propertyInfo) {
        PropertyValueInfo<?> propertyValueInfo = propertyInfo.getPropertyValueInfo();
        return propertyValueInfo != null && propertyValueInfo.getValue() != null && !"".equals(propertyValueInfo.getValue());
    }

    private Object asInfo(Object id, String name) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", id);
        info.put("name", name);
        return info;
    }
}
