package com.energyict.mdc.channels.serial.modem;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 29/04/13 - 14:47
 */
public class SimplePEMPModemProperties extends AbstractPEMPModemProperties {

    private String phoneNumber;
    private String commandPrefix;
    private Duration connectTimeout;
    private Duration delayAfterConnect;
    private Duration delayBeforeSend;
    private Duration commandTimeout;
    private BigDecimal commandTry;
    private String modemInitStrings;
    private Duration lineToggleDelay;
    private PEMPModemConfiguration modemConfiguration;

    public SimplePEMPModemProperties(String phoneNumber, String commandPrefix, Duration connectTimeout, Duration delayAfterConnect, Duration delayBeforeSend, Duration commandTimeout, BigDecimal commandTry, String modemInitStrings, Duration lineToggleDelay, PEMPModemConfiguration modemConfiguration) {
        this.phoneNumber = phoneNumber;
        this.commandPrefix = commandPrefix;
        this.connectTimeout = connectTimeout;
        this.delayAfterConnect = delayAfterConnect;
        this.delayBeforeSend = delayBeforeSend;
        this.commandTimeout = commandTimeout;
        this.commandTry = commandTry;
        this.modemInitStrings = modemInitStrings;
        this.lineToggleDelay = lineToggleDelay;
        this.modemConfiguration = modemConfiguration;
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
        return new ArrayList<>(0);
    }

    @Override
    protected List<String> getModemInitStrings() {
        List<String> modemInitStringList = new ArrayList<>();
        modemInitStringList.add(this.modemInitStrings);
        return modemInitStringList;
    }

    @Override
    protected Duration getLineToggleDelay() {
        return this.lineToggleDelay;
    }

    @Override
    protected PEMPModemConfiguration getPEMPModemConfiguration() {
        return  modemConfiguration;
    }
}
