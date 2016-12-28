/*
 * EnermetE70X.java
 *
 * Created on 28 juli 2004, 10:28
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author  Koen
 *
 * @beginchanges
KV|08112004|HHU's getSerialNumber() implementation uses securitylevel 1 and password 1. Should have a public read. Mail has been send to Asko!
KV|15022005|Changed RegisterConfig to allow B field obiscodes != 1
KV|23032005|Changed header to be compatible with protocol version tool
KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
KV|29072005|Changes to parsing of event log numbers (0x)
KV|01092005|Add manufacturer specific code
 * @endchanges
 */
public class EnermetE70X extends EnermetBase {

    private RegisterConfig regs = new EDPRegisterConfig(); // we should use an infotype property to determine the registerset

    public EnermetE70X(PropertySpecService propertySpecService) {
    	super(propertySpecService);
        setTestE70xConnection(true);
    }

    @Override
    protected RegisterConfig getRegs() {
        return regs;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-05-13 16:50:47 +0200 (Wed, 13 May 2015) $";
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        iec1107Connection=new EnermetE70XIEC1107Connection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor,ERROR_SIGNATURE, software7E1);
        iec1107Connection.setNoBreakRetry(isTestE70xConnection());
        enermetLoadProfile = new EnermetLoadProfile(this);
        return iec1107Connection;
    }

}