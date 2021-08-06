package com.energyict.protocolimplv2.umi;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UmiConfigurationSupport implements HasDynamicProperties {
    private final PropertySpecService propertySpecService;
    private String umiId;

    public UmiConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
        //Arrays.asList(this.stringSpec("UmiId", false, PropertyTranslationKeys.UMI_ID));
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        umiId = properties.getTypedProperty("UmiId");
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected PropertySpec stringSpec(String name, boolean required, TranslationKey translationKey) {
        PropertySpecBuilder<String> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::stringSpec);
        return specBuilder.finish();
    }

    protected PropertySpec longSpec(String name, boolean required, TranslationKey translationKey) {
        PropertySpecBuilder<Long> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::longSpec);
        return specBuilder.finish();
    }

    protected PropertySpec intSpec(String name, boolean required, TranslationKey translationKey) {
        PropertySpecBuilder<Integer> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::integerSpec);
        return specBuilder.finish();
    }
}