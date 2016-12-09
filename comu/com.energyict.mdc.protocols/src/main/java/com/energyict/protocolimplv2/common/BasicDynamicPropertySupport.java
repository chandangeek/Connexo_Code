package com.energyict.protocolimplv2.common;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
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
public class BasicDynamicPropertySupport implements HasDynamicProperties {

    public static final String TIMEOUT = "Timeout";
    public static final String TIMEZONE = "TimeZone";
    public static final String RETRIES = "Retries";
    public static final String FORCED_DELAY = "ForcedDelay";
    public static final String DELAY_AFTER_ERROR = "DelayAfterError";

    public static final TimeDuration DEFAULT_TIMEOUT = TimeDuration.seconds(10);
    public static final String DEFAULT_TIMEZONE = "GMT";
    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final TimeDuration DEFAULT_FORCED_DELAY = TimeDuration.NONE;
    public static final TimeDuration DEFAULT_DELAY_AFTER_ERROR = TimeDuration.millis(100);

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    public BasicDynamicPropertySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                timeOutPropertySpec(),
                timeZonePropertySpec(),
                retriesPropertySPec(),
                forcedDelayPropertySpec(),
                delayAfterErrorPropertySpec()
        );
    }

    private PropertySpec delayAfterErrorPropertySpec() {
        return propertySpecService
                .temporalAmountSpec()
                .named(DELAY_AFTER_ERROR, CommonV2TranslationKeys.DELAY_AFTER_ERROR)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(this.getDefaultDelayAfterError())
                .finish();
    }

    protected TimeDuration getDefaultDelayAfterError() {
        return DEFAULT_DELAY_AFTER_ERROR;
    }

    public PropertySpec forcedDelayPropertySpec() {
        return propertySpecService
                .temporalAmountSpec()
                .named(FORCED_DELAY, CommonV2TranslationKeys.FORCED_DELAY)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(this.getDefaultForcedDelay())
                .finish();
    }

    protected TimeDuration getDefaultForcedDelay() {
        return DEFAULT_FORCED_DELAY;
    }

    private PropertySpec retriesPropertySPec() {
        return propertySpecService
                .bigDecimalSpec()
                .named(RETRIES, CommonV2TranslationKeys.RETRIES)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(DEFAULT_RETRIES)
                .finish();
    }

    private PropertySpec timeZonePropertySpec() {
        TimeZone[] timeZones = Arrays.asList(TimeZone.getAvailableIDs()).stream().map(TimeZone::getTimeZone).toArray(TimeZone[]::new);
        PropertySpecBuilder<TimeZone> builder = this.propertySpecService
                .timezoneSpec()
                .named(TIMEZONE, CommonV2TranslationKeys.TIMEZONE)
                .fromThesaurus(this.thesaurus)
                .addValues(timeZones)
                .setDefaultValue(TimeZone.getTimeZone(DEFAULT_TIMEZONE))
                .markEditable();
        return builder.finish();
    }

    private PropertySpec timeOutPropertySpec() {
        return propertySpecService
                .temporalAmountSpec()
                .named(TIMEOUT, CommonV2TranslationKeys.TIMEOUT)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(this.getDefaultTimeout())
                .finish();
    }

    protected TimeDuration getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected PropertySpec bigDecimalSpec(TranslationKey translationKey, BigDecimal defaultValue) {
        return this.propertySpecService
                .bigDecimalSpec()
                .named(translationKey)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(defaultValue)
                .finish();
    }

    protected PropertySpec booleanSpec(TranslationKey translationKey, Boolean defaultValue) {
        return this.propertySpecService
                .booleanSpec()
                .named(translationKey)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(defaultValue)
                .finish();
    }

    protected PropertySpec stringSpec(TranslationKey translationKey, String defaultValue, String... possibleValues) {
        return this.propertySpecService
                .stringSpec()
                .named(translationKey)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(defaultValue)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
    }

}