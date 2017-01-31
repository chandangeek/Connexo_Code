/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.util.Map;

public interface HasDynamicPropertiesWithValues extends HasDynamicProperties {

    Map<String, Object> getProperties();
    
}
