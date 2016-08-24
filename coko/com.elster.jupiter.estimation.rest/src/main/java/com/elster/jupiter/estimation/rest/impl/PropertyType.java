package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.AdvanceReadingsSettingsFactory;
import com.elster.jupiter.estimation.AdvanceReadingsSettingsWithoutNoneFactory;

public enum PropertyType implements com.elster.jupiter.rest.util.properties.PropertyType {
    ADVANCEREADINGSSETTINGS(AdvanceReadingsSettingsFactory.class),
    ADVANCEREADINGSSETTINGSWITHOUTNONE(AdvanceReadingsSettingsWithoutNoneFactory.class)
    ;

    private Class valueFactoryClass;

    PropertyType(Class valueFactoryClass) {
        this.valueFactoryClass = valueFactoryClass;
    }
}
