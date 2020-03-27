package com.elster.jupiter.systemproperties;

public interface SystemProperty {

    String getName();
    String getValue();
    void setValue(String value);
    void update();
}
