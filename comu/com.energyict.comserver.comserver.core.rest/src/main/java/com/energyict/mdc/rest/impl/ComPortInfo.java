package com.energyict.mdc.rest.impl;

import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public abstract class ComPortInfo<T extends ComPortShadow> {

    public int id;
    public String direction;
    public String name;
    public String description;
    public boolean active;
    public boolean bound;
    public ComPortType comPortType;
    public int comServer_id;
    public int numberOfSimultaneousConnections;
    public Date modificationDate;
    public Integer ringCount;
    public Integer maximumNumberOfDialErrors;
    public TimeDurationInfo connectTimeout;
    public TimeDurationInfo delayAfterConnect;
    public TimeDurationInfo delayBeforeSend;
    public TimeDurationInfo atCommandTimeout;
    public BigDecimal atCommandTry;
    public List<Map<String, String>> modemInitStrings;
    public String addressSelector;
    public String postDialCommands;
    public String comPortName;
    public BaudrateValue baudrate;
    public NrOfDataBits nrOfDataBits;
    public NrOfStopBits nrOfStopBits;
    public FlowControl flowControl;
    public Parities parity;
    public Integer comPortPool_id;
    public Integer portNumber;
    public Integer bufferSize;
    public Boolean useHttps;
    public String keyStoreFilePath;
    public String trustStoreFilePath;
    public String keyStorePassword;
    public String trustStorePassword;
    public String contextPath;

    public ComPortInfo() {
    }

    public ComPortInfo(ComPort comPort) {
        this.id = comPort.getId();
        this.name = comPort.getName();
        this.description = comPort.getDescription();
        this.active = comPort.isActive();
        this.bound = comPort.isInbound();
        this.comServer_id = comPort.getComServer()!=null?comPort.getComServer().getId():0;
        this.comPortType = comPort.getComPortType();
        this.numberOfSimultaneousConnections = comPort.getNumberOfSimultaneousConnections();
        this.modificationDate = comPort.getModificationDate();
    }

    protected void writeToShadow(T shadow) {
        shadow.setName(name);
        shadow.setDescription(description);
        shadow.setComServerId(comServer_id);
        shadow.setActive(active);
        shadow.setType(this.comPortType);
    }

    public abstract T asShadow();
}
