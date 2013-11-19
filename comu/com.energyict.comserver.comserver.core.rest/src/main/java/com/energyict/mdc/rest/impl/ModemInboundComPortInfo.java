package com.energyict.mdc.rest.impl;

import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.ports.ModemBasedInboundComPort;
import com.energyict.mdc.shadow.ports.ModemBasedInboundComPortShadow;
import java.math.BigDecimal;
import java.util.List;

public class ModemInboundComPortInfo extends ComPortInfo<ModemBasedInboundComPortShadow> {

    public int ringCount;
    public int maximumNumberOfDialErrors;
    public TimeDurationInfo connectTimeout;
    public TimeDurationInfo delayAfterConnect;
    public TimeDurationInfo delayBeforeSend;
    public TimeDurationInfo atCommandTimeout;
    public BigDecimal atCommandTry;
    public List<String> modemInitStrings;
    public String addressSelector;
    public String postDialCommands;
    public SerialPortConfiguration serialPortConfiguration;
    public String comPortName;
    public BaudrateValue baudrate;
    public NrOfDataBits nrOfDataBits;
    public NrOfStopBits nrOfStopBits;
    public FlowControl flowControl;
    public Parities parity;

    public ModemInboundComPortInfo() {
    }

    public ModemInboundComPortInfo(ModemBasedInboundComPort comPort) {
        super(comPort);
        this.ringCount = comPort.getRingCount();
        this.maximumNumberOfDialErrors = comPort.getMaximumNumberOfDialErrors();
        this.connectTimeout = new TimeDurationInfo(comPort.getConnectTimeout());
        this.delayAfterConnect = new TimeDurationInfo(comPort.getDelayAfterConnect());
        this.delayBeforeSend = new TimeDurationInfo(comPort.getDelayBeforeSend());
        this.atCommandTimeout = new TimeDurationInfo(comPort.getAtCommandTimeout());
        this.atCommandTry = comPort.getAtCommandTry();
        this.modemInitStrings = comPort.getModemInitStrings();
        this.addressSelector = comPort.getAddressSelector();
        this.postDialCommands = comPort.getPostDialCommands();
        this.serialPortConfiguration = comPort.getSerialPortConfiguration();
        this.comPortName = comPort.getSerialPortConfiguration().getComPortName();
        this.baudrate = comPort.getSerialPortConfiguration().getBaudrate();
        this.nrOfDataBits = comPort.getSerialPortConfiguration().getNrOfDataBits();
        this.nrOfStopBits = comPort.getSerialPortConfiguration().getNrOfStopBits();
        this.flowControl = comPort.getSerialPortConfiguration().getFlowControl();
        this.parity = comPort.getSerialPortConfiguration().getParity();
    }

    @Override
    protected void writeToShadow(ModemBasedInboundComPortShadow shadow) {
        super.writeToShadow(shadow);
        shadow.setRingCount(this.ringCount);
        shadow.setMaximumNumberOfDialErrors(this.maximumNumberOfDialErrors);
        shadow.setConnectTimeout(this.connectTimeout.asTimeDuration());
        shadow.setDelayAfterConnect(this.delayAfterConnect.asTimeDuration());
        shadow.setDelayBeforeSend(this.delayBeforeSend.asTimeDuration());
        shadow.setAtCommandTimeout(this.atCommandTimeout.asTimeDuration());
        shadow.setAtCommandTry(this.atCommandTry);
        shadow.setModemInitStrings(this.modemInitStrings);
        shadow.setAddressSelector(this.addressSelector);
        shadow.setPostDialCommands(this.postDialCommands);
        shadow.setSerialPortConfiguration(this.serialPortConfiguration);
        shadow.setSerialPortConfiguration(new SerialPortConfiguration(
                this.comPortName,
                this.baudrate,
                this.nrOfDataBits,
                this.nrOfStopBits,
                this.parity,
                this.flowControl));
    }

    @Override
    public ModemBasedInboundComPortShadow asShadow() {
        ModemBasedInboundComPortShadow shadow = new ModemBasedInboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }
}
