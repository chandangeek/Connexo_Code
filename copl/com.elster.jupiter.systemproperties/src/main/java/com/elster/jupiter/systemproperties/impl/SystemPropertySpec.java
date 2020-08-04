package com.elster.jupiter.systemproperties.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.systemproperties.SystemProperty;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SystemPropertySpec {

    String getKey();
    void actionOnChange(SystemProperty property);
    PropertySpec getPropertySpec();
}
