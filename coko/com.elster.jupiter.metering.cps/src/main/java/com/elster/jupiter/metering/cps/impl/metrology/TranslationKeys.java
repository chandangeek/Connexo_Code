package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    CPS_DOMAIN_NAME(UsagePoint.class.getName(), "Usage point"),
    SIMPLE_CPS_NAME("usage.point.cps.simple.name", "Simple usage point CAS"),
    VERSIONED_CPS_NAME("usage.point.cps.versioned.name", "Versioned usage point CAS"),
    CPS_PROPERTIES_NAME("usage.point.cps.properties.name", "Name"),
    CPS_PROPERTIES_NAME_DESCRIPTION("usage.point.cps.properties.name.descr", "Name for CAS"),
    CPS_PROPERTIES_ENHANCED_SUPPORT("usage.point.cps.properties.enhanced.support", "Enhanced support"),
    CPS_PROPERTIES_ENHANCED_SUPPORT_DESCRIPTION("usage.point.cps.properties.enhanced.support.descr", "Cool enhanced support"),
    CPS_PROPERTIES_COMBOBOX("usage.point.cps.properties.combobox", "Combobox"),
    CPS_PROPERTIES_COMBOBOX_DESCRIPTION("usage.point.cps.properties.combobox.descr", "Combobox for CAS"),
    ;

    private String key;
    private String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
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
