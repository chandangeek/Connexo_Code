/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.AdvanceReadingsSettingsFactory;
import com.elster.jupiter.estimation.AdvanceReadingsSettingsWithoutNoneFactory;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mbarinov on 18.08.2016.
 */
public class AdvanceReadingsSettingsValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && (propertySpec.getValueFactory() instanceof AdvanceReadingsSettingsFactory || propertySpec.getValueFactory() instanceof AdvanceReadingsSettingsWithoutNoneFactory);
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        if (propertySpec.getValueFactory() instanceof AdvanceReadingsSettingsFactory) {
            return PropertyType.ADVANCEREADINGSSETTINGS;
        }
        return PropertyType.ADVANCEREADINGSSETTINGSWITHOUTNONE;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        Map map = (Map) infoValue;
        String advanceSettings = NoneAdvanceReadingsSettings.NONE_ADVANCE_READINGS_SETTINGS;
        Object bulkProperty = map.get(BulkAdvanceReadingsSettings.BULK_ADVANCE_READINGS_SETTINGS);
        if ((bulkProperty != null) && ((Boolean) bulkProperty)) {
            advanceSettings = BulkAdvanceReadingsSettings.BULK_ADVANCE_READINGS_SETTINGS;
        } else if (map.get("readingType") != null) {
            advanceSettings = ((Map) map.get("readingType")).containsKey("mRID") ?
                    (String)((Map) map.get("readingType")).get("mRID") : (String)((Map) map.get("readingType")).get("mrid");
        }
        return propertySpec.getValueFactory().fromStringValue(advanceSettings);
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        if (domainValue != null && !(domainValue instanceof ReadingTypeAdvanceReadingsSettings)){
            Map <String, Boolean> propertyValueMap = new HashMap<>();
            propertyValueMap.put(domainValue.toString(), true);
            return propertyValueMap;
        }
        return domainValue;
    }
}
