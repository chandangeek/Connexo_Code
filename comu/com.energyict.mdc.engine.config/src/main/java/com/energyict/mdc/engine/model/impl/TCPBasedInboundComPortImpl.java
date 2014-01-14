package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.ComServer;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.TCPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class TCPBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements ServerTCPBasedInboundComPort {

    @Inject
    protected TCPBasedInboundComPortImpl(DataModel dataModel) {
        super(dataModel);
    }

    public void init(ComServer owner) {
        this.setComServer(owner);
    }

    @Override
    public boolean isTCPBased() {
        return true;
    }

}