package com.energyict.mdc.rest.impl;

import com.energyict.cpo.ShadowList;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import com.energyict.mdc.shadow.ports.InboundComPortShadow;
import com.energyict.mdc.shadow.ports.OutboundComPortShadow;
import com.energyict.mdc.shadow.servers.InboundOutboundComServerShadow;
import java.util.ArrayList;
import java.util.List;

public abstract class InboundOutboundComServerInfo<S extends InboundOutboundComServerShadow> extends ComServerInfo<S> {

    public InboundOutboundComServerInfo() {
    }

    public InboundOutboundComServerInfo(ComServer comServer) {
        super(comServer);
    }

    public InboundOutboundComServerInfo(ComServer comServer, List<ComPort> comPorts) {
        super(comServer, comPorts);
    }

    protected final void updateInboundComPorts(InboundOutboundComServerShadow shadow) {
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

    protected final void updateOutboundComPorts(InboundOutboundComServerShadow shadow) {
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
