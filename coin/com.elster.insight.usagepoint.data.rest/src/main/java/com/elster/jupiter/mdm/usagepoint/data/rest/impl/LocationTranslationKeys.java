/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by david on 5/5/2016.
 */
public enum LocationTranslationKeys implements TranslationKey {
    ZIP_CODE("zipCode", "Zip code"),
    COUNTRY_CODE("countryCode", "Country code"),
    COUNTRY_NAME("countryName", "Country name"),
    ADMINISTRATIVE_AREA("administrativeArea", "Administrative area"),
    LOCALITY("locality", "Locality"),
    SUBLOCALITY("subLocality", "Sublocality"),
    STREET_TYPE("streetType", "Street type"),
    STREET_NAME("streetName", "Street name"),
    STREET_NUMBER("streetNumber", "Street number"),
    ESTABLISHMENT_TYPE("establishmentType", "Establishment type"),
    ESTABLISHMENT_NAME("establishmentName", "Establishment name"),
    ESTABLISHMENT_NUMBER("establishmentNumber", "Establishment number"),
    ADDRESS_DETAIL("addressDetail", "Address details");


    private final String key;
    private final String defaultFormat;

    LocationTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }


    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
