package com.energyict.mdc.rest.impl;

import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.ports.ModemBasedInboundComPort;
import com.energyict.mdc.shadow.ports.ModemBasedInboundComPortShadow;

public class ModemInboundComPortInfo extends InboundComPortInfo<ModemBasedInboundComPortShadow> {


    public ModemInboundComPortInfo() {
        this.comPortType = ComPortType.SERIAL;
    }

    public ModemInboundComPortInfo(ModemBasedInboundComPort comPort) {
        super(comPort);
        this.ringCount = comPort.getRingCount();
        this.maximumNumberOfDialErrors = comPort.getMaximumNumberOfDialErrors();
        this.connectTimeout = comPort.getConnectTimeout()!=null?new TimeDurationInfo(comPort.getConnectTimeout()):null;
        this.delayAfterConnect = comPort.getDelayAfterConnect()!=null?new TimeDurationInfo(comPort.getDelayAfterConnect()):null;
        this.delayBeforeSend = comPort.getDelayBeforeSend()!=null?new TimeDurationInfo(comPort.getDelayBeforeSend()):null;
        this.atCommandTimeout = comPort.getAtCommandTimeout()!=null?new TimeDurationInfo(comPort.getAtCommandTimeout()):null;
        this.atCommandTry = comPort.getAtCommandTry();
        this.modemInitStrings = comPort.getModemInitStrings();
        this.addressSelector = comPort.getAddressSelector();
        this.postDialCommands = comPort.getPostDialCommands();
        if (comPort.getSerialPortConfiguration()!=null) {
            this.comPortName = comPort.getSerialPortConfiguration().getComPortName();
            this.baudrate = comPort.getSerialPortConfiguration().getBaudrate();
            this.nrOfDataBits = comPort.getSerialPortConfiguration().getNrOfDataBits();
            this.nrOfStopBits = comPort.getSerialPortConfiguration().getNrOfStopBits();
            this.flowControl = comPort.getSerialPortConfiguration().getFlowControl();
            this.parity = comPort.getSerialPortConfiguration().getParity();
        }
    }

    @Override
    protected void writeToShadow(ModemBasedInboundComPortShadow shadow) {
        super.writeToShadow(shadow);
        shadow.setRingCount(this.ringCount);
        shadow.setMaximumNumberOfDialErrors(this.maximumNumberOfDialErrors);
        if (this.connectTimeout!=null) {
            shadow.setConnectTimeout(this.connectTimeout.asTimeDuration());
        }
        if (this.delayAfterConnect!=null) {
            shadow.setDelayAfterConnect(this.delayAfterConnect.asTimeDuration());
        }
        if (this.delayBeforeSend!=null) {
            shadow.setDelayBeforeSend(this.delayBeforeSend.asTimeDuration());
        }
        if (this.atCommandTimeout!=null) {
            shadow.setAtCommandTimeout(this.atCommandTimeout.asTimeDuration());
        }
        shadow.setAtCommandTry(this.atCommandTry);
        shadow.setModemInitStrings(this.modemInitStrings);
        shadow.setAddressSelector(this.addressSelector);
        shadow.setPostDialCommands(this.postDialCommands);
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
