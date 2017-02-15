/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * FAFRegisters.java
 *
 * Created on 15 december 2004, 15:21
 */

package com.energyict.protocolimpl.sctm.fbc;

import com.energyict.mdc.common.ObisCode;

import com.energyict.protocolimpl.metcom.Metcom;
import com.energyict.protocolimpl.sctm.base.AbstractSCTMRegisterReader;
import com.energyict.protocolimpl.sctm.base.SCTMRegisterSpec;

/**
 *
 * @author  Koen
 */
public class FBCRegisters extends AbstractSCTMRegisterReader {

    /** Creates a new instance of FBCRegisters */
    public FBCRegisters(Metcom metcom) {
        super(metcom);
        initSCTMRegisterSpecs();
    }

    private void initSCTMRegisterSpecs() {

        /*
         *   ----------- Use of ObisCode D field -----------
         *   D=8 energy cumulated
         *   D=29 energy cummulated every second, reset at period TM1 (TM) close
         *   D=30 energy cummulated every second, reset at period TM2 close
         *   D=128 energy cummulated every TM1 (TM)
         *   D=129 energy cummulated every TM2
         *   D=130 energy advance or demand for TM1 (TM)
         *   D=131 energy advance or demand for TM2
         *   D=132 maximum demand A
         *   D=133 maximum demand B
         *   D=134 maximum demand C
         */

        // 16 inputs energy updated every second
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(102,01,16,ObisCode.fromString("1.1.82.8.0.255"),"input @ cumulative energy updated every second"));

    } // private void initSCTMRegisterSpecs()
}
