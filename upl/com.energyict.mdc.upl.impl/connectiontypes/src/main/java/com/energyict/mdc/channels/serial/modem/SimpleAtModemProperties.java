package com.energyict.mdc.channels.serial.modem;

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

    public SimpleAtModemProperties(String phoneNumber, String atCommandPrefix, TemporalAmount connectTimeout, TemporalAmount delayAfterConnect, TemporalAmount delayBeforeSend, TemporalAmount atCommandTimeout, BigDecimal atCommandTry, List<String> globalModemInitStrings, List<String> modemInitStrings, String addressSelector, TemporalAmount lineToggleDelay ,String postDialCommands) {
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
    protected String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    protected String getCommandPrefix() {
        return atCommandPrefix;
    }

    @Override
    protected TemporalAmount getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    protected TemporalAmount getDelayAfterConnect() {
        return delayAfterConnect;
    }

    @Override
    protected TemporalAmount getDelayBeforeSend() {
        return delayBeforeSend;
    }

    @Override
    protected TemporalAmount getCommandTimeOut() {
        return atCommandTimeout;
    }

    @Override
    protected BigDecimal getCommandTry() {
        return atCommandTry;
    }

    @Override
    protected List<String> getGlobalModemInitStrings() {
        return globalModemInitStrings;
    }

    @Override
    protected List<String> getModemInitStrings() {
        return modemInitStrings;
    }

    @Override
    protected String getAddressSelector() {
        return addressSelector;
    }

    @Override
    protected String getPostDialCommands() {
        return postDialCommands;
    }

    @Override
    protected TemporalAmount getLineToggleDelay() {
        return this.lineToggleDelay;
    }
}