/*
 * EnermetE70X.java
 *
 * Created on 28 juli 2004, 10:28
 */

package com.energyict.protocolimpl.iec1107.enermete60x;

import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.customerconfig.UcontoRegisterConfig;
import com.energyict.protocolimpl.iec1107.enermete70x.EnermetBase;

import javax.inject.Inject;

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
public class EnermetE60X extends EnermetBase {

    @Override
    public String getProtocolDescription() {
        return "Enernet E6xx IEC1107 (VDEW)";
    }

    RegisterConfig regs = new UcontoRegisterConfig();

    @Inject
    public EnermetE60X(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected RegisterConfig getRegs() {
        return regs;
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

} // class EnermetE60X
