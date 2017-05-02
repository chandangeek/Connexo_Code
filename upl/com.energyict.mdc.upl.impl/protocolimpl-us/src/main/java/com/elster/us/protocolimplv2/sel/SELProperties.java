package com.elster.us.protocolimplv2.sel;

import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.elster.us.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class SELProperties implements HasDynamicProperties {

    public static final String DEVICE_TIMEZONE = "deviceTimeZone";
    public static final String TIMEZONE = "Timezone";
    public static final String RETRIES = "Retries";
    public static final String DEVICE_PWD = "Password";
    public static final String MAX_INTERVAL_RETRIEVAL_IN_DAYS = "MaxIntervalRetrievalInDays";
    public static final String LP_RECORDER = "LoadProfileRecorder"; //supported types are COI(Change-Over-Interval) or EOI(End-Of-Interval)
    public static final String LEVEL_E_PWD = "LevelEPassword";

    private static final TimeZone DEFAULT_DEVICE_TIMEZONE = TimeZone.getDefault();
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();
    ;
    private static final int DEFAULT_RETRIES = 3;
    private static final String DEFAULT_DEVICE_PWD = "SEL";
    private static final int DEFAULT_MAX_INTERVAL_RETRIEVAL_IN_DAYS = 7;
    private static final String DEFAULT_LP_RECORDER = "COI"; //supported types are COI(Change-Over-Interval) or EOI(End-Of-Interval)
    private static final String DEFAULT_LEVEL_E_PWD = "BLONDEL";
    private final PropertySpecService propertySpecService;

    private TypedProperties properties;

    public SELProperties(PropertySpecService propertySpecService) {
        this(com.energyict.protocolimpl.properties.TypedProperties.empty(), propertySpecService);
    }

    public SELProperties(TypedProperties properties, PropertySpecService propertySpecService) {
        this.properties = properties;
        this.propertySpecService = propertySpecService;
    }


    public void setAllProperties(TypedProperties other) {
        properties.setAllProperties(other);
    }

    public String getDevicePassword() {
        String retVal = properties.getTypedProperty(DEVICE_PWD);
        if (retVal == null) {
            retVal = DEFAULT_DEVICE_PWD;
        }
        return retVal;
    }

    public String getLevelEPassword() {
        String retVal = properties.getTypedProperty(LEVEL_E_PWD);
        if (retVal == null || retVal.isEmpty()) {
            retVal = DEFAULT_LEVEL_E_PWD;
        }
        return retVal;
    }

    public String getDeviceTimezone() {
        return properties.getTypedProperty(DEVICE_TIMEZONE, DEFAULT_DEVICE_TIMEZONE).getID();
    }

    public String getTimezone() {
        return properties.getTypedProperty(TIMEZONE, DEFAULT_TIMEZONE).getID();
    }

    public int getRetries() {
        return properties.getTypedProperty(RETRIES, DEFAULT_RETRIES);
    }

    public int getMaxIntervalRetrievalInDays() {
        return properties.getTypedProperty(MAX_INTERVAL_RETRIEVAL_IN_DAYS, DEFAULT_MAX_INTERVAL_RETRIEVAL_IN_DAYS);
    }

    public String getLPRecorder() {
        String retVal = properties.getTypedProperty(LP_RECORDER);
        if (!"EOI".equalsIgnoreCase(retVal)) {
            retVal = DEFAULT_LP_RECORDER;
        } else {
            retVal = "EOI";
        }
        return retVal;
    }

    @Override
    public String toString() {
        return "SELProperties" + " = {\r\n" +
                "getRetries" + " = {\r\n" +
                "getTimezone" + " = {\r\n" +
                "getDeviceTimezone" + " = {\r\n" +
                "getMaxIntervalRetrievalInDays" + " = {\r\n" +
                "getLPRecorder" + " = {\r\n" +
                "getLevelERecorder" + " = {\r\n" +
                "" + " = {\r\n";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder(RETRIES, false, PropertyTranslationKeys.SEL_RETRIES, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(TIMEZONE, false, PropertyTranslationKeys.SEL_TIMEZONE, this.propertySpecService::timeZoneSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DEVICE_PWD, false, PropertyTranslationKeys.SEL_DEVICE_PWD, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DEVICE_TIMEZONE, false, PropertyTranslationKeys.SEL_DEVICE_TIMEZONE, this.propertySpecService::timeZoneSpec).finish(),
                UPLPropertySpecFactory.specBuilder(MAX_INTERVAL_RETRIEVAL_IN_DAYS, false, PropertyTranslationKeys.SEL_MAX_INTERVAL_RETRIEVAL_IN_DAYS, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(LP_RECORDER, false, PropertyTranslationKeys.SEL_LP_RECORDER, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(LEVEL_E_PWD, false, PropertyTranslationKeys.SEL_LEVEL_E_PWD, this.propertySpecService::stringSpec).finish()
        );
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties.setAllProperties(properties);
    }
}