/*
 * FAFRegisters.java
 *
 * Created on 15 december 2004, 15:21
 */

package com.energyict.protocolimpl.sctm.base;

import com.energyict.protocolimpl.metcom.Metcom;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTM;

/**
 *
 * @author  Koen
 */
public class GenericRegisters extends AbstractSCTMRegisterReader {

    /** Creates a new instance of FBCRegisters */
    public GenericRegisters(Metcom metcom) {
        super(metcom);
        initSCTMRegisterSpecs();
    }

    // KV 06092005 WVEM
    public GenericRegisters(SiemensSCTM siemensSCTM) {
        super(siemensSCTM);
        initSCTMRegisterSpecs();
    }

    private void initSCTMRegisterSpecs() {
    } // private void initSCTMRegisterSpecs()
}
