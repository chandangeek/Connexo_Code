package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.shadow.ports.InboundComPortPoolShadow;
import java.util.ArrayList;
import java.util.List;

public class InboundComPortPoolInfo extends ComPortPoolInfo<InboundComPortPool> {

    public InboundComPortPoolInfo() {
    }

    public InboundComPortPoolInfo(InboundComPortPool comPortPool) {
        super(comPortPool);
        this.discoveryProtocolPluggableClassId = comPortPool.getDiscoveryProtocolPluggableClassId().getId();
        if (comPortPool.getComPorts()!=null) {
            this.inboundComPorts = new ArrayList<>(comPortPool.getComPorts().size());
            for (InboundComPort inboundComPort : comPortPool.getComPorts()) {
                inboundComPorts.add(ComPortInfoFactory.asInboundInfo(inboundComPort));
            }
        }
    }

    @Override
    protected void writeToShadow(InboundComPortPoolShadow shadow) {
        super.writeToShadow(shadow);
        shadow.setDiscoveryProtocolPluggableClassId(this.discoveryProtocolPluggableClassId);
        List<Integer> inboundComPortsIds = new ArrayList<>();
        if (inboundComPorts!=null && !inboundComPorts.isEmpty()) {
            for (InboundComPortInfo inboundComPort : this.inboundComPorts) {
                inboundComPortsIds.add(inboundComPort.id);
            }
        }

        shadow.setInboundComPortIds(inboundComPortsIds);

    }

    @Override
    public InboundComPortPoolShadow asShadow() {
        InboundComPortPoolShadow shadow = new InboundComPortPoolShadow();
        this.writeToShadow(shadow);
        return shadow;
    }
}
