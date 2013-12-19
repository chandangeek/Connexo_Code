package com.energyict.mdc.engine.model.impl;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.UDPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class UDPBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements ServerUDPBasedInboundComPort {

    private int bufferSize;

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