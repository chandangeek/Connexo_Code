package com.energyict.mdc.io.impl;

import com.energyict.mdc.channels.serial.modem.AbstractAtModemProperties;

import java.math.BigDecimal;
import java.time.temporal.TemporalAmount;
import java.util.List;

/**
 * @author sva
 * @since 23/11/12 (8:57)
 */
public class SimpleAtModemProperties extends AbstractAtModemProperties {

    private String phoneNumber;
    private String atCommandPrefix;
    private TemporalAmount connectTimeout;
    private TemporalAmount delayAfterConnect;
    private TemporalAmount delayBeforeSend;
    private TemporalAmount atCommandTimeout;
    private BigDecimal atCommandTry;
    private List<String> globalModemInitStrings;
    private List<String> modemInitStrings;
    private String addressSelector;
    private TemporalAmount lineToggleDelay;
    private String postDialCommands;

    public SimpleAtModemProperties(String phoneNumber, String atCommandPrefix, TemporalAmount connectTimeout, TemporalAmount delayAfterConnect, TemporalAmount delayBeforeSend, TemporalAmount atCommandTimeout, BigDecimal atCommandTry, List<String> modemInitStrings, String addressSelector, TemporalAmount lineToggleDelay, String postDialCommands, List<String> globalModemInitStrings) {
        this.phoneNumber = phoneNumber;
        this.atCommandPrefix = atCommandPrefix;
        this.connectTimeout = connectTimeout;
        this.delayAfterConnect = delayAfterConnect;
        this.delayBeforeSend = delayBeforeSend;
        this.atCommandTimeout = atCommandTimeout;
        this.atCommandTry = atCommandTry;
        this.globalModemInitStrings = globalModemInitStrings;
        this.modemInitStrings = modemInitStrings;
        this.addressSelector = addressSelector;
        this.lineToggleDelay = lineToggleDelay;
        this.postDialCommands = postDialCommands;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getCommandPrefix() {
        return atCommandPrefix;
    }

    @Override
    public TemporalAmount getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public TemporalAmount getDelayAfterConnect() {
        return delayAfterConnect;
    }

    @Override
    public TemporalAmount getDelayBeforeSend() {
        return delayBeforeSend;
    }

    @Override
    public TemporalAmount getCommandTimeOut() {
        return atCommandTimeout;
    }

    @Override
    public BigDecimal getCommandTry() {
        return atCommandTry;
    }

    @Override
    public List<String> getGlobalModemInitStrings() {
        return globalModemInitStrings;
    }

    @Override
    public List<String> getModemInitStrings() {
        return modemInitStrings;
    }

    @Override
    public String getAddressSelector() {
        return addressSelector;
    }

    @Override
    public String getPostDialCommands() {
        return postDialCommands;
    }

    @Override
    public TemporalAmount getLineToggleDelay() {
        return this.lineToggleDelay;
    }
}