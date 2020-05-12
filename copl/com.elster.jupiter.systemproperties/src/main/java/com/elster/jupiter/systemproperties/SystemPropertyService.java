package com.elster.jupiter.systemproperties;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface SystemPropertyService {

    public static final String COMPONENT_NAME = "SYP";

    List<SystemProperty> getAllSystemProperties();

    Optional<SystemProperty> findSystemPropertyByKey(String key);

    Optional<SystemPropertySpec> findPropertySpec(String key);

    String getPropertyValue(String key);

    void actionOnPropertyChange(SystemProperty systemProperty);

    void readAndProcessUpdatedProperties();

    void setPropertyValue(String key, String value);
}
