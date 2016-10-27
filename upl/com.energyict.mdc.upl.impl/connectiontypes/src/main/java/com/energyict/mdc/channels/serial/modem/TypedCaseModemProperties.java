package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.pluggable.ConfigurationInquirySupport;
import com.energyict.mdc.tasks.ConnectionTaskProperty;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 30/04/13 - 13:27
 */
public class TypedCaseModemProperties extends AbstractCaseModemProperties implements ConfigurationSupport, ConfigurationInquirySupport {

    public static final String MODEM_DIAL_PREFIX = "modem_dial_prefix";         // the prefix command to use when performing the actual dial to the modem of the device
    public static final String CONNECT_TIMEOUT = "modem_connect_timeout";       // timeout for the connect command
    public static final String DELAY_AFTER_CONNECT = "modem_delay_after_connect";   // timeout to wait after a connect command has been received
    public static final String DELAY_BEFORE_SEND = "modem_senddelay";           // delay to wait before we send a command
    public static final String COMMAND_TIMEOUT = "modem_command_timeout";       // timeout for regular commands
    public static final String COMMAND_TRIES = "modem_command_tries";           // the number of attempts a command should be send to the modem before
    public static final String GLOBAL_MODEM_INIT_STRINGS = "modem_global_init_string";   // the initialization strings for this modem type (separated by a colon
    public static final String MODEM_INIT_STRINGS = "modem_init_string";        // the initialization strings for this modem type modem
    public static final String MODEM_ADDRESS_SELECTOR = "modem_address_select"; // the address selector to use after a physical connect
    public static final String DTR_TOGGLE_DELAY = "disconnect_line_toggle_delay";// the delay between DTR line toggles, which are used to disconnect the active connection.

    private static final String DEFAULT_GLOBAL_MODEM_INIT_STRINGS = "";
    private static final String DEFAULT_MODEM_INIT_STRINGS = "";
    private static final BigDecimal DEFAULT_COMMAND_TRIES = new BigDecimal(5);
    private static final TimeDuration DEFAULT_COMMAND_TIMEOUT = new TimeDuration(10, TimeDuration.SECONDS);
    private static final TimeDuration DEFAULT_DELAY_BEFORE_SEND = new TimeDuration(500, TimeDuration.MILLISECONDS);
    private static final TimeDuration DEFAULT_DELAY_AFTER_CONNECT = new TimeDuration(500, TimeDuration.MILLISECONDS);
    private static final TimeDuration DEFAULT_CONNECT_TIMEOUT = new TimeDuration(30, TimeDuration.SECONDS);
    private static final String DEFAULT_MODEM_DIAL_PREFIX = "";
    private static final String DEFAULT_MODEM_ADDRESS_SELECTOR = "";
    private static final TimeDuration DEFAULT_DTR_TOGGLE_DELAY = new TimeDuration(2, TimeDuration.SECONDS);

    private TypedProperties properties;
    private Map<String, PropertySpec> propertySpecs;

    public TypedCaseModemProperties() {
        this(new ArrayList<ConnectionTaskProperty>(0));
    }

