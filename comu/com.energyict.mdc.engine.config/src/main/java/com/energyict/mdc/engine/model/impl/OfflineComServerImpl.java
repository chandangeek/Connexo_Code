package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.OfflineComServer;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OfflineComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:37)
 */
public class OfflineComServerImpl extends ComServerImpl implements ServerOfflineComServer {

    public static OfflineComServer from(DataModel dataModel) {
        return dataModel.getInstance(OfflineComServerImpl.class);
    }

    protected OfflineComServerImpl () {
        super();
    }

    @Override
    public String getType () {
        return OfflineComServer.class.getName();
    }

    @Override
    public boolean isOffline () {
        return true;
    }

}