package com.energyict.mdc.rest.impl;

import com.energyict.cpo.ShadowList;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import com.energyict.mdc.shadow.ports.InboundComPortShadow;
import com.energyict.mdc.shadow.ports.OutboundComPortShadow;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "comServerType")
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
    public List<InboundComPortInfo<? extends ComPortShadow>> inboundComPorts;
    public List<OutboundComPortInfo> outboundComPorts;

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
        inboundComPorts = new ArrayList<>();
        outboundComPorts = new ArrayList<>();
        for (final ComPort comPort : comServer.getComPorts()) {
            if (InboundComPort.class.isAssignableFrom(comPort.getClass())) {
                inboundComPorts.add(ComPortInfoFactory.asInboundInfo(comPort));
            } else {
                outboundComPorts.add(ComPortInfoFactory.asOutboundInfo(comPort));
            }
        }
    }

    public ComServerInfo(ComServer comServer, List<ComPort> comPorts) {
        this(comServer);
        inboundComPorts = new ArrayList<>();
        outboundComPorts = new ArrayList<>();
        for (final ComPort comPort : comPorts) {
            if (InboundComPort.class.isAssignableFrom(comPort.getClass())) {
                inboundComPorts.add(ComPortInfoFactory.asInboundInfo(comPort));
            } else {
                outboundComPorts.add(ComPortInfoFactory.asOutboundInfo(comPort));
            }
        }
    }

    public OnlineComServerShadow writeToShadow(OnlineComServerShadow shadow) {
        shadow.setName(name);
        shadow.setActive(active);
        shadow.setServerLogLevel(serverLogLevel);
        shadow.setCommunicationLogLevel(communicationLogLevel);
        shadow.setChangesInterPollDelay(changesInterPollDelay.asTimeDuration());
        shadow.setSchedulingInterPollDelay(schedulingInterPollDelay.asTimeDuration());

        updateInboundComPorts(shadow);
        updateOutboundComPorts(shadow);

        return shadow;
    }

    private void updateInboundComPorts(OnlineComServerShadow shadow) {
        ShadowList<InboundComPortShadow> inboundComPortShadows = shadow.getInboundComPortShadows();
        List<InboundComPortShadow> toBeDeletedInbound = new ArrayList<>(inboundComPortShadows);
        for (InboundComPortInfo comPort : inboundComPorts) {
            boolean configuredComPortFound = false;
            for (InboundComPortShadow comPortShadow : inboundComPortShadows) {
                if (comPort.id==comPortShadow.getId()) {
                    configuredComPortFound=true;
                    comPort.writeToShadow(comPortShadow);
                    toBeDeletedInbound.remove(comPortShadow);
                }
            }
            if (!configuredComPortFound) {
                ComPortShadow comPortShadow = comPort.asShadow();
                inboundComPortShadows.add((InboundComPortShadow) comPortShadow);
            }
        }
        for (InboundComPortShadow inboundComPortShadow : toBeDeletedInbound) {
            inboundComPortShadows.remove(inboundComPortShadow);
        }
    }

    private void updateOutboundComPorts(OnlineComServerShadow shadow) {
        ShadowList<OutboundComPortShadow> outboundComPortShadows = shadow.getOutboundComPortShadows();
        List<OutboundComPortShadow> toBeDeletedOutbound = new ArrayList<>(outboundComPortShadows);
        for (OutboundComPortInfo comPort : outboundComPorts) {
            boolean configuredComPortFound = false;
            for (OutboundComPortShadow comPortShadow : outboundComPortShadows) {
                if (comPort.id==comPortShadow.getId()) {
                    configuredComPortFound=true;
                    comPort.writeToShadow(comPortShadow);
                    toBeDeletedOutbound.remove(comPortShadow);
                }
            }
            if (!configuredComPortFound) {
                ComPortShadow comPortShadow = comPort.asShadow();
                outboundComPortShadows.add((OutboundComPortShadow) comPortShadow);
            }
        }
        for (OutboundComPortShadow outboundComPortShadow : toBeDeletedOutbound) {
            outboundComPortShadows.remove(outboundComPortShadow);
        }
    }
}
