package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.TCPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class TCPBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements ServerTCPBasedInboundComPort {

    protected TCPBasedInboundComPortImpl () {
        super();
    }

    public static ServerTCPBasedInboundComPort from(DataModel dataModel, ComServer owner) {
        return dataModel.getInstance(TCPBasedInboundComPortImpl.class).init(owner);
    }

    private ServerTCPBasedInboundComPort init(ComServer owner) {
        this.setComServer(owner);
        return this;
    }

    @Override
    public boolean isTCPBased() {
        return true;
    }

}