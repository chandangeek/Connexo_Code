package com.energyict.protocolimplv2.dlms.a2.properties;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class A2ConfigurationSupport extends DlmsConfigurationSupport {

    public static final BigDecimal DEFAULT_LIMIT_MAX_NR_OF_DAYS = BigDecimal.valueOf(0);

    public static final String CALLING_AP_TITLE_PROPERTY = "CallingAPTitle";
    public static final String LIMIT_MAX_NR_OF_DAYS_PROPERTY = "LimitMaxNrOfDays";
    public static final String TIME_INTERVAL_OVER_CLOCK_SYNC = "TimeIntervalOverClockSync";

    public A2ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(callingAPTitlePropertySpec());
        propertySpecs.add(limitMaxNumberOfDaysPropertySpec());
        propertySpecs.add(timeIntervalOverClockSync());
        return propertySpecs;
    }

    private PropertySpec limitMaxNumberOfDaysPropertySpec() {
        return this.bigDecimalSpec(LIMIT_MAX_NR_OF_DAYS_PROPERTY, DEFAULT_LIMIT_MAX_NR_OF_DAYS, PropertyTranslationKeys.V2_DLMS_LIMIT_MAX_NR_OF_DAYS);
    }

    private PropertySpec callingAPTitlePropertySpec() {
        return this.fixedLengthHexStringPropertySpec(CALLING_AP_TITLE_PROPERTY, 16, PropertyTranslationKeys.V2_DLMS_IDIS_CALLING_AP_TITLE);
    }

    private PropertySpec timeIntervalOverClockSync() {
        return this.booleanSpecBuilder(TIME_INTERVAL_OVER_CLOCK_SYNC, true, PropertyTranslationKeys.TIME_INTERVAL_OVER_CLOCK_SYNC);
    }

    protected PropertySpec bigDecimalSpec(String name, BigDecimal defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec fixedLengthHexStringPropertySpec(String name, int length, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey,() -> this.getPropertySpecService().hexStringSpecOfExactLength(length))
                .finish();
    }
}
