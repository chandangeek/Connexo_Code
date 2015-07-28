package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.io.BaudrateValue;
import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.NrOfDataBits;
import com.energyict.mdc.io.NrOfStopBits;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.google.common.collect.Range;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.ModemBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class ModemBasedInboundComPortImpl extends InboundComPortImpl implements ModemBasedInboundComPort, InboundComPort {

    @Min(value = 1, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}")
    private int ringCount;
    @Min(value = 1, groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}")
    private int maximumDialErrors;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private TimeDuration connectTimeout;
    private TimeDuration delayAfterConnect;
    private TimeDuration delayBeforeSend;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private TimeDuration atCommandTimeout;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private BigDecimal atCommandTry;
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
    private String modemInitStrings;
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
    private String globalModemInitStrings;
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
    private String addressSelector;
    @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
    private String postDialCommands;
    @Valid
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
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
    public List<String> getGlobalModemInitStrings() {
        if (Checks.is(globalModemInitStrings).emptyOrOnlyWhiteSpace()) {
            return Collections.emptyList();
        }
        return Arrays.asList(globalModemInitStrings.split(";")); // TODO Fix AtModemComponent location
    }

    @Override
    public void setGlobalModemInitStrings(List<String> globalModemInitStrings) {
        StringBuilder composedString = new StringBuilder();
        if (globalModemInitStrings != null && !globalModemInitStrings.isEmpty()) {
            for (String each : globalModemInitStrings) {
                composedString.append(each);
                composedString.append(";"); // TODO Fix AtModemComponent location
            }
            this.globalModemInitStrings = composedString.substring(0, composedString.length() - 1);
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
        return serialPortConfiguration!=null?serialPortConfiguration.asEnumWithName(getName()):null;
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
        public ModemBasedInboundComPortBuilder globalAtModemInitStrings(List<String> globalAtModemInitStrings) {
            comPort.setGlobalModemInitStrings(globalAtModemInitStrings);
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
        @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
        private BigDecimal baudrate;
        @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
        private BigDecimal nrOfDataBits;
        @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
        private BigDecimal nrOfStopBits;
        @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
        @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
        private String flowControl;
        @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
        @Size(max= Table.SHORT_DESCRIPTION_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
        private String parity;

        private LegacySerialPortConfiguration() {
        }

        public LegacySerialPortConfiguration(SerialPortConfiguration serialPortConfiguration) {
            baudrate= serialPortConfiguration.getBaudrate().value();
            nrOfDataBits=serialPortConfiguration.getNrOfDataBits().value();
            nrOfStopBits=serialPortConfiguration.getNrOfStopBits().value();
            parity=serialPortConfiguration.getParity().value();
            flowControl=serialPortConfiguration.getFlowControl().value();
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

        public SerialPortConfiguration asEnumWithName(String name) {
            return new SerialPortConfiguration(name,
                    BaudrateValue.valueFor(baudrate),
                    NrOfDataBits.valueFor(nrOfDataBits),
                    NrOfStopBits.valueFor(nrOfStopBits),
                    Parities.valueFor(parity),
                    FlowControl.valueFor(flowControl));
        }
    }
}