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
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.logging.LoggingContext;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;
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

    private Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected final PropertySpecService getPropertySpecService() {
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
        return getPropertySpecs().stream()
                .anyMatch(input -> property.equals(input.getName()));
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return getPropertySpecs().stream()
                .filter(propertySpec -> name.equals(propertySpec.getName()))
                .findFirst()
                .orElse(null);
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

    protected final String format(EstimationBlock block) {
        return EstimationBlockFormatter.getInstance().format(block);
    }

    protected final LoggingContext initLoggingContext(EstimationBlock block) {
        return LoggingContext.get().with(ImmutableMap.of(
                "block", EstimationBlockFormatter.getInstance().format(block),
                "readingType", block.getReadingType().getMRID()));
    }
}
