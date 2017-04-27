/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.properties;

import com.elster.jupiter.metering.ReadingTypeValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;

import java.util.HashMap;
import java.util.Map;

public class ReadingTypeValueConverter implements PropertyValueConverter {

    public static final ReadingTypeValueConverter INSTANCE = new ReadingTypeValueConverter();

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && ReadingTypeValueFactory.ReadingTypeReference.class.isAssignableFrom(propertySpec.getValueFactory()
                .getValueType());
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        if (propertySpec.getValueFactory() instanceof ReadingTypeValueFactory) {
                ReadingTypeValueFactory.Mode factoryMode = ((ReadingTypeValueFactory) propertySpec.getValueFactory()).getMode();
                switch (factoryMode) {
                    case ALL:
                        return ReadingPropertyType.READING_TYPE;
                    case ONLY_REGULAR:
                        return ReadingPropertyType.REGULAR_READINGTYPE;
                    case ONLY_IRREGULAR:
                        return ReadingPropertyType.IRREGULAR_READINGTYPE;
                }
        }
        return SimplePropertyType.UNKNOWN;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue instanceof Map) {
            Map info = (Map) infoValue;
            String mrid = (String) info.get("mRID");
            return propertySpec.getValueFactory().fromStringValue(mrid);
        }
        return null;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        ReadingTypeValueFactory.ReadingTypeReference readingTypeReference = (ReadingTypeValueFactory.ReadingTypeReference) domainValue;
        Map<String, String> info = new HashMap<>();
        info.put("mRID", readingTypeReference.getReadingType().getMRID());
        info.put("fullAliasName", readingTypeReference.getReadingType().getFullAliasName());
        return info;
    }
}
