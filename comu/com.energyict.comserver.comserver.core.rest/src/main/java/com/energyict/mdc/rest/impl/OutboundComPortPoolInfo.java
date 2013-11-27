package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.OutboundComPort;
import com.energyict.mdc.ports.OutboundComPortPool;
import com.energyict.mdc.shadow.ports.OutboundComPortPoolShadow;
import java.util.ArrayList;
import java.util.List;

public class OutboundComPortPoolInfo extends ComPortPoolInfo<OutboundComPortPoolShadow> {

    public OutboundComPortPoolInfo() {
    }

    public OutboundComPortPoolInfo(OutboundComPortPool comPortPool) {
        super(comPortPool);
        if (comPortPool.getComPorts()!=null) {
            outboundComPorts = new ArrayList<>(comPortPool.getComPorts().size());
            for (OutboundComPort outboundComPort : comPortPool.getComPorts()) {
                outboundComPorts.add(ComPortInfoFactory.asOutboundInfo(outboundComPort));
            }
        }
        taskExecutionTimeout = new TimeDurationInfo(comPortPool.getTaskExecutionTimeout());
    }

    @Override
    protected void writeToShadow(OutboundComPortPoolShadow shadow) {
        super.writeToShadow(shadow);
        shadow.setTaskExecutionTimeout(this.taskExecutionTimeout.asTimeDuration());
        List<Integer> outboundComPortsIds = new ArrayList<>();
        if (this.outboundComPorts!=null && !this.outboundComPorts.isEmpty()) {
            for (OutboundComPortInfo outboundComPort : this.outboundComPorts) {
                outboundComPortsIds.add(outboundComPort.id);
            }
        }
        shadow.setOutboundComPortIds(outboundComPortsIds);
    }

    @Override
    public OutboundComPortPoolShadow asShadow() {
        OutboundComPortPoolShadow shadow = new OutboundComPortPoolShadow();
        this.writeToShadow(shadow);
        return shadow;
    }
}
