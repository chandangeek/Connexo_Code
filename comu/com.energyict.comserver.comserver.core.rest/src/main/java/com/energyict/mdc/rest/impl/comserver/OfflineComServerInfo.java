package com.energyict.mdc.rest.impl.comserver;

import com.energyict.cpo.ShadowList;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import com.energyict.mdc.shadow.ports.OutboundComPortShadow;
import com.energyict.mdc.shadow.servers.OfflineComServerShadow;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OfflineComServerInfo extends ComServerInfo<OfflineComServerShadow> {

    public OfflineComServerInfo() {
    }

    public OfflineComServerInfo(OfflineComServer comServer) {
        super(comServer);
    }

    public OfflineComServerInfo(OfflineComServer comServer, List<ComPort> comPorts) {
        super(comServer, comPorts);
    }

    @Override
    public OfflineComServerShadow writeToShadow(OfflineComServerShadow shadow) {
        super.writeToShadow(shadow);
        updateOutboundComPorts(shadow);
        return shadow;
    }

    public OfflineComServerShadow asShadow() {
        OfflineComServerShadow offlineComServerShadow = new OfflineComServerShadow();
        this.writeToShadow(offlineComServerShadow);
        return offlineComServerShadow;
    }

    private void updateOutboundComPorts(OfflineComServerShadow shadow) {
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
