package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.channel.serial.modemproperties.AbstractPaknetModemProperties;
import com.energyict.mdc.channels.nls.PropertyTranslationKeys;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author sva
 * @since 18/03/13 - 16:33
 */
public class TypedPaknetModemProperties extends AbstractPaknetModemProperties implements HasDynamicProperties {

    private static final String DEFAULT_MODEM_INIT_STRINGS = "1:0;2:0;3:0;4:10;5:0;6:5";
    private static final BigDecimal DEFAULT_COMMAND_TRIES = new BigDecimal(5);
    private static final Duration DEFAULT_COMMAND_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private final PropertySpecService propertySpecService;
    private TypedProperties properties;

    public TypedPaknetModemProperties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public TypedPaknetModemProperties(TypedProperties properties, PropertySpecService propertySpecService) {
        super();
        this.properties = properties;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                phoneNumberSpec(),
                delayBeforeSendSpec(),
                atCommandTimeoutSpec(),
                atCommandTriesSpec(),
                atModemInitStringSpec(),
                atCommandPrefixSpec(),
                atConnectTimeoutSpec(),
                dtrToggleDelaySpec(),
                delayAfterConnectSpec()
        );
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties = properties;
    }

    @Override
    public String getPhoneNumber() {
        return (String) getProperty(PHONE_NUMBER_PROPERTY_NAME);
    }

    @Override
    public String getCommandPrefix() {
        Object value = getProperty(MODEM_DIAL_PREFIX);
        return value != null ? (String) value : DEFAULT_MODEM_DIAL_PREFIX;
    }

    @Override
    public Duration getConnectTimeout() {
        Object value = getProperty(CONNECT_TIMEOUT);
        return value != null ? (Duration) value : DEFAULT_CONNECT_TIMEOUT;
    }

    @Override
    public Duration getDelayAfterConnect() {
        Object value = getProperty(DELAY_AFTER_CONNECT);
        return value != null ? (Duration) value : DEFAULT_DELAY_AFTER_CONNECT;
    }

    @Override
    public Duration getDelayBeforeSend() {
        Object value = getProperty(DELAY_BEFORE_SEND);
        return value != null ? (Duration) value : DEFAULT_DELAY_BEFORE_SEND;
    }

    @Override
    public Duration getCommandTimeOut() {
        Object value = getProperty(COMMAND_TIMEOUT);
        return value != null ? (Duration) value : DEFAULT_COMMAND_TIMEOUT;
    }

    @Override
    public BigDecimal getCommandTry() {
        Object value = getProperty(COMMAND_TRIES);
        return value != null ? (BigDecimal) value : DEFAULT_COMMAND_TRIES;
    }

    @Override
    public List<String> getGlobalModemInitStrings() {
        return new ArrayList<>(0);
    }

    @Override
    public List<String> getModemInitStrings() {
        Object value = getProperty(MODEM_INIT_STRINGS);
        String initStringSpecs = value != null ? (String) value : DEFAULT_MODEM_INIT_STRINGS;
        if (!initStringSpecs.isEmpty()) {
            return Arrays.asList(initStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Duration getLineToggleDelay() {
        Object value = getProperty(DTR_TOGGLE_DELAY);
        return value != null ? (Duration) value : DEFAULT_DTR_TOGGLE_DELAY;
    }

    protected TypedProperties getAllProperties() {
        return this.properties;
    }

    protected Object getProperty(String propertyName) {
        return this.getAllProperties().getProperty(propertyName);
    }

    protected void setProperty(String propertyName, Object value) {
        this.properties.setProperty(propertyName, value);
    }

    public PropertySpec atModemInitStringSpec() {
        return this.stringWithDefaultSpec(MODEM_INIT_STRINGS, PropertyTranslationKeys.SERIAL_MODEM_INIT_STRINGS, false, DEFAULT_MODEM_INIT_STRINGS);
    }

    public PropertySpec atCommandTriesSpec() {
        return this.bigDecimalSpec(COMMAND_TRIES, PropertyTranslationKeys.SERIAL_MODEM_COMMAND_TRIES, false, DEFAULT_COMMAND_TRIES);
    }

    public PropertySpec atCommandTimeoutSpec() {
        return this.durationSpec(COMMAND_TIMEOUT, PropertyTranslationKeys.SERIAL_MODEM_COMMAND_TIMEOUT, false, DEFAULT_COMMAND_TIMEOUT);
    }

    public PropertySpec delayBeforeSendSpec() {
        return this.durationSpec(DELAY_BEFORE_SEND, PropertyTranslationKeys.SERIAL_MODEM_DELAY_BEFORE_SEND, false, DEFAULT_DELAY_BEFORE_SEND);
    }

    public PropertySpec delayAfterConnectSpec() {
        return this.durationSpec(DELAY_AFTER_CONNECT, PropertyTranslationKeys.SERIAL_MODEM_DELAY_AFTER_CONNECT, false, DEFAULT_DELAY_AFTER_CONNECT);
    }

    public PropertySpec atConnectTimeoutSpec() {
        return this.durationSpec(CONNECT_TIMEOUT, PropertyTranslationKeys.SERIAL_MODEM_CONNECT_TIMEOUT, false, DEFAULT_CONNECT_TIMEOUT);
    }

    public PropertySpec atCommandPrefixSpec() {
        return this.stringWithDefaultSpec(MODEM_DIAL_PREFIX, PropertyTranslationKeys.SERIAL_MODEM_DIAL_PREFIX, false, DEFAULT_MODEM_DIAL_PREFIX);
    }

    public PropertySpec dtrToggleDelaySpec() {
        return this.durationSpec(DTR_TOGGLE_DELAY, PropertyTranslationKeys.SERIAL_MODEM_DTR_TOGGLE_DELAY, false, DEFAULT_DTR_TOGGLE_DELAY);
    }

    public PropertySpec phoneNumberSpec() {
        return UPLPropertySpecFactory.specBuilder(PHONE_NUMBER_PROPERTY_NAME, false, PropertyTranslationKeys.SERIAL_MODEM_PHONE_NUMBER_PROPERTY, this.propertySpecService::stringSpec).finish();
    }

    private PropertySpec durationSpec(String name, TranslationKey translationKey, boolean required, Duration defaultValue) {
        PropertySpecBuilder<Duration> durationPropertySpecBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, this.propertySpecService::durationSpec);
        durationPropertySpecBuilder.setDefaultValue(defaultValue);
        return durationPropertySpecBuilder.finish();
    }

    private PropertySpec bigDecimalSpec(String name, TranslationKey translationKey, boolean required, BigDecimal defaultValue) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, this.propertySpecService::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        return specBuilder.finish();
    }

    private PropertySpec stringWithDefaultSpec(String name, TranslationKey translationKey, boolean required, String defaultValue, String... validValues) {
        PropertySpecBuilder<String> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, this.propertySpecService::stringSpec);
        specBuilder.setDefaultValue(defaultValue);
        specBuilder.addValues(validValues);
        if (validValues.length > 0) {
            specBuilder.markExhaustive();
        }
        return specBuilder.finish();
    }
}