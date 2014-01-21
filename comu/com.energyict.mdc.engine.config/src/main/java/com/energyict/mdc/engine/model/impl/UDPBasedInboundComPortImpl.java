package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.google.inject.Provider;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.UDPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class UDPBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements UDPBasedInboundComPort {

    private int bufferSize;

    @Inject
    protected UDPBasedInboundComPortImpl(DataModel dataModel, Provider<ComPortPoolMember> comPortPoolMemberProvider) {
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

    @Override
    public void setBufferSize(int bufferSize) {
        validateGreaterThanZero(bufferSize, "comport.datagrambuffersize");
        this.bufferSize = bufferSize;
    }
    
    static class UDPBasedInboundComPortBuilderImpl
            extends IpBasedInboundComPortBuilderImpl<UDPBasedInboundComPort.UDPBasedInboundComPortBuilder, UDPBasedInboundComPort>
            implements UDPBasedInboundComPort.UDPBasedInboundComPortBuilder {

        protected UDPBasedInboundComPortBuilderImpl(Provider<UDPBasedInboundComPort> ipBasedInboundComPortProvider) {
            super(UDPBasedInboundComPort.UDPBasedInboundComPortBuilder.class, ipBasedInboundComPortProvider);
        }

        @Override
        public UDPBasedInboundComPortBuilder bufferSize(int bufferSize) {
            comPort.setBufferSize(bufferSize);
            return this;
        }
    }
    
}