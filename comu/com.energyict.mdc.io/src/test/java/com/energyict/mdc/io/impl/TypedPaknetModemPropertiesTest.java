/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TypedPaknetModemPropertiesTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private Thesaurus thesaurus;

    @Test
    public void getCommandPrefixTest() {
        TypedPaknetModemProperties properties = this.getTestInstance();

        // Business method
        String commandPrefix = properties.getCommandPrefix();

        // Asserts
        assertThat(commandPrefix).isEqualTo(TypedPaknetModemProperties.DEFAULT_MODEM_DIAL_PREFIX);
    }

    @Test
    public void getConnectTimeoutTest() {
        TypedPaknetModemProperties properties = this.getTestInstance();

        // Business method
        TimeDuration timeOut = properties.getConnectTimeout();

        // Asserts

        assertThat(timeOut).isEqualTo(TypedPaknetModemProperties.DEFAULT_CONNECT_TIMEOUT);
    }

    @Test
    public void getDelayAfterConnectTest() {
        TypedPaknetModemProperties properties = this.getTestInstance();

        // Business method
        TimeDuration delayAfterConnect = properties.getDelayAfterConnect();

        // Asserts
        assertThat(delayAfterConnect).isEqualTo(TypedPaknetModemProperties.DEFAULT_DELAY_AFTER_CONNECT);
    }

    @Test
    public void getDelayBeforeSendTest(){
        TypedPaknetModemProperties properties = this.getTestInstance();

        // Business method
        TimeDuration delayBeforeSend = properties.getDelayBeforeSend();

        // Asserts
        assertThat(delayBeforeSend).isEqualTo(TypedPaknetModemProperties.DEFAULT_DELAY_BEFORE_SEND);
    }

    @Test
    public void getCommandTimeOutTest(){
        TypedPaknetModemProperties properties = this.getTestInstance();

        // Business method
        TimeDuration timeOut = properties.getCommandTimeOut();

        // Asserts
        assertThat(timeOut).isEqualTo(TypedPaknetModemProperties.DEFAULT_COMMAND_TIMEOUT);
    }

    @Test
    public void getCommandTryTest(){
        TypedPaknetModemProperties properties = this.getTestInstance();

        // Business method
        BigDecimal commandTry = properties.getCommandTry();

        // Asserts
        assertThat(commandTry).isEqualTo(TypedPaknetModemProperties.DEFAULT_COMMAND_TRIES);
    }

    @Test
    public void getGlobalModemInitStringsTest(){
        TypedPaknetModemProperties properties = this.getTestInstance();

        // Business method
        List<String> globalModemInitStrings = properties.getGlobalModemInitStrings();

        // Asserts
        assertThat(globalModemInitStrings).isEqualTo(Collections.emptyList());
    }

    @Test
    public void getModemInitStringsTest(){
        TypedPaknetModemProperties properties = this.getTestInstance();

        // Business method
        List<String> modemInitStrings = properties.getModemInitStrings();

        // Asserts
        assertThat(modemInitStrings).hasSize(1);
        assertThat(modemInitStrings.get(0)).isEqualTo(TypedPaknetModemProperties.DEFAULT_MODEM_INIT_STRINGS);
    }

    @Test
    public void getLineToggleDelayTest(){
        TypedPaknetModemProperties properties = this.getTestInstance();

        // Business method
        TimeDuration lineToggleDelay = properties.getLineToggleDelay();

        // Asserts
        assertThat(lineToggleDelay).isEqualTo(TypedPaknetModemProperties.DEFAULT_DTR_TOGGLE_DELAY);
    }

    private TypedPaknetModemProperties getTestInstance() {
        return new TypedPaknetModemProperties(this.propertySpecService, this.thesaurus);
    }

}