    public TypedCaseModemProperties(List<ConnectionTaskProperty> properties) {
        super();
        this.properties = TypedProperties.empty();
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
            Map<String, PropertySpec> temp = new HashMap<>();
            this.initializePropertySpecs(temp);
            this.propertySpecs = temp;
        }
    }

    private void initializePropertySpecs(Map<String, PropertySpec> propertySpecs) {
        propertySpecs.put(MODEM_ADDRESS_SELECTOR, modemAddressSelectorSpec());
        propertySpecs.put(CONNECT_TIMEOUT, atConnectTimeoutSpec());
        propertySpecs.put(MODEM_DIAL_PREFIX, atCommandPrefixSpec());
        propertySpecs.put(GLOBAL_MODEM_INIT_STRINGS, atGlobalModemInitStringSpec());
        propertySpecs.put(MODEM_INIT_STRINGS, atModemInitStringSpec());
        propertySpecs.put(COMMAND_TRIES, atCommandTriesSpec());
        propertySpecs.put(COMMAND_TIMEOUT, atCommandTimeoutSpec());
        propertySpecs.put(PHONE_NUMBER_PROPERTY_NAME, phoneNumberSpec());
        propertySpecs.put(DELAY_AFTER_CONNECT, delayAfterConnectSpec());
        propertySpecs.put(DELAY_BEFORE_SEND, delayBeforeSendSpec());
        propertySpecs.put(DTR_TOGGLE_DELAY, dtrToggleDelaySpec());
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return PHONE_NUMBER_PROPERTY_NAME.equals(name);
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
        allOptionalProperties.add(modemAddressSelectorSpec());
        allOptionalProperties.add(dtrToggleDelaySpec());
        return allOptionalProperties;
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
    protected TimeDuration getConnectTimeout() {
        Object value = getProperty(CONNECT_TIMEOUT);
        return value != null ? (TimeDuration) value : DEFAULT_CONNECT_TIMEOUT;
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
        Object value = getProperty(COMMAND_TIMEOUT);
        return value != null ? (TimeDuration) value : DEFAULT_COMMAND_TIMEOUT;
    }

    @Override
    protected BigDecimal getCommandTry() {
        Object value = getProperty(COMMAND_TRIES);
        return value != null ? (BigDecimal) value : DEFAULT_COMMAND_TRIES;
    }

    @Override
    protected List<String> getGlobalModemInitStrings() {
        Object value = getProperty(GLOBAL_MODEM_INIT_STRINGS);
        String globalInitStringSpecs = value != null ? (String) value : DEFAULT_GLOBAL_MODEM_INIT_STRINGS;
        if (!globalInitStringSpecs.isEmpty()) {
            return Arrays.asList(globalInitStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
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
    protected String getAddressSelector() {
        Object value = getProperty(MODEM_ADDRESS_SELECTOR);
        return value != null ? (String) value : DEFAULT_MODEM_ADDRESS_SELECTOR;
    }

    @Override
    protected TimeDuration getLineToggleDelay() {
        Object value = getProperty(DTR_TOGGLE_DELAY);
        return value != null ? (TimeDuration) value : DEFAULT_DTR_TOGGLE_DELAY;
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

    public static PropertySpec atGlobalModemInitStringSpec() {
        return PropertySpecFactory.stringPropertySpec(GLOBAL_MODEM_INIT_STRINGS, DEFAULT_GLOBAL_MODEM_INIT_STRINGS);
    }

    public static PropertySpec atModemInitStringSpec() {
        return PropertySpecFactory.stringPropertySpec(MODEM_INIT_STRINGS, DEFAULT_MODEM_INIT_STRINGS);
    }

    public static PropertySpec atCommandTriesSpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(COMMAND_TRIES, DEFAULT_COMMAND_TRIES);
    }

    public static PropertySpec atCommandTimeoutSpec() {
        return PropertySpecFactory.timeDurationPropertySpec(COMMAND_TIMEOUT, DEFAULT_COMMAND_TIMEOUT);
    }

    public static PropertySpec delayBeforeSendSpec() {
        return PropertySpecFactory.timeDurationPropertySpec(DELAY_BEFORE_SEND, DEFAULT_DELAY_BEFORE_SEND);
    }

    public static PropertySpec delayAfterConnectSpec() {
        return PropertySpecFactory.timeDurationPropertySpec(DELAY_AFTER_CONNECT, DEFAULT_DELAY_AFTER_CONNECT);
    }

    public static PropertySpec atConnectTimeoutSpec() {
        return PropertySpecFactory.timeDurationPropertySpec(CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
    }

    public static PropertySpec atCommandPrefixSpec() {
        return PropertySpecFactory.stringPropertySpec(MODEM_DIAL_PREFIX, DEFAULT_MODEM_DIAL_PREFIX);
    }

    public static PropertySpec dtrToggleDelaySpec() {
        return PropertySpecFactory.timeDurationPropertySpec(DTR_TOGGLE_DELAY, DEFAULT_DTR_TOGGLE_DELAY);
    }

    public static PropertySpec modemAddressSelectorSpec() {
        return PropertySpecFactory.stringPropertySpec(MODEM_ADDRESS_SELECTOR, DEFAULT_MODEM_ADDRESS_SELECTOR);
    }

    public static PropertySpec phoneNumberSpec() {
        return PropertySpecFactory.stringPropertySpec(PHONE_NUMBER_PROPERTY_NAME);
    }
}
