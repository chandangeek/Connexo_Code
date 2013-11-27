package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.servers.OfflineComServer;
import com.energyict.mdc.servers.OnlineComServer;
import com.energyict.mdc.servers.RemoteComServer;
import java.util.List;

public class ComServerInfoFactory {

    public static ComServerInfo<?> asInfo(ComServer comServer) {
        if (OnlineComServer.class.isAssignableFrom(comServer.getClass())) {
            return new OnlineComServerInfo((OnlineComServer) comServer);
        }
        else if (OfflineComServerInfo.class.isAssignableFrom(comServer.getClass())) {
            return new OfflineComServerInfo((OfflineComServer) comServer);
        }
        else if (RemoteComServer.class.isAssignableFrom(comServer.getClass())) {
            return new RemoteComServerInfo((RemoteComServer) comServer);
        }
        else
            throw new IllegalArgumentException("Unsupported ComServer type "+comServer.getClass().getSimpleName());

    }

    public static ComServerInfo<?> asInfo(ComServer comServer, List<ComPort> comPortList) {
        if (OnlineComServer.class.isAssignableFrom(comServer.getClass())) {
            return new OnlineComServerInfo((OnlineComServer) comServer, comPortList);
        }
        else if (OfflineComServerInfo.class.isAssignableFrom(comServer.getClass())) {
            return new OfflineComServerInfo((OfflineComServer) comServer, comPortList);
        }
        else if (RemoteComServer.class.isAssignableFrom(comServer.getClass())) {
            return new RemoteComServerInfo((RemoteComServer) comServer, comPortList);
        }
        else
            throw new IllegalArgumentException("Unsupported ComServer type "+comServer.getClass().getSimpleName());

    }
}
