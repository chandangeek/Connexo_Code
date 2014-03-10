package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.IPBasedInboundComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.google.inject.Provider;
import javax.validation.Payload;
import javax.validation.constraints.Min;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.IPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
@UniquePortNumber(groups = {Save.Create.class, Save.Update.class})
public abstract class IPBasedInboundComPortImpl extends InboundComPortImpl implements IPBasedInboundComPort, Payload {

    @Min(value = 1, groups = {Save.Create.class, Save.Update.class}, message = "{"+ Constants.MDC_VALUE_TOO_SMALL+"}")
    private int portNumber;
    @Min(value = 1, groups = {Save.Create.class, Save.Update.class}, message = "{"+ Constants.MDC_VALUE_TOO_SMALL+"}")
    private int numberOfSimultaneousConnections;

    protected IPBasedInboundComPortImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public int getPortNumber() {
        return portNumber;
    }

    @Override
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    @Override
    public int getNumberOfSimultaneousConnections() {
        return numberOfSimultaneousConnections;
    }

    @Override
    public void setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections) {
        this.numberOfSimultaneousConnections = numberOfSimultaneousConnections;
    }

    protected void validateCreate() {
        super.validateCreate();
        validateDuplicatePortNumber();
    }

    @Override
    protected void copyFrom(ComPort source) {
        super.copyFrom(source);
        IPBasedInboundComPort mySource = (IPBasedInboundComPort) source;
        this.setPortNumber(mySource.getPortNumber());
        this.setNumberOfSimultaneousConnections(mySource.getNumberOfSimultaneousConnections());
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

    static class IpBasedInboundComPortBuilderImpl<B extends IpBasedInboundComPortBuilder<B,C>, C extends IPBasedInboundComPort & InboundComPort>
            extends InboundComPortBuilderImpl<B, C>
            implements IpBasedInboundComPortBuilder<B,C> {
        protected IpBasedInboundComPortBuilderImpl(Class<B> clazz, Provider<C> ipBasedInboundComPortProvider) {
            super(clazz, ipBasedInboundComPortProvider);
        }

        @Override
        public B portNumber(int portNumber) {
            comPort.setPortNumber(portNumber);
            return self;
        }

        @Override
        public B numberOfSimultaneousConnections(int numberOfConnections) {
            comPort.setNumberOfSimultaneousConnections(numberOfConnections);
            return self;
        }
    }


}