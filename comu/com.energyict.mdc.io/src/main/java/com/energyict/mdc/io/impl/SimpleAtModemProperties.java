package com.energyict.mdc.io.impl;

import com.elster.jupiter.time.TimeDuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 23/11/12 (8:57)
 */
public class SimpleAtModemProperties implements AtModemProperties {

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
    private List<AtPostDialCommand> postDialCommands;

    public SimpleAtModemProperties(String phoneNumber, String atCommandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration atCommandTimeout, BigDecimal atCommandTry, List<String> modemInitStrings, String addressSelector, TimeDuration lineToggleDelay, List<AtPostDialCommand> postDialCommands, List<String> globalModemInitStrings) {
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
        this.postDialCommands = new ArrayList<>(postDialCommands);
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
    public TimeDuration getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public TimeDuration getDelayAfterConnect() {
        return delayAfterConnect;
    }

    @Override
    public TimeDuration getDelayBeforeSend() {
        return delayBeforeSend;
    }

    @Override
    public TimeDuration getCommandTimeOut() {
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
    public List<AtPostDialCommand> getPostDialCommands() {
        return postDialCommands;
    }

    @Override
    public TimeDuration getLineToggleDelay() {
        return this.lineToggleDelay;
    }

}