package com.elster.jupiter.cps.rest;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.impl.SimplePropertyType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.rest.util.properties.PredefinedPropertyValuesInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomPropertySetInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public CustomPropertySetInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public CustomPropertySetInfo from(RegisteredCustomPropertySet rcps) {
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

    public CustomPropertySetInfo getGeneralInfo(RegisteredCustomPropertySet rcps) {
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        if (rcps != null) {
            CustomPropertySet<?, ?> cps = rcps.getCustomPropertySet();
            info.id = rcps.getId();
            info.viewPrivileges = rcps.getViewPrivileges();
            info.editPrivileges = rcps.getEditPrivileges();

            info.customPropertySetId = cps.getId();
            info.name = cps.getName();
            String domainNameUntranslated = cps.getDomainClass().getName();
            info.domainName = thesaurus.getStringBeyondComponent(domainNameUntranslated, domainNameUntranslated);
            info.isActive = true;
            info.isRequired = cps.isRequired();
            info.isVersioned = cps.isVersioned();
            info.defaultViewPrivileges = cps.defaultViewPrivileges();
            info.defaultEditPrivileges = cps.defaultEditPrivileges();
        }
        return info;
    }

    public CustomPropertySetAttributeInfo getPropertyInfo(PropertySpec propertySpec) {
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

    public CustomPropertySetAttributeTypeInfo getPropertyTypeInfo(PropertySpec propertySpec) {
        CustomPropertySetAttributeTypeInfo info = new CustomPropertySetAttributeTypeInfo();
        if (propertySpec != null) {
            info.type = propertySpec.getValueFactory().getValueType().getName();
            info.typeSimpleName = thesaurus.getString(info.type, info.type);
            info.simplePropertyType = SimplePropertyType.getTypeFrom(propertySpec.getValueFactory());
            info.predefinedPropertyValuesInfo = getPredefinedPropertyValueInfo(propertySpec);
        }
        return info;
    }

    public PredefinedPropertyValuesInfo<?> getPredefinedPropertyValueInfo(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null || possibleValues.getAllValues().isEmpty()) {
            return null;
        }
        Object[] possibleObjects = new Object[possibleValues.getAllValues().size()];
        for (int i = 0; i < possibleValues.getAllValues().size(); i++) {
            //TODO No conversion for now! https://jira.eict.vpdc/browse/COMU-3291
            possibleObjects[i] = possibleValues.getAllValues().get(i);
        }
        PropertySelectionMode selectionMode = propertySpec.getPossibleValues().getSelectionMode();
        return new PredefinedPropertyValuesInfo<>(possibleObjects, selectionMode, propertySpec.getPossibleValues().isExhaustive());
    }

    public PropertyValueInfo<?> getPropertyValueInfo(PropertySpec propertySpec, Function<String, Object> propertyValueProvider) {
        Object propertyValue = getPropertyValue(propertySpec, propertyValueProvider);
        Object defaultValue = getDefaultValue(propertySpec);
        return new PropertyValueInfo<>(propertyValue, defaultValue);
    }

    private Object getPropertyValue(PropertySpec propertySpec, Function<String, Object> valueProvider) {
        if (valueProvider == null) {
            return null;
        }
        //TODO No conversion for now! https://jira.eict.vpdc/browse/COMU-3291
        return valueProvider.apply(propertySpec.getName());
    }

    private Object getDefaultValue(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValues = propertySpec.getPossibleValues();
        if (possibleValues == null) {
            return null;
        }
        //TODO No conversion for now! https://jira.eict.vpdc/browse/COMU-3291
        return possibleValues.getDefault();
    }
}