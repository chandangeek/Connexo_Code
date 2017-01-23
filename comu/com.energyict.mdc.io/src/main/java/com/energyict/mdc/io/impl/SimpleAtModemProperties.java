package com.energyict.mdc.io.impl;

import com.elster.jupiter.time.TimeDuration;
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
    private TimeDuration connectTimeout;
    private TimeDuration delayAfterConnect;
    private TimeDuration delayBeforeSend;
    private TimeDuration atCommandTimeout;
    private BigDecimal atCommandTry;
    private List<String> globalModemInitStrings;
    private List<String> modemInitStrings;
    private String addressSelector;
    private TimeDuration lineToggleDelay;
    private String postDialCommands;

    public SimpleAtModemProperties(String phoneNumber, String atCommandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration atCommandTimeout, BigDecimal atCommandTry, List<String> modemInitStrings, String addressSelector, TimeDuration lineToggleDelay, String postDialCommands, List<String> globalModemInitStrings) {
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
        return connectTimeout.asTemporalAmount();
    }

    @Override
    public TemporalAmount getDelayAfterConnect() {
        return delayAfterConnect.asTemporalAmount();
    }

    @Override
    public TemporalAmount getDelayBeforeSend() {
        return delayBeforeSend.asTemporalAmount();
    }

    @Override
    public TemporalAmount getCommandTimeOut() {
        return atCommandTimeout.asTemporalAmount();
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
        return this.lineToggleDelay.asTemporalAmount();
    }
}