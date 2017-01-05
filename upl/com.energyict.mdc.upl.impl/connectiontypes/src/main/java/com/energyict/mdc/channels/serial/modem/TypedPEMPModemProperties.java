package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author sva
 * @since 29/04/13 - 15:08
 */
public class TypedPEMPModemProperties extends AbstractPEMPModemProperties implements HasDynamicProperties {

    public static final String MODEM_DIAL_PREFIX = "modem_dial_prefix";         // the prefix command to use when performing the actual dial to the modem of the device
    public static final String CONNECT_TIMEOUT = "modem_connect_timeout";       // timeout for the connect command
    public static final String DELAY_AFTER_CONNECT = "modem_delay_after_connect";   // timeout to wait after a connect command has been received
    public static final String DELAY_BEFORE_SEND = "modem_senddelay";           // delay to wait before we send a command
    public static final String COMMAND_TIMEOUT = "modem_command_timeout";       // timeout for regular commands
    public static final String COMMAND_TRIES = "modem_command_tries";           // the number of attempts a command should be send to the modem before
    public static final String MODEM_INIT_STRINGS = "modem_init_string";        // the initialization strings for this modem type modem
    public static final String DTR_TOGGLE_DELAY = "disconnect_line_toggle_delay";// the delay between DTR line toggles, which are used to disconnect the active connection.
    public static final String MODEM_CONFIGURATION_KEY = "modem_configuration_key";// the PEMPModemConfiguration to use

    private static final String DEFAULT_MODEM_INIT_STRINGS = "1:0;2:0;3:0;4:10;5:0;6:5";
    private static final BigDecimal DEFAULT_COMMAND_TRIES = new BigDecimal(5);
    private static final Duration DEFAULT_COMMAND_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_DELAY_BEFORE_SEND = Duration.ofMillis(500);
    private static final Duration DEFAULT_DELAY_AFTER_CONNECT = Duration.ofMillis(500);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final String DEFAULT_MODEM_DIAL_PREFIX = "";
    private static final Duration DEFAULT_DTR_TOGGLE_DELAY = Duration.ofSeconds(2);

    private TypedProperties properties;
    private Map<String, PropertySpec> propertySpecs;

    public TypedPEMPModemProperties() {
    }

    public TypedPEMPModemProperties(TypedProperties properties) {
        super();
        this.properties = properties;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(atConnectTimeoutSpec(),
                atCommandPrefixSpec(),
                atModemInitStringSpec(),
                atCommandTriesSpec(),
                atCommandTimeoutSpec(),
                phoneNumberSpec(),
                delayAfterConnectSpec(),
                delayBeforeSendSpec(),
                dtrToggleDelaySpec(),
                modemConfigurationKeySpec());
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties = properties;
    }

    @Override
    protected String getPhoneNumber() {
        return (String) getProperty(PHONE_NUMBER_PROPERTY_NAME);
    }

    @Override
    protected String getCommandPrefix() {
        Object value = getProperty(MODEM_DIAL_PREFIX);
        return value != null ? (String) value : DEFAULT_MODEM_DIAL_PREFIX;
    }

    @Override
    protected Duration getConnectTimeout() {
        Object value = getProperty(CONNECT_TIMEOUT);
        return value != null ? (Duration) value : DEFAULT_CONNECT_TIMEOUT;
    }

    @Override
    protected Duration getDelayAfterConnect() {
        Object value = getProperty(DELAY_AFTER_CONNECT);
        return value != null ? (Duration) value : DEFAULT_DELAY_AFTER_CONNECT;
    }

    @Override
    protected Duration getDelayBeforeSend() {
        Object value = getProperty(DELAY_BEFORE_SEND);
        return value != null ? (Duration) value : DEFAULT_DELAY_BEFORE_SEND;
    }

    @Override
    protected Duration getCommandTimeOut() {
        Object value = getProperty(COMMAND_TIMEOUT);
        return value != null ? (Duration) value : DEFAULT_COMMAND_TIMEOUT;
    }

    @Override
    protected BigDecimal getCommandTry() {
        Object value = getProperty(COMMAND_TRIES);
        return value != null ? (BigDecimal) value : DEFAULT_COMMAND_TRIES;
    }

    @Override
    protected List<String> getGlobalModemInitStrings() {
        return new ArrayList<>(0);
    }

    @Override
    protected List<String> getModemInitStrings() {
        Object value = getProperty(MODEM_INIT_STRINGS);
        String initStringSpecs = value != null ? (String) value : DEFAULT_MODEM_INIT_STRINGS;
        if (!initStringSpecs.isEmpty()) {
            return Arrays.asList(initStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    protected Duration getLineToggleDelay() {
        Object value = getProperty(DTR_TOGGLE_DELAY);
        return value != null ? (Duration) value : DEFAULT_DTR_TOGGLE_DELAY;
    }

    @Override
    protected PEMPModemConfiguration getPEMPModemConfiguration() {
        return (PEMPModemConfiguration) getProperty(MODEM_CONFIGURATION_KEY);
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

    public static PropertySpec atModemInitStringSpec() {
        return UPLPropertySpecFactory.stringWithDefault(MODEM_INIT_STRINGS, false, DEFAULT_MODEM_INIT_STRINGS);
    }

    public static PropertySpec atCommandTriesSpec() {
        return UPLPropertySpecFactory.bigDecimal(COMMAND_TRIES, false, DEFAULT_COMMAND_TRIES);
    }

    public static PropertySpec atCommandTimeoutSpec() {
        return UPLPropertySpecFactory.duration(COMMAND_TIMEOUT, false, DEFAULT_COMMAND_TIMEOUT);
    }

    public static PropertySpec delayBeforeSendSpec() {
        return UPLPropertySpecFactory.duration(DELAY_BEFORE_SEND, false, DEFAULT_DELAY_BEFORE_SEND);
    }

    public static PropertySpec delayAfterConnectSpec() {
        return UPLPropertySpecFactory.duration(DELAY_AFTER_CONNECT, false, DEFAULT_DELAY_AFTER_CONNECT);
    }

    public static PropertySpec atConnectTimeoutSpec() {
        return UPLPropertySpecFactory.duration(CONNECT_TIMEOUT, false, DEFAULT_CONNECT_TIMEOUT);
    }

    public static PropertySpec atCommandPrefixSpec() {
        return UPLPropertySpecFactory.stringWithDefault(MODEM_DIAL_PREFIX, false, DEFAULT_MODEM_DIAL_PREFIX);
    }

    public static PropertySpec dtrToggleDelaySpec() {
        return UPLPropertySpecFactory.duration(DTR_TOGGLE_DELAY, false, DEFAULT_DTR_TOGGLE_DELAY);
    }

    public static PropertySpec modemConfigurationKeySpec() {
        return UPLPropertySpecFactory.string(MODEM_CONFIGURATION_KEY, true, Stream.of(PEMPModemConfiguration.values()).map(PEMPModemConfiguration::getKey).toArray(String[]::new));
    }

    public static PropertySpec phoneNumberSpec() {
        return UPLPropertySpecFactory.string(PHONE_NUMBER_PROPERTY_NAME, true);
    }
}
