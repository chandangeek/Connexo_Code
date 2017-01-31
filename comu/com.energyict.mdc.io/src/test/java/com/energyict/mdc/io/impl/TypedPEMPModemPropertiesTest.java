/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import java.util.Collections;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TypedPEMPModemPropertiesTest extends TypedPEMPModemProperties {

    public TypedPEMPModemPropertiesTest() {
        super(null, null);
    }

    @Test
    public void getCommandPrefixTest(){
        assertThat(this.getCommandPrefix()).isEqualTo(DEFAULT_MODEM_DIAL_PREFIX);
    }

    @Test
    public void getConnectTimeoutTest() {
        assertThat(this.getConnectTimeout()).isEqualTo(DEFAULT_CONNECT_TIMEOUT);
    }

    @Test
    public void getDelayAfterConnectTest() {
        assertThat(this.getDelayAfterConnect()).isEqualTo(DEFAULT_DELAY_AFTER_CONNECT);
    }

    @Test
    public void getDelayBeforeSendTest(){
        assertThat(this.getDelayBeforeSend()).isEqualTo(DEFAULT_DELAY_BEFORE_SEND);
    }

    @Test
    public void getCommandTimeOutTest(){
        assertThat(this.getCommandTimeOut()).isEqualTo(DEFAULT_COMMAND_TIMEOUT);
    }

    @Test
    public void getCommandTryTest(){
        assertThat(this.getCommandTry()).isEqualTo(DEFAULT_COMMAND_TRIES);
    }

    @Test
    public void getGlobalModemInitStringsTest(){
        assertThat(getGlobalModemInitStrings()).isEqualTo(Collections.emptyList());
    }

    @Test
    public void getModemInitStringsTest(){
        assertThat(getModemInitStrings()).hasSize(1);
        assertThat(getModemInitStrings().get(0)).isEqualTo(DEFAULT_MODEM_INIT_STRINGS);
    }

    @Test
    public void getLineToggleDelayTest(){
        assertThat(getLineToggleDelay()).isEqualTo(DEFAULT_DTR_TOGGLE_DELAY);
    }

}
