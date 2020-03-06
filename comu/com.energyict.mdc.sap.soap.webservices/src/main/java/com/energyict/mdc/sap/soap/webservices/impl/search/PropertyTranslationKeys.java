/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.search;

import com.elster.jupiter.nls.TranslationKey;

public enum PropertyTranslationKeys implements TranslationKey {

    SAP(SapAttributesSearchablePropertyGroup.GROUP_NAME, "SAP attributes"),
    DEVICE_IDENTIFIER(DeviceIdentifierSearchableProperty.PROPERTY_NAME, "Device identifier"),
    DEVICE_LOCATION(DeviceLocationSearchableProperty.PROPERTY_NAME, "Device location"),
    POINT_OF_DELIVERY(PointOfDeliverySearchableProperty.PROPERTY_NAME, "Point of delivery identifier"),
    REGISTERED(RegisteredSearchableProperty.PROPERTY_NAME, "Registered"),
    LOGICAL_REGISTER_NUMBER(LogicalRegisterNumberSearchableProperty.PROPERTY_NAME, "Logical register number"),
    PROFILE_ID(ProfileIdSearchableProperty.PROPERTY_NAME, "Profile id");

    private String key;
    private String defaultFormat;

    PropertyTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }


    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}