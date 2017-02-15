package com.energyict.mdc.channel.serial.modemproperties;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author sva
 * @since 23/11/12 (9:07)
 */
public class TypedAtModemProperties extends AbstractAtModemProperties implements HasDynamicProperties, Serializable {

    protected static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(60);
    private static final String DEFAULT_MODEM_ADDRESS_SELECTOR = "";
    private static final String DEFAULT_MODEM_POST_DIAL_COMMANDS = "";
    private static final String DEFAULT_MODEM_GLOBAL_INIT_STRINGS = "ATS0=0E0V1";   // Auto-answer disabled: modem will not answer incoming calls
    private static final String DEFAULT_MODEM_INIT_STRINGS = "";
    private static final BigDecimal DEFAULT_COMMAND_TRIES = new BigDecimal(3);
    private static final Duration DEFAULT_COMMAND_TIMEOUT = Duration.ofSeconds(5);
    private TypedProperties properties;

    public TypedAtModemProperties() {
    }

    public TypedAtModemProperties(TypedProperties properties) {
        this.properties = properties;
    }

    public static PropertySpec atModemAddressSelectorSpec() {
        return Services.propertySpecService().stringSpec().named(MODEM_ADDRESS_SELECTOR, MODEM_ADDRESS_SELECTOR).describedAs(MODEM_ADDRESS_SELECTOR)
                .setDefaultValue(DEFAULT_MODEM_ADDRESS_SELECTOR).finish();
    }

    public static PropertySpec atModemPostDialCommandsSpec() {
        return Services.propertySpecService().stringSpec().named(MODEM_POST_DIAL_COMMANDS, MODEM_POST_DIAL_COMMANDS).describedAs(MODEM_POST_DIAL_COMMANDS)
                .setDefaultValue(DEFAULT_MODEM_POST_DIAL_COMMANDS).finish();
    }

    public static PropertySpec atGlobalModemInitStringSpec() {
        return Services.propertySpecService().stringSpec().named(MODEM_GLOBAL_INIT_STRINGS, MODEM_GLOBAL_INIT_STRINGS).describedAs(MODEM_GLOBAL_INIT_STRINGS)
                .setDefaultValue(DEFAULT_MODEM_GLOBAL_INIT_STRINGS).finish();
    }

    public static PropertySpec atModemInitStringSpec() {
        return Services.propertySpecService().stringSpec().named(MODEM_INIT_STRINGS, MODEM_INIT_STRINGS).describedAs(MODEM_INIT_STRINGS)
                .setDefaultValue(DEFAULT_MODEM_INIT_STRINGS).finish();
    }

    public static PropertySpec atCommandTriesSpec() {
        return Services.propertySpecService().bigDecimalSpec().named(COMMAND_TRIES, COMMAND_TRIES).describedAs(COMMAND_TRIES)
                .setDefaultValue(DEFAULT_COMMAND_TRIES).finish();
    }

    public static PropertySpec atCommandTimeoutSpec() {
        return Services.propertySpecService().durationSpec().named(COMMAND_TIMEOUT, COMMAND_TIMEOUT).describedAs(COMMAND_TIMEOUT)
                .setDefaultValue(DEFAULT_COMMAND_TIMEOUT).finish();
    }

    public static PropertySpec delayBeforeSendSpec() {
        return Services.propertySpecService().durationSpec().named(DELAY_BEFORE_SEND, DELAY_BEFORE_SEND).describedAs(DELAY_BEFORE_SEND)
                .setDefaultValue(DEFAULT_DELAY_BEFORE_SEND).finish();
    }

    public static PropertySpec delayAfterConnectSpec() {
        return Services.propertySpecService().durationSpec().named(DELAY_AFTER_CONNECT, DELAY_AFTER_CONNECT).describedAs(DELAY_AFTER_CONNECT)
                .setDefaultValue(DEFAULT_DELAY_AFTER_CONNECT).finish();
    }

    public static PropertySpec atConnectTimeoutSpec() {
        return Services.propertySpecService().durationSpec().named(CONNECT_TIMEOUT, CONNECT_TIMEOUT).describedAs(CONNECT_TIMEOUT)
                .setDefaultValue(DEFAULT_COMMAND_TIMEOUT).finish();
    }

    public static PropertySpec atCommandPrefixSpec() {
        return Services.propertySpecService().stringSpec().named(MODEM_DIAL_PREFIX, MODEM_DIAL_PREFIX).describedAs(MODEM_DIAL_PREFIX)
                .setDefaultValue(DEFAULT_MODEM_DIAL_PREFIX).finish();
    }

