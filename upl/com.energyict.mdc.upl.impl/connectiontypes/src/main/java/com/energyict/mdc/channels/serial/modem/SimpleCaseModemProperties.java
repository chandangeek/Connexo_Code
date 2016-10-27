package com.energyict.mdc.channels.serial.modem;

import com.energyict.cbo.TimeDuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author sva
 * @since 30/04/13 - 13:23
 */
public class SimpleCaseModemProperties extends AbstractCaseModemProperties{

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

    public SimpleCaseModemProperties(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, List<String> globalModemInitStrings, List<String> modemInitStrings, String addressSelector, TimeDuration lineToggleDelay) {
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
    protected TimeDuration getConnectTimeout() {
        return this.connectTimeout;
    }

    @Override
    protected TimeDuration getDelayAfterConnect() {
        return this.delayAfterConnect;
    }

    @Override
    protected TimeDuration getDelayBeforeSend() {
        return this.delayBeforeSend;
    }

    @Override
    protected TimeDuration getCommandTimeOut() {
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
    protected TimeDuration getLineToggleDelay() {
        return this.lineToggleDelay;
    }
}