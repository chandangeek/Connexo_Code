package com.energyict.mdc.rest.impl;

import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.servers.ComServer;

public class ComServerInfo {

    public String fullName;
    public String name;

    public boolean active;
    public ComServer.LogLevel serverLogLevel;
    public ComServer.LogLevel communicationLogLevel;
    public TimeDuration changesInterPollDelay;
    public TimeDuration schedulingInterPollDelay;


    public ComServerInfo(ComServer comServer) {
        this.name = comServer.getName();
        this.fullName = comServer.getFullName();
        this.active = comServer.isActive();
        this.serverLogLevel = comServer.getServerLogLevel();
        this.communicationLogLevel = comServer.getCommunicationLogLevel();
        this.changesInterPollDelay = comServer.getChangesInterPollDelay();
        this.schedulingInterPollDelay = comServer.getSchedulingInterPollDelay();



    }
}
