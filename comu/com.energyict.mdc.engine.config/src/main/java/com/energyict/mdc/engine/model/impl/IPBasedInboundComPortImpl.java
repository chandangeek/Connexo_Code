package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.IPBasedInboundComPort;
import com.google.inject.Provider;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.IPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public abstract class IPBasedInboundComPortImpl extends InboundComPortImpl implements IPBasedInboundComPort {

    private int portNumber;
    private int numberOfSimultaneousConnections;

    protected IPBasedInboundComPortImpl(DataModel dataModel, Provider<ComPortPoolMember> comPortPoolMemberProvider) {
        super(dataModel, comPortPoolMemberProvider);
    }

    @Override
    public int getPortNumber() {
        return portNumber;
    }

    @Override
    public void setPortNumber(int portNumber) {
        validateGreaterThanZero(this.portNumber, "comport.portnumber");
        this.portNumber = portNumber;
    }

    @Override
    public int getNumberOfSimultaneousConnections() {
        return numberOfSimultaneousConnections;
    }

    @Override
    public void setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections) {
        validateGreaterThanZero(this.numberOfSimultaneousConnections, "comport.numberofsimultaneousconnections");
        this.numberOfSimultaneousConnections = numberOfSimultaneousConnections;
    }

    protected void validate() {
        super.validate();
        validateDuplicatePortNumber();
    }

    private void validateDuplicatePortNumber()  {
        for (ComPort comPort : getComServer().getComPorts()) {
            if(comPort.getId() != this.getId()){
                if(IPBasedInboundComPort.class.isAssignableFrom(comPort.getClass())){
                    IPBasedInboundComPort ipBasedInboundComPort = (IPBasedInboundComPort) comPort;
                    if(ipBasedInboundComPort.getPortNumber() == this.portNumber){
                        throw new TranslatableApplicationException("duplicatecomportpercomserver", "'{0}' should be unique per comserver (duplicate: {1})", "comport.portnumber", portNumber);
                    }
                }
            }
        }
    }

    static class IpBasedInboundComPortBuilderImpl<B extends IpBasedInboundComPortBuilder<B,C>, C extends ServerIPBasedInboundComPort & ServerInboundComPort>
            extends InboundComPortBuilderImpl<B, C>
            implements IpBasedInboundComPortBuilder<B,C> {
        protected IpBasedInboundComPortBuilderImpl(Class<B> clazz, Provider<C> ipBasedInboundComPortProvider) {
            super(clazz, ipBasedInboundComPortProvider);
        }

        @Override
        public IpBasedInboundComPortBuilder portNumber(int portNumber) {
            comPort.setPortNumber(portNumber);
            return this;
        }
    }


}