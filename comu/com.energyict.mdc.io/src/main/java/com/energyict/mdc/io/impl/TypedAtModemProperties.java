package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.time.TimeDuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 23/11/12 (9:07)
 */
public class TypedAtModemProperties implements AtModemProperties, HasDynamicProperties {

    public static final String DELAY_BEFORE_SEND = "atmodem_senddelay";         // delay to wait before we send a command
    public static final String DELAY_AFTER_CONNECT = "atmodem_delay_after_con"; // timeout to wait after a connect command has been received
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
    private static final TimeDuration DEFAULT_AT_COMMAND_TIMEOUT = new TimeDuration(5, TimeDuration.TimeUnit.SECONDS);
    private static final TimeDuration DEFAULT_DELAY_BEFORE_SEND = new TimeDuration(500, TimeDuration.TimeUnit.MILLISECONDS);
    private static final TimeDuration DEFAULT_DELAY_AFTER_CONNECT = new TimeDuration(500, TimeDuration.TimeUnit.MILLISECONDS);
    private static final TimeDuration DEFAULT_AT_CONNECT_TIMEOUT = new TimeDuration(60, TimeDuration.TimeUnit.SECONDS);
    private static final String DEFAULT_AT_MODEM_DIAL_PREFIX = "";
    private static final TimeDuration DEFAULT_DTR_TOGGLE_DELAY = new TimeDuration(2, TimeDuration.TimeUnit.SECONDS);

    private final PropertySpecService propertySpecService;
    private final List<AtPostDialCommand> postDialCommands;
    private TypedProperties properties;
    private Map<String, PropertySpec> propertySpecs;

    public TypedAtModemProperties(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        this.properties = TypedProperties.empty();
        this.postDialCommands = Collections.emptyList();
    }

    public TypedAtModemProperties(TypedProperties properties, List<AtPostDialCommand> postDialCommands, PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        this.properties = TypedProperties.copyOf(properties);
        this.postDialCommands = Collections.unmodifiableList(postDialCommands);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        this.ensurePropertySpecsInitialized();
        return this.propertySpecs.get(name);
    }

    private void ensurePropertySpecsInitialized () {
        if (this.propertySpecs == null) {
            Map<String, PropertySpec> temp = new HashMap<>();
            this.initializePropertySpecs(temp);
            this.propertySpecs = temp;
        }
    }

    private void initializePropertySpecs (Map<String, PropertySpec> propertySpecs) {
        propertySpecs.put(AT_MODEM_POST_DIAL_COMMANDS, atModemPostDialCommandsSpec(this.propertySpecService));
        propertySpecs.put(AT_MODEM_ADDRESS_SELECTOR, atModemAddressSelectorSpec(this.propertySpecService));
        propertySpecs.put(AT_CONNECT_TIMEOUT, atConnectTimeoutSpec(this.propertySpecService));
        propertySpecs.put(AT_MODEM_DIAL_PREFIX, atCommandPrefixSpec(this.propertySpecService));
        propertySpecs.put(AT_MODEM_GLOBAL_INIT_STRINGS, atGlobalModemInitStringSpec(this.propertySpecService));
        propertySpecs.put(AT_MODEM_INIT_STRINGS, atModemInitStringSpec(this.propertySpecService));
        propertySpecs.put(AT_COMMAND_TRIES, atCommandTriesSpec(this.propertySpecService));
        propertySpecs.put(AT_COMMAND_TIMEOUT, atCommandTimeoutSpec(this.propertySpecService));
        propertySpecs.put(PHONE_NUMBER_PROPERTY_NAME, phoneNumberSpec(this.propertySpecService));
        propertySpecs.put(DELAY_AFTER_CONNECT, delayAfterConnectSpec(this.propertySpecService));
        propertySpecs.put(DELAY_BEFORE_SEND, delayBeforeSendSpec(this.propertySpecService));
        propertySpecs.put(DTR_TOGGLE_DELAY, dtrToggleDelaySpec(this.propertySpecService));
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        this.ensurePropertySpecsInitialized();
        return new ArrayList<>(this.propertySpecs.values());
    }

    @Override
    public String getPhoneNumber() {
        return (String) getProperty(PHONE_NUMBER_PROPERTY_NAME);
    }

    @Override
    public String getCommandPrefix() {
        return (String) getProperty(AT_MODEM_DIAL_PREFIX);
    }

