package com.elster.jupiter.systemproperties;

import java.util.List;
import java.util.Optional;

public interface SystemPropertyService {

    List<SystemProperty> getAllSystemProperties();
    //SystemProperty getSystemPropertiesById();//Needed?
    Optional<SystemProperty> getSystemPropertiesByName(String name);

    Optional<SystemPropertySpec> getPropertySpec(String key);

}
