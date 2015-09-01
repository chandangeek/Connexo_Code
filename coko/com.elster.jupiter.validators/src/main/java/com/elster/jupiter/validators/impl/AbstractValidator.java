package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MissingRequiredProperty;
import java.util.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 10/07/2014
 * Time: 14:33
 */
abstract class AbstractValidator implements IValidator {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    protected final Map<String, Object> properties;

    AbstractValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.properties = Collections.emptyMap();
    }
    
    AbstractValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        for (String propertyName : getRequiredProperties()) {
            checkRequiredProperty(propertyName, properties);
        }
        this.properties = properties;
    }

    @Override
    public Optional<ReadingQualityType> getReadingQualityTypeCode() {
        return Optional.empty();
    }

    @Override
    public Map<Instant, ValidationResult> finish() {
        return Collections.emptyMap();
    }

    final Thesaurus getThesaurus() {
        return thesaurus;
    }

    final PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public String getDisplayName(String property) {
        return getThesaurus().getString(getPropertyNlsKey(property).getKey(), getPropertyDefaultFormat(property));
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getString(getNlsKey().getKey(), getDefaultFormat());
    }

    boolean isAProperty(final String property) {
        return Iterables.any(getPropertySpecs(), new Predicate<PropertySpec>() {
            @Override
            public boolean apply(PropertySpec input) {
                return property.equals(input.getName());
            }
        });
    }
    
    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec propertySpec : getPropertySpecs()) {
            if (name.equals(propertySpec.getName())) {
                return propertySpec;
            }
        }
        return null;
    }
    
    private void checkRequiredProperty(String propertyName, Map<String, Object> properties) {
        if (!properties.containsKey(propertyName)) {
            throw new MissingRequiredProperty(getThesaurus(), propertyName);
        }
    }
    
    protected String getBaseKey() {
        return this.getClass().getName();
    }
    
    @Override
    public NlsKey getNlsKey() {
        return SimpleNlsKey.key(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN, getBaseKey());
    }
    
    @Override
    public NlsKey getPropertyNlsKey(String property) {
        if (isAProperty(property)) {
            /*
             * Component=UNI and Layer=REST because the front-end will try to translate the property itself, using unifyingjs framework
             */
            return SimpleNlsKey.key("UNI", Layer.REST, property);
        }
        return null;
    }
    
    @Override
    public List<Pair<? extends NlsKey, String>> getExtraTranslations() {
        return Collections.emptyList();
    }
}
