package com.energyict.protocolimplv2.nta.esmr50.common;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.DescriptionTranslationKey;

import java.util.ArrayList;
import java.util.List;

public class ESMR50MbusConfigurationSupport {

    public static final String DEFAULT_KEY = "DefaultKey";
    public static final String FUAK        = "FirmwareUpgradeAuthenticationKey";

    private final PropertySpecService propertySpecService;

    public ESMR50MbusConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(defaultKeyPropertySpec());
        propertySpecs.add(fuakPropertySpec());
        return propertySpecs;
    }

    private PropertySpec defaultKeyPropertySpec() {
        return this.keyAccessorTypeReferencePropertySpec(DEFAULT_KEY, PropertyTranslationKeys.V2_NTA_DEFAULT_KEY);
    }

    private PropertySpec fuakPropertySpec() {
        return this.keyAccessorTypeReferencePropertySpec(FUAK, PropertyTranslationKeys.V2_NTA_FIRMWARE_UPGRADE_AUTHENTICATION_KEY);
    }

    private PropertySpec keyAccessorTypeReferencePropertySpec(String name, TranslationKey translationKey) {
        return this.propertySpecService
                .referenceSpec(KeyAccessorType.class.getName())
                .named(name, translationKey)
                .describedAs(new DescriptionTranslationKey(translationKey))
                .finish();
    }

}
