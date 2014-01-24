package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.rest.impl.TimeDurationInfo;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

public class OutboundComPortPoolInfo extends ComPortPoolInfo<OutboundComPortPool> {

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
    protected OutboundComPortPool writeTo(OutboundComPortPool source, EngineModelService engineModelService) {
        super.writeTo(source,engineModelService);
        source.setTaskExecutionTimeout(this.taskExecutionTimeout.asTimeDuration());
        if (outboundComPorts !=null) {
            for (OutboundComPortInfo outboundComPortInfo : this.outboundComPorts) {
                OutboundComPort outboundComPort;
                if(outboundComPortInfo.id>0){
                    outboundComPort = (OutboundComPort)engineModelService.findComPort(outboundComPortInfo.id);
                    outboundComPortInfo.writeTo(outboundComPort,engineModelService);
                } else {
                    ComServer comServer = engineModelService.findComServer(outboundComPortInfo.comServer_id);
                    if(comServer!=null){
                        outboundComPort = outboundComPortInfo.createNew(comServer, engineModelService);
                        outboundComPortInfo.writeTo(outboundComPort,engineModelService);
                    } else {
                        throw new WebApplicationException("Could not find comserver with id "+outboundComPortInfo.comServer_id, Response.Status.BAD_REQUEST);
                    }
                }
                outboundComPortInfo.writeTo(outboundComPort,engineModelService);
            }
        }
        return source;
    }


    @Override
    protected OutboundComPortPool createNew(EngineModelService engineModelService) {
        return engineModelService.newOutboundComPortPool();
    }
}
