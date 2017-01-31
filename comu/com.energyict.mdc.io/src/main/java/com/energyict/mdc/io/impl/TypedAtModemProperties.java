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
 * @since 23/11/12 (9:07)
 */
public class TypedAtModemProperties implements AtModemProperties, HasDynamicProperties {

    static final String DEFAULT_AT_MODEM_ADDRESS_SELECTOR = "";
    static final String DEFAULT_AT_MODEM_POST_DIAL_COMMANDS = "";
    static final String DEFAULT_AT_MODEM_GLOBAL_INIT_STRINGS = "ATS0=0E0V1";   // Auto-answer disabled: modem will not answer incoming calls
    static final String DEFAULT_AT_MODEM_INIT_STRINGS = "";
    static final BigDecimal DEFAULT_AT_COMMAND_TRIES = new BigDecimal(3);
    static final TimeDuration DEFAULT_AT_COMMAND_TIMEOUT = new TimeDuration(5, TimeDuration.TimeUnit.SECONDS);
    static final TimeDuration DEFAULT_DELAY_BEFORE_SEND = new TimeDuration(500, TimeDuration.TimeUnit.MILLISECONDS);
    static final TimeDuration DEFAULT_DELAY_AFTER_CONNECT = new TimeDuration(500, TimeDuration.TimeUnit.MILLISECONDS);
    static final TimeDuration DEFAULT_AT_CONNECT_TIMEOUT = new TimeDuration(60, TimeDuration.TimeUnit.SECONDS);
    static final String DEFAULT_AT_MODEM_DIAL_PREFIX = "";
    static final TimeDuration DEFAULT_DTR_TOGGLE_DELAY = new TimeDuration(2, TimeDuration.TimeUnit.SECONDS);

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private final List<AtPostDialCommand> postDialCommands;
    private TypedProperties properties;
    private Map<String, PropertySpec> propertySpecs;

