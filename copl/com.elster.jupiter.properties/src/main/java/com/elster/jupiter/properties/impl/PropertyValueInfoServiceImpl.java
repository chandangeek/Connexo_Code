package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.PropertyInfoFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.PropertyValueConverter;
import com.elster.jupiter.properties.PropertyValueInfoService;
import com.elster.jupiter.properties.SimplePropertyType;
import com.elster.jupiter.rest.util.properties.PredefinedPropertyValuesInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyType;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by mbarinov on 17.08.2016.
 */
@Component(name = "com.elster.jupiter.properties.propertyvalueinfoservice", service = PropertyValueInfoService.class)
public class PropertyValueInfoServiceImpl implements PropertyValueInfoService {

    private PropertyInfoFactory propertyInfoFactory = new PropertyInfoFactory();
    private static PropertyValueConverter DEFAULT_CONVERTER = new DefaultPropertyValueConverter();
    private final List<PropertyValueConverter> converters = new CopyOnWriteArrayList<>();

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
    }

    @Override
    public void addPropertyValueInfoConverter(PropertyValueConverter converter) {
        this.converters.add(converter);
    }

    @Override
    public void removePropertyValueInfoConverter(PropertyValueConverter converter) {
        this.converters.remove(converter);
    }

    @Override
    public PropertyValueConverter getConverter(PropertySpec propertySpec) {
        return this.converters.stream()
                .filter(converter -> converter.canProcess(propertySpec))
                .findAny()
                .orElse(DEFAULT_CONVERTER);
    }

    @Override
    public PropertyInfo getPropertyInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider) {
        PropertyType propertyType = getConverter(propertySpec).getPropertyType(propertySpec);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo(propertyType, null, getPredefinedPropertyValueInfo(propertySpec, propertyType), null);
        PropertyValueInfo propertyValueInfo = getConverter(propertySpec).convertValueToInfo(propertySpec, getPropertyValue(propertySpec, propertyValueProvider), getDefaultValue(propertySpec));
        return new PropertyInfo(propertySpec.getDisplayName(), propertySpec.getName(), propertyValueInfo, propertyTypeInfo, propertySpec.isRequired());
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
        return propertySpecs
                .stream()
                .map(propertySpec -> getPropertyInfo(propertySpec, propertyValues::get))
                .collect(Collectors.toList());
    }

    @Override
    public Object findPropertyValue(PropertySpec propertySpec, List<PropertyInfo> propertyInfos) {
        for (PropertyInfo propertyInfo : propertyInfos) {
            if (propertySpec.getName().equals(propertyInfo.key) && hasValue(propertyInfo)) {
                return getConverter(propertySpec).convertInfoToValue(propertySpec, propertyInfo.propertyValueInfo.getValue());
            }
        }
        return null;
    }

    private Object getPropertyValue(PropertySpec propertySpec, Function<String, Object> propertyValueProvider) {
        if (propertyValueProvider == null) {
            return null;
        }
        return propertyInfoFactory.asInfoObject(propertyValueProvider.apply(propertySpec.getName()));
    }

    private Object getDefaultValue(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        return propertyInfoFactory.asInfoObject(possibleValues.getDefault());
    }

    private PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec propertySpec, PropertyType propertyType) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        if (possibleValues.getAllValues().size() <= 1) {
            // this means we have a default value, so no predefinedPropertyValues necessary in frontend.
            return null;
        }
        Object[] possibleObjects = new Object[possibleValues.getAllValues().size()];

        for (int i = 0; i < possibleValues.getAllValues().size(); i++) {
            if (propertyType == SimplePropertyType.SELECTIONGRID || propertyType == SimplePropertyType.LISTREADINGQUALITY || propertyType == SimplePropertyType.DEVICECONFIGURATIONLIST) {
                possibleObjects[i] = possibleValues.getAllValues().get(i);
            } else {
                possibleObjects[i] = propertyInfoFactory.asInfoObjectForPredefinedValues(possibleValues.getAllValues().get(i));
            }
        }

        return new PredefinedPropertyValuesInfo<>(possibleObjects, possibleValues.getSelectionMode(), propertySpec.getPossibleValues().isExhaustive(), propertySpec.getPossibleValues().isEditable());
    }

    private boolean hasValue(PropertyInfo propertyInfo) {
        PropertyValueInfo<?> propertyValueInfo = propertyInfo.getPropertyValueInfo();
        return propertyValueInfo != null && propertyValueInfo.getValue() != null && !"".equals(propertyValueInfo.getValue());
    }
}
