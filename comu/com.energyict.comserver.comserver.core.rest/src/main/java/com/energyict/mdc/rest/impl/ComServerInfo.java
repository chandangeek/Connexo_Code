package com.energyict.mdc.rest.impl;

import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comServerDescriptor")
@JsonSubTypes({ @JsonSubTypes.Type(value = OnlineComServerInfo.class, name = "OnlineComServer")
     /*, @JsonSubTypes.Type(value = RemoteComServerInfo.class, name = "RemoteComServer")*/
     /*, @JsonSubTypes.Type(value = OfflineComServerInfo.class, name = "OfflineComServer")*/ })
public class ComServerInfo {
    public int id;

    public String name;

    public boolean active;
    public ComServer.LogLevel serverLogLevel;
    public ComServer.LogLevel communicationLogLevel;
    public TimeDurationInfo changesInterPollDelay;
    public TimeDurationInfo schedulingInterPollDelay;

    public ComServerInfo() {
    }

    public ComServerInfo(ComServer comServer) {
        this.id=comServer.getId();
        this.name = comServer.getName();
        this.active = comServer.isActive();
        this.serverLogLevel = comServer.getServerLogLevel();
        this.communicationLogLevel = comServer.getCommunicationLogLevel();
        this.changesInterPollDelay = new TimeDurationInfo(comServer.getChangesInterPollDelay());
        this.schedulingInterPollDelay = new TimeDurationInfo(comServer.getSchedulingInterPollDelay());
    }

    public OnlineComServerShadow asShadow() {
        OnlineComServerShadow shadow = new OnlineComServerShadow();
        shadow.setName(name);
        shadow.setActive(active);
        shadow.setServerLogLevel(serverLogLevel);
        shadow.setCommunicationLogLevel(communicationLogLevel);
        shadow.setChangesInterPollDelay(new TimeDuration(changesInterPollDelay.count+" "+changesInterPollDelay.timeUnit));
        shadow.setSchedulingInterPollDelay(new TimeDuration(schedulingInterPollDelay.count+" "+schedulingInterPollDelay.timeUnit));
        return shadow;
    }
}
