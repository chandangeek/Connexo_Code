/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Map;

interface Properties {

    PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus);

    void copyFromMap(Map<String, Object> properties, PropertySetter propertySetter);

    void copyToMap(Map<String, Object> properties, PropertySetter propertySetter);

}
