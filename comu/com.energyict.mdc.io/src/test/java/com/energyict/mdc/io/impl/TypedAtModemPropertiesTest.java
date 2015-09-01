package com.energyict.mdc.io.impl;

import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing the defaults
 * Copyrights EnergyICT
 * Date: 1/09/2015
 * Time: 15:13
 */
public class TypedAtModemPropertiesTest extends TypedAtModemProperties {

    public TypedAtModemPropertiesTest() {
        super(null);
    }

    @Test
    public void getCommandPrefixTest(){
        assertThat(this.getCommandPrefix()).isEqualTo(DEFAULT_AT_MODEM_DIAL_PREFIX);
    }

    @Test
    public void getConnectTimeoutTest() {
        assertThat(this.getConnectTimeout()).isEqualTo(DEFAULT_AT_CONNECT_TIMEOUT);
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
        assertThat(this.getCommandTimeOut()).isEqualTo(DEFAULT_AT_COMMAND_TIMEOUT);
    }

    @Test
    public void getCommandTryTest(){
        assertThat(this.getCommandTry()).isEqualTo(DEFAULT_AT_COMMAND_TRIES);
    }

    @Test
    public void getGlobalModemInitStringsTest(){
        assertThat(getGlobalModemInitStrings()).hasSize(1);
        assertThat(getGlobalModemInitStrings().get(0)).isEqualTo("ATS0=0E0V1");
    }

    @Test
    public void getModemInitStringsTest(){
        assertThat(getModemInitStrings()).isEqualTo(Collections.emptyList());
    }

    @Test
    public void getLineToggleDelayTest(){
        assertThat(getLineToggleDelay()).isEqualTo(DEFAULT_DTR_TOGGLE_DELAY);
    }

    @Test
    public void getAddressSelectorTest(){
        assertThat(getAddressSelector()).isEqualTo(DEFAULT_AT_MODEM_ADDRESS_SELECTOR);
    }
}
