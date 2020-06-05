package com.elster.jupiter.systemproperties;

import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SystemProperty {

    String getKey();
    Object getValue();
    PropertySpec getPropertySpec();
    void setValue(Object value);
    void update();
}
