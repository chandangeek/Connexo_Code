package com.energyict.protocolimplv2.ace4000;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:58:56
 */
public class ACE4000Properties {

    public static final String TIMEOUT = "Timeout";
    public static final String RETRIES = "Retries";
    public static final BigDecimal DEFAULT_TIMEOUT = new BigDecimal("30000");
    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal("3");

    public enum TranslationKeys implements TranslationKey {

        TIMEOUT("ACE4000.property.timeout", "Timeout"),
        RETRIES("ACE4000.property.retries", "Retries");

        private final String key;
        private final String defaultFormat;

        TranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }

    }
    private final PropertySpecService propertySpecService;

    public TypedProperties properties;

    public ACE4000Properties(PropertySpecService propertySpecService) {
        this(TypedProperties.empty(), propertySpecService);
    }

    public ACE4000Properties(TypedProperties properties, PropertySpecService propertySpecService) {
        super();
        this.properties = properties;
        this.propertySpecService = propertySpecService;
    }

    public List<PropertySpec> getPropertySpecs(Thesaurus thesaurus) {
        List<PropertySpec> optional = new ArrayList<>();
        optional.add(this.propertySpecService.bigDecimalSpec().named(TIMEOUT, TranslationKeys.TIMEOUT).fromThesaurus(thesaurus).finish());
        optional.add(this.propertySpecService.bigDecimalSpec().named(RETRIES, TranslationKeys.RETRIES).fromThesaurus(thesaurus).finish());
        return optional;
    }

    public int getTimeout() {
        return properties.getIntegerProperty(TIMEOUT, DEFAULT_TIMEOUT).intValue();
    }

    public int getRetries() {
        return properties.getIntegerProperty(RETRIES, DEFAULT_RETRIES).intValue();
    }

}