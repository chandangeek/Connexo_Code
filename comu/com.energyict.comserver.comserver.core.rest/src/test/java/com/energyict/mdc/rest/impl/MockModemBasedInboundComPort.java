package com.energyict.mdc.rest.impl;

import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import com.google.common.base.Optional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 17/01/14
 * Time: 8:51
 */
public class MockModemBasedInboundComPort implements ModemBasedInboundComPort {

    private int ringCount;
    private int dialErrors;
    private TimeDuration connectTimeOut;
    private TimeDuration delayAfterConnect;
    private TimeDuration delayBeforeSend;
    private TimeDuration atCommandTimeout;
    private BigDecimal atCommandTry;
    private List<String> modemInitStrings;
    private String addressSelector;
    private String s;
    private String postDialCommands;
    private SerialPortConfiguration serialPortConfiguration;
    private InboundComPortPool inboundComPortPool;
    private ComServer comServer;
    private boolean active;
    private String description;
    private String name;
    private ComPortType type;
    private int numberOfSimultaneousConnections;
    private boolean obsolete;

    @Override
    public int getRingCount() {
        return ringCount;
    }

    @Override
    public void setRingCount(int i) {
        ringCount = i;
    }

    @Override
    public int getMaximumDialErrors() {
        return dialErrors;
    }

    @Override
    public void setMaximumDialErrors(int i) {
        dialErrors = i;
    }

    @Override
    public TimeDuration getConnectTimeout() {
        return connectTimeOut;
    }

    @Override
    public void setConnectTimeout(TimeDuration timeDuration) {
        this.connectTimeOut = timeDuration;
    }

    @Override
    public TimeDuration getDelayAfterConnect() {
        return delayAfterConnect;
    }

    @Override
    public void setDelayAfterConnect(TimeDuration timeDuration) {
        delayAfterConnect = timeDuration;
    }

    @Override
    public TimeDuration getDelayBeforeSend() {
        return delayBeforeSend;
    }

    @Override
    public void setDelayBeforeSend(TimeDuration timeDuration) {
        delayBeforeSend = timeDuration;
    }

    @Override
    public TimeDuration getAtCommandTimeout() {
        return atCommandTimeout;
    }

    @Override
    public void setAtCommandTimeout(TimeDuration timeDuration) {
        atCommandTimeout = timeDuration;
    }

    @Override
    public BigDecimal getAtCommandTry() {
        return atCommandTry;
    }

    @Override
    public void setAtCommandTry(BigDecimal bigDecimal) {
        atCommandTry = bigDecimal;
    }

    @Override
    public List<String> getModemInitStrings() {
        return modemInitStrings;
    }

    @Override
    public void setModemInitStrings(List<String> strings) {
        modemInitStrings = strings;
    }

    @Override
    public String getAddressSelector() {
        return addressSelector;
    }

    @Override
    public void setAddressSelector(String s) {
        addressSelector = s;
    }

    @Override
    public String getPostDialCommands() {
        return postDialCommands;
    }

    @Override
    public void setPostDialCommands(String s) {
        postDialCommands = s;
    }

    @Override
    public SerialPortConfiguration getSerialPortConfiguration() {
        return serialPortConfiguration;
    }

    @Override
    public void setSerialPortConfiguration(SerialPortConfiguration serialPortConfiguration) {
        this.serialPortConfiguration = serialPortConfiguration;
    }

    @Override
    public Optional<InboundComPortPool> getComPortPool() {
        return Optional.of(inboundComPortPool);
    }

    @Override
    public void setComPortPool(InboundComPortPool inboundComPortPool) {
        this.inboundComPortPool = inboundComPortPool;
    }

    @Override
    public boolean isTCPBased() {
        return false;
    }

    @Override
    public boolean isUDPBased() {
        return false;
    }

    @Override
    public boolean isModemBased() {
        return true;
    }

    @Override
    public boolean isServletBased() {
        return false;
    }

    @Override
    public UtcInstant getModificationDate() {
        return new UtcInstant(new Date());
    }

    @Override
    public ComServer getComServer() {
        return comServer;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean b) {
        active = b;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isInbound() {
        return true;
    }

    @Override
    public int getNumberOfSimultaneousConnections() {
        return numberOfSimultaneousConnections;
    }

    @Override
    public void setNumberOfSimultaneousConnections(int i) {
        numberOfSimultaneousConnections = i;
    }

    @Override
    public ComPortType getComPortType() {
        return type;
    }

    @Override
    public void setComPortType(ComPortType comPortType) {
        type = comPortType;
    }

    @Override
    public String getType() {
        return type.name();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String s) {
        name = s;
    }

    @Override
    public void setDescription(String s) {
        description = s;
    }

    @Override
    public long getId() {
        return 3;
    }

    @Override
    public void save() {
        //do nothing
    }

    @Override
    public void makeObsolete() {
        obsolete = true;
    }

    @Override
    public boolean isObsolete() {
        return obsolete;
    }

    @Override
    public Date getObsoleteDate() {
        return new Date();
    }
}
