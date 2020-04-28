package com.elster.jupiter.systemproperties;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SystemProperty {

    String getKey();
    String getValue();
    void setValue(String value);
    void update();
}
