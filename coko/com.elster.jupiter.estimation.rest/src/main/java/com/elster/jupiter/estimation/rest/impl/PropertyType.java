/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.AdvanceReadingsSettingsFactory;
import com.elster.jupiter.estimation.AdvanceReadingsSettingsWithoutNoneFactory;
import com.elster.jupiter.estimation.CalendarWithEventSettingsFactory;

public enum PropertyType implements com.elster.jupiter.properties.rest.PropertyType {
    ADVANCEREADINGSSETTINGS(AdvanceReadingsSettingsFactory.class),
    ADVANCEREADINGSSETTINGSWITHOUTNONE(AdvanceReadingsSettingsWithoutNoneFactory.class),
    CALENDARWITHEVENTCODE(CalendarWithEventSettingsFactory.class)
    ;

    private Class valueFactoryClass;

    PropertyType(Class valueFactoryClass) {
        this.valueFactoryClass = valueFactoryClass;
    }
}
