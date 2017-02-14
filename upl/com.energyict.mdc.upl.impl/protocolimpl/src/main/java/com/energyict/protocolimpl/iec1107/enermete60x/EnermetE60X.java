/*
 * EnermetE70X.java
 *
 * Created on 28 juli 2004, 10:28
 */

package com.energyict.protocolimpl.iec1107.enermete60x;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.customerconfig.UcontoRegisterConfig;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.enermete70x.EnermetBase;

import java.io.IOException;

/**
 *
 * @author  Koen
 *
 * @beginchanges
KV|15022005|Changed RegisterConfig to allow B field obiscodes != 1
KV|23032005|Changed header to be compatible with protocol version tool
KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
KV|01092005|Add manufacturer specific code
 * @endchanges
 */
public class EnermetE60X extends EnermetBase implements SerialNumberSupport {

    private RegisterConfig regs = new UcontoRegisterConfig();

    public EnermetE60X(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected RegisterConfig getRegs() {
        return regs;
    }

    @Override
    public String getSerialNumber() {
        try {
            return getDataReadingCommandFactory().getSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:00 +0200 (Thu, 26 Nov 2015)$";
    }

}