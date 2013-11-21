package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.ports.InboundComPortPool;
import com.energyict.mdc.shadow.ports.InboundComPortPoolShadow;
import java.util.ArrayList;
import java.util.List;

public class InboundComPortPoolInfo extends ComPortPoolInfo<InboundComPortPoolShadow> {
    private List<InboundComPortInfo> inboundComPorts;
    private int discoveryProtocolPluggableClassId;

    public InboundComPortPoolInfo() {
    }

    public InboundComPortPoolInfo(InboundComPortPool comPortPool) {
        super(comPortPool);
        this.discoveryProtocolPluggableClassId = comPortPool.getDiscoveryProtocolPluggableClass().getId();
        this.inboundComPorts = new ArrayList<>(comPortPool.getComPorts().size());
        for (InboundComPort inboundComPort : comPortPool.getComPorts()) {
            inboundComPorts.add(ComPortInfoFactory.asInboundInfo(inboundComPort));
        }
    }

    @Override
    protected void writeToShadow(InboundComPortPoolShadow shadow) {
        super.writeToShadow(shadow);
        shadow.setDiscoveryProtocolPluggableClassId(this.discoveryProtocolPluggableClassId);
        List<Integer> inboundComPortsIds = new ArrayList<>(this.inboundComPorts.size());
        for (InboundComPortInfo inboundComPort : this.inboundComPorts) {
            inboundComPortsIds.add(inboundComPort.id);
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
