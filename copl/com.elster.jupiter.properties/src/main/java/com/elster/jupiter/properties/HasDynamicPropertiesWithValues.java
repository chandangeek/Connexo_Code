package com.elster.jupiter.properties;

import java.util.Map;

public interface HasDynamicPropertiesWithValues extends HasDynamicProperties {

    Map<String, Object> getProperties();
    
}
