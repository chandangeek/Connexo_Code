package com.energyict.mdc.channels.serial.modem;

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

    public static final String DELAY_BEFORE_SEND = "atmodem_senddelay";         // delay to wait before we send a command
    public static final String DELAY_AFTER_CONNECT = "atmodem_delay_after_connect"; // timeout to wait after a connect command has been received
    public static final String AT_COMMAND_TIMEOUT = "atmodem_command_timeout";  // timeout for regular AT commands
    public static final String AT_CONNECT_TIMEOUT = "atmodem_connect_timeout";  // timeout for the AT connect command
    public static final String AT_COMMAND_TRIES = "atmodem_command_tries";      // the number of attempts a command should be send to the modem before
    public static final String AT_MODEM_GLOBAL_INIT_STRINGS = "atmodem_global_init_string";   // the initialization strings for this modem type (separated by a colon
    public static final String AT_MODEM_INIT_STRINGS = "atmodem_init_string";   // the initialization strings for this modem type (separated by a colon
    public static final String AT_MODEM_DIAL_PREFIX = "atmodem_dial_prefix";    // the prefix at command which goes between the "ATD" and the actual phoneNumber
    public static final String AT_MODEM_ADDRESS_SELECTOR = "atmodem_address_select";     // the address selector to use after a physical connect
    public static final String AT_MODEM_POST_DIAL_COMMANDS = "atmodem_postdial_command";     // the set of post dial commandos to launch after a physical connect
    public static final String DTR_TOGGLE_DELAY = "disconnect_line_toggle_delay";// the delay between DTR line toggles, which are used to disconnect the active connection.

    private static final String DEFAULT_AT_MODEM_ADDRESS_SELECTOR = "";
    private static final String DEFAULT_AT_MODEM_POST_DIAL_COMMANDS = "";
    private static final String DEFAULT_AT_MODEM_GLOBAL_INIT_STRINGS = "ATS0=0E0V1";   // Auto-answer disabled: modem will not answer incoming calls
    private static final String DEFAULT_AT_MODEM_INIT_STRINGS = "";
    private static final BigDecimal DEFAULT_AT_COMMAND_TRIES = new BigDecimal(3);
    private static final Duration DEFAULT_AT_COMMAND_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_DELAY_BEFORE_SEND = Duration.ofMillis(500);
    private static final Duration DEFAULT_DELAY_AFTER_CONNECT = Duration.ofMillis(500);
    private static final Duration DEFAULT_AT_CONNECT_TIMEOUT = Duration.ofSeconds(60);
    private static final String DEFAULT_AT_MODEM_DIAL_PREFIX = "";
    private static final Duration DEFAULT_DTR_TOGGLE_DELAY = Duration.ofSeconds(2);

    private TypedProperties properties;

    public TypedAtModemProperties() {
    }

    public TypedAtModemProperties(TypedProperties properties) {
        this.properties = properties;
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
    protected String getPhoneNumber() {
        return this.properties.getTypedProperty(PHONE_NUMBER_PROPERTY_NAME);
    }

    @Override
    protected String getCommandPrefix() {
        Object value = this.properties.getTypedProperty(AT_MODEM_DIAL_PREFIX);
        return value != null ? (String) value : DEFAULT_AT_MODEM_DIAL_PREFIX;
    }

    @Override
    protected Duration getConnectTimeout() {
        Object value = this.properties.getTypedProperty(AT_CONNECT_TIMEOUT);
        return value != null ? (Duration) value : DEFAULT_AT_CONNECT_TIMEOUT;
    }

    @Override
    protected Duration getDelayAfterConnect() {
        Object value = this.properties.getTypedProperty(DELAY_AFTER_CONNECT);
        return value != null ? (Duration) value : DEFAULT_DELAY_AFTER_CONNECT;
    }

    @Override
    protected Duration getDelayBeforeSend() {
        Object value = this.properties.getTypedProperty(DELAY_BEFORE_SEND);
        return value != null ? (Duration) value : DEFAULT_DELAY_BEFORE_SEND;
    }

    @Override
    protected Duration getCommandTimeOut() {
        Object value = this.properties.getTypedProperty(AT_COMMAND_TIMEOUT);
        return value != null ? (Duration) value : DEFAULT_AT_COMMAND_TIMEOUT;
    }

    @Override
    protected BigDecimal getCommandTry() {
        Object value = this.properties.getTypedProperty(AT_COMMAND_TRIES);
        return value != null ? (BigDecimal) value : DEFAULT_AT_COMMAND_TRIES;
    }

    @Override
    protected List<String> getGlobalModemInitStrings() {
        Object value = this.properties.getTypedProperty(AT_MODEM_GLOBAL_INIT_STRINGS);
        String globalInitStringSpecs = value != null ? (String) value : DEFAULT_AT_MODEM_GLOBAL_INIT_STRINGS;
        if (!globalInitStringSpecs.isEmpty()) {
            return Arrays.asList(globalInitStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    protected List<String> getModemInitStrings() {
        Object value = this.properties.getTypedProperty(AT_MODEM_INIT_STRINGS);
        String initStringSpecs = value != null ? (String) value : DEFAULT_AT_MODEM_INIT_STRINGS;
        if (!initStringSpecs.isEmpty()) {
            return Arrays.asList(initStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    protected Duration getLineToggleDelay() {
        Object value = this.properties.getTypedProperty(DTR_TOGGLE_DELAY);
        return value != null ? (Duration) value : DEFAULT_DTR_TOGGLE_DELAY;
    }

    @Override
    protected String getAddressSelector() {
        Object value = this.properties.getTypedProperty(AT_MODEM_ADDRESS_SELECTOR);
        return value != null ? (String) value : DEFAULT_AT_MODEM_ADDRESS_SELECTOR;
    }


    protected String getPostDialCommands() {
        Object value = this.properties.getTypedProperty(AT_MODEM_POST_DIAL_COMMANDS);
        return value != null ? (String) value : DEFAULT_AT_MODEM_POST_DIAL_COMMANDS;
    }

    protected TypedProperties getAllProperties() {
        return this.properties;
    }

    protected void setProperty(String propertyName, Object value) {
        this.properties.setProperty(propertyName, value);
    }

    public static PropertySpec atModemAddressSelectorSpec() {
        return Services.propertySpecService().stringSpec().named(AT_MODEM_ADDRESS_SELECTOR, AT_MODEM_ADDRESS_SELECTOR).describedAs(AT_MODEM_ADDRESS_SELECTOR)
                .setDefaultValue(DEFAULT_AT_MODEM_ADDRESS_SELECTOR).finish();
    }

    public static PropertySpec atModemPostDialCommandsSpec() {
        return Services.propertySpecService().stringSpec().named(AT_MODEM_POST_DIAL_COMMANDS, AT_MODEM_POST_DIAL_COMMANDS).describedAs(AT_MODEM_POST_DIAL_COMMANDS)
                .setDefaultValue(DEFAULT_AT_MODEM_POST_DIAL_COMMANDS).finish();
    }

    public static PropertySpec atGlobalModemInitStringSpec() {
        return Services.propertySpecService().stringSpec().named(AT_MODEM_GLOBAL_INIT_STRINGS, AT_MODEM_GLOBAL_INIT_STRINGS).describedAs(AT_MODEM_GLOBAL_INIT_STRINGS)
                .setDefaultValue(DEFAULT_AT_MODEM_GLOBAL_INIT_STRINGS).finish();
    }

    public static PropertySpec atModemInitStringSpec() {
        return Services.propertySpecService().stringSpec().named(AT_MODEM_INIT_STRINGS, AT_MODEM_INIT_STRINGS).describedAs(AT_MODEM_INIT_STRINGS)
                .setDefaultValue(DEFAULT_AT_MODEM_INIT_STRINGS).finish();
    }

    public static PropertySpec atCommandTriesSpec() {
        return Services.propertySpecService().bigDecimalSpec().named(AT_COMMAND_TRIES, AT_COMMAND_TRIES).describedAs(AT_COMMAND_TRIES)
                .setDefaultValue(DEFAULT_AT_COMMAND_TRIES).finish();
    }

    public static PropertySpec atCommandTimeoutSpec() {
        return Services.propertySpecService().durationSpec().named(AT_COMMAND_TIMEOUT, AT_COMMAND_TIMEOUT).describedAs(AT_COMMAND_TIMEOUT)
                .setDefaultValue(DEFAULT_AT_COMMAND_TIMEOUT).finish();
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
        return Services.propertySpecService().durationSpec().named(AT_CONNECT_TIMEOUT, AT_CONNECT_TIMEOUT).describedAs(AT_CONNECT_TIMEOUT)
                .setDefaultValue(DEFAULT_AT_COMMAND_TIMEOUT).finish();
    }

    public static PropertySpec atCommandPrefixSpec() {
        return Services.propertySpecService().stringSpec().named(AT_MODEM_DIAL_PREFIX, AT_MODEM_DIAL_PREFIX).describedAs(AT_MODEM_DIAL_PREFIX)
                .setDefaultValue(DEFAULT_AT_MODEM_DIAL_PREFIX).finish();
    }

    public static PropertySpec dtrToggleDelaySpec() {
        return Services.propertySpecService().durationSpec().named(DTR_TOGGLE_DELAY, DTR_TOGGLE_DELAY).describedAs(DTR_TOGGLE_DELAY)
                .setDefaultValue(DEFAULT_DTR_TOGGLE_DELAY).finish();
    }

    public static PropertySpec phoneNumberSpec() {
        return Services.propertySpecService().durationSpec().named(PHONE_NUMBER_PROPERTY_NAME, PHONE_NUMBER_PROPERTY_NAME).describedAs(PHONE_NUMBER_PROPERTY_NAME).finish();
    }
}