/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterMapping.java
 *
 * Created on 15 september 2006, 9:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.protocolimpl.base.ObisCodeExtensions;
import com.energyict.protocolimpl.itron.vectron.Vectron;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author Koen
 */
public class RegisterFactory {

    List registers=null;
    static public final int MAX_NR_OF_PEAKS=5;
    static public final int MAX_NR_OF_RATES=4;
    static public final int MAX_NR_OF_SELFREADS=4;

    static public final int SELFREADS_BASE_ADDRESS=0x2800;
    static public final int SELFREADS_BLOCK_SIZE=0x74;

    // F field
    static public int PRESENT_REGISTERS=255;
    static public int BILLING_REGISTERS=0;
    static public int LAST_SEASON_REGISTERS=4;


    Vectron vectron;

    /** Creates a new instance of RegisterMapping */
    public RegisterFactory(Vectron vectron) {
        this.vectron=vectron;
    }

    public void init() throws IOException {
        buildRegisters();
        createSelfreadRegisters();
    }


    private void buildRegisters() throws IOException {
        registers=new ArrayList();

        // Register 1 demand values and TOU
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().isEnergy()) {
                // no energy in register 1
            }
            else {
                registers.add(new Register(0x2120, ObisCode.fromString("1.1." + vectron.getBasePagesFactory()
                        .getRegisterConfigurationBasePage()
                        .getRegister1RateEMapping()
                        .getObisCField() + "." + ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND + ".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));
                registers.add(new Register(0x2124,0x2128, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));
                registers.add(new Register(0x2150,0x215C, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().getObisCField()+"."+ObisCodeExtensions.OBISCODE_D_PREVIOUS_MAX+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));
                registers.add(new Register(0x2484,0x24A0, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));

                registers.add(new Register(0x24b0, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".0."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));

                registers.add(new Register(0x27a0,0x27a4, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().getObisCField()+"."+(ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK+1)+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));
            }
        }
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().isEnergy()) {
                // no energy in register 1
            }
            else {
                registers.add(new Register(0x2138,0x213c, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".1.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x2144,0x2148, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".2.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x2154,0x2158 ,ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".3.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x2164,0x2168 ,ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".4.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));

                registers.add(new Register(0x248a,0x24a4 ,ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".1."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x2490,0x24a8 ,ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".2."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x2496,0x24ac ,ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".3."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x249c,0x24c1 ,ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".4."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));

                registers.add(new Register(0x27c8, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".1.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x27cc, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".2.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x27d0, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".3.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x27d4, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".4.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));

                registers.add(new Register(0x27e8, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".1."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x27eb, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".2."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x27ee, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".3."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
                registers.add(new Register(0x27f1, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".4."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping()));
            }
        }
        // Register 2 demand or energy and TOU
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().isEnergy()) {
                registers.add(new Register(0x2119, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(0x2481, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
            }
            else {
                registers.add(new Register(0x2118,0x211c, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(0x2160,0x2191, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCodeExtensions.OBISCODE_D_PREVIOUS_MAX+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(0x217a, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(0x24b3, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".0."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(0x2481,0x2468, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(0x27a8,0x27ac, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+(ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK+1)+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
            }
        }
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().isEnergy()) {
                registers.add(new Register(0x2172, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".1.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x2179, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".2.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x2180, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".3.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x2187, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".4.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));

                registers.add(new Register(0x2487, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".1."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x248d, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".2."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x2493, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".3."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x2499, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".4."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
            }
            else {
                registers.add(new Register(0x2172,0x2176, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".1.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x2180,0x2184, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".2.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x21b0,0x21b4, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".3.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x21b8,0x21bc, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".4.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));

                registers.add(new Register(0x2487,0x246c, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".1."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x248d,0x2470, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".2."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x2493,0x2474, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".3."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x2499,0x2478, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".4."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));

                registers.add(new Register(0x27d8, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".1.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x27dc, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".2.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x27e0, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".3.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x27e4, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".4.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));

                registers.add(new Register(0x27f4, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".1."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x27f7, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".2."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x27fa, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".3."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
                registers.add(new Register(0x27fd, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".4."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping()));
            }
        }
        // Register 3 demand or energy
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().isEnergy()) {
                registers.add(new Register(0x21d0, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping()));
                registers.add(new Register(0x2461, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping()));
            }
            else {
                registers.add(new Register(0x2134, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping()));
                registers.add(new Register(0x21d0,0x21d4, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping()));
                registers.add(new Register(0x2461,0x247c, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping()));
                registers.add(new Register(0x24b6, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".0."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping()));
                registers.add(new Register(0x27b0,0x27b4,  ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().getObisCField()+"."+(ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK+1)+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping()));
            }
        }

        // Register 4 demand or energy
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().isEnergy()) {
                registers.add(new Register(0x21d8, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping()));
                registers.add(new Register(0x24bc, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping()));
            }
            else {
                registers.add(new Register(0x2140, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping()));
                registers.add(new Register(0x21d8,0x21dc, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping()));
                registers.add(new Register(0x27b8,0x27bc, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().getObisCField()+"."+(ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK+1)+".0.255"), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping()));
                registers.add(new Register(0x24bc,0x2464, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping()));
                registers.add(new Register(0x24b9, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().getObisCField()+"."+ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND+".0."+LAST_SEASON_REGISTERS), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping()));

            }
        }
    } // private void buildRegisters() throws IOException

    private void createSelfreadRegisters() throws IOException {
        // self read registers
        // The 4 selfread register sets (0..3) are in a circular configuration. vzSelfReadIndex is the most recent set. this has to be obis billingSet 0
        int vzSelfReadIndex = (MAX_NR_OF_SELFREADS + (vectron.getBasePagesFactory().getSelfreadIndexBasePage().getIndex()-1)) % MAX_NR_OF_SELFREADS;
        for (int obisBillingSet=0;obisBillingSet<MAX_NR_OF_SELFREADS;obisBillingSet++) {
            int address = 0x2800 + (vzSelfReadIndex%MAX_NR_OF_SELFREADS)*0x74;
            vzSelfReadIndex++;
            buildSelfReadRegisters(address, obisBillingSet);
        }
    }

    private void buildSelfReadRegisters(int address, int obisFField) throws IOException {


        //****************************************** R E G I S T E R 1 demand ****************************************************
        // Register 1 demand rate E
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().isEnergy()) {
                // no energy in register 1
            }
            else {
                registers.add(new Register(address+4,address+7, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));
                registers.add(new Register(address+0x20,address+0x23, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().getObisCField()+"."+(ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK+1)+".0."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));
            }
        }

        //****************************************** R E G I S T E R 1 TOU demand****************************************************
        // Register 1 TOU demand
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1TOUMapping().isEnergy()) {
                // no energy in register 1
            }
            else {
                registers.add(new Register(address+0x3c,address+0x3f, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".1."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));
                registers.add(new Register(address+0x43,address+0x46, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".2."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));
                registers.add(new Register(address+0x4a,address+0x4d, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".3."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));
                registers.add(new Register(address+0x51,address+0x54, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".4."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister1RateEMapping()));
            }
        }

        //****************************************** R E G I S T E R 2 energy & demand ****************************************************
        // Register 2 demand or energy rate E
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().isEnergy()) {
                registers.add(new Register(address+0x0b, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
            }
            else {
                registers.add(new Register(address+0x0b,address+0x0e, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(address+0x27,address+0x2a, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+(ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK+1)+".0."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
            }
        }

        //****************************************** R E G I S T E R 2 TOU energy & demand ****************************************************
        // Register 2 TOU demand or energy
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2TOUMapping().isEnergy()) {
                registers.add(new Register(address+0x58, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".1."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(address+0x5f, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".1."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(address+0x66, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".1."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(address+0x6d, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".1."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
            }
            else {
                registers.add(new Register(address+0x58,address+0x5b, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".1."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(address+0x5f,address+0x62, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".2."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(address+0x66,address+0x69, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".3."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
                registers.add(new Register(address+0x6d,address+0x70, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".4."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister2RateEMapping()));
            }
        }

        //****************************************** R E G I S T E R 3 energy & demand ****************************************************
        // Register 3 demand or energy
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().isEnergy()) {
                registers.add(new Register(address+0x12, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping()));
            }
            else {
                registers.add(new Register(address+0x12,address+0x15, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping()));
                registers.add(new Register(address+0x2e,address+0x31, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping().getObisCField()+"."+(ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK+1)+".0."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister3RateEMapping()));
            }
        }

        //****************************************** R E G I S T E R 4 energy & demand ****************************************************
        // Register 4 demand or energy
        if (!vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().isNoMapping()) {
            if (vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().isEnergy()) {
                registers.add(new Register(address+0x19, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().getObisCField()+"."+ObisCode.CODE_D_TIME_INTEGRAL1+".0."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping()));
            }
            else {
                registers.add(new Register(address+0x19,address+0x1c, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().getObisCField()+"."+ObisCode.CODE_D_MAXIMUM_DEMAND+".0."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping()));
                registers.add(new Register(address+0x35,address+0x38, ObisCode.fromString("1.1."+vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping().getObisCField()+"."+(ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK+1)+".0."+obisFField), vectron.getBasePagesFactory().getRegisterConfigurationBasePage().getRegister4RateEMapping()));
            }
        }
    } // private void buildSelfReadRegisters() throws IOException


    public Register findRegisterByAddress(int index) throws IOException {
        Iterator it = registers.iterator();
        while(it.hasNext()) {
            Register r = (Register)it.next();
            if (r.getAddress()==index)
                return r;
        }
        throw new IOException("Register with index "+index+" does not exist!");
    }

    public Register findRegisterByObisCode(ObisCode obisCode) throws IOException {
        Iterator it = registers.iterator();
        while(it.hasNext()) {
            Register r = (Register)it.next();
            if (r.getObisCode().equals(obisCode))
                return r;
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

    public String getRegisterInfo() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = registers.iterator();
        while(it.hasNext()) {
            Register r = (Register)it.next();
            strBuff.append(""+r);
        }
        return strBuff.toString();
    }

}  // public class RegisterFactory
