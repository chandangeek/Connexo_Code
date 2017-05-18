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
        if (NoneCalendarWithEventSettings.INSTANCE.name().equalsIgnoreCase(infoValue.toString())) {
            return NoneCalendarWithEventSettings.INSTANCE;
        }
        if (infoValue instanceof Map){
            Map map = (Map) infoValue;
            String advanceSettings = null;
            if (!(Boolean) map.get("discardDays")) {
                return NoneCalendarWithEventSettings.INSTANCE;
            } else {
                advanceSettings = map.get("discardDays").toString() + ":";
                if (map.get("calendar") == null || map.get("eventCode") == null){
                    return propertySpec.getValueFactory().fromStringValue(advanceSettings);
                }
                else {
                    advanceSettings += map.get("calendar");
                    advanceSettings += map.get("eventCode");
                }
            }
            return propertySpec.getValueFactory().fromStringValue(advanceSettings);
        } else {
            return NoneCalendarWithEventSettings.INSTANCE;
        }
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        if (domainValue != null && (domainValue instanceof DiscardDaySettings)) {
            return new CalendarWithEventCodeInfo((DiscardDaySettings) domainValue);
        }
        return domainValue;
    }
}
