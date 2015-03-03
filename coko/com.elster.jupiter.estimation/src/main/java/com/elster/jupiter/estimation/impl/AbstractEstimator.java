package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.MessageSeeds;
import com.elster.jupiter.estimation.MissingRequiredProperty;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.Pair;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by igh on 3/03/2015.
 */
public abstract class AbstractEstimator implements IEstimator {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    protected final Map<String, Object> properties;

    AbstractEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.properties = Collections.emptyMap();
    }

    AbstractEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        for (String propertyName : getRequiredProperties()) {
            checkRequiredProperty(propertyName, properties);
        }
        this.properties = properties;
    }

    private void checkRequiredProperty(String propertyName, Map<String, Object> properties) {
        if (!properties.containsKey(propertyName)) {
            throw new MissingRequiredProperty(getThesaurus(), propertyName);
        }
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
