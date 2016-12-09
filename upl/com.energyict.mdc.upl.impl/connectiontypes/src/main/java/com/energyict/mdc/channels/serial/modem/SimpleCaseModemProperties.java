package com.energyict.mdc.channels.serial.modem;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * @author sva
 * @since 30/04/13 - 13:23
 */
public class SimpleCaseModemProperties extends AbstractCaseModemProperties {

    private String phoneNumber;
    private String commandPrefix;
    private Duration connectTimeout;
    private Duration delayAfterConnect;
    private Duration delayBeforeSend;
    private Duration commandTimeout;
    private BigDecimal commandTry;
    private List<String> globalModemInitStrings;
    private List<String> modemInitStrings;
    private String addressSelector;
    private Duration lineToggleDelay;

    public SimpleCaseModemProperties(String phoneNumber, String commandPrefix, Duration connectTimeout, Duration delayAfterConnect, Duration delayBeforeSend, Duration commandTimeout, BigDecimal commandTry, List<String> globalModemInitStrings, List<String> modemInitStrings, String addressSelector, Duration lineToggleDelay) {
        this.phoneNumber = phoneNumber;
        this.commandPrefix = commandPrefix;
        this.connectTimeout = connectTimeout;
        this.delayAfterConnect = delayAfterConnect;
        this.delayBeforeSend = delayBeforeSend;
        this.commandTimeout = commandTimeout;
        this.commandTry = commandTry;
        this.globalModemInitStrings = globalModemInitStrings;
        this.modemInitStrings = modemInitStrings;
        this.addressSelector = addressSelector;
        this.lineToggleDelay = lineToggleDelay;
    }

    @Override
    protected String getPhoneNumber() {
        return this.phoneNumber;
    }

    @Override
    protected String getCommandPrefix() {
        return this.commandPrefix;
    }

    @Override
    protected Duration getConnectTimeout() {
        return this.connectTimeout;
    }

    @Override
    protected Duration getDelayAfterConnect() {
        return this.delayAfterConnect;
    }

    @Override
    protected Duration getDelayBeforeSend() {
        return this.delayBeforeSend;
    }

    @Override
    protected Duration getCommandTimeOut() {
        return this.commandTimeout;
    }

    @Override
    protected BigDecimal getCommandTry() {
        return this.commandTry;
    }

    @Override
    protected List<String> getGlobalModemInitStrings() {
        return globalModemInitStrings;
    }

    @Override
    protected List<String> getModemInitStrings() {
        return this.modemInitStrings;
    }

    @Override
    public String getAddressSelector() {
        return addressSelector;
    }

    @Override
    protected Duration getLineToggleDelay() {
        return this.lineToggleDelay;
    }
}