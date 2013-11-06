package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.OnlineComServer;
import java.util.Date;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonBackReference;

@XmlRootElement(name="ComPort")

public class ComPortInfo {
    @XmlID
    public int id;
    public String comPortType;
    public String description;
    public Date modificationDate;
    public int numberOfSimultaneousConnections;
    public boolean active;
    public boolean bound;
    @JsonBackReference
    public ComServerInfo comServer;


    public ComPortInfo(ComPort comPort) {
        this(comPort, new OnlineComServerInfo((OnlineComServer) comPort.getComServer()));
    }

    public ComPortInfo(ComPort comPort, OnlineComServerInfo onlineComServerInfo) {
        this.id = comPort.getId();
        this.comPortType = comPort.getComPortType().toString();
        this.description = comPort.getDescription();
        this.modificationDate = comPort.getModificationDate();
        this.numberOfSimultaneousConnections = comPort.getNumberOfSimultaneousConnections();
        this.active = comPort.isActive();
        this.bound = comPort.isInbound();
        this.comServer = onlineComServerInfo;
    }
}