    public TypedAtModemProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.properties = TypedProperties.empty();
        this.postDialCommands = Collections.emptyList();
    }

    public TypedAtModemProperties(TypedProperties properties, List<AtPostDialCommand> postDialCommands, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.properties = TypedProperties.copyOf(properties);
        this.postDialCommands = Collections.unmodifiableList(postDialCommands);
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        this.ensurePropertySpecsInitialized();
        return new ArrayList<>(this.propertySpecs.values());
    }

    private void ensurePropertySpecsInitialized () {
        if (this.propertySpecs == null) {
            Map<String, PropertySpec> temp = new HashMap<>();
            this.initializePropertySpecs(temp);
            this.propertySpecs = temp;
        }
    }

    private void initializePropertySpecs (Map<String, PropertySpec> propertySpecs) {
        propertySpecs.put(ModemPropertySpecNames.POST_DIAL_COMMANDS, atModemPostDialCommandsSpec());
        propertySpecs.put(ModemPropertySpecNames.ADDRESS_SELECTOR, atModemAddressSelectorSpec());
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
        return (String) properties.getProperty(ModemPropertySpecNames.DIAL_PREFIX, DEFAULT_AT_MODEM_DIAL_PREFIX);
    }

    @Override
    public TimeDuration getConnectTimeout() {
        return (TimeDuration) properties.getProperty(ModemPropertySpecNames.CONNECT_TIMEOUT, DEFAULT_AT_CONNECT_TIMEOUT );
    }

    @Override
    public TimeDuration getDelayAfterConnect() {
        return (TimeDuration) properties.getProperty(ModemPropertySpecNames.DELAY_AFTER_CONNECT, DEFAULT_DELAY_AFTER_CONNECT);
    }

    @Override
    public TimeDuration getDelayBeforeSend() {
        return (TimeDuration) properties.getProperty(ModemPropertySpecNames.DELAY_BEFORE_SEND, DEFAULT_DELAY_BEFORE_SEND );
    }

    @Override
    public TimeDuration getCommandTimeOut() {
        return (TimeDuration) properties.getProperty(ModemPropertySpecNames.COMMAND_TIMEOUT, DEFAULT_AT_COMMAND_TIMEOUT );
    }

    @Override
    public BigDecimal getCommandTry() {
        return (BigDecimal) properties.getProperty(ModemPropertySpecNames.COMMAND_TRIES, DEFAULT_AT_COMMAND_TRIES );
    }

    @Override
    public List<String> getGlobalModemInitStrings() {
        String  globalInitStringSpecs  = (String) properties.getProperty(ModemPropertySpecNames.GLOBAL_INIT_STRINGS, DEFAULT_AT_MODEM_GLOBAL_INIT_STRINGS);
        if (!globalInitStringSpecs.isEmpty()) {
            return Arrays.asList(globalInitStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getModemInitStrings() {
        String initStringSpecs = (String) properties.getProperty(ModemPropertySpecNames.INIT_STRINGS, DEFAULT_AT_MODEM_INIT_STRINGS);
        if (!initStringSpecs.isEmpty()) {
            return Arrays.asList(initStringSpecs.split(AtModemComponent.SEPARATOR));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public TimeDuration getLineToggleDelay() {
        return (TimeDuration) properties.getProperty(ModemPropertySpecNames.DTR_TOGGLE_DELAY, DEFAULT_DTR_TOGGLE_DELAY);
    }

    @Override
    public String getAddressSelector() {
        return (String) properties.getProperty(ModemPropertySpecNames.ADDRESS_SELECTOR, DEFAULT_AT_MODEM_ADDRESS_SELECTOR);
    }

    public List<AtPostDialCommand> getPostDialCommands() {
        return this.postDialCommands;
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

    private PropertySpec atModemAddressSelectorSpec() {
        return this.stringPropertySpec(TranslationKeys.ADDRESS_SELECTOR, DEFAULT_AT_MODEM_ADDRESS_SELECTOR);
    }

    private PropertySpec atModemPostDialCommandsSpec() {
        return this.stringPropertySpec(TranslationKeys.POST_DIAL_COMMANDS, DEFAULT_AT_MODEM_POST_DIAL_COMMANDS);
    }

    private PropertySpec atGlobalModemInitStringSpec() {
        return this.stringPropertySpec(TranslationKeys.GLOBAL_INIT_STRINGS, DEFAULT_AT_MODEM_GLOBAL_INIT_STRINGS);
    }

    private PropertySpec atModemInitStringSpec() {
        return this.stringPropertySpec(TranslationKeys.INIT_STRINGS, DEFAULT_AT_MODEM_INIT_STRINGS);
    }

    private PropertySpec atCommandTriesSpec() {
        return this.bigDecimalPropertySpec(TranslationKeys.COMMAND_TRIES, DEFAULT_AT_COMMAND_TRIES);
    }

    private PropertySpec atCommandTimeoutSpec() {
        return this.timeDurationPropertySpec(TranslationKeys.COMMAND_TIMEOUT, DEFAULT_AT_COMMAND_TIMEOUT);
    }

    private PropertySpec delayBeforeSendSpec() {
        return this.timeDurationPropertySpec(TranslationKeys.DELAY_BEFORE_SEND, DEFAULT_DELAY_BEFORE_SEND);
    }

    private PropertySpec delayAfterConnectSpec() {
        return this.timeDurationPropertySpec(TranslationKeys.DELAY_AFTER_CONNECT, DEFAULT_DELAY_AFTER_CONNECT);
    }

    private PropertySpec atConnectTimeoutSpec() {
        return this.timeDurationPropertySpec(TranslationKeys.CONNECT_TIMEOUT, DEFAULT_AT_CONNECT_TIMEOUT);
    }

    private PropertySpec atCommandPrefixSpec() {
        return this.stringPropertySpec(TranslationKeys.DIAL_PREFIX, DEFAULT_AT_MODEM_DIAL_PREFIX);
    }

    private PropertySpec dtrToggleDelaySpec() {
        return this.timeDurationPropertySpec(TranslationKeys.DTR_TOGGLE_DELAY, DEFAULT_DTR_TOGGLE_DELAY);
    }

    private PropertySpec phoneNumberSpec() {
        return this.propertySpecService
                .stringSpec()
                .named(TranslationKeys.PHONE_NUMBER_PROPERTY)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish();
    }

}