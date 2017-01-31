/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators;

import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationBlockFormatter;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.MissingRequiredProperty;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.logging.LoggingContext;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class AbstractEstimator implements Estimator {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    private final Map<String, Object> properties;
    private Logger logger;

    protected AbstractEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.properties = Collections.emptyMap();
    }

    protected AbstractEstimator(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        getRequiredProperties().forEach(propertyName -> checkRequiredProperty(propertyName, properties));
        this.properties = properties;
    }

    @Override
    public final void init(Logger logger) {
        this.logger = logger == null ? Logger.getLogger(this.getClass().getName()) : logger;
        init();
    }

    protected abstract void init();

    protected final Logger getLogger() {
        return logger == null ? Logger.getLogger(this.getClass().getName()) : logger;
    }

    private void checkRequiredProperty(String propertyName, Map<String, Object> properties) {
        if (!properties.containsKey(propertyName)) {
            throw new MissingRequiredProperty(getThesaurus(), propertyName);
        }
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected final PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getString(getNlsKey().getKey(), getDefaultFormat());
    }

    protected final Object getProperty(String key) {
        return properties.get(key);
    }

    protected final <T> Optional<T> getProperty(String key, Class<T> clazz) {
        return Optional.ofNullable(properties.get(key)).map(clazz::cast);
    }

    protected final String getBaseKey() {
        return this.getClass().getName();
    }

    @Override
    public NlsKey getNlsKey() {
        return SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.DOMAIN, getBaseKey());
    }

    protected final String format(EstimationBlock block) {
        return EstimationBlockFormatter.getInstance().format(block);
    }

    protected final LoggingContext initLoggingContext(EstimationBlock block) {
        // parent context will be automatically closed by EstimationService#previewEstimate
        // do not close it here, manually otherwise all other estimators will fail because rule parameter will be erased
        return LoggingContext.getCloseableContext().with(
                ImmutableMap.of(
                        "block", EstimationBlockFormatter.getInstance().format(block),
                        "readingType", block.getReadingType().getMRID()));
    }
}