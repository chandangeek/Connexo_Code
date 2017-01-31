/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * FAFRegisters.java
 *
 * Created on 15 december 2004, 15:21
 */

package com.energyict.protocolimpl.sctm.faf;

import com.energyict.mdc.common.ObisCode;

import com.energyict.protocolimpl.metcom.Metcom;
import com.energyict.protocolimpl.sctm.base.AbstractSCTMRegisterReader;
import com.energyict.protocolimpl.sctm.base.SCTMRegisterSpec;

/**
 *
 * @author  Koen
 */
public class FAF20Registers extends AbstractSCTMRegisterReader {

    /** Creates a new instance of FAFRegisters */
    public FAF20Registers(Metcom metcom) {
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

        // 24 serial registers cumulated energy every second, reset at interval close tm1
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(100,00,24,ObisCode.fromString("1.1.82.29.0.255"),"serial register @ cumulative energy every second. Reset at TM1"));
        // 24 serial registers cumulated energy every second, reset at interval close tm2
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(101,00,24,ObisCode.fromString("1.1.82.30.0.255"),"serial register @ cumulative energy every second. Reset at TM2"));
        // 24 serial registers cumulated energy every tm1
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(102,00,24,ObisCode.fromString("1.1.82.128.0.255"),"serial register @ cumulative energy every TM1"));
        // 24 serial registers cumulated energy every tm2
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(103,00,24,ObisCode.fromString("1.1.82.129.0.255"),"serial register @ cumulative energy every TM2"));
        // 24 serial registers energy advance (if 702=1) (demand if 702=0 for tm1)
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(104,00,24,ObisCode.fromString("1.1.82.130.0.255"),"serial register @ energy advance or demand in TM1"));
        // 24 serial registers energy advance (if 702=1) (demand if 702=0 for tm2)
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(105,00,24,ObisCode.fromString("1.1.82.131.0.255"),"serial register @ energy advance or demand in TM2"));

        // 16 result registers cumulated energy every second, reset at interval close tm1
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(110,00,16,ObisCode.fromString("1.25.82.29.0.255"),"result register @ cumulative energy every second. Reset at TM1"));
        // 16 result registers cumulated energy every second, reset at interval close tm2
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(111,00,16,ObisCode.fromString("1.25.82.30.0.255"),"result register @ cumulative energy every second. Reset at TM2"));
        // 16 result registers cumulated energy every tm1
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(112,00,16,ObisCode.fromString("1.25.82.128.0.255"),"result register @ cumulative energy every TM1"));
        // 16 result registers cumulated energy every tm2
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(113,00,16,ObisCode.fromString("1.25.82.129.0.255"),"result register @ cumulative energy every TM2"));
        // 16 result registers energy advance (if 702=1) (demand if 702=0 for tm1)
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(114,00,16,ObisCode.fromString("1.25.82.130.0.255"),"result register @ energy advance or demand in TM1"));
        // 16 result registers energy advance (if 702=1) (demand if 702=0 for tm2)
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(115,00,16,ObisCode.fromString("1.25.82.131.0.255"),"result register @ energy advance or demand in TM2"));

        // 16 channels, tariffs
        int tariffCount=1;
        for (int period = 1;period <= 3; period++) {
            for (int rate = 1;rate <= 8; rate++) {
                int address = 200+period*10+rate;
                getSctmRegisterSpecs().add(new SCTMRegisterSpec(address,00,16,ObisCode.fromString("1.1.82.8."+tariffCount+".255"),"channel @ cumulative energy tariff "+tariffCount));
                address = 300+period*10+rate;
                getSctmRegisterSpecs().add(new SCTMRegisterSpec(address,00,16,ObisCode.fromString("1.1.82.132."+tariffCount+".255"),"channel @ maximum A"+tariffCount));
                address = 400+period*10+rate;
                getSctmRegisterSpecs().add(new SCTMRegisterSpec(address,00,16,ObisCode.fromString("1.1.82.133."+tariffCount+".255"),"channel @ maximum B"+tariffCount));
                address = 500+period*10+rate;
                getSctmRegisterSpecs().add(new SCTMRegisterSpec(address,00,16,ObisCode.fromString("1.1.82.134."+tariffCount+".255"),"channel @ maximum C"+tariffCount));
                tariffCount++;
            } // for (int rate = 1;rate <= 8; rate++)
        } // for (int period = 1;period <= 3; period++)


    } // private void initSCTMRegisterSpecs()



}