    @Override
    public TimeDuration getConnectTimeout() {
        return (TimeDuration) getProperty(AT_CONNECT_TIMEOUT);
    }

    @Override
    public TimeDuration getDelayAfterConnect() {
        return (TimeDuration) getProperty(DELAY_AFTER_CONNECT);
    }

    @Override
    public TimeDuration getDelayBeforeSend() {
        return (TimeDuration) getProperty(DELAY_BEFORE_SEND);
    }

    @Override
    public TimeDuration getCommandTimeOut() {
        return (TimeDuration) getProperty(AT_COMMAND_TIMEOUT);
    }

    @Override
    public BigDecimal getCommandTry() {
        return (BigDecimal) getProperty(AT_COMMAND_TRIES);
    }

    @Override
    public List<String> getGlobalModemInitStrings() {
        Object value = getProperty(AT_MODEM_GLOBAL_INIT_STRINGS);
        String globalInitStringSpecs = value != null ? (String) value : DEFAULT_AT_MODEM_GLOBAL_INIT_STRINGS;
        if (!globalInitStringSpecs.isEmpty()) {
            return Arrays.asList(globalInitStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getModemInitStrings() {
        String initStringSpecs = (String) getProperty(AT_MODEM_INIT_STRINGS);
        if (!initStringSpecs.isEmpty()) {
            return Arrays.asList(initStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public TimeDuration getLineToggleDelay() {
        return (TimeDuration) getProperty(DTR_TOGGLE_DELAY);
    }

    @Override
    public String getAddressSelector() {
        return (String) getProperty(AT_MODEM_ADDRESS_SELECTOR);
    }

    public List<AtPostDialCommand> getPostDialCommands() {
        return this.postDialCommands;
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

    public static PropertySpec atModemAddressSelectorSpec(PropertySpecService propertySpecService) {
        return propertySpecService.stringPropertySpec(AT_MODEM_ADDRESS_SELECTOR, false, DEFAULT_AT_MODEM_ADDRESS_SELECTOR);
    }

    public static PropertySpec atModemPostDialCommandsSpec(PropertySpecService propertySpecService) {
        return propertySpecService.stringPropertySpec(AT_MODEM_POST_DIAL_COMMANDS, false, DEFAULT_AT_MODEM_POST_DIAL_COMMANDS);
    }

    public static PropertySpec atGlobalModemInitStringSpec(PropertySpecService propertySpecService) {
        return propertySpecService.stringPropertySpec(AT_MODEM_GLOBAL_INIT_STRINGS, false, DEFAULT_AT_MODEM_GLOBAL_INIT_STRINGS);
    }

    public static PropertySpec atModemInitStringSpec(PropertySpecService propertySpecService) {
        return propertySpecService.stringPropertySpec(AT_MODEM_INIT_STRINGS, false, DEFAULT_AT_MODEM_INIT_STRINGS);
    }

    public static PropertySpec atCommandTriesSpec(PropertySpecService propertySpecService) {
        return propertySpecService.bigDecimalPropertySpec(AT_COMMAND_TRIES, false, DEFAULT_AT_COMMAND_TRIES);
    }

    public static PropertySpec atCommandTimeoutSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(AT_COMMAND_TIMEOUT, false, DEFAULT_AT_COMMAND_TIMEOUT);
    }

    public static PropertySpec delayBeforeSendSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(DELAY_BEFORE_SEND, false, DEFAULT_DELAY_BEFORE_SEND);
    }

    public static PropertySpec delayAfterConnectSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(DELAY_AFTER_CONNECT, false, DEFAULT_DELAY_AFTER_CONNECT);
    }

    public static PropertySpec atConnectTimeoutSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(AT_CONNECT_TIMEOUT, false, DEFAULT_AT_CONNECT_TIMEOUT);
    }

    public static PropertySpec atCommandPrefixSpec(PropertySpecService propertySpecService) {
        return propertySpecService.stringPropertySpec(AT_MODEM_DIAL_PREFIX, false, DEFAULT_AT_MODEM_DIAL_PREFIX);
    }

    public static PropertySpec dtrToggleDelaySpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(DTR_TOGGLE_DELAY, false, DEFAULT_DTR_TOGGLE_DELAY);
    }

    public static PropertySpec phoneNumberSpec(PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(PHONE_NUMBER_PROPERTY_NAME, true, new StringFactory());
    }

}