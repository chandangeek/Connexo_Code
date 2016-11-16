package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.TimeDuration;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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
    private static final TimeDuration DEFAULT_AT_COMMAND_TIMEOUT = new TimeDuration(5, TimeDuration.SECONDS);
    private static final TimeDuration DEFAULT_DELAY_BEFORE_SEND = new TimeDuration(500, TimeDuration.MILLISECONDS);
    private static final TimeDuration DEFAULT_DELAY_AFTER_CONNECT = new TimeDuration(500, TimeDuration.MILLISECONDS);
    private static final TimeDuration DEFAULT_AT_CONNECT_TIMEOUT = new TimeDuration(60, TimeDuration.SECONDS);
    private static final String DEFAULT_AT_MODEM_DIAL_PREFIX = "";
    private static final TimeDuration DEFAULT_DTR_TOGGLE_DELAY = new TimeDuration(2, TimeDuration.SECONDS);

    private TypedProperties properties;

    public TypedAtModemProperties() {
    }

    public TypedAtModemProperties(TypedProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
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
    public void setProperties(Properties properties) throws PropertyValidationException {
        this.properties = com.energyict.cpo.TypedProperties.copyOf(properties);
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
    protected TimeDuration getConnectTimeout() {
        Object value = this.properties.getTypedProperty(AT_CONNECT_TIMEOUT);
        return value != null ? (TimeDuration) value : DEFAULT_AT_CONNECT_TIMEOUT;
    }

    @Override
    protected TimeDuration getDelayAfterConnect() {
        Object value = this.properties.getTypedProperty(DELAY_AFTER_CONNECT);
        return value != null ? (TimeDuration) value : DEFAULT_DELAY_AFTER_CONNECT;
    }

    @Override
    protected TimeDuration getDelayBeforeSend() {
        Object value = this.properties.getTypedProperty(DELAY_BEFORE_SEND);
        return value != null ? (TimeDuration) value : DEFAULT_DELAY_BEFORE_SEND;
    }

    @Override
    protected TimeDuration getCommandTimeOut() {
        Object value = this.properties.getTypedProperty(AT_COMMAND_TIMEOUT);
        return value != null ? (TimeDuration) value : DEFAULT_AT_COMMAND_TIMEOUT;
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
    protected TimeDuration getLineToggleDelay() {
        Object value = this.properties.getTypedProperty(DTR_TOGGLE_DELAY);
        return value != null ? (TimeDuration) value : DEFAULT_DTR_TOGGLE_DELAY;
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
        return UPLPropertySpecFactory.string(AT_MODEM_ADDRESS_SELECTOR, DEFAULT_AT_MODEM_ADDRESS_SELECTOR, false);
    }

    public static PropertySpec atModemPostDialCommandsSpec() {
        return UPLPropertySpecFactory.string(AT_MODEM_POST_DIAL_COMMANDS, DEFAULT_AT_MODEM_POST_DIAL_COMMANDS, false);
    }

    public static PropertySpec atGlobalModemInitStringSpec() {
        return UPLPropertySpecFactory.string(AT_MODEM_GLOBAL_INIT_STRINGS, DEFAULT_AT_MODEM_GLOBAL_INIT_STRINGS, false);
    }

    public static PropertySpec atModemInitStringSpec() {
        return UPLPropertySpecFactory.string(AT_MODEM_INIT_STRINGS, DEFAULT_AT_MODEM_INIT_STRINGS, false);
    }

    public static PropertySpec atCommandTriesSpec() {
        return UPLPropertySpecFactory.bigDecimal(AT_COMMAND_TRIES, false, DEFAULT_AT_COMMAND_TRIES);
    }

    public static PropertySpec atCommandTimeoutSpec() {
        return UPLPropertySpecFactory.timeDuration(AT_COMMAND_TIMEOUT, DEFAULT_AT_COMMAND_TIMEOUT, false, true);
    }

    public static PropertySpec delayBeforeSendSpec() {
        return UPLPropertySpecFactory.timeDuration(DELAY_BEFORE_SEND, DEFAULT_DELAY_BEFORE_SEND, false, true);
    }

    public static PropertySpec delayAfterConnectSpec() {
        return UPLPropertySpecFactory.timeDuration(DELAY_AFTER_CONNECT, DEFAULT_DELAY_AFTER_CONNECT, false, true);
    }

    public static PropertySpec atConnectTimeoutSpec() {
        return UPLPropertySpecFactory.timeDuration(AT_CONNECT_TIMEOUT, DEFAULT_AT_CONNECT_TIMEOUT, false, true);
    }

    public static PropertySpec atCommandPrefixSpec() {
        return UPLPropertySpecFactory.string(AT_MODEM_DIAL_PREFIX, DEFAULT_AT_MODEM_DIAL_PREFIX, false);
    }

    public static PropertySpec dtrToggleDelaySpec() {
        return UPLPropertySpecFactory.timeDuration(DTR_TOGGLE_DELAY, DEFAULT_DTR_TOGGLE_DELAY, false, true);
    }

    public static PropertySpec phoneNumberSpec() {
        return UPLPropertySpecFactory.string(PHONE_NUMBER_PROPERTY_NAME, true);
    }
}