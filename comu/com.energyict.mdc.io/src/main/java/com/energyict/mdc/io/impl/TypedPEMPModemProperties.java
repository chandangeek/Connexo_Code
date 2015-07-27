package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.PEMPModemConfiguration;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.time.TimeDuration;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author sva
 * @since 29/04/13 - 15:08
 */
public class TypedPEMPModemProperties implements PEMPModemProperties, HasDynamicProperties {

    public static final String MODEM_DIAL_PREFIX = "modem_dial_prefix";         // the prefix command to use when performing the actual dial to the modem of the device
    public static final String CONNECT_TIMEOUT = "modem_connect_timeout";       // timeout for the connect command
    public static final String DELAY_AFTER_CONNECT = "modem_delay_after_con";   // timeout to wait after a connect command has been received
    public static final String DELAY_BEFORE_SEND = "modem_senddelay";           // delay to wait before we send a command
    public static final String COMMAND_TIMEOUT = "modem_command_timeout";       // timeout for regular commands
    public static final String COMMAND_TRIES = "modem_command_tries";           // the number of attempts a command should be send to the modem before
    public static final String GLOBAL_MODEM_INIT_STRINGS = "modem_global_init_string";   // the initialization strings for this modem type (separated by a colon
    public static final String MODEM_INIT_STRINGS = "modem_init_string";        // the initialization strings for this modem type modem
    public static final String DTR_TOGGLE_DELAY = "disconnect_line_toggle_delay";// the delay between DTR line toggles, which are used to disconnect the active connection.
    public static final String MODEM_CONFIGURATION_KEY = "modem_configuration_key";// the PEMPModemConfiguration to use

    private static final String DEFAULT_GLOBAL_MODEM_INIT_STRINGS = "";
    private static final String DEFAULT_MODEM_INIT_STRINGS = "";
    private static final BigDecimal DEFAULT_COMMAND_TRIES = new BigDecimal(5);
    private static final TimeDuration DEFAULT_COMMAND_TIMEOUT = new TimeDuration(10, TimeDuration.TimeUnit.SECONDS);
    private static final TimeDuration DEFAULT_DELAY_BEFORE_SEND = new TimeDuration(500, TimeDuration.TimeUnit.MILLISECONDS);
    private static final TimeDuration DEFAULT_DELAY_AFTER_CONNECT = new TimeDuration(500, TimeDuration.TimeUnit.MILLISECONDS);
    private static final TimeDuration DEFAULT_CONNECT_TIMEOUT = new TimeDuration(30, TimeDuration.TimeUnit.SECONDS);
    private static final String DEFAULT_MODEM_DIAL_PREFIX = "";
    private static final TimeDuration DEFAULT_DTR_TOGGLE_DELAY = new TimeDuration(2, TimeDuration.TimeUnit.SECONDS);

    private final PropertySpecService propertySpecService;
    private TypedProperties properties;
    private Map<String, PropertySpec> propertySpecs;

    public TypedPEMPModemProperties(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        this.properties = TypedProperties.empty();
    }

    public TypedPEMPModemProperties(TypedProperties properties, PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        this.validateAndSetProperties(properties);
    }

    protected void validateAndSetProperties(TypedProperties properties) {
        this.properties = TypedProperties.empty();
        for (String propertyName : properties.propertyNames()) {
            switch (propertyName) {
                case MODEM_CONFIGURATION_KEY:
                    this.properties.setProperty(propertyName, PEMPModemConfiguration.getPEMPModemConfiguration((String) properties.getProperty(propertyName)));
                    break;
                default:
                    this.properties.setProperty(propertyName, properties.getProperty(propertyName));
                    break;
            }
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
        propertySpecs.put(CONNECT_TIMEOUT, atConnectTimeoutSpec(this.propertySpecService));
        propertySpecs.put(MODEM_DIAL_PREFIX, atCommandPrefixSpec(this.propertySpecService));
        propertySpecs.put(GLOBAL_MODEM_INIT_STRINGS, atGlobalModemInitStringSpec(this.propertySpecService));
        propertySpecs.put(MODEM_INIT_STRINGS, atModemInitStringSpec(this.propertySpecService));
        propertySpecs.put(COMMAND_TRIES, atCommandTriesSpec(this.propertySpecService));
        propertySpecs.put(COMMAND_TIMEOUT, atCommandTimeoutSpec(this.propertySpecService));
        propertySpecs.put(PHONE_NUMBER_PROPERTY_NAME, phoneNumberSpec(this.propertySpecService));
        propertySpecs.put(DELAY_AFTER_CONNECT, delayAfterConnectSpec(this.propertySpecService));
        propertySpecs.put(DELAY_BEFORE_SEND, delayBeforeSendSpec(this.propertySpecService));
        propertySpecs.put(DTR_TOGGLE_DELAY, dtrToggleDelaySpec(this.propertySpecService));
        propertySpecs.put(MODEM_CONFIGURATION_KEY, modemConfigurationKeySpec(this.propertySpecService));
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
        return (String) getProperty(MODEM_DIAL_PREFIX);
    }

    @Override
    public TimeDuration getConnectTimeout() {
        return (TimeDuration) getProperty(CONNECT_TIMEOUT);
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
        return (TimeDuration) getProperty(COMMAND_TIMEOUT);
    }

    @Override
    public BigDecimal getCommandTry() {
        return (BigDecimal) getProperty(COMMAND_TRIES);
    }

    @Override
    public List<String> getModemInitStrings() {
        List<String> modemInitStringList = new ArrayList<>();
        modemInitStringList.add((String) getProperty(MODEM_INIT_STRINGS));
        return modemInitStringList;
    }

    @Override
    public List<String> getGlobalModemInitStrings() {
        String globalInitStringSpecs = (String) getProperty(GLOBAL_MODEM_INIT_STRINGS);
        if (!globalInitStringSpecs.isEmpty()) {
            return Arrays.asList(globalInitStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public TimeDuration getLineToggleDelay() {
        return (TimeDuration) getProperty(DTR_TOGGLE_DELAY);
    }

    @Override
    public PEMPModemConfiguration getConfiguration() {
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

    public static PropertySpec atGlobalModemInitStringSpec(PropertySpecService propertySpecService) {
        return propertySpecService.stringPropertySpec(GLOBAL_MODEM_INIT_STRINGS, false, DEFAULT_GLOBAL_MODEM_INIT_STRINGS);
    }

    public static PropertySpec atModemInitStringSpec(PropertySpecService propertySpecService) {
        return propertySpecService.stringPropertySpec(MODEM_INIT_STRINGS, false, DEFAULT_MODEM_INIT_STRINGS);
    }

    public static PropertySpec atCommandTriesSpec(PropertySpecService propertySpecService) {
        return propertySpecService.bigDecimalPropertySpec(COMMAND_TRIES, false, DEFAULT_COMMAND_TRIES);
    }

    public static PropertySpec atCommandTimeoutSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(COMMAND_TIMEOUT, false, DEFAULT_COMMAND_TIMEOUT);
    }

    public static PropertySpec delayBeforeSendSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(DELAY_BEFORE_SEND, false, DEFAULT_DELAY_BEFORE_SEND);
    }

    public static PropertySpec delayAfterConnectSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(DELAY_AFTER_CONNECT, false, DEFAULT_DELAY_AFTER_CONNECT);
    }

    public static PropertySpec atConnectTimeoutSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(CONNECT_TIMEOUT, false, DEFAULT_CONNECT_TIMEOUT);
    }

    public static PropertySpec atCommandPrefixSpec(PropertySpecService propertySpecService) {
        return propertySpecService.stringPropertySpec(MODEM_DIAL_PREFIX, false, DEFAULT_MODEM_DIAL_PREFIX);
    }

    public static PropertySpec dtrToggleDelaySpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(DTR_TOGGLE_DELAY, false, DEFAULT_DTR_TOGGLE_DELAY);
    }

    public static PropertySpec modemConfigurationKeySpec(PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(MODEM_CONFIGURATION_KEY, true, new StringFactory());
    }

    public static PropertySpec phoneNumberSpec(PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(PHONE_NUMBER_PROPERTY_NAME, true, new StringFactory());
    }

}