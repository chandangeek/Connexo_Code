package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.rest.impl.TimeDurationInfo;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public abstract class ComPortInfo<T extends ComPortShadow> {

    public int id;
    public String direction;
    public String name;
    public String description;
    public boolean active;
    public boolean bound;
    @XmlJavaTypeAdapter(ComPortTypeAdapter.class)
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
    @XmlJavaTypeAdapter(BaudrateAdapter.class)
    public BaudrateValue baudrate;
    @XmlJavaTypeAdapter(NrOfDataBitsAdapter.class)
    public NrOfDataBits nrOfDataBits;
    @XmlJavaTypeAdapter(NrOfStopBitsAdapter.class)
    public NrOfStopBits nrOfStopBits;
    @XmlJavaTypeAdapter(value = FlowControlAdapter.class)
    public FlowControl flowControl;
    @XmlJavaTypeAdapter(ParitiesAdapter.class)
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
