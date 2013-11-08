/*
 * FAFRegisters.java
 *
 * Created on 15 december 2004, 15:21
 */

package com.energyict.protocolimpl.sctm.base;

import java.io.*;
import java.util.*;

import com.energyict.protocolimpl.sctm.base.*;
import com.energyict.protocolimpl.metcom.Metcom;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTM; // KV 06092005 WVEM

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
