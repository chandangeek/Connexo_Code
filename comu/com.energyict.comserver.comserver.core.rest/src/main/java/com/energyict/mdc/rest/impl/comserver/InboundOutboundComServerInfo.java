package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public abstract class InboundOutboundComServerInfo<S extends ComServer> extends ComServerInfo<S> {

    public InboundOutboundComServerInfo() {
    }

    public InboundOutboundComServerInfo(ComServer comServer) {
        super(comServer);
    }

    public InboundOutboundComServerInfo(ComServer comServer, List<ComPort> comPorts) {
        super(comServer, comPorts);
    }

    protected final void updateInboundComPorts(ComServer comServer,EngineModelService engineModelService) {
        List<ComPort> inboundComPorts = new ArrayList<>();
        for (InboundComPortInfo comPortInfo : this.inboundComPorts) {
            InboundComPort inboundComPort;
            if(comPortInfo.id>0){
               inboundComPort = (InboundComPort) engineModelService.findComPort(comPortInfo.id);
            } else {
               inboundComPort = comPortInfo.createNew(comServer, engineModelService);
            }
            comPortInfo.writeTo(inboundComPort,engineModelService);
            inboundComPorts.add(inboundComPort);
            comServer.setComPorts(inboundComPorts);
        }
    }

    protected final void updateOutboundComPorts(ComServer comServer,EngineModelService engineModelService) {
        List<ComPort> outboundComPorts = new ArrayList<>();
        for (OutboundComPortInfo comPortInfo : this.outboundComPorts) {
            if(comPortInfo.id>0){
                OutboundComPort comPort = (OutboundComPort) engineModelService.findComPort(comPortInfo.id);
                comPortInfo.writeTo(comPort,engineModelService);
                outboundComPorts.add(comPort);
            } else {
                OutboundComPort serverOutboundComPort = engineModelService.newOutbound(comServer);
                comPortInfo.writeTo(serverOutboundComPort,engineModelService);
                outboundComPorts.add(serverOutboundComPort);
            }
        }
        comServer.setComPorts(outboundComPorts);
    }
}
