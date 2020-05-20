package com.elster.jupiter.systemproperties;

import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface SystemPropertyService {

    public static final String COMPONENT_NAME = "SYP";

    List<SystemProperty> getAllSystemProperties();

    Optional<SystemProperty> findSystemPropertyByKey(String key);

    PropertySpec findPropertySpec(String key);

    Object getPropertyValue(String key);

    void setPropertyValue(String key, Object value);
}
