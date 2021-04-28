package com.energyict.protocolimplv2.ace4000;

import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:58:56
 */
public class ACE4000Properties {

    public static final String TIMEOUT = DeviceProtocol.Property.TIMEOUT.getName();
    public static final String RETRIES = DeviceProtocol.Property.RETRIES.getName();
    public static final String MUST_KEEP_LISTENING = DeviceProtocol.Property.MUST_KEEP_LISTENING.getName();

    public static final BigDecimal DEFAULT_TIMEOUT = new BigDecimal("30000");
    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal("3");

    private final PropertySpecService propertySpecService;
    public TypedProperties properties;

    ACE4000Properties(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        this.properties = com.energyict.mdc.upl.TypedProperties.empty();
    }

    private void copyProperties(TypedProperties properties) {
        this.properties = com.energyict.mdc.upl.TypedProperties.copyOf(properties);
    }

    void setAllProperties(TypedProperties properties) {
        this.copyProperties(properties);
    }

    List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.timeoutPropertySpec(),
                this.retriesPropertySpec(),
                this.mustKeepListeningPropertySpec());
    }

    private PropertySpec timeoutPropertySpec() {
        return this.bigDecimalSpec(TIMEOUT, false, DEFAULT_TIMEOUT, PropertyTranslationKeys.V2_ACE4000_TIMEOUT);
    }

    private PropertySpec retriesPropertySpec() {
        return this.bigDecimalSpec(RETRIES, false, DEFAULT_RETRIES, PropertyTranslationKeys.V2_ACE4000_RETRIES);
    }

    private PropertySpec mustKeepListeningPropertySpec() {
        return this.BooleanSpec(MUST_KEEP_LISTENING, false, false, PropertyTranslationKeys.V2_ACE4000_RETRIES);
    }

    public int getTimeout() {
        return this.getIntegerProperty(TIMEOUT, DEFAULT_TIMEOUT);
    }

    public boolean shouldKeepListening(){
        if(properties.hasValueFor(MUST_KEEP_LISTENING)){
            return properties.<Boolean>getTypedProperty(MUST_KEEP_LISTENING);
        }else {
            return false;
        }
    }

    public int getRetries() {
        return this.getIntegerProperty(RETRIES, DEFAULT_RETRIES);
    }

    private int getIntegerProperty(String name, BigDecimal defaultValue) {
        Object value = this.properties.getProperty(name);
        if (value == null) {
            return defaultValue.intValue();
        } else {
            return ((BigDecimal) value).intValue();
        }
    }

    private PropertySpec bigDecimalSpec(String name, boolean required, BigDecimal defaultValue, TranslationKey translationKey) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, this.propertySpecService::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        return specBuilder.finish();
    }

    private PropertySpec BooleanSpec(String name, boolean required, Boolean defaultValue, TranslationKey translationKey) {
        PropertySpecBuilder<Boolean> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, this.propertySpecService::booleanSpec);
        specBuilder.setDefaultValue(defaultValue);
        return specBuilder.finish();
    }

}