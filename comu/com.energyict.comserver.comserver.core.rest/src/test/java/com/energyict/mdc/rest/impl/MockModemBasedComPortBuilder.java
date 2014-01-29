package com.energyict.mdc.rest.impl;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import java.math.BigDecimal;
import java.util.List;

public class MockModemBasedComPortBuilder implements ModemBasedInboundComPort.ModemBasedInboundComPortBuilder {

    public int ringCount;
    public int maximumDialErrors;
    public TimeDuration connectTimeout;
    public TimeDuration delayAfterConnect;
    public TimeDuration delayBeforeSend;
    public TimeDuration atCommandTimeout;
    public BigDecimal atCommandTry;
    public List<String> initStrings;
    public String addressSelector;
    public String postDialCommands;
    public SerialPortConfiguration serialPortConfiguration;
    public InboundComPortPool comPortPool;
    public String name;
    public ComPortType comPortType;
    public boolean active;
    public String description;

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder ringCount(int ringCount) {
        this.ringCount = ringCount;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder maximumDialErrors(int maximumDialErrors) {
        this.maximumDialErrors = maximumDialErrors;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder connectTimeout(TimeDuration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder delayAfterConnect(TimeDuration delayAfterConnect) {
        this.delayAfterConnect = delayAfterConnect;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder delayBeforeSend(TimeDuration delayBeforeSend) {
        this.delayBeforeSend = delayBeforeSend;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder atCommandTimeout(TimeDuration atCommandTimeout) {
        this.atCommandTimeout = atCommandTimeout;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder atCommandTry(BigDecimal atCommandTry) {
        this.atCommandTry = atCommandTry;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder atModemInitStrings(List<String> initStrings) {
        this.initStrings = initStrings;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder addressSelector(String addressSelector) {
        this.addressSelector = addressSelector;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder postDialCommands(String postDialCommands) {
        this.postDialCommands = postDialCommands;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder serialPortConfiguration(SerialPortConfiguration serialPortConfiguration) {
        this.serialPortConfiguration = serialPortConfiguration;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder comPortPool(InboundComPortPool comPortPool) {
        this.comPortPool = comPortPool;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public ModemBasedInboundComPort.ModemBasedInboundComPortBuilder description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public ModemBasedInboundComPort add() {
        return null;
    }

}