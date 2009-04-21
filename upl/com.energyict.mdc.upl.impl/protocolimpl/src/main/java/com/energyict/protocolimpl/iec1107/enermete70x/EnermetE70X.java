/*
 * EnermetE70X.java
 *
 * Created on 28 juli 2004, 10:28
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
// com.energyict.protocolimpl.iec1107.enermete70x.EnermetE70X
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
    
    RegisterConfig regs = new EDPRegisterConfig(); // we should use an infotype property to determine the registerset
    
    /** Creates a new instance of EnermetE70X */
    public EnermetE70X() {
    	setTestE70xConnection(true);
    }
    
    protected RegisterConfig getRegs() {
        return regs;
    }    

    public String getProtocolVersion() {
        return "$Date$";
    }

    
} // class EnermetE70X
