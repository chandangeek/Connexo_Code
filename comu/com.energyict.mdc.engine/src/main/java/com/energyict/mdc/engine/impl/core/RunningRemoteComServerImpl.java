/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.engine.impl.core.remote.RemoteComServerDAOImpl;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Extends the {@link RunningComServerImpl} and specializes on
 * {@link com.energyict.mdc.engine.config.RemoteComServer}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-21 (16:20)
 */
public class RunningRemoteComServerImpl extends RunningComServerImpl {

    public RunningRemoteComServerImpl(RemoteComServer comServer, RemoteComServerDAOImpl comServerDAO, ServiceProvider serviceProvider) {
        super(comServer, comServerDAO, null, null, Executors.defaultThreadFactory(), serviceProvider);
        comServerDAO.setComServer(this);
    }

    public RunningRemoteComServerImpl(RemoteComServer comServer, RemoteComServerDAOImpl comServerDAO, EmbeddedWebServerFactory embeddedWebServerFactory, ServiceProvider serviceProvider) {
        super(comServer, comServerDAO, null, null, Executors.defaultThreadFactory(), embeddedWebServerFactory, serviceProvider);
        comServerDAO.setComServer(this);
    }

    @Override
    protected void startComServerDAO () {
        if (this.comServerDAONeedsStarting()) {
            super.startComServerDAO();
        }
    }

    private boolean comServerDAONeedsStarting () {
        Set<ServerProcessStatus> notStartedOrStarting =
                EnumSet.complementOf(EnumSet.of(
                            ServerProcessStatus.STARTED,
                            ServerProcessStatus.STARTING));
        return notStartedOrStarting.contains(this.getComServerDAO().getStatus());
    }

}