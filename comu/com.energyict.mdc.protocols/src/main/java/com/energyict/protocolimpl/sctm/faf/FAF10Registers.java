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
public class FAF10Registers extends AbstractSCTMRegisterReader {

    /** Creates a new instance of FAFRegisters */
    public FAF10Registers(Metcom metcom) {
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


        // 8 input registers cumulated energy every tm
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(100,00,8,ObisCode.fromString("1.1.82.130.0.255"),"input register @ energy advance on tm"));
        // 8 result registers cumulated energy every tm
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(101,00,8,ObisCode.fromString("1.9.82.130.0.255"),"result register @ energy advance on tm"));
        // 8 input registers cumulated energy every per2
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(102,00,8,ObisCode.fromString("1.1.82.131.0.255"),"input register @ energy advance on per2"));
        // 8 result registers cumulated energy every per2
        getSctmRegisterSpecs().add(new SCTMRegisterSpec(103,00,8,ObisCode.fromString("1.9.82.131.0.255"),"result register @ energy advance on per2"));


        for (int channel=0;channel<8;channel++) {
            // 8 input registers cumulated energy up to tm
            int address = 200+(channel*10);
            getSctmRegisterSpecs().add(new SCTMRegisterSpec(address,00,ObisCode.fromString("1."+(channel+1)+".82.128.0.255"),"input channel "+(channel+1)+", cumulated energy up to tm"));
            for (int tariff=1;tariff<=3;tariff++) {
                address = 200+(channel*10)+tariff;
                // 8 input registers cumulated energy up to tm for tariff 1..3
                getSctmRegisterSpecs().add(new SCTMRegisterSpec(address,00,ObisCode.fromString("1."+(channel+1)+".82.128."+tariff+".255"),"input channel "+(channel+1)+", cumulated energy up to tm for tariff "+tariff));
            }
        }

        for (int channel=0;channel<8;channel++) {
            // 8 result registers cumulated energy up to tm
            int address = 400+(channel*10);
            getSctmRegisterSpecs().add(new SCTMRegisterSpec(address,00,ObisCode.fromString("1."+(channel+9)+".82.128.0.255"),"result channel "+(channel+1)+", cumulated energy up to tm"));
            for (int tariff=1;tariff<=3;tariff++) {
                address = 400+(channel*10)+tariff;
                // 8 result registers cumulated energy up to tm for tariff 1..3
                getSctmRegisterSpecs().add(new SCTMRegisterSpec(address,00,ObisCode.fromString("1."+(channel+9)+".128.29."+tariff+".255"),"result channel "+(channel+1)+", cumulated energy up to tm for tariff "+tariff));
            }
        }

    } // private void initSCTMRegisterSpecs()



}
