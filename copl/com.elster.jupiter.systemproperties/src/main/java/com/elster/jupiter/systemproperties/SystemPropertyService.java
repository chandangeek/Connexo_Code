package com.elster.jupiter.systemproperties;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface SystemPropertyService {

    public static final String COMPONENT_NAME = "SYP";

    List<SystemProperty> getAllSystemProperties();

    Optional<SystemProperty> getSystemPropertiesByKey(String key);

    Optional<SystemPropertySpec> getPropertySpec(String key);

    void actionOnPropertyChange(SystemProperty systemProperty, SystemPropertySpec spec);

    void readAndCheckProperties();
}
