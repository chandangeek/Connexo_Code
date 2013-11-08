/*
 * FAFRegisters.java
 *
 * Created on 15 december 2004, 15:21
 */

package com.energyict.protocolimpl.sctm.fag;

import java.io.*;
import java.util.*;

import com.energyict.protocolimpl.sctm.base.*;
import com.energyict.protocolimpl.metcom.Metcom;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;

/**
 *
 * @author  Koen
 */
public class FAGRegisters extends AbstractSCTMRegisterReader {
    
    /** Creates a new instance of FBCRegisters */
    public FAGRegisters(Metcom metcom) {
        super(metcom);
        initSCTMRegisterSpecs();
    }
    
    private void initSCTMRegisterSpecs() {
    } // private void initSCTMRegisterSpecs()
}
