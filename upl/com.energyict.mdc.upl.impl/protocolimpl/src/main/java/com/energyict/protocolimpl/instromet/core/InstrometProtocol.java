package com.energyict.protocolimpl.instromet.core;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.instromet.connection.InstrometConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class InstrometProtocol extends AbstractProtocol {

	private InstrometConnection instrometConnection=null;

    public InstrometProtocol(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    protected abstract void doTheInit() throws IOException ;

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
    	setInstrometConnection(
    			new InstrometConnection(
    					inputStream,
    					outputStream,
    					timeoutProperty,
    					protocolRetriesProperty,
    					forcedDelay,
    					echoCancelling,
    					halfDuplexController));
        doTheInit();
        return getInstrometConnection();
    }

    public InstrometConnection getInstrometConnection() {
        return instrometConnection;
    }

    protected void setInstrometConnection(InstrometConnection instrometConnection) {
        this.instrometConnection = instrometConnection;
    }

}