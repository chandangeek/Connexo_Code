package com.elster.jupiter.fileimport;

import com.elster.jupiter.nls.*;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by Lucian on 5/22/2015.
 */
public abstract class FileImporterAbstractFactory  implements FileImporterFactory {

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile Map<String, Object> properties;
    private Logger logger;

    public FileImporterAbstractFactory(){

    }

    protected FileImporterAbstractFactory(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.properties = Collections.emptyMap();
    }

    protected FileImporterAbstractFactory(Thesaurus thesaurus, PropertySpecService propertySpecService, Map<String, Object> properties) {
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

    public Thesaurus getThesaurus() {
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
        return SimpleNlsKey.key(FileImportService.COMPONENT_NAME, Layer.DOMAIN, getBaseKey());
    }

    @Override
    public NlsKey getPropertyNlsKey(String property) {
        if (isAProperty(property)) {
            return SimpleNlsKey.key("UNI", Layer.REST, property);
        }
        return null;
    }


    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public void setThesaurus(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(getName(), Layer.DOMAIN);
    }




}
