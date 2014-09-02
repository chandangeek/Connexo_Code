package com.energyict.protocols.mdc.channels.serial.modem;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.protocols.mdc.services.impl.Bus;

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
public class TypedCaseModemProperties extends AbstractCaseModemProperties implements HasDynamicProperties {

    public static final String MODEM_DIAL_PREFIX = "modem_dial_prefix";         // the prefix command to use when performing the actual dial to the modem of the device
    public static final String CONNECT_TIMEOUT = "modem_connect_timeout";       // timeout for the connect command
    public static final String DELAY_AFTER_CONNECT = "modem_delay_after_con";   // timeout to wait after a connect command has been received
    public static final String DELAY_BEFORE_SEND = "modem_senddelay";           // delay to wait before we send a command
    public static final String COMMAND_TIMEOUT = "modem_command_timeout";       // timeout for regular commands
    public static final String COMMAND_TRIES = "modem_command_tries";           // the number of attempts a command should be send to the modem before
    public static final String MODEM_INIT_STRINGS = "modem_init_string";        // the initialization strings for this modem type modem
    public static final String MODEM_ADDRESS_SELECTOR = "modem_address_select"; // the address selector to use after a physical connect
    public static final String DTR_TOGGLE_DELAY = "disconnect_line_toggle_delay";// the delay between DTR line toggles, which are used to disconnect the active connection.

    private static final String DEFAULT_MODEM_INIT_STRINGS = "1:0,2:0,3:0,4:10,5:0,6:5";
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
        this(new ArrayList<ConnectionProperty>(0));
    }

    public TypedCaseModemProperties(List<ConnectionProperty> properties) {
        super();
        this.properties = TypedProperties.empty();
        for (ConnectionProperty property : properties) {
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
        propertySpecs.put(MODEM_INIT_STRINGS, atModemInitStringSpec());
        propertySpecs.put(COMMAND_TRIES, atCommandTriesSpec());
        propertySpecs.put(COMMAND_TIMEOUT, atCommandTimeoutSpec());
        propertySpecs.put(PHONE_NUMBER_PROPERTY_NAME, phoneNumberSpec());
        propertySpecs.put(DELAY_AFTER_CONNECT, delayAfterConnectSpec());
        propertySpecs.put(DELAY_BEFORE_SEND, delayBeforeSendSpec());
        propertySpecs.put(DTR_TOGGLE_DELAY, dtrToggleDelaySpec());
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        this.ensurePropertySpecsInitialized();
        return new ArrayList<>(this.propertySpecs.values());
    }

    @Override
    protected String getPhoneNumber() {
        return (String) getProperty(PHONE_NUMBER_PROPERTY_NAME);
    }

    @Override
    protected String getCommandPrefix() {
        return (String) getProperty(MODEM_DIAL_PREFIX);
    }

    @Override
    protected TimeDuration getConnectTimeout() {
        return (TimeDuration) getProperty(CONNECT_TIMEOUT);
    }

    @Override
    protected TimeDuration getDelayAfterConnect() {
        return (TimeDuration) getProperty(DELAY_AFTER_CONNECT);
    }

    @Override
    protected TimeDuration getDelayBeforeSend() {
        return (TimeDuration) getProperty(DELAY_BEFORE_SEND);
    }

    @Override
    protected TimeDuration getCommandTimeOut() {
        return (TimeDuration) getProperty(COMMAND_TIMEOUT);
    }

    @Override
    protected BigDecimal getCommandTry() {
        return (BigDecimal) getProperty(COMMAND_TRIES);
    }

    @Override
    protected List<String> getModemInitStrings() {
        String initStringSpecs = (String) getProperty(MODEM_INIT_STRINGS);
        if (!initStringSpecs.isEmpty()) {
            return Arrays.asList(initStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    protected String getAddressSelector() {
        return (String) getProperty(MODEM_ADDRESS_SELECTOR);
    }

    @Override
    protected TimeDuration getLineToggleDelay() {
        return (TimeDuration) getProperty(DTR_TOGGLE_DELAY);
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
        return Bus.getPropertySpecService().stringPropertySpec(MODEM_INIT_STRINGS, false, DEFAULT_MODEM_INIT_STRINGS);
    }

    public static PropertySpec atCommandTriesSpec() {
        return Bus.getPropertySpecService().bigDecimalPropertySpec(COMMAND_TRIES, false, DEFAULT_COMMAND_TRIES);
    }

    public static PropertySpec atCommandTimeoutSpec() {
        return Bus.getPropertySpecService().timeDurationPropertySpec(COMMAND_TIMEOUT, false, DEFAULT_COMMAND_TIMEOUT);
    }

    public static PropertySpec delayBeforeSendSpec() {
        return Bus.getPropertySpecService().timeDurationPropertySpec(DELAY_BEFORE_SEND, false, DEFAULT_DELAY_BEFORE_SEND);
    }

    public static PropertySpec delayAfterConnectSpec() {
        return Bus.getPropertySpecService().timeDurationPropertySpec(DELAY_AFTER_CONNECT, false, DEFAULT_DELAY_AFTER_CONNECT);
    }

    public static PropertySpec atConnectTimeoutSpec() {
        return Bus.getPropertySpecService().timeDurationPropertySpec(CONNECT_TIMEOUT, false, DEFAULT_CONNECT_TIMEOUT);
    }

    public static PropertySpec atCommandPrefixSpec() {
        return Bus.getPropertySpecService().stringPropertySpec(MODEM_DIAL_PREFIX, false, DEFAULT_MODEM_DIAL_PREFIX);
    }

    public static PropertySpec dtrToggleDelaySpec() {
        return Bus.getPropertySpecService().timeDurationPropertySpec(DTR_TOGGLE_DELAY, false, DEFAULT_DTR_TOGGLE_DELAY);
    }

    public static PropertySpec modemAddressSelectorSpec() {
        return Bus.getPropertySpecService().stringPropertySpec(MODEM_ADDRESS_SELECTOR, false, DEFAULT_MODEM_ADDRESS_SELECTOR);
    }

    public static PropertySpec phoneNumberSpec() {
        return Bus.getPropertySpecService().basicPropertySpec(PHONE_NUMBER_PROPERTY_NAME, true, new StringFactory());
    }

}