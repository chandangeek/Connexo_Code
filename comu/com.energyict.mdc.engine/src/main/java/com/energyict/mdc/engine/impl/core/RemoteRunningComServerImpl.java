package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.impl.core.remote.RemoteComServerDAOImpl;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.issues.IssueService;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Extends the {@link RunningComServerImpl} and specializes on
 * {@link com.energyict.mdc.engine.model.RemoteComServer}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-21 (16:20)
 */
public class RemoteRunningComServerImpl extends RunningComServerImpl {

    public RemoteRunningComServerImpl(RemoteComServer comServer, RemoteComServerDAOImpl comServerDAO, ServiceProvider serviceProvider) {
        super(comServer, comServerDAO, null, null, Executors.defaultThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
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