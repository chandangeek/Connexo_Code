package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.ComServer;
import com.google.inject.Provider;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.UDPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class UDPBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements ServerUDPBasedInboundComPort {

    private int bufferSize;

    public void init(ComServer owner) {
        this.setComServer(owner);
    }

    @Inject
    protected UDPBasedInboundComPortImpl(DataModel dataModel, Provider<ComPortPoolMemberImpl> comPortPoolMemberProvider) {
        super(dataModel, comPortPoolMemberProvider);
    }

    @Override
    public boolean isUDPBased() {
        return true;
    }

    protected void validate () {
        super.validate();
        validateNotNull(this.bufferSize, "comport.datagrambuffersize");
    }


    @Override
    public int getBufferSize () {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        validateGreaterThanZero(this.bufferSize, "comport.datagrambuffersize");
        this.bufferSize = bufferSize;
    }
}