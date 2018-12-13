/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MissingRequiredProperty;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class AbstractValidator implements IValidator {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    protected final Map<String, Object> properties;
    private Logger logger;

    public AbstractValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.properties = Collections.emptyMap();
    }

    public AbstractValidator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.properties = properties;
    }

    @Override
    public final void init(Channel channel, ReadingType readingType, Range<Instant> interval, Logger logger) {
        this.logger = logger == null ? Logger.getLogger(this.getClass().getName()) : logger;
        init(channel, readingType, interval);
    }

    protected final Logger getLogger() {
        return logger == null ? Logger.getLogger(this.getClass().getName()) : logger;
    }

    protected void checkRequiredProperties() {
        getRequiredProperties().forEach(propertyName -> checkRequiredProperty(propertyName, properties));
    }

    @Override
    public Optional<QualityCodeIndex> getReadingQualityCodeIndex(){
        return Optional.empty();
    }

    @Override
    public Map<Instant, ValidationResult> finish() {
        return Collections.emptyMap();
    }

    public final Thesaurus getThesaurus() {
        return thesaurus;
    }

    public final PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public String getDisplayName(String property) {
        return this.getPropertySpec(property).map(PropertySpec::getDisplayName).orElse(property);
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getString(getNlsKey().getKey(), getDefaultFormat());
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
}