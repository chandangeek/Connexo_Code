package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.UDPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class UDPBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements UDPBasedInboundComPort {

    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    @Min(value=1, groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_VALUE_TOO_SMALL+"}")
    private int bufferSize;

    @Inject
    protected UDPBasedInboundComPortImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    public boolean isUDPBased() {
        return true;
    }

    @Override
    protected void copyFrom(ComPort source) {
        super.copyFrom(source);
        this.setBufferSize(((UDPBasedInboundComPort)source).getBufferSize());
    }

    @Override
    public int getBufferSize () {
        return bufferSize;
    }

    @Override
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    static class UDPBasedInboundComPortBuilderImpl
            extends IpBasedInboundComPortBuilderImpl<UDPBasedInboundComPort.UDPBasedInboundComPortBuilder, UDPBasedInboundComPort>
            implements UDPBasedInboundComPort.UDPBasedInboundComPortBuilder {

        protected UDPBasedInboundComPortBuilderImpl(UDPBasedInboundComPort ipBasedInboundComPort, String name, int numberOfSimultaneousConnections, int portNumber) {
            super(UDPBasedInboundComPort.UDPBasedInboundComPortBuilder.class, ipBasedInboundComPort, name, numberOfSimultaneousConnections, portNumber);
            comPort.setComPortType(ComPortType.UDP);
        }

        @Override
        public UDPBasedInboundComPortBuilder bufferSize(int bufferSize) {
            comPort.setBufferSize(bufferSize);
            return this;
        }
    }
    
}