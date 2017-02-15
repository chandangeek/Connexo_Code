package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.channels.nls.PropertyTranslationKeys;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.channels.serial.modemproperties.AbstractCaseModemProperties;
import com.energyict.mdc.channels.serial.modemproperties.AtModemComponent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 30/04/13 - 13:27
 */
public class TypedCaseModemProperties extends AbstractCaseModemProperties implements HasDynamicProperties {

    private static final String DEFAULT_GLOBAL_MODEM_INIT_STRINGS = "";
    private static final String DEFAULT_MODEM_INIT_STRINGS = "";
    private static final BigDecimal DEFAULT_COMMAND_TRIES = new BigDecimal(5);
    private static final Duration DEFAULT_COMMAND_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final String DEFAULT_MODEM_ADDRESS_SELECTOR = "";
    private final PropertySpecService propertySpecService;
    private TypedProperties properties;
    private Map<String, PropertySpec> propertySpecs;

    public TypedCaseModemProperties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public TypedCaseModemProperties(TypedProperties properties, PropertySpecService propertySpecService) {
        super();
        this.properties = properties;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                modemAddressSelectorSpec(),
                atConnectTimeoutSpec(),
                atCommandPrefixSpec(),
                atGlobalModemInitStringSpec(),
                atModemInitStringSpec(),
                atCommandTriesSpec(),
                atCommandTimeoutSpec(),
                phoneNumberSpec(),
                delayAfterConnectSpec(),
                delayBeforeSendSpec(),
                dtrToggleDelaySpec());
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties = properties;
    }

    private void ensurePropertySpecsInitialized() {
        if (this.propertySpecs == null) {
            Map<String, PropertySpec> temp = new HashMap<>();
            this.initializePropertySpecs(temp);
            this.propertySpecs = temp;
        }
    }

    private void initializePropertySpecs(Map<String, PropertySpec> propertySpecs) {
        propertySpecs.put(MODEM_ADDRESS_SELECTOR, modemAddressSelectorSpec());
        propertySpecs.put(CONNECT_TIMEOUT, atConnectTimeoutSpec());
        propertySpecs.put(MODEM_DIAL_PREFIX, atCommandPrefixSpec());
        propertySpecs.put(MODEM_GLOBAL_INIT_STRINGS, atGlobalModemInitStringSpec());
        propertySpecs.put(MODEM_INIT_STRINGS, atModemInitStringSpec());
        propertySpecs.put(COMMAND_TRIES, atCommandTriesSpec());
        propertySpecs.put(COMMAND_TIMEOUT, atCommandTimeoutSpec());
        propertySpecs.put(PHONE_NUMBER_PROPERTY_NAME, phoneNumberSpec());
        propertySpecs.put(DELAY_AFTER_CONNECT, delayAfterConnectSpec());
        propertySpecs.put(DELAY_BEFORE_SEND, delayBeforeSendSpec());
        propertySpecs.put(DTR_TOGGLE_DELAY, dtrToggleDelaySpec());
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
        Object value = getProperty(MODEM_GLOBAL_INIT_STRINGS);
        String globalInitStringSpecs = value != null ? (String) value : DEFAULT_GLOBAL_MODEM_INIT_STRINGS;
        if (!globalInitStringSpecs.isEmpty()) {
            return Arrays.asList(globalInitStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
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
    public String getAddressSelector() {
        Object value = getProperty(MODEM_ADDRESS_SELECTOR);
        return value != null ? (String) value : DEFAULT_MODEM_ADDRESS_SELECTOR;
    }

    @Override
    public Duration getLineToggleDelay() {
        Object value = getProperty(DTR_TOGGLE_DELAY);
        return value != null ? (Duration) value : DEFAULT_DTR_TOGGLE_DELAY;
    }

    public TypedProperties getAllProperties() {
        return this.properties;
    }

    public Object getProperty(String propertyName) {
        return this.getAllProperties().getProperty(propertyName);
    }

    public void setProperty(String propertyName, Object value) {
        this.properties.setProperty(propertyName, value);
    }

    public PropertySpec atGlobalModemInitStringSpec() {
        return this.stringWithDefaultSpec(MODEM_GLOBAL_INIT_STRINGS, PropertyTranslationKeys.SERIAL_MODEM_GLOBAL_INIT_STRINGS ,false, DEFAULT_GLOBAL_MODEM_INIT_STRINGS);
    }

    public PropertySpec atModemInitStringSpec() {
        return this.stringWithDefaultSpec(MODEM_INIT_STRINGS, PropertyTranslationKeys.SERIAL_MODEM_INIT_STRINGS, false, DEFAULT_MODEM_INIT_STRINGS);
    }

    public PropertySpec atCommandTriesSpec() {
        return this.bigDecimalSpec(COMMAND_TRIES, PropertyTranslationKeys.SERIAL_MODEM_COMMAND_TRIES , false, DEFAULT_COMMAND_TRIES);
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

    public PropertySpec modemAddressSelectorSpec() {
        return this.stringWithDefaultSpec(MODEM_ADDRESS_SELECTOR, PropertyTranslationKeys.SERIAL_MODEM_ADDRESS_SELECTOR, false, DEFAULT_MODEM_ADDRESS_SELECTOR);
    }

    public PropertySpec phoneNumberSpec() {
        return UPLPropertySpecFactory.specBuilder(PHONE_NUMBER_PROPERTY_NAME, true, PropertyTranslationKeys.SERIAL_MODEM_PHONE_NUMBER_PROPERTY, this.propertySpecService::stringSpec).finish();
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

    private PropertySpec bigDecimalSpec(String name, TranslationKey translationKey, boolean required, BigDecimal defaultValue) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, this.propertySpecService::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        return specBuilder.finish();
    }

    private PropertySpec durationSpec(String name, TranslationKey translationKey, boolean required, Duration defaultValue) {
        PropertySpecBuilder<Duration> durationPropertySpecBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, this.propertySpecService::durationSpec);
        durationPropertySpecBuilder.setDefaultValue(defaultValue);
        return durationPropertySpecBuilder.finish();
    }
}
