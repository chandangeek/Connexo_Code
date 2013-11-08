package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import java.util.Date;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ComPort")

public class ComPortInfo {
    @XmlID
    public int id;
    public String name;
    public String comPortType;
    public String description;
    public Date modificationDate;
    public int numberOfSimultaneousConnections;
    public boolean active;
    public boolean bound;
    public int comserver_id;


    public ComPortInfo(ComPort comPort) {
        this.id = comPort.getId();
        this.name = comPort.getName();
        this.comPortType = comPort.getComPortType().toString();
        this.description = comPort.getDescription();
        this.modificationDate = comPort.getModificationDate();
        this.numberOfSimultaneousConnections = comPort.getNumberOfSimultaneousConnections();
        this.active = comPort.isActive();
        this.bound = comPort.isInbound();
        this.comserver_id = comPort.getComServer().getId();
    }
}
