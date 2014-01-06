package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.UDPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class UDPBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements ServerUDPBasedInboundComPort {

    private int bufferSize;

    public static ServerUDPBasedInboundComPort from(DataModel dataModel, ComServer owner) {
        return dataModel.getInstance(UDPBasedInboundComPortImpl.class).init(owner);
    }

    private ServerUDPBasedInboundComPort init(ComServer owner) {
        this.setComServer(owner);
        return this;
    }

    protected UDPBasedInboundComPortImpl () {
        super();
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