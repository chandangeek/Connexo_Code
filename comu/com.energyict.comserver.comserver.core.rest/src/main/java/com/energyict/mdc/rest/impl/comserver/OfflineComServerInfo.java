package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OfflineComServer;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class OfflineComServerInfo extends ComServerInfo<OfflineComServer> {

    public OfflineComServerInfo() {
    }

    public OfflineComServerInfo(OfflineComServer comServer) {
        super(comServer);
    }

    public OfflineComServerInfo(OfflineComServer comServer, List<ComPort> comPorts) {
        super(comServer, comPorts);
    }

    @Override
    public OfflineComServer writeTo(OfflineComServer source,EngineModelService engineModelService) {
        super.writeTo(source, engineModelService);
        return source;
    }

    @Override
    protected OfflineComServer createNew(EngineModelService engineModelService) {
        return engineModelService.newOfflineComServerInstance();
    }
}
