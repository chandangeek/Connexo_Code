/*
 * EnermetE70X.java
 *
 * Created on 28 juli 2004, 10:28
 */

package com.energyict.protocolimpl.iec1107.enermete60x;

import com.energyict.protocolimpl.iec1107.enermete70x.*;
import com.energyict.protocolimpl.customerconfig.*;
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

    RegisterConfig regs = new UcontoRegisterConfig();
    
    /** Creates a new instance of EnermetE70X */
    public EnermetE60X() {
    }
    
    protected RegisterConfig getRegs() {
        return regs;
    }    
    
    public String getProtocolVersion() {
        return "$Revision: 1.7 $";
    }
    
} // class EnermetE60X
