package com.elster.jupiter.systemproperties;

import java.util.List;
import java.util.Optional;

public interface SystemPropertyService {

    List<SystemProperty> getAllSystemProperties();

    Optional<SystemProperty> getSystemPropertiesByName(String name);

    Optional<SystemPropertySpec> getPropertySpec(String key);

    void actionOnPropertyChange(SystemProperty systemProperty, SystemPropertySpec spec);
}