    public static PropertySpec dtrToggleDelaySpec() {
        return Services.propertySpecService().durationSpec().named(DTR_TOGGLE_DELAY, DTR_TOGGLE_DELAY).describedAs(DTR_TOGGLE_DELAY)
                .setDefaultValue(DEFAULT_DTR_TOGGLE_DELAY).finish();
    }

    public static PropertySpec phoneNumberSpec() {
        return Services.propertySpecService().durationSpec().named(PHONE_NUMBER_PROPERTY_NAME, PHONE_NUMBER_PROPERTY_NAME).describedAs(PHONE_NUMBER_PROPERTY_NAME).finish();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(phoneNumberSpec(),
                delayBeforeSendSpec(),
                atCommandTimeoutSpec(),
                atCommandTriesSpec(),
                atGlobalModemInitStringSpec(),
                atModemInitStringSpec(),
                atCommandPrefixSpec(),
                atConnectTimeoutSpec(),
                delayAfterConnectSpec(),
                atModemAddressSelectorSpec(),
                atModemPostDialCommandsSpec(),
                dtrToggleDelaySpec());
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties = properties;
    }

    @Override
    public String getPhoneNumber() {
        return this.properties.getTypedProperty(PHONE_NUMBER_PROPERTY_NAME);
    }

    @Override
    public String getCommandPrefix() {
        Object value = this.properties.getTypedProperty(MODEM_DIAL_PREFIX);
        return value != null ? (String) value : DEFAULT_MODEM_DIAL_PREFIX;
    }

    @Override
    public Duration getConnectTimeout() {
        Object value = this.properties.getTypedProperty(CONNECT_TIMEOUT);
        return value != null ? (Duration) value : DEFAULT_CONNECT_TIMEOUT;
    }

    @Override
    public Duration getDelayAfterConnect() {
        Object value = this.properties.getTypedProperty(DELAY_AFTER_CONNECT);
        return value != null ? (Duration) value : DEFAULT_DELAY_AFTER_CONNECT;
    }

    @Override
    public Duration getDelayBeforeSend() {
        Object value = this.properties.getTypedProperty(DELAY_BEFORE_SEND);
        return value != null ? (Duration) value : DEFAULT_DELAY_BEFORE_SEND;
    }

    @Override
    public Duration getCommandTimeOut() {
        Object value = this.properties.getTypedProperty(COMMAND_TIMEOUT);
        return value != null ? (Duration) value : DEFAULT_COMMAND_TIMEOUT;
    }

    @Override
    public BigDecimal getCommandTry() {
        Object value = this.properties.getTypedProperty(COMMAND_TRIES);
        return value != null ? (BigDecimal) value : DEFAULT_COMMAND_TRIES;
    }

    @Override
    public List<String> getGlobalModemInitStrings() {
        Object value = this.properties.getTypedProperty(MODEM_GLOBAL_INIT_STRINGS);
        String globalInitStringSpecs = value != null ? (String) value : DEFAULT_MODEM_GLOBAL_INIT_STRINGS;
        if (!globalInitStringSpecs.isEmpty()) {
            return Arrays.asList(globalInitStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getModemInitStrings() {
        Object value = this.properties.getTypedProperty(MODEM_INIT_STRINGS);
        String initStringSpecs = value != null ? (String) value : DEFAULT_MODEM_INIT_STRINGS;
        if (!initStringSpecs.isEmpty()) {
            return Arrays.asList(initStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Duration getLineToggleDelay() {
        Object value = this.properties.getTypedProperty(DTR_TOGGLE_DELAY);
        return value != null ? (Duration) value : DEFAULT_DTR_TOGGLE_DELAY;
    }

    @Override
    public String getAddressSelector() {
        Object value = this.properties.getTypedProperty(MODEM_ADDRESS_SELECTOR);
        return value != null ? (String) value : DEFAULT_MODEM_ADDRESS_SELECTOR;
    }

    public String getPostDialCommands() {
        Object value = this.properties.getTypedProperty(MODEM_POST_DIAL_COMMANDS);
        return value != null ? (String) value : DEFAULT_MODEM_POST_DIAL_COMMANDS;
    }

    protected TypedProperties getAllProperties() {
        return this.properties;
    }

    protected void setProperty(String propertyName, Object value) {
        this.properties.setProperty(propertyName, value);
    }
}