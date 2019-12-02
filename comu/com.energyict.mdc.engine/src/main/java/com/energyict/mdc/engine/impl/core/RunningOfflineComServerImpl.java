package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.OfflineComServer;
import com.energyict.mdc.engine.impl.core.remote.OfflineComServerDAOImpl;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;

import java.util.concurrent.Executors;


public class RunningOfflineComServerImpl extends RunningComServerImpl {

    public RunningOfflineComServerImpl(OfflineComServer comServer, OfflineComServerDAOImpl comServerDAO, ServiceProvider serviceProvider) {
        super(comServer, comServerDAO, null, null, Executors.defaultThreadFactory(), serviceProvider);
        comServerDAO.setComServer(this);
    }

    public RunningOfflineComServerImpl(OfflineComServer comServer, OfflineComServerDAOImpl comServerDAO, EmbeddedWebServerFactory embeddedWebServerFactory, ServiceProvider serviceProvider) {
        super(comServer, comServerDAO, null, null, Executors.defaultThreadFactory(), embeddedWebServerFactory, serviceProvider);
        comServerDAO.setComServer(this);
    }
}