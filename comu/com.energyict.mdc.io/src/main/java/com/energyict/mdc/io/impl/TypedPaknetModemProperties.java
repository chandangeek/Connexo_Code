/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
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
    private final Thesaurus thesaurus;
    private TypedProperties properties;
    private Map<String, PropertySpec> propertySpecs;

    public TypedPaknetModemProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.properties = TypedProperties.empty();
    }

    public TypedPaknetModemProperties(TypedProperties properties, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
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
        propertySpecs.put(ModemPropertySpecNames.CONNECT_TIMEOUT, atConnectTimeoutSpec());
        propertySpecs.put(ModemPropertySpecNames.DIAL_PREFIX, atCommandPrefixSpec());
        propertySpecs.put(ModemPropertySpecNames.GLOBAL_INIT_STRINGS, atGlobalModemInitStringSpec());
        propertySpecs.put(ModemPropertySpecNames.INIT_STRINGS, atModemInitStringSpec());
        propertySpecs.put(ModemPropertySpecNames.COMMAND_TRIES, atCommandTriesSpec());
        propertySpecs.put(ModemPropertySpecNames.COMMAND_TIMEOUT, atCommandTimeoutSpec());
        propertySpecs.put(PHONE_NUMBER_PROPERTY_NAME, phoneNumberSpec());
        propertySpecs.put(ModemPropertySpecNames.DELAY_AFTER_CONNECT, delayAfterConnectSpec());
        propertySpecs.put(ModemPropertySpecNames.DELAY_BEFORE_SEND, delayBeforeSendSpec());
        propertySpecs.put(ModemPropertySpecNames.DTR_TOGGLE_DELAY, dtrToggleDelaySpec());
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

    private PropertySpec stringPropertySpec(TranslationKeys name, String defaultValue) {
        return this.propertySpecService
                .stringSpec()
                .named(name)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec bigDecimalPropertySpec(TranslationKeys name, BigDecimal defaultValue) {
        return this.propertySpecService
                .bigDecimalSpec()
                .named(name)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec timeDurationPropertySpec(TranslationKeys name, TimeDuration defaultValue) {
        return this.propertySpecService
                .timeDurationSpec()
                .named(name)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec atModemInitStringSpec() {
        return this.stringPropertySpec(TranslationKeys.INIT_STRINGS, DEFAULT_MODEM_INIT_STRINGS);
    }

    private PropertySpec atGlobalModemInitStringSpec() {
        return this.stringPropertySpec(TranslationKeys.GLOBAL_INIT_STRINGS, DEFAULT_GLOBAL_MODEM_INIT_STRINGS);
    }

    private PropertySpec atCommandTriesSpec() {
        return this.bigDecimalPropertySpec(TranslationKeys.COMMAND_TRIES, DEFAULT_COMMAND_TRIES);
    }

    private PropertySpec atCommandTimeoutSpec() {
        return this.timeDurationPropertySpec(TranslationKeys.COMMAND_TIMEOUT, DEFAULT_COMMAND_TIMEOUT);
    }

    private PropertySpec delayBeforeSendSpec() {
        return this.timeDurationPropertySpec(TranslationKeys.DELAY_BEFORE_SEND, DEFAULT_DELAY_BEFORE_SEND);
    }

    private PropertySpec delayAfterConnectSpec() {
        return this.timeDurationPropertySpec(TranslationKeys.DELAY_AFTER_CONNECT, DEFAULT_DELAY_AFTER_CONNECT);
    }

    private PropertySpec atConnectTimeoutSpec() {
        return this.timeDurationPropertySpec(TranslationKeys.CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
    }

    private PropertySpec atCommandPrefixSpec() {
        return this.stringPropertySpec(TranslationKeys.DIAL_PREFIX, DEFAULT_MODEM_DIAL_PREFIX);
    }

    private PropertySpec dtrToggleDelaySpec() {
        return this.timeDurationPropertySpec(TranslationKeys.DTR_TOGGLE_DELAY, DEFAULT_DTR_TOGGLE_DELAY);
    }

    private PropertySpec phoneNumberSpec() {
        return propertySpecService
                .stringSpec()
                .named(TranslationKeys.PHONE_NUMBER_PROPERTY)
                .fromThesaurus(this.thesaurus)
                .finish();
    }

}