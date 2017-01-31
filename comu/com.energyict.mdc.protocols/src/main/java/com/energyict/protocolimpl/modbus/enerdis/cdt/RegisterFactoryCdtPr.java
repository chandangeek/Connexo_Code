/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.enerdis.cdt;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;

/**
 * Responsibilities:
 *  - Accesspoint to Registers
 *  - All parsing
 *
 * @author fbo
 */

class RegisterFactoryCdtPr extends RegisterFactory {

    static final Unit percent   = Unit.get(BaseUnit.PERCENT, 1);
    static final Unit fpUnit    = Unit.get(BaseUnit.UNITLESS);
    static final Unit kvarh     = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3);
    static final Unit kVAh      = Unit.get(BaseUnit.VOLTAMPEREHOUR, 3);
    static final Unit VA        = Unit.get(BaseUnit.VOLTAMPERE);
    static final Unit VAr       = Unit.get(BaseUnit.VOLTAMPEREREACTIVE);
    static final Unit W         = Unit.get(BaseUnit.WATT);
    static final Unit A         = Unit.get(BaseUnit.AMPERE);
    static final Unit V         = Unit.get(BaseUnit.VOLT);
    static final Unit kWh       = Unit.get(BaseUnit.WATTHOUR, 3);


    private HoldingRegister v_ph1_1_sec;
    private HoldingRegister v_ph2_1_sec;
    private HoldingRegister v_ph3_1_sec;

    private HoldingRegister i_ph1_1_sec;
    private HoldingRegister i_ph2_1_sec;
    private HoldingRegister i_ph3_1_sec;

    private HoldingRegister p_ph1_1_sec;
    private HoldingRegister p_ph2_1_sec;
    private HoldingRegister p_ph3_1_sec;

    private HoldingRegister p_tot_1_sec;

    private HoldingRegister fp_ph1_1_sec;
    private HoldingRegister fp_ph2_1_sec;
    private HoldingRegister fp_ph3_1_sec;

    private HoldingRegister fp_tot_1_sec;

    private HoldingRegister q_ph1_1_sec;
    private HoldingRegister q_ph2_1_sec;
    private HoldingRegister q_ph3_1_sec;

    private HoldingRegister q_tot_1_sec;

    private HoldingRegister avg_v_ph1;
    private HoldingRegister avg_v_ph2;
    private HoldingRegister avg_v_ph3;

    private HoldingRegister avg_i_ph1;
    private HoldingRegister avg_i_ph2;
    private HoldingRegister avg_i_ph3;

    private HoldingRegister avg_p_ph1;
    private HoldingRegister avg_p_ph2;
    private HoldingRegister avg_p_ph3;

    private HoldingRegister avg_p_tot;

    private HoldingRegister avg_fp_ph1;
    private HoldingRegister avg_fp_ph2;
    private HoldingRegister avg_fp_ph3;

    private HoldingRegister avg_fp_global;

    private HoldingRegister avg_q_ph1;
    private HoldingRegister avg_q_ph2;
    private HoldingRegister avg_q_ph3;

    private HoldingRegister avg_q_tot;

    private HoldingRegister active_energy_ph1;
    private HoldingRegister reactive_energy_ph1;

    private HoldingRegister active_energy_ph2;
    private HoldingRegister reactive_energy_ph2;

    private HoldingRegister active_energy_ph3;
    private HoldingRegister reactive_energy_ph3;

    private HoldingRegister active_energy_tot;
    private HoldingRegister reactive_energy_tot;

    private HoldingRegister active_energy_tou_0;
    private HoldingRegister active_energy_tou_1;

    private HoldingRegister active_energy_int_0;
    private HoldingRegister active_energy_int_1;
    private HoldingRegister active_energy_int_2;
    private HoldingRegister active_energy_int_3;
    private HoldingRegister active_energy_int_4;
    private HoldingRegister active_energy_int_5;

    private HoldingRegister u_ph12;
    private HoldingRegister u_ph23;
    private HoldingRegister u_ph31;

    private HoldingRegister avg_u_ph12;
    private HoldingRegister avg_u_ph23;
    private HoldingRegister avg_u_ph31;

    private HoldingRegister max_i_ph1;
    private HoldingRegister max_i_ph2;
    private HoldingRegister max_i_ph3;

    private HoldingRegister max_v_ph1;
    private HoldingRegister max_v_ph2;
    private HoldingRegister max_v_ph3;

    private HoldingRegister max_p_ph1;
    private HoldingRegister max_p_ph2;
    private HoldingRegister max_p_ph3;

    private HoldingRegister max_p_tot;

    private HoldingRegister max_q_ph1;
    private HoldingRegister max_q_ph2;
    private HoldingRegister max_q_ph3;

    private HoldingRegister max_q_tot;

    private HoldingRegister max_fp;

    private HoldingRegister reactive_energy_tou_0;
    private HoldingRegister reactive_energy_tou_1;

    private HoldingRegister reactive_energy_int_0;
    private HoldingRegister reactive_energy_int_1;
    private HoldingRegister reactive_energy_int_2;
    private HoldingRegister reactive_energy_int_3;
    private HoldingRegister reactive_energy_int_4;
    private HoldingRegister reactive_energy_int_5;



    /** Creates a new instance of RegisterFactory */
    public RegisterFactoryCdtPr(Modbus modBus) {
        super(modBus);

        init();
    }

    protected void init() {

        String d;
        String o;

        d = "Result 1s line to neutral voltage phase 1";
        v_ph1_1_sec = add( "1.1.32.7.0.255", 0x0012, d, V, Type.WORD);
        d = "Result 1s line to neutral voltage phase 2";
        v_ph2_1_sec = add( "1.1.52.7.0.255", 0x0014, d, V, Type.WORD);
        d = "Result 1s line to neutral voltage phase 3";
        v_ph3_1_sec = add( "1.1.72.7.0.255", 0x0016, d, V, Type.WORD);

        d = "Result 1s current phase 1";
        i_ph1_1_sec = add( "1.1.31.7.0.255", 0x0018, d, A, Type.WORD);
        d = "Result 1s current phase 2";
        i_ph2_1_sec = add( "1.1.51.7.0.255", 0x001A, d, A, Type.WORD);
        d = "Result 1s current phase 3";
        i_ph3_1_sec = add( "1.1.71.7.0.255", 0x001C, d, A, Type.WORD);

        d = "Result 1s active power phase 1";
        p_ph1_1_sec = add( "1.1.21.7.0.255", 0x001E, d, W, Type.LONG_WORD);
        d = "Result 1s active power phase 2";
        p_ph2_1_sec = add( "1.1.41.7.0.255", 0x0022, d, W, Type.LONG_WORD);
        d = "Result 1s active power phase 3";
        p_ph3_1_sec = add( "1.1.61.7.0.255", 0x0026, d, W, Type.LONG_WORD);

        d = "Result 1s total active power";
        p_tot_1_sec = add( "1.1.1.7.0.255", 0x002A, d, W, Type.LONG_WORD);

        d = "average power factor phase 1";
        fp_ph1_1_sec  = add( "1.1.33.7.0.255", 0x002E, d, fpUnit, Type.BYTE);
        d = "average power factor phase 2";
        fp_ph2_1_sec  = add( "1.1.53.7.0.255", 0x002F, d, fpUnit, Type.BYTE);
        d = "average power factor phase 3";
        fp_ph3_1_sec  = add( "1.1.73.7.0.255", 0x0030, d, fpUnit, Type.BYTE);

        d = "total average power factor";
        fp_tot_1_sec  = add( "1.1.13.7.0.255", 0x0031, d, fpUnit, Type.BYTE);

        d = "Result 1s reactive power Q Ph1";
        q_ph1_1_sec = add( "1.1.23.7.0.255", 0x0032, d, VAr, Type.LONG_WORD);
        d = "Result 1s reactive power Q Ph2";
        q_ph2_1_sec = add( "1.1.43.7.0.255", 0x0036, d, VAr, Type.LONG_WORD);
        d = "Result 1s reactive power Q Ph3";
        q_ph3_1_sec = add( "1.1.63.7.0.255", 0x003A, d, VAr, Type.LONG_WORD);

        d = "Result 1s reactive power Q totale";
        q_tot_1_sec = add( "1.1.3.7.0.255", 0x003E, d, VAr, Type.LONG_WORD);


        d = "Average x min. line to neutral voltage phase 1";
        avg_v_ph1 = add( "1.1.32.5.0.255", 0x02B2, d, V, Type.WORD);
        d = "Average x min. line to neutral voltage phase 2";
        avg_v_ph2 = add( "1.1.52.5.0.255", 0x02B4, d, V, Type.WORD);
        d = "Average x min. line to neutral voltage phase 3";
        avg_v_ph3 = add( "1.1.72.5.0.255", 0x02B6, d, V, Type.WORD);

        d = "Average x min. current phase 1";
        avg_i_ph1 = add( "1.1.31.5.0.255", 0x02B8, d, A, Type.WORD);
        d = "Average x min. current phase 2";
        avg_i_ph2 = add( "1.1.51.5.0.255", 0x02BA, d, A, Type.WORD);
        d = "Average x min. current phase 3";
        avg_i_ph3 = add( "1.1.71.5.0.255", 0x02BC, d, A, Type.WORD);

        d = "Average x min. active power phase 1";
        avg_p_ph1 = add( "1.1.21.5.0.255", 0x02BE, d, W, Type.LONG_WORD);
        d = "Average x min. active power phase 2";
        avg_p_ph2 = add( "1.1.41.5.0.255", 0x02C2, d, W, Type.LONG_WORD);
        d = "Average x min. active power phase 3";
        avg_p_ph3 = add( "1.1.61.5.0.255", 0x02C6, d, W, Type.LONG_WORD);

        d = "Average x min. total active power";
        avg_p_tot = add( "1.1.1.5.0.255", 0x02CA, d, W, Type.LONG_WORD);

        d = "Average x min. power factor phase 1";
        avg_fp_ph1 = add( "1.1.33.5.0.255", 0x02CE, d, fpUnit, Type.BYTE);
        d = "Average x min. power factor phase 2";
        avg_fp_ph2 = add( "1.1.53.5.0.255", 0x02CF, d, fpUnit, Type.BYTE);
        d = "Average x min. power factor phase 3";
        avg_fp_ph3 = add( "1.1.73.5.0.255", 0x02D0, d, fpUnit, Type.BYTE);

        d = "Average x min. total power factor";
        avg_fp_global = add( "1.1.13.5.0.255", 0x02D1, d, fpUnit, Type.BYTE);

        d = "Average x min. reactive power phase 1";
        avg_q_ph1 = add( "1.1.23.5.0.255", 0x02D2, d, VAr, Type.LONG_WORD);
        d = "Average x min. reactive power phase 2";
        avg_q_ph2 = add( "1.1.43.5.0.255", 0x02D6, d, VAr, Type.LONG_WORD);
        d = "Average x min. reactive power phase 3";
        avg_q_ph3 = add( "1.1.63.5.0.255", 0x02DA, d, VAr, Type.LONG_WORD);

        d = "Average x min. total reactive power";
        avg_q_tot = add( "1.1.3.5.0.255", 0x02DE, d, VAr, Type.LONG_WORD);

        d = "Active energy meter phase 1 (W/sec)";
        o = "1.1.21.8.0.255";
        active_energy_ph1 = add( o, 0x02E2, d, kWh, Type.LONG_DOUBLE_WORD);
        d = "Reactive energy meter phase 1 (var/sec)";
        o = "1.1.23.8.0.255";
        reactive_energy_ph1 = add( o, 0x02EA, d, kvarh, Type.LONG_DOUBLE_WORD);

        d = "Active energy meter phase 2 (W/sec)";
        o = "1.1.41.8.0.255";
        active_energy_ph2 = add( o, 0x02F2, d, kWh, Type.LONG_DOUBLE_WORD);
        d = "Reactive energy meter phase 2 (var/sec)";
        o = "1.1.43.8.0.255";
        reactive_energy_ph2 = add( o, 0x02FA, d, kvarh, Type.LONG_DOUBLE_WORD);

        d = "Active energy meter phase 3 (W/sec)";
        o = "1.1.61.8.0.255";
        active_energy_ph3 = add( o, 0x0302, d, kWh, Type.LONG_DOUBLE_WORD);
        d = "Reactive energy meter phase 3 (var/sec)";
        o = "1.1.63.8.0.255";
        reactive_energy_ph3 = add( o, 0x030A, d, kvarh, Type.LONG_DOUBLE_WORD);

        d = "Total active energy meter(W/sec)";
        o = "1.1.1.8.0.255";
        active_energy_tot = add( o, 0x0312, d, kWh, Type.LONG_DOUBLE_WORD);
        d = "Total reactive energy meter(var/sec)";
        o = "1.1.3.8.0.255";
        reactive_energy_tot = add( o, 0x031A, d, kvarh, Type.LONG_DOUBLE_WORD);


        d = "Active energy meter tariff 0";
        o = "1.1.1.8.1.255";
        active_energy_tou_0 = add( o, 0x0332, d, kWh, Type.LONG_DOUBLE_WORD);
        d = "Active energy meter tariff 1";
        o = "1.1.1.8.2.255";
        active_energy_tou_1 = add( o, 0x033A, d, kWh, Type.LONG_DOUBLE_WORD);

        d = "Active energy meter time interval 0";
        o = "1.1.1.8.1.255";
        active_energy_int_0 = add( o, 0x03B2, d, kWh, Type.LONG_DOUBLE_WORD);
        d = "Active energy meter time interval 1";
        o = "1.1.1.8.2.255";
        active_energy_int_1 = add( o, 0x03BA, d, kWh, Type.LONG_DOUBLE_WORD);
        d = "Active energy meter time interval 2";
        o = "1.1.1.8.3.255";
        active_energy_int_2 = add( o, 0x03C2, d, kWh, Type.LONG_DOUBLE_WORD);
        d = "Active energy meter time interval 3";
        o = "1.1.1.8.4.255";
        active_energy_int_3  = add( o, 0x03CA, d, kWh, Type.LONG_DOUBLE_WORD);
        d = "Active energy meter time interval 4";
        o = "1.1.1.8.5.255";
        active_energy_int_4 = add( o, 0x03D2, d, kWh, Type.LONG_DOUBLE_WORD);
        d = "Active energy meter time interval 5";
        o = "1.1.1.8.6.255";
        active_energy_int_5  = add( o, 0x03DA, d, kWh, Type.LONG_DOUBLE_WORD);

        d = "Result 1 sec line to line voltage U Ph12";
        o = "1.1.154.7.0.255";
        u_ph12  = add( o, 0x16C0, d, kWh, Type.WORD);
        d = "Result 1 sec line to line voltage U Ph23";
        o = "1.1.155.7.0.255";
        u_ph23 = add( o, 0x16C2, d, kWh, Type.WORD);
        d = "Result 1 sec line to line voltage U Ph31";
        o = "1.1.156.7.0.255";
        u_ph31 = add( o, 0x16C4, d, kWh, Type.WORD);

        d = "Avergage x min. line to line voltage U Ph12";
        o = "1.1.154.5.0.255";
        avg_u_ph12 = add( o, 0x1762, d, kWh, Type.WORD);
        d = "Average x min. line to line voltage U Ph23";
        o = "1.1.155.5.0.255";
        avg_u_ph23 = add( o, 0x1764, d, kWh, Type.WORD);
        d = "Average x min. line to line voltage U Ph31";
        o = "1.1.156.5.0.255";
        avg_u_ph31 = add( o, 0x1766, d, kWh, Type.WORD);


        d = "Maximum Of I Ph 1";
        max_i_ph1 = add( "1.1.31.6.0.255", 0x1768, d, A, Type.DATE_AND_WORD);
        d = "Maximum Of I Ph 2";
        max_i_ph2 = add( "1.1.51.6.0.255", 0x1770, d, A, Type.DATE_AND_WORD);
        d = "Maximum Of I Ph 3";
        max_i_ph3 = add( "1.1.71.6.0.255", 0x1778, d, A, Type.DATE_AND_WORD);

        d = "Maximum Of V Ph 1";
        max_v_ph1 = add( "1.1.32.6.0.255", 0x1780, d, V, Type.DATE_AND_WORD);
        d = "Maximum Of V Ph 2";
        max_v_ph2 = add( "1.1.52.6.0.255", 0x1788, d, V, Type.DATE_AND_WORD);
        d = "Maximum Of V Ph 3";
        max_v_ph3 = add( "1.1.72.6.0.255", 0x1790, d, V, Type.DATE_AND_WORD);

        d = "Maximum Of P Ph 1";
        o = "1.1.21.6.0.255";
        max_p_ph1 = add( o, 0x1798, d, W, Type.DATE_AND_LONG_WORD);
        d = "Maximum Of P Ph 2";
        o = "1.1.41.6.0.255";
        max_p_ph2 = add( o, 0x17A2, d, W, Type.DATE_AND_LONG_WORD);
        d = "Maximum Of P Ph 3";
        o = "1.1.61.6.0.255";
        max_p_ph3 = add( o, 0x17AC, d, W, Type.DATE_AND_LONG_WORD);

        d = "Maximum Of P totale";
        o = "1.1.1.6.0.255";
        max_p_tot = add( o, 0x17B6, d, W, Type.DATE_AND_LONG_WORD);

        d = "Maximum Of Q Ph 1";
        o = "1.1.23.6.0.255";
        max_q_ph1 = add( o, 0x17C0, d, VAr, Type.DATE_AND_LONG_WORD);
        d = "Maximum Of Q Ph 2";
        o = "1.1.43.6.0.255";
        max_q_ph2 = add( o, 0x17CA, d, VAr, Type.DATE_AND_LONG_WORD);
        d = "Maximum Of Q Ph 3";
        o = "1.1.63.6.0.255";
        max_q_ph3 = add( o, 0x17D4, d, VAr, Type.DATE_AND_LONG_WORD);;

        d = "Maximum Of Q totale";
        o = "1.1.3.6.0.255";
        max_q_tot = add( o, 0x17DE, d, VAr, Type.DATE_AND_LONG_WORD);

        d = "Maximum FP";
        max_fp = add( "1.1.13.6.0.255", 0x02A2, d, fpUnit, Type.DATE_AND_WORD);


        d = "Reactive energy meter tou 0";
        o = "1.1.3.8.1.255";
        reactive_energy_tou_0 = add( o, 0x1A02, d, kvarh, Type.LONG_DOUBLE_WORD);
        d = "Reactive energy meter tou 1";
        o = "1.1.3.8.2.255";
        reactive_energy_tou_1 = add( o, 0x1A0A, d, kvarh, Type.LONG_DOUBLE_WORD);

        d = "Reactive energy meter time interval 0";
        o = "1.1.3.8.1.255";
        reactive_energy_int_0 = add( o, 0x1A82, d, kvarh, Type.LONG_DOUBLE_WORD);
        d = "Reactive energy meter time interval 1";
        o = "1.1.3.8.2.255";
        reactive_energy_int_1 = add( o, 0x1A8A, d, kvarh, Type.LONG_DOUBLE_WORD);
        d = "Reactive energy meter time interval 2";
        o = "1.1.3.8.3.255";
        reactive_energy_int_2 = add( o, 0x1A92, d, kvarh, Type.LONG_DOUBLE_WORD);
        d = "Reactive energy meter time interval 3";
        o = "1.1.3.8.4.255";
        reactive_energy_int_3  = add( o, 0x1A9A, d, kvarh, Type.LONG_DOUBLE_WORD);
        d = "Reactive energy meter time interval 4";
        o = "1.1.3.8.5.255";
        reactive_energy_int_4 = add( o, 0x1AA2, d, kvarh, Type.LONG_DOUBLE_WORD);
        d = "Reactive energy meter time interval 5";
        o = "1.1.3.8.6.255";
        reactive_energy_int_5  = add( o, 0x1AAA, d, kvarh, Type.LONG_DOUBLE_WORD);


    }


    /** @return short desciption of ALL the possibly available obiscodes */
    public String toString() {

        StringBuffer result;

        try {
            result = new StringBuffer()

            .append( toDbgString( v_ph1_1_sec ) ).append( "\n" )
            .append( toDbgString( v_ph2_1_sec ) ).append( "\n" )
            .append( toDbgString( v_ph3_1_sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( i_ph1_1_sec ) ).append( "\n" )
            .append( toDbgString( i_ph2_1_sec ) ).append( "\n" )
            .append( toDbgString( i_ph3_1_sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( p_ph1_1_sec ) ).append( "\n" )
            .append( toDbgString( p_ph2_1_sec ) ).append( "\n" )
            .append( toDbgString( p_ph3_1_sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( p_tot_1_sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( fp_ph1_1_sec ) ).append( "\n" )
            .append( toDbgString( fp_ph2_1_sec ) ).append( "\n" )
            .append( toDbgString( fp_ph3_1_sec ) ).append( "\n" )
            .append( toDbgString( fp_tot_1_sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( q_ph1_1_sec ) ).append( "\n" )
            .append( toDbgString( q_ph2_1_sec ) ).append( "\n" )
            .append( toDbgString( q_ph3_1_sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( q_tot_1_sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_v_ph1 ) ).append( "\n" )
            .append( toDbgString( avg_v_ph2 ) ).append( "\n" )
            .append( toDbgString( avg_v_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_i_ph1 ) ).append( "\n" )
            .append( toDbgString( avg_i_ph2 ) ).append( "\n" )
            .append( toDbgString( avg_i_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_p_ph1 ) ).append( "\n" )
            .append( toDbgString( avg_p_ph2 ) ).append( "\n" )
            .append( toDbgString( avg_p_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_p_tot ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_fp_ph1 ) ).append( "\n" )
            .append( toDbgString( avg_fp_ph2 ) ).append( "\n" )
            .append( toDbgString( avg_fp_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_fp_global ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_q_ph1 ) ).append( "\n" )
            .append( toDbgString( avg_q_ph2 ) ).append( "\n" )
            .append( toDbgString( avg_q_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_q_tot ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_energy_ph1 ) ).append( "\n" )
            .append( toDbgString( reactive_energy_ph1 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_energy_ph2 ) ).append( "\n" )
            .append( toDbgString( reactive_energy_ph2 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_energy_ph3 ) ).append( "\n" )
            .append( toDbgString( reactive_energy_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_energy_tot ) ).append( "\n" )
            .append( toDbgString( reactive_energy_tot ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_energy_tou_0 ) ).append( "\n" )
            .append( toDbgString( active_energy_tou_1 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_energy_int_0 ) ).append( "\n" )
            .append( toDbgString( active_energy_int_1 ) ).append( "\n" )
            .append( toDbgString( active_energy_int_2 ) ).append( "\n" )
            .append( toDbgString( active_energy_int_3 ) ).append( "\n" )
            .append( toDbgString( active_energy_int_4 ) ).append( "\n" )
            .append( toDbgString( active_energy_int_5 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( u_ph12 ) ).append( "\n" )
            .append( toDbgString( u_ph23 ) ).append( "\n" )
            .append( toDbgString( u_ph31 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_u_ph12 ) ).append( "\n" )
            .append( toDbgString( avg_u_ph23 ) ).append( "\n" )
            .append( toDbgString( avg_u_ph31 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_i_ph1 ) ).append( "\n" )
            .append( toDbgString( max_i_ph2 ) ).append( "\n" )
            .append( toDbgString( max_i_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_v_ph1 ) ).append( "\n" )
            .append( toDbgString( max_v_ph2 ) ).append( "\n" )
            .append( toDbgString( max_v_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_p_ph1 ) ).append( "\n" )
            .append( toDbgString( max_p_ph2 ) ).append( "\n" )
            .append( toDbgString( max_p_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_p_tot ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_q_ph1 ) ).append( "\n" )
            .append( toDbgString( max_q_ph2 ) ).append( "\n" )
            .append( toDbgString( max_q_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_q_tot ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_fp ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( reactive_energy_tou_0 ) ).append( "\n" )
            .append( toDbgString( reactive_energy_tou_1 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( reactive_energy_int_0 ) ).append( "\n" )
            .append( toDbgString( reactive_energy_int_1 ) ).append( "\n" )
            .append( toDbgString( reactive_energy_int_2 ) ).append( "\n" )
            .append( toDbgString( reactive_energy_int_3 ) ).append( "\n" )
            .append( toDbgString( reactive_energy_int_4 ) ).append( "\n" )
            .append( toDbgString( reactive_energy_int_5 ) ).append( "\n" )
            .append( "\n" )

              ;
            return result.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Convert a HoldingRegister to a debug string.
     */
    private String toDbgString(HoldingRegister register) throws IOException{
        String key      = register.getName();

        return
            new StringBuffer()

                .append( register.getName() )
                .append( " " )
                .append( register.registerValue(key).toString() )
                    .toString();

    }

}
