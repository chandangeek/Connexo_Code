package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import java.util.List;

public class ComServerInfoFactory {

    public static ComServerInfo<?> asInfo(ComServer comServer) {
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

    public static ComServerInfo<?> asInfo(ComServer comServer, List<ComPort> comPortList, EngineModelService engineModelService) {
        if (OnlineComServer.class.isAssignableFrom(comServer.getClass())) {
            return new OnlineComServerInfo((OnlineComServer) comServer, comPortList, engineModelService);
        }
        else if (OfflineComServer.class.isAssignableFrom(comServer.getClass())) {
            return new OfflineComServerInfo((OfflineComServer) comServer, comPortList, engineModelService);
        }
        else if (RemoteComServer.class.isAssignableFrom(comServer.getClass())) {
            return new RemoteComServerInfo((RemoteComServer) comServer, comPortList, engineModelService);
        }
        else
            throw new IllegalArgumentException("Unsupported ComServer type "+comServer.getClass().getSimpleName());

    }
}
