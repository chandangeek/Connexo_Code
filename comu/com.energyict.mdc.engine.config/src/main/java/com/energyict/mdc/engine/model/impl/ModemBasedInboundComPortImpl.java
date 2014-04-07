package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.channels.serial.BaudrateValue;
import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.protocol.api.channels.serial.NrOfDataBits;
import com.energyict.mdc.protocol.api.channels.serial.NrOfStopBits;
import com.energyict.mdc.protocol.api.channels.serial.Parities;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import com.google.common.collect.Range;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.ModemBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class ModemBasedInboundComPortImpl extends InboundComPortImpl implements ModemBasedInboundComPort, InboundComPort {

    @Min(value = 1, groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_VALUE_TOO_SMALL+"}")
    private int ringCount;
    @Min(value = 1, groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_VALUE_TOO_SMALL+"}")
    private int maximumDialErrors;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private TimeDuration connectTimeout;
    private TimeDuration delayAfterConnect;
    private TimeDuration delayBeforeSend;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private TimeDuration atCommandTimeout;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private BigDecimal atCommandTry;
    private String modemInitStrings;
    private String addressSelector;
    private String postDialCommands;
    @Valid
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private LegacySerialPortConfiguration serialPortConfiguration;

    @Inject
    protected ModemBasedInboundComPortImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
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
        if (!Range.<Integer>closed(1, 1).contains(numberOfSimultaneousConnections)) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.VALUE_NOT_IN_RANGE);
        }
    }

    @Override
    public void setRingCount(int ringCount) {
        this.ringCount = ringCount;
    }

    @Override
    public int getMaximumDialErrors() {
        return maximumDialErrors;
    }

    @Override
    public void setMaximumDialErrors(int maximumDialErrors) {
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
        return serialPortConfiguration!=null?serialPortConfiguration.asEnum():null;
    }

    @Override
    public void setSerialPortConfiguration(SerialPortConfiguration serialPortConfiguration) {
        if (serialPortConfiguration==null) {
            this.serialPortConfiguration=null;
        } else {
            this.serialPortConfiguration = new LegacySerialPortConfiguration(serialPortConfiguration);
        }
    }

    @Override
    public boolean isModemBased() {
        return true;
    }

    @Override
    protected void copyFrom(ComPort source) {
        super.copyFrom(source);
        ModemBasedInboundComPort mySource = (ModemBasedInboundComPort) source;
        this.setRingCount(mySource.getRingCount());
        this.setMaximumDialErrors(mySource.getMaximumDialErrors());
        this.setConnectTimeout(mySource.getConnectTimeout());
        this.setDelayAfterConnect(mySource.getDelayAfterConnect());
        this.setDelayBeforeSend(mySource.getDelayBeforeSend());
        this.setAtCommandTimeout(mySource.getAtCommandTimeout());
        this.setAtCommandTry(mySource.getAtCommandTry());
        this.setModemInitStrings(mySource.getModemInitStrings());
        this.setAddressSelector(mySource.getAddressSelector());
        this.setPostDialCommands(mySource.getPostDialCommands());
        this.setSerialPortConfiguration(mySource.getSerialPortConfiguration());
    }

    public static class ModemBasedInboundComPortBuilderImpl extends InboundComPortBuilderImpl<ModemBasedInboundComPortBuilder, ModemBasedInboundComPort>
            implements ModemBasedInboundComPortBuilder {

        protected ModemBasedInboundComPortBuilderImpl(ModemBasedInboundComPort comPort, String name) {
            super(ModemBasedInboundComPortBuilder.class, comPort, name);
            comPort.setComPortType(ComPortType.SERIAL);
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

    }

    public static class LegacySerialPortConfiguration {
        @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
        private String comPortName;
        @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
        private BigDecimal baudrate;
        @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
        private BigDecimal nrOfDataBits;
        @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
        private BigDecimal nrOfStopBits;
        @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
        private String flowControl;
        @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
        private String parity;

        private LegacySerialPortConfiguration() {
        }

        public LegacySerialPortConfiguration(SerialPortConfiguration serialPortConfiguration) {
            comPortName=serialPortConfiguration.getComPortName();
            baudrate= serialPortConfiguration.getBaudrate().getBaudrate();
            nrOfDataBits=serialPortConfiguration.getNrOfDataBits().getNrOfDataBits();
            nrOfStopBits=serialPortConfiguration.getNrOfStopBits().getNrOfStopBits();
            parity=serialPortConfiguration.getParity().getParity();
            flowControl=serialPortConfiguration.getFlowControl().getFlowControl();
        }

        private String getComPortName() {
            return comPortName;
        }

        private void setComPortName(String comPortName) {
            this.comPortName = comPortName;
        }

        private BigDecimal getBaudrate() {
            return baudrate;
        }

        private void setBaudrate(BigDecimal baudrate) {
            this.baudrate = baudrate;
        }

        private BigDecimal getNrOfDataBits() {
            return nrOfDataBits;
        }

        private void setNrOfDataBits(BigDecimal nrOfDataBits) {
            this.nrOfDataBits = nrOfDataBits;
        }

        private BigDecimal getNrOfStopBits() {
            return nrOfStopBits;
        }

        private void setNrOfStopBits(BigDecimal nrOfStopBits) {
            this.nrOfStopBits = nrOfStopBits;
        }

        private String getFlowControl() {
            return flowControl;
        }

        private void setFlowControl(String flowControl) {
            this.flowControl = flowControl;
        }

        private String getParity() {
            return parity;
        }

        private void setParity(String parity) {
            this.parity = parity;
        }

        public SerialPortConfiguration asEnum() {
            return new SerialPortConfiguration(comPortName,
                    BaudrateValue.valueFor(baudrate),
                    NrOfDataBits.valueFor(nrOfDataBits),
                    NrOfStopBits.valueFor(nrOfStopBits),
                    Parities.valueFor(parity),
                    FlowControl.valueFor(flowControl));
        }
    }
}