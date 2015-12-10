package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OfflineComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.RemoteComServer;
import javax.inject.Inject;

import java.util.List;

public class ComServerInfoFactory {

    private final ComPortInfoFactory comPortInfoFactory;

    @Inject
    public ComServerInfoFactory(ComPortInfoFactory comPortInfoFactory) {
        this.comPortInfoFactory = comPortInfoFactory;
    }

    public static ComServerInfo<?,?> asInfo(ComServer comServer) {
        if (OnlineComServer.class.isAssignableFrom(comServer.getClass())) {
            return new OnlineComServerInfo((OnlineComServer) comServer);
        }
        else if (OfflineComServer.class.isAssignableFrom(comServer.getClass())) {
            return new OfflineComServerInfo((OfflineComServer) comServer);
        }
        else if (RemoteComServer.class.isAssignableFrom(comServer.getClass())) {
            return new RemoteComServerInfo((RemoteComServer) comServer);
        }
        else
            throw new IllegalArgumentException("Unsupported ComServer type "+comServer.getClass().getSimpleName());

    }

    public ComServerInfo<?,?> asInfo(ComServer comServer, List<ComPort> comPortList, EngineConfigurationService engineConfigurationService) {
        if (OnlineComServer.class.isAssignableFrom(comServer.getClass())) {
            return new OnlineComServerInfo((OnlineComServer) comServer, comPortList, engineConfigurationService, comPortInfoFactory);
        }
        else if (OfflineComServer.class.isAssignableFrom(comServer.getClass())) {
            return new OfflineComServerInfo((OfflineComServer) comServer, comPortList, engineConfigurationService, comPortInfoFactory);
        }
        else if (RemoteComServer.class.isAssignableFrom(comServer.getClass())) {
            return new RemoteComServerInfo((RemoteComServer) comServer, comPortList, engineConfigurationService, comPortInfoFactory);
        }
        else
            throw new IllegalArgumentException("Unsupported ComServer type "+comServer.getClass().getSimpleName());

    }
}
