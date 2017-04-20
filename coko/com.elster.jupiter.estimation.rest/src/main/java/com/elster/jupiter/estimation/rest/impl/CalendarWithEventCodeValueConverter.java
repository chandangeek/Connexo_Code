/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.DiscardDaySettings;
import com.elster.jupiter.estimation.CalendarWithEventSettingsFactory;
import com.elster.jupiter.estimation.NoneCalendarWithEventSettings;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;

import java.util.Map;

/**
 * Created by aeryomin on 10.04.2017.
 */
public class CalendarWithEventCodeValueConverter implements PropertyValueConverter {
    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && (propertySpec.getValueFactory() instanceof CalendarWithEventSettingsFactory);
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return com.elster.jupiter.estimation.rest.impl.PropertyType.CALENDARWITHEVENTCODE;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue.toString().equalsIgnoreCase("INSTANCE")) {
            return NoneCalendarWithEventSettings.INSTANCE;
        }
        Map map = (Map) infoValue;
        String advanceSettings = null;
        if ((Boolean) map.get("discardDays") == false) {
            return NoneCalendarWithEventSettings.INSTANCE;
        } else {
            advanceSettings = map.get("discardDays").toString() + ":";
            advanceSettings += map.get("calendar") != null ? map.get("calendar").toString() + ":" : "";
            advanceSettings += map.get("eventCode") != null ? map.get("eventCode").toString() : "";
        }
        return propertySpec.getValueFactory().fromStringValue(advanceSettings);
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        if (domainValue != null && (domainValue instanceof DiscardDaySettings)) {
            return new CalendarWithEventCodeInfo((DiscardDaySettings) domainValue);
        }
        return domainValue;
    }
}
