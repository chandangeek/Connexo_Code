/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OfflineComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.RemoteComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;

import javax.inject.Inject;
import java.util.List;

public class ComServerInfoFactory {

    private final ComPortInfoFactory comPortInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public ComServerInfoFactory(ComPortInfoFactory comPortInfoFactory, Thesaurus thesaurus) {
        this.comPortInfoFactory = comPortInfoFactory;
        this.thesaurus = thesaurus;
    }

    public ComServerInfo<?,?> asInfo(ComServer comServer) {
        if (OnlineComServer.class.isAssignableFrom(comServer.getClass())) {
            ComServerInfo comServerInfo = new OnlineComServerInfo((OnlineComServer) comServer);
            comServerInfo.displayComServerType = TranslationKeys.COMSERVER_ONLINE.getDisplayName(thesaurus);
            return comServerInfo;
        }
        else if (OfflineComServer.class.isAssignableFrom(comServer.getClass())) {
            ComServerInfo comServerInfo = new OfflineComServerInfo((OfflineComServer) comServer);
            comServerInfo.displayComServerType = TranslationKeys.COMSERVER_OFFLINE.getDisplayName(thesaurus);
            return comServerInfo;
        }
        else if (RemoteComServer.class.isAssignableFrom(comServer.getClass())) {
            ComServerInfo comServerInfo = new RemoteComServerInfo((RemoteComServer) comServer);
            comServerInfo.displayComServerType = TranslationKeys.COMSERVER_REMOTE.getDisplayName(thesaurus);
            return comServerInfo;
        }
        else
            throw new IllegalArgumentException("Unsupported ComServer type "+comServer.getClass().getSimpleName());
    }

    public ComServerInfo<?, ?> asInfo(ComServer comServer, List<ComPort> comPortList, EngineConfigurationService engineConfigurationService) {
        if (OnlineComServer.class.isAssignableFrom(comServer.getClass())) {
            ComServerInfo comServerInfo = new OnlineComServerInfo((OnlineComServer) comServer, comPortList, engineConfigurationService, comPortInfoFactory);
            comServerInfo.displayComServerType = TranslationKeys.COMSERVER_ONLINE.getDisplayName(thesaurus);
            return comServerInfo;
        }
        else if (OfflineComServer.class.isAssignableFrom(comServer.getClass())) {
            ComServerInfo comServerInfo = new OfflineComServerInfo((OfflineComServer) comServer, comPortList, engineConfigurationService, comPortInfoFactory);
            comServerInfo.displayComServerType = TranslationKeys.COMSERVER_OFFLINE.getDisplayName(thesaurus);
            return comServerInfo;
        }
        else if (RemoteComServer.class.isAssignableFrom(comServer.getClass())) {
            ComServerInfo comServerInfo = new RemoteComServerInfo((RemoteComServer) comServer, comPortList, engineConfigurationService, comPortInfoFactory);
            comServerInfo.displayComServerType = TranslationKeys.COMSERVER_REMOTE.getDisplayName(thesaurus);
            return comServerInfo;
        }
        else
            throw new IllegalArgumentException("Unsupported ComServer type "+comServer.getClass().getSimpleName());

    }
}
