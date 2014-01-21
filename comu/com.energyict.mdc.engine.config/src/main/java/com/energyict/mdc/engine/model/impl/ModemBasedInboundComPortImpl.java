package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.ServerComChannelBasedInboundComPort;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.google.common.collect.Range;
import com.google.inject.Provider;

import java.util.Collections;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.ModemBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class ModemBasedInboundComPortImpl extends InboundComPortImpl implements ServerComChannelBasedInboundComPort, ModemBasedInboundComPort {

    private int ringCount;
    private int maximumDialErrors;
    private TimeDuration connectTimeout;
    private TimeDuration delayAfterConnect;
    private TimeDuration delayBeforeSend;
    private TimeDuration atCommandTimeout;
    private BigDecimal atCommandTry;
    private String modemInitStrings;
    private String addressSelector;
    private String postDialCommands;
    private SerialPortConfiguration serialPortConfiguration;

    @Inject
    protected ModemBasedInboundComPortImpl(DataModel dataModel, Provider<ComPortPoolMember> comPortPoolMemberProvider) {
        super(dataModel, comPortPoolMemberProvider);
    }

    public int getRingCount() {
        return ringCount;
    }

    @Override
    public int getNumberOfSimultaneousConnections() {
        return 1;
    }

    @Override
    public void setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections) {
        validateInRange(Range.<Integer>closed(1, 1), numberOfSimultaneousConnections, "numberOfSimultaneousConnections");
        // NO-OP
    }

    @Override
    public void setRingCount(int ringCount) {
        validateGreaterThanZero(ringCount, "comport.ringcount");
        this.ringCount = ringCount;
    }

    @Override
    public int getMaximumDialErrors() {
        return maximumDialErrors;
    }

    @Override
    public void setMaximumDialErrors(int maximumDialErrors) {
        validateGreaterThanZero(maximumDialErrors, "comport.maxnumberofdialerrors");
        this.maximumDialErrors = maximumDialErrors;
    }

    @Override
    public TimeDuration getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(TimeDuration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public TimeDuration getDelayAfterConnect() {
        return delayAfterConnect;
    }

    @Override
    public void setDelayAfterConnect(TimeDuration delayAfterConnect) {
        this.delayAfterConnect = delayAfterConnect;
    }

    @Override
    public TimeDuration getDelayBeforeSend() {
        return delayBeforeSend;
    }

    @Override
    public void setDelayBeforeSend(TimeDuration delayBeforeSend) {
        this.delayBeforeSend = delayBeforeSend;
    }

    @Override
    public TimeDuration getAtCommandTimeout() {
        return atCommandTimeout;
    }

    @Override
    public void setAtCommandTimeout(TimeDuration atCommandTimeout) {
        this.atCommandTimeout = atCommandTimeout;
    }

    @Override
    public BigDecimal getAtCommandTry() {
        return atCommandTry;
    }

    @Override
    public void setAtCommandTry(BigDecimal atCommandTry) {
        this.atCommandTry = atCommandTry;
    }

    @Override
    public List<String> getModemInitStrings() {
        if (Checks.is(modemInitStrings).emptyOrOnlyWhiteSpace()) {
            return Collections.emptyList();
        }
        return Arrays.asList(modemInitStrings.split(";")); // TODO Fix AtModemComponent location
    }

    @Override
    public void setModemInitStrings(List<String> modemInitStrings) {
        StringBuilder composedString = new StringBuilder();
        if (modemInitStrings != null && !modemInitStrings.isEmpty()) {
            for (String each : modemInitStrings) {
                composedString.append(each);
                composedString.append(";"); // TODO Fix AtModemComponent location
            }
            this.modemInitStrings = composedString.substring(0, composedString.length() - 1);
        }
    }

    @Override
    public String getAddressSelector() {
        return addressSelector;
    }

    @Override
    public void setAddressSelector(String addressSelector) {
        this.addressSelector = addressSelector;
    }

    @Override
    public String getPostDialCommands() {
        return postDialCommands;
    }

    @Override
    public void setPostDialCommands(String postDialCommands) {
        this.postDialCommands = postDialCommands;
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
    public boolean isModemBased() {
        return true;
    }

    protected void validate() {
        super.validate();
        validateNotNull(this.getRingCount(), "comport.ringcount");
        validateNotNull(this.getMaximumDialErrors(), "comport.maxnumberofdialerrors");
        validateNotNull(this.getConnectTimeout(), "comport.connecttimeout");
        validateNotNull(this.getAtCommandTimeout(), "comport.atcommandtimeout");
        validateNotNull(this.getAtCommandTry(), "comport.atcommandtry");
        validateNotNull(this.getSerialPortConfiguration(), "comport.serailportconfiguration");
        validateNotNull(this.getSerialPortConfiguration().getComPortName(), "comport.comportname");
        validateSerialPortConfigurationComPortName();
        validateNotNull(this.getSerialPortConfiguration().getBaudrate(), "comport.baudrate");
        validateNotNull(this.getSerialPortConfiguration().getNrOfDataBits(), "comport.numberofdatabits");
        validateNotNull(this.getSerialPortConfiguration().getNrOfStopBits(), "comport.numberofstopbits");
        validateNotNull(this.getSerialPortConfiguration().getParity(), "comport.parity");
        validateNotNull(this.getSerialPortConfiguration().getFlowControl(), "comport.flowcontrol");
    }

    private void validateSerialPortConfigurationComPortName() {
        if (!this.getName().equals(this.getSerialPortConfiguration().getComPortName())) {
            throw new TranslatableApplicationException("comport.serialportconfigurationcomportname", "The comport name of the serial port configuration ({0}) should match the name of the comport ({1}).",
                    this.getSerialPortConfiguration().getComPortName(), this.getName());
        }
    }

    public static class ModemBasedInboundComPortBuilderImpl extends InboundComPortBuilderImpl<ModemBasedInboundComPortBuilder, ModemBasedInboundComPort>
            implements ModemBasedInboundComPortBuilder {

        protected ModemBasedInboundComPortBuilderImpl(Provider<ModemBasedInboundComPort> inboundComPortProvider) {
            super(ModemBasedInboundComPortBuilder.class, inboundComPortProvider);
        }

        @Override
        public ModemBasedInboundComPortBuilder ringCount(int ringCount) {
            comPort.setRingCount(ringCount);
            return this;
        }

        @Override
        public ModemBasedInboundComPortBuilder maximumDialErrors(int maximumDialErrors) {
            comPort.setMaximumDialErrors(maximumDialErrors);
            return this;
        }

        @Override
        public ModemBasedInboundComPortBuilder connectTimeout(TimeDuration connectTimeout) {
            comPort.setConnectTimeout(connectTimeout);
            return this;
        }

        @Override
        public ModemBasedInboundComPortBuilder delayAfterConnect(TimeDuration delayAfterConnect) {
            comPort.setDelayAfterConnect(delayAfterConnect);
            return this;
        }

        @Override
        public ModemBasedInboundComPortBuilder delayBeforeSend(TimeDuration delayBeforeSend) {
            comPort.setDelayBeforeSend(delayBeforeSend);
            return this;
        }

        @Override
        public ModemBasedInboundComPortBuilder atCommandTimeout(TimeDuration atCommandTimeout) {
            comPort.setAtCommandTimeout(atCommandTimeout);
            return this;
        }

        @Override
        public ModemBasedInboundComPortBuilder atCommandTry(BigDecimal atCommandTry) {
            comPort.setAtCommandTry(atCommandTry);
            return this;
        }

        @Override
        public ModemBasedInboundComPortBuilder atModemInitStrings(List<String> initStrings) {
            comPort.setModemInitStrings(initStrings);
            return this;
        }

        @Override
        public ModemBasedInboundComPortBuilder addressSelector(String addressSelector) {
            comPort.setAddressSelector(addressSelector);
            return this;
        }

        @Override
        public ModemBasedInboundComPortBuilder postDialCommands(String postDialCommands) {
            comPort.setPostDialCommands(postDialCommands);
            return this;
        }

        @Override
        public ModemBasedInboundComPortBuilder serialPortConfiguration(SerialPortConfiguration serialPortConfiguration) {
            comPort.setSerialPortConfiguration(serialPortConfiguration);
            return this;
        }
    }
}