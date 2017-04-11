package com.elster.us.protocolimplv2.sel;

import com.elster.us.nls.PropertyTranslationKeys;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class SELProperties implements HasDynamicProperties {

    public final static String DEVICE_TIMEZONE = "deviceTimeZone";
    public final static String TIMEZONE = "Timezone";
    public final static String RETRIES = "Retries";
    public final static String DEVICE_PWD = "Password";
    public final static String MAX_INTERVAL_RETRIEVAL_IN_DAYS = "MaxIntervalRetrievalInDays";
    public final static String LP_RECORDER = "LoadProfileRecorder"; //supported types are COI(Change-Over-Interval) or EOI(End-Of-Interval)
    public final static String LEVEL_E_PWD = "LevelEPassword";

    private final static TimeZone DEFAULT_DEVICE_TIMEZONE = TimeZone.getDefault();
    private final static TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();
    ;
    private final static int DEFAULT_RETRIES = 3;
    private final static String DEFAULT_DEVICE_PWD = "SEL";
    private final static int DEFAULT_MAX_INTERVAL_RETRIEVAL_IN_DAYS = 7;
    private final static String DEFAULT_LP_RECORDER = "COI"; //supported types are COI(Change-Over-Interval) or EOI(End-Of-Interval)
    private final static String DEFAULT_LEVEL_E_PWD = "BLONDEL";
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
        try {
            String str = properties.getTypedProperty(RETRIES);
            return Integer.parseInt(str);
        } catch (Throwable t) {
            return DEFAULT_RETRIES;
        }
    }

    public int getMaxIntervalRetrievalInDays() {
        try {
            return properties.getTypedProperty(MAX_INTERVAL_RETRIEVAL_IN_DAYS, new BigDecimal(DEFAULT_MAX_INTERVAL_RETRIEVAL_IN_DAYS)).toBigInteger().intValue();
        } catch (Throwable t) {
            return DEFAULT_MAX_INTERVAL_RETRIEVAL_IN_DAYS;
        }
    }

    public String getLPRecorder() {
        String retVal = properties.getTypedProperty(LP_RECORDER);
        if (!retVal.equalsIgnoreCase("EOI")) {
            retVal = DEFAULT_LP_RECORDER;
        } else {
            retVal = "EOI";
        }
        return retVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELProperties").append(" = {\r\n");
        sb.append("getRetries").append(" = {\r\n");
        sb.append("getTimezone").append(" = {\r\n");
        sb.append("getDeviceTimezone").append(" = {\r\n");
        sb.append("getMaxIntervalRetrievalInDays").append(" = {\r\n");
        sb.append("getLPRecorder").append(" = {\r\n");
        sb.append("getLevelERecorder").append(" = {\r\n");
        sb.append("").append(" = {\r\n");
        return sb.toString();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder(RETRIES, false, PropertyTranslationKeys.SEL_RETRIES, this.propertySpecService::bigDecimalSpec).finish(),
                UPLPropertySpecFactory.specBuilder(TIMEZONE, false, PropertyTranslationKeys.SEL_TIMEZONE, this.propertySpecService::timeZoneSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DEVICE_PWD, false, PropertyTranslationKeys.SEL_DEVICE_PWD, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(DEVICE_TIMEZONE, false, PropertyTranslationKeys.SEL_DEVICE_TIMEZONE, this.propertySpecService::timeZoneSpec).finish(),
                UPLPropertySpecFactory.specBuilder(MAX_INTERVAL_RETRIEVAL_IN_DAYS, false, PropertyTranslationKeys.SEL_MAX_INTERVAL_RETRIEVAL_IN_DAYS, this.propertySpecService::bigDecimalSpec).finish(),
                UPLPropertySpecFactory.specBuilder(LP_RECORDER, false, PropertyTranslationKeys.SEL_LP_RECORDER, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(LEVEL_E_PWD, false, PropertyTranslationKeys.SEL_LEVEL_E_PWD, this.propertySpecService::stringSpec).finish()
        );
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties.setAllProperties(properties);
    }
}