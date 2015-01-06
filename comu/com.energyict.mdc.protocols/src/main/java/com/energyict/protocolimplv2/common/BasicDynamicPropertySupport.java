package com.energyict.protocolimplv2.common;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 12/9/14
 * Time: 12:23 PM
 */
public class BasicDynamicPropertySupport implements HasDynamicProperties{

    public static final String TIMEOUT = "Timeout";
    public static final String TIMEZONE = "TimeZone";
    public static final String RETRIES = "Retries";
    public static final String FORCED_DELAY = "ForcedDelay";
    public static final String DELAY_AFTER_ERROR = "DelayAfterError";
    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";

    public static final TimeDuration DEFAULT_TIMEOUT = TimeDuration.seconds(10);
    public static final String DEFAULT_TIMEZONE = "GMT";
    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final TimeDuration DEFAULT_FORCED_DELAY = TimeDuration.NONE;
    public static final TimeDuration DEFAULT_DELAY_AFTER_ERROR = TimeDuration.millis(100);

    private final PropertySpecService propertySpecService;

    public BasicDynamicPropertySupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                timeOutPropertySpec(),
                timeZonePropertySpec(),
                retriesPropertySPec(),
                forceDelayPropertySpec(),
                delayAfterErrorPropertySpec()
        );
    }

    public PropertySpec delayAfterErrorPropertySpec() {
        return propertySpecService.timeDurationPropertySpec(DELAY_AFTER_ERROR, false, getDefaultDelayAfterError());
    }

    public TimeDuration getDefaultDelayAfterError() {
        return DEFAULT_DELAY_AFTER_ERROR;
    }

    public PropertySpec forceDelayPropertySpec() {
        return propertySpecService.timeDurationPropertySpec(FORCED_DELAY, false, getDefaultForcedDelay());
    }

    public TimeDuration getDefaultForcedDelay() {
        return DEFAULT_FORCED_DELAY;
    }

    public PropertySpec retriesPropertySPec() {
        return propertySpecService.bigDecimalPropertySpec(RETRIES, false, getDefaultRetries());
    }

    public BigDecimal getDefaultRetries() {
        return DEFAULT_RETRIES;
    }

    public PropertySpec timeZonePropertySpec() {
        return propertySpecService.timeZonePropertySpec(TIMEZONE, false, TimeZone.getTimeZone(DEFAULT_TIMEZONE));
    }

    public PropertySpec timeOutPropertySpec() {
        return propertySpecService.timeDurationPropertySpec(TIMEOUT, false, getDefaultTimeout());
    }

    public TimeDuration getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    @Override
    public PropertySpec getPropertySpec(String propertySpecName) {
        return getPropertySpecs().stream().filter(propertySpec -> propertySpec.getName().equals(propertySpecName)).findAny().orElse(null);
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

}