package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.OnlineComServer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonManagedReference;

@XmlRootElement(name = "OnlineComServer")
public class OnlineComServerInfo extends ComServerInfo {
    public String queryAPIPostUri;
    public boolean usesDefaultQueryAPIPostUri;
    public String eventRegistrationUri;
    public boolean usesDefaultEventRegistrationUri;
    public int storeTaskQueueSize;
    public int numberOfStoreTaskThreads;
    public int storeTaskThreadPriority;
    @JsonManagedReference
    public List<ComPortInfo> comPorts;

    public OnlineComServerInfo() {
    }

    public OnlineComServerInfo(OnlineComServer onlineComServer) {
        super(onlineComServer);
        this.comServerDescriptor="OnlineComServer";
        this.queryAPIPostUri = onlineComServer.getQueryApiPostUri();
        this.usesDefaultQueryAPIPostUri = onlineComServer.usesDefaultQueryApiPostUri();
        this.eventRegistrationUri = onlineComServer.getEventRegistrationUri();
        this.usesDefaultEventRegistrationUri = onlineComServer.usesDefaultEventRegistrationUri();
        this.storeTaskQueueSize = onlineComServer.getStoreTaskQueueSize();
        this.numberOfStoreTaskThreads = onlineComServer.getNumberOfStoreTaskThreads();
        this.storeTaskThreadPriority = onlineComServer.getStoreTaskThreadPriority();
        comPorts = new ArrayList<>();
        for (ComPort comPort : onlineComServer.getComPorts()) {
            comPorts.add(new ComPortInfo(comPort, this));
        }

    }

}
