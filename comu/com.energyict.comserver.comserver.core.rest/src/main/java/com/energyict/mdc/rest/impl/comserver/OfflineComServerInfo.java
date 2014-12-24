package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OfflineComServer;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class OfflineComServerInfo extends ComServerInfo<OfflineComServer> {

    public OfflineComServerInfo() {
    }

    public OfflineComServerInfo(OfflineComServer comServer) {
        super(comServer);
    }

    public OfflineComServerInfo(OfflineComServer comServer, List<ComPort> comPorts, EngineConfigurationService engineConfigurationService) {
        super(comServer, comPorts, engineConfigurationService);
    }

    @Override
    public OfflineComServer writeTo(OfflineComServer source,EngineConfigurationService engineConfigurationService) {
        super.writeTo(source, engineConfigurationService);
        return source;
    }

    @Override
    protected OfflineComServer createNew(EngineConfigurationService engineConfigurationService) {
        return engineConfigurationService.newOfflineComServerInstance();
    }
}
