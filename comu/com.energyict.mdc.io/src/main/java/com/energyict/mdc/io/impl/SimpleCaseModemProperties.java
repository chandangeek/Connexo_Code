package com.energyict.mdc.io.impl;

import com.elster.jupiter.time.TimeDuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author sva
 * @since 30/04/13 - 13:23
 */
public class SimpleCaseModemProperties implements CaseModemProperties {

    private String phoneNumber;
    private String commandPrefix;
    private TimeDuration connectTimeout;
    private TimeDuration delayAfterConnect;
    private TimeDuration delayBeforeSend;
    private TimeDuration commandTimeout;
    private BigDecimal commandTry;
    private List<String> globalModemInitStrings;
    private List<String> modemInitStrings;
    private String addressSelector;
    private TimeDuration lineToggleDelay;

    public SimpleCaseModemProperties(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, String addressSelector, TimeDuration lineToggleDelay, List<String> globalModemInitStrings) {
        super();
        this.phoneNumber = phoneNumber;
        this.commandPrefix = commandPrefix;
        this.connectTimeout = connectTimeout;
        this.delayAfterConnect = delayAfterConnect;
        this.delayBeforeSend = delayBeforeSend;
        this.commandTimeout = commandTimeout;
        this.commandTry = commandTry;
        this.modemInitStrings = modemInitStrings;
        this.addressSelector = addressSelector;
        this.lineToggleDelay = lineToggleDelay;
        this.globalModemInitStrings = globalModemInitStrings;
    }

    @Override
    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    @Override
    public String getCommandPrefix() {
        return this.commandPrefix;
    }

    @Override
    public TimeDuration getConnectTimeout() {
        return this.connectTimeout;
    }

    @Override
    public TimeDuration getDelayAfterConnect() {
        return this.delayAfterConnect;
    }

    @Override
    public TimeDuration getDelayBeforeSend() {
        return this.delayBeforeSend;
    }

    @Override
    public TimeDuration getCommandTimeOut() {
        return this.commandTimeout;
    }

    @Override
    public BigDecimal getCommandTry() {
        return this.commandTry;
    }

    @Override
    public List<String> getGlobalModemInitStrings() {
        return globalModemInitStrings;
    }

    @Override
    public List<String> getModemInitStrings() {
        return this.modemInitStrings;
    }

    @Override
    public String getAddressSelector() {
        return addressSelector;
    }

    @Override
    public TimeDuration getLineToggleDelay() {
        return this.lineToggleDelay;
    }

}