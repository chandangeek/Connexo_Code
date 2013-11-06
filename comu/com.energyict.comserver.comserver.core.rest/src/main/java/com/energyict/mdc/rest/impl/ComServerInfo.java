package com.energyict.mdc.rest.impl;

import com.energyict.mdc.servers.ComServer;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ComServerInfo {

    public String comServerDescriptor;
    public String fullName;
    public String name;

    public boolean active;
    public ComServer.LogLevel serverLogLevel;
    public ComServer.LogLevel communicationLogLevel;
    public TimeDurationInfo changesInterPollDelay;
    public TimeDurationInfo schedulingInterPollDelay;

    public ComServerInfo() {
    }

    public ComServerInfo(ComServer comServer) {
        this.name = comServer.getName();
        this.fullName = comServer.getFullName();
        this.active = comServer.isActive();
        this.serverLogLevel = comServer.getServerLogLevel();
        this.communicationLogLevel = comServer.getCommunicationLogLevel();
        this.changesInterPollDelay = new TimeDurationInfo(comServer.getChangesInterPollDelay());
        this.schedulingInterPollDelay = new TimeDurationInfo(comServer.getSchedulingInterPollDelay());
    }
}
