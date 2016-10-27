package com.energyict.mdc.channels.serial.modem;

import com.energyict.cbo.TimeDuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 29/04/13 - 14:09
 */
public class SimplePaknetModemProperties extends AbstractPaknetModemProperties {

    private String phoneNumber;
    private String commandPrefix;
    private TimeDuration connectTimeout;
    private TimeDuration delayAfterConnect;
    private TimeDuration delayBeforeSend;
    private TimeDuration commandTimeout;
    private BigDecimal commandTry;
    private String modemInitStrings;
    private TimeDuration lineToggleDelay;

    public SimplePaknetModemProperties(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, String modemInitStrings, TimeDuration lineToggleDelay) {
        this.phoneNumber = phoneNumber;
        this.commandPrefix = commandPrefix;
        this.connectTimeout = connectTimeout;
        this.delayAfterConnect = delayAfterConnect;
        this.delayBeforeSend = delayBeforeSend;
        this.commandTimeout = commandTimeout;
        this.commandTry = commandTry;
        this.modemInitStrings = modemInitStrings;
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
        return new ArrayList<>(0);
    }

    @Override
    protected List<String> getModemInitStrings() {
        List<String> modemInitStringList = new ArrayList<>();
        modemInitStringList.add(this.modemInitStrings);
        return modemInitStringList;
    }

    @Override
    protected TimeDuration getLineToggleDelay() {
        return this.lineToggleDelay;
    }
}
