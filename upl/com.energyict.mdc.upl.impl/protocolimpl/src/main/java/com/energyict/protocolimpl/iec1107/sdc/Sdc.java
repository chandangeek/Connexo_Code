/*
 * Sdc.java
 *
 * Created on 28 juli 2004, 10:28
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;

import java.io.IOException;
// com.energyict.protocolimpl.iec1107.sdc.Sdc
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
public class Sdc extends SdcBase implements SerialNumberSupport {

    private RegisterConfig regs = new EDPRegisterConfig(); // we should use an infotype property to determine the registerset

    Sdc(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected RegisterConfig getRegs() {
        return regs;
    }

    @Override
    public String getSerialNumber() {
        ObisCode oc = new ObisCode(1,0,0,0,0,255);
        try {
            return readRegister(oc).getText();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:25:14 +0200 (Thu, 26 Nov 2015)$";
    }

}