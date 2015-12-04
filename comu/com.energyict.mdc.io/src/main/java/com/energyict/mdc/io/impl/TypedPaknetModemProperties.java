package com.energyict.mdc.io.impl;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.naming.ModemPropertySpecNames;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 18/03/13 - 16:33
 */
public class TypedPaknetModemProperties implements PaknetModemProperties, HasDynamicProperties {

    static final String DEFAULT_MODEM_INIT_STRINGS = "1:0;2:0;3:0;4:10;5:0;6:5";
    static final String DEFAULT_GLOBAL_MODEM_INIT_STRINGS = "";
    static final BigDecimal DEFAULT_COMMAND_TRIES = new BigDecimal(5);
    static final TimeDuration DEFAULT_COMMAND_TIMEOUT = new TimeDuration(10, TimeDuration.TimeUnit.SECONDS);
    static final TimeDuration DEFAULT_DELAY_BEFORE_SEND = new TimeDuration(500, TimeDuration.TimeUnit.MILLISECONDS);
    static final TimeDuration DEFAULT_DELAY_AFTER_CONNECT = new TimeDuration(500, TimeDuration.TimeUnit.MILLISECONDS);
    static final TimeDuration DEFAULT_CONNECT_TIMEOUT = new TimeDuration(30, TimeDuration.TimeUnit.SECONDS);
    static final String DEFAULT_MODEM_DIAL_PREFIX = "";
    static final TimeDuration DEFAULT_DTR_TOGGLE_DELAY = new TimeDuration(2, TimeDuration.TimeUnit.SECONDS);

    private final PropertySpecService propertySpecService;
    private TypedProperties properties;
    private Map<String, PropertySpec> propertySpecs;

    public TypedPaknetModemProperties(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        this.properties = TypedProperties.empty();
    }

    public TypedPaknetModemProperties(TypedProperties properties, PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        this.properties = TypedProperties.copyOf(properties);
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        this.ensurePropertySpecsInitialized();
        return new ArrayList<>(this.propertySpecs.values());
    }

    private void ensurePropertySpecsInitialized() {
        if (this.propertySpecs == null) {
            Map<String, PropertySpec> temp = new HashMap<>();
            this.initializePropertySpecs(temp);
            this.propertySpecs = temp;
        }
    }

    private void initializePropertySpecs(Map<String, PropertySpec> propertySpecs) {
        propertySpecs.put(ModemPropertySpecNames.CONNECT_TIMEOUT, atConnectTimeoutSpec(this.propertySpecService));
        propertySpecs.put(ModemPropertySpecNames.DIAL_PREFIX, atCommandPrefixSpec(this.propertySpecService));
        propertySpecs.put(ModemPropertySpecNames.GLOBAL_INIT_STRINGS, atGlobalModemInitStringSpec(this.propertySpecService));
        propertySpecs.put(ModemPropertySpecNames.INIT_STRINGS, atModemInitStringSpec(this.propertySpecService));
        propertySpecs.put(ModemPropertySpecNames.COMMAND_TRIES, atCommandTriesSpec(this.propertySpecService));
        propertySpecs.put(ModemPropertySpecNames.COMMAND_TIMEOUT, atCommandTimeoutSpec(this.propertySpecService));
        propertySpecs.put(PHONE_NUMBER_PROPERTY_NAME, phoneNumberSpec(this.propertySpecService));
        propertySpecs.put(ModemPropertySpecNames.DELAY_AFTER_CONNECT, delayAfterConnectSpec(this.propertySpecService));
        propertySpecs.put(ModemPropertySpecNames.DELAY_BEFORE_SEND, delayBeforeSendSpec(this.propertySpecService));
        propertySpecs.put(ModemPropertySpecNames.DTR_TOGGLE_DELAY, dtrToggleDelaySpec(this.propertySpecService));
    }

    @Override
    public String getPhoneNumber() {
        return (String) properties.getProperty(PHONE_NUMBER_PROPERTY_NAME);
    }

    @Override
    public String getCommandPrefix() {
        return (String) properties.getProperty(ModemPropertySpecNames.DIAL_PREFIX, DEFAULT_MODEM_DIAL_PREFIX);
    }

    @Override
    public TimeDuration getConnectTimeout() {
        return (TimeDuration) properties.getProperty(ModemPropertySpecNames.CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
    }

    @Override
    public TimeDuration getDelayAfterConnect() {
        return (TimeDuration) properties.getProperty(ModemPropertySpecNames.DELAY_AFTER_CONNECT, DEFAULT_DELAY_AFTER_CONNECT );
    }

    @Override
    public TimeDuration getDelayBeforeSend() {
        return (TimeDuration) properties.getProperty(ModemPropertySpecNames.DELAY_BEFORE_SEND, DEFAULT_DELAY_BEFORE_SEND);
    }

    @Override
    public TimeDuration getCommandTimeOut() {
        return (TimeDuration) properties.getProperty(ModemPropertySpecNames.COMMAND_TIMEOUT, DEFAULT_COMMAND_TIMEOUT);
    }

    @Override
    public BigDecimal getCommandTry() {
        return (BigDecimal) properties.getProperty(ModemPropertySpecNames.COMMAND_TRIES, DEFAULT_COMMAND_TRIES);
    }

    @Override
    public List<String> getModemInitStrings() {
        return Collections.singletonList((String) properties.getProperty(ModemPropertySpecNames.INIT_STRINGS, DEFAULT_MODEM_INIT_STRINGS ));
    }

    @Override
    public List<String> getGlobalModemInitStrings() {
        String globalInitStringSpecs = (String) properties.getProperty(ModemPropertySpecNames.GLOBAL_INIT_STRINGS, DEFAULT_GLOBAL_MODEM_INIT_STRINGS);
        if (!globalInitStringSpecs.isEmpty()) {
            return Arrays.asList(globalInitStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public TimeDuration getLineToggleDelay() {
        return (TimeDuration) properties.getProperty(ModemPropertySpecNames.DTR_TOGGLE_DELAY, DEFAULT_DTR_TOGGLE_DELAY);
    }

    protected void setProperty(String propertyName, Object value) {
        this.properties.setProperty(propertyName, value);
    }

    public static PropertySpec atModemInitStringSpec(PropertySpecService propertySpecService) {
        return propertySpecService.stringPropertySpec(ModemPropertySpecNames.INIT_STRINGS, false, DEFAULT_MODEM_INIT_STRINGS);
    }

    public static PropertySpec atGlobalModemInitStringSpec(PropertySpecService propertySpecService) {
        return propertySpecService.stringPropertySpec(ModemPropertySpecNames.GLOBAL_INIT_STRINGS, false, DEFAULT_GLOBAL_MODEM_INIT_STRINGS);
    }

    public static PropertySpec atCommandTriesSpec(PropertySpecService propertySpecService) {
        return propertySpecService.bigDecimalPropertySpec(ModemPropertySpecNames.COMMAND_TRIES, false, DEFAULT_COMMAND_TRIES);
    }

    public static PropertySpec atCommandTimeoutSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(ModemPropertySpecNames.COMMAND_TIMEOUT, false, DEFAULT_COMMAND_TIMEOUT);
    }

    public static PropertySpec delayBeforeSendSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(ModemPropertySpecNames.DELAY_BEFORE_SEND, false, DEFAULT_DELAY_BEFORE_SEND);
    }

    public static PropertySpec delayAfterConnectSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(ModemPropertySpecNames.DELAY_AFTER_CONNECT, false, DEFAULT_DELAY_AFTER_CONNECT);
    }

    public static PropertySpec atConnectTimeoutSpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(ModemPropertySpecNames.CONNECT_TIMEOUT, false, DEFAULT_CONNECT_TIMEOUT);
    }

    public static PropertySpec atCommandPrefixSpec(PropertySpecService propertySpecService) {
        return propertySpecService.stringPropertySpec(ModemPropertySpecNames.DIAL_PREFIX, false, DEFAULT_MODEM_DIAL_PREFIX);
    }

    public static PropertySpec dtrToggleDelaySpec(PropertySpecService propertySpecService) {
        return propertySpecService.timeDurationPropertySpec(ModemPropertySpecNames.DTR_TOGGLE_DELAY, false, DEFAULT_DTR_TOGGLE_DELAY);
    }

    public static PropertySpec phoneNumberSpec(PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(PHONE_NUMBER_PROPERTY_NAME, true, new StringFactory());
    }

}