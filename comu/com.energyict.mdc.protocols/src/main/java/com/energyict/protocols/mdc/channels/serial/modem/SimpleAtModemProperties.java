package com.energyict.protocols.mdc.channels.serial.modem;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.protocols.mdc.channels.serial.modem.postdial.AbstractAtPostDialCommand;

import java.math.BigDecimal;
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
    private List<String> modemInitStrings;
    private String addressSelector;
    private TimeDuration lineToggleDelay;
    private List<AbstractAtPostDialCommand> postDialCommands;

    public SimpleAtModemProperties(String phoneNumber, String atCommandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration atCommandTimeout, BigDecimal atCommandTry, List<String> modemInitStrings, String addressSelector, TimeDuration lineToggleDelay ,String postDialCommands) {
        this.phoneNumber = phoneNumber;
        this.atCommandPrefix = atCommandPrefix;
        this.connectTimeout = connectTimeout;
        this.delayAfterConnect = delayAfterConnect;
        this.delayBeforeSend = delayBeforeSend;
        this.atCommandTimeout = atCommandTimeout;
        this.atCommandTry = atCommandTry;
        this.modemInitStrings = modemInitStrings;
        this.addressSelector = addressSelector;
        this.lineToggleDelay = lineToggleDelay;
        this.postDialCommands = super.parseAndValidatePostDialCommands(postDialCommands);
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
    protected TimeDuration getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    protected TimeDuration getDelayAfterConnect() {
        return delayAfterConnect;
    }

    @Override
    protected TimeDuration getDelayBeforeSend() {
        return delayBeforeSend;
    }

    @Override
    protected TimeDuration getCommandTimeOut() {
        return atCommandTimeout;
    }

    @Override
    protected BigDecimal getCommandTry() {
        return atCommandTry;
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
    protected List<AbstractAtPostDialCommand> getPostDialCommands() {
        return postDialCommands;
    }

    @Override
    protected TimeDuration getLineToggleDelay() {
        return this.lineToggleDelay;
    }
}