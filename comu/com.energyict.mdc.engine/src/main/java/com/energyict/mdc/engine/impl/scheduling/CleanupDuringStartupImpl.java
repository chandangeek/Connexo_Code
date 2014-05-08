package com.energyict.mdc.engine.impl.scheduling;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.OutboundCapableComServer;

import java.sql.SQLException;

/**
 * Provides a default implementation for the {@link CleanupDuringStartup} interface
 * that uses the {@link ComServerDAO}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-01 (16:33)
 */
public class CleanupDuringStartupImpl implements CleanupDuringStartup {

    private ComServerDAO comServerDAO;
    private OutboundCapableComServer comServer;

    public CleanupDuringStartupImpl (OutboundCapableComServer comServer, ComServerDAO comServerDAO) {
        super();
        this.comServer = comServer;
        this.comServerDAO = comServerDAO;
    }

    @Override
    public void releaseInterruptedTasks () throws SQLException {
        this.comServerDAO.releaseInterruptedTasks(this.comServer);
    }

}