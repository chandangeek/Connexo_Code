package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.pluggable.ConfigurationInquirySupport;
import com.energyict.mdc.tasks.ConnectionTaskProperty;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 23/11/12 (9:07)
 */
public class TypedAtModemProperties extends AbstractAtModemProperties implements ConfigurationSupport, ConfigurationInquirySupport, Serializable {

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
    private Map<String, PropertySpec> propertySpecs;

    public TypedAtModemProperties() {
        this(new ArrayList<ConnectionTaskProperty>(0));
    }

    public TypedAtModemProperties(List<ConnectionTaskProperty> properties) {
        super();
        this.properties = TypedProperties.empty();
        validateAndSetProperties(properties);
    }

    protected void validateAndSetProperties(List<ConnectionTaskProperty> properties) {
        for (ConnectionTaskProperty property : properties) {
            this.properties.setProperty(property.getName(), property.getValue());
        }
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        this.ensurePropertySpecsInitialized();
        return this.propertySpecs.get(name);
    }

    private void ensurePropertySpecsInitialized() {
        if (this.propertySpecs == null) {
            Map<String, PropertySpec> temp = new HashMap<String, PropertySpec>();
            this.initializePropertySpecs(temp);
            this.propertySpecs = temp;
        }
    }

    private void initializePropertySpecs(Map<String, PropertySpec> propertySpecs) {
        propertySpecs.put(AT_MODEM_ADDRESS_SELECTOR, atModemAddressSelectorSpec());
        propertySpecs.put(AT_MODEM_POST_DIAL_COMMANDS, atModemPostDialCommandsSpec());
        propertySpecs.put(AT_CONNECT_TIMEOUT, atConnectTimeoutSpec());
        propertySpecs.put(AT_MODEM_DIAL_PREFIX, atCommandPrefixSpec());
        propertySpecs.put(AT_MODEM_GLOBAL_INIT_STRINGS, atGlobalModemInitStringSpec());
        propertySpecs.put(AT_MODEM_INIT_STRINGS, atModemInitStringSpec());
        propertySpecs.put(AT_COMMAND_TRIES, atCommandTriesSpec());
        propertySpecs.put(AT_COMMAND_TIMEOUT, atCommandTimeoutSpec());
        propertySpecs.put(PHONE_NUMBER_PROPERTY_NAME, phoneNumberSpec());
        propertySpecs.put(DELAY_AFTER_CONNECT, delayAfterConnectSpec());
        propertySpecs.put(DELAY_BEFORE_SEND, delayBeforeSendSpec());
        propertySpecs.put(DTR_TOGGLE_DELAY, dtrToggleDelaySpec());
    }

    @Override
    public boolean isRequiredProperty(String name) {
        for (PropertySpec propertySpec : getRequiredProperties()) {
            if (propertySpec.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Arrays.asList(phoneNumberSpec());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> allOptionalProperties = new ArrayList<>();
        allOptionalProperties.add(delayBeforeSendSpec());
        allOptionalProperties.add(atCommandTimeoutSpec());
        allOptionalProperties.add(atCommandTriesSpec());
        allOptionalProperties.add(atGlobalModemInitStringSpec());
        allOptionalProperties.add(atModemInitStringSpec());
        allOptionalProperties.add(atCommandPrefixSpec());
        allOptionalProperties.add(atConnectTimeoutSpec());
        allOptionalProperties.add(delayAfterConnectSpec());
        allOptionalProperties.add(atModemAddressSelectorSpec());
        allOptionalProperties.add(atModemPostDialCommandsSpec());
        allOptionalProperties.add(dtrToggleDelaySpec());
        return allOptionalProperties;
    }

    @Override
    protected String getPhoneNumber() {
        return (String) getProperty(PHONE_NUMBER_PROPERTY_NAME);
    }

    @Override
    protected String getCommandPrefix() {
        Object value = getProperty(AT_MODEM_DIAL_PREFIX);
        return value != null ? (String) value : DEFAULT_AT_MODEM_DIAL_PREFIX;
    }

    @Override
    protected TimeDuration getConnectTimeout() {
        Object value = getProperty(AT_CONNECT_TIMEOUT);
        return value != null ? (TimeDuration) value : DEFAULT_AT_CONNECT_TIMEOUT;
    }

    @Override
    protected TimeDuration getDelayAfterConnect() {
        Object value = getProperty(DELAY_AFTER_CONNECT);
        return value != null ? (TimeDuration) value : DEFAULT_DELAY_AFTER_CONNECT;
    }

    @Override
    protected TimeDuration getDelayBeforeSend() {
        Object value = getProperty(DELAY_BEFORE_SEND);
        return value != null ? (TimeDuration) value : DEFAULT_DELAY_BEFORE_SEND;
    }

    @Override
    protected TimeDuration getCommandTimeOut() {
        Object value = getProperty(AT_COMMAND_TIMEOUT);
        return value != null ? (TimeDuration) value : DEFAULT_AT_COMMAND_TIMEOUT;
    }

    @Override
    protected BigDecimal getCommandTry() {
        Object value = getProperty(AT_COMMAND_TRIES);
        return value != null ? (BigDecimal) value : DEFAULT_AT_COMMAND_TRIES;
    }

    @Override
    protected List<String> getGlobalModemInitStrings() {
        Object value = getProperty(AT_MODEM_GLOBAL_INIT_STRINGS);
        String globalInitStringSpecs = value != null ? (String) value : DEFAULT_AT_MODEM_GLOBAL_INIT_STRINGS;
        if (!globalInitStringSpecs.isEmpty()) {
            return Arrays.asList(globalInitStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    protected List<String> getModemInitStrings() {
        Object value = getProperty(AT_MODEM_INIT_STRINGS);
        String initStringSpecs = value != null ? (String) value : DEFAULT_AT_MODEM_INIT_STRINGS;
        if (!initStringSpecs.isEmpty()) {
            return Arrays.asList(initStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    protected TimeDuration getLineToggleDelay() {
        Object value = getProperty(DTR_TOGGLE_DELAY);
        return value != null ? (TimeDuration) value : DEFAULT_DTR_TOGGLE_DELAY;
    }

    @Override
    protected String getAddressSelector() {
        Object value = getProperty(AT_MODEM_ADDRESS_SELECTOR);
        return value != null ? (String) value : DEFAULT_AT_MODEM_ADDRESS_SELECTOR;
    }


    protected String getPostDialCommands() {
        Object value = getProperty(AT_MODEM_POST_DIAL_COMMANDS);
        return value != null ? (String) value : DEFAULT_AT_MODEM_POST_DIAL_COMMANDS;
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

    public static PropertySpec atModemAddressSelectorSpec() {
        return PropertySpecFactory.stringPropertySpec(AT_MODEM_ADDRESS_SELECTOR, DEFAULT_AT_MODEM_ADDRESS_SELECTOR);
    }

    public static PropertySpec atModemPostDialCommandsSpec() {
        return PropertySpecFactory.stringPropertySpec(AT_MODEM_POST_DIAL_COMMANDS, DEFAULT_AT_MODEM_POST_DIAL_COMMANDS);
    }

    public static PropertySpec atGlobalModemInitStringSpec() {
        return PropertySpecFactory.stringPropertySpec(AT_MODEM_GLOBAL_INIT_STRINGS, DEFAULT_AT_MODEM_GLOBAL_INIT_STRINGS);
    }

    public static PropertySpec atModemInitStringSpec() {
        return PropertySpecFactory.stringPropertySpec(AT_MODEM_INIT_STRINGS, DEFAULT_AT_MODEM_INIT_STRINGS);
    }

    public static PropertySpec atCommandTriesSpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(AT_COMMAND_TRIES, DEFAULT_AT_COMMAND_TRIES);
    }

    public static PropertySpec atCommandTimeoutSpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(AT_COMMAND_TIMEOUT, DEFAULT_AT_COMMAND_TIMEOUT);
    }

    public static PropertySpec delayBeforeSendSpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(DELAY_BEFORE_SEND, DEFAULT_DELAY_BEFORE_SEND);
    }

    public static PropertySpec delayAfterConnectSpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(DELAY_AFTER_CONNECT, DEFAULT_DELAY_AFTER_CONNECT);
    }

    public static PropertySpec atConnectTimeoutSpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(AT_CONNECT_TIMEOUT, DEFAULT_AT_CONNECT_TIMEOUT);
    }

    public static PropertySpec atCommandPrefixSpec() {
        return PropertySpecFactory.stringPropertySpec(AT_MODEM_DIAL_PREFIX, DEFAULT_AT_MODEM_DIAL_PREFIX);
    }

    public static PropertySpec dtrToggleDelaySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(DTR_TOGGLE_DELAY, DEFAULT_DTR_TOGGLE_DELAY);
    }

    public static PropertySpec phoneNumberSpec() {
        return PropertySpecFactory.stringPropertySpec(PHONE_NUMBER_PROPERTY_NAME);
    }
}