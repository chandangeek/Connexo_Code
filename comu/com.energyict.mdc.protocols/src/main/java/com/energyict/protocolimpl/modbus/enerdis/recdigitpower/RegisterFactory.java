/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.enerdis.recdigitpower;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * RegisterFactory is used as a central point for parsing.
 *
 * @author fbo
 * @beginchanges
 * GNA|25042008| changed default timeZone to meterTimezone
 */

class RegisterFactory extends AbstractRegisterFactory {

    private final static boolean debug = true;

    static final Unit V         = Unit.get(BaseUnit.VOLT, -1);
    static final Unit A         = Unit.get(BaseUnit.AMPERE, -1);
    static final Unit W         = Unit.get(BaseUnit.WATT);
    static final Unit VAr       = Unit.get(BaseUnit.VOLTAMPEREREACTIVE);
    static final Unit VA        = Unit.get(BaseUnit.VOLTAMPERE);

    static final Unit kWh       = Unit.get(BaseUnit.WATTHOUR, 3);
    static final Unit kvarh     = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3);
    static final Unit kVAh      = Unit.get(BaseUnit.VOLTAMPEREHOUR, 3);
    static final Unit fpUnit    = Unit.get(BaseUnit.UNITLESS, 2);
    static final Unit percent   = Unit.get(BaseUnit.PERCENT, -1);

    /* Sorry for not camel casing, but names contain numbers, seems more readable*/

    private HoldingRegister v_1_1sec;
    private HoldingRegister v_2_1sec;
    private HoldingRegister v_3_1sec;

    private HoldingRegister u12_1sec;
    private HoldingRegister u23_1sec;
    private HoldingRegister u31_1sec;

    private HoldingRegister i1_1sec;
    private HoldingRegister i2_1sec;
    private HoldingRegister i3_1sec;

    private HoldingRegister p1_1sec;
    private HoldingRegister p2_1sec;
    private HoldingRegister p3_1sec;
    private HoldingRegister pt_1sec;

    private HoldingRegister q1_1sec;
    private HoldingRegister q2_1sec;
    private HoldingRegister q3_1sec;
    private HoldingRegister qt_1sec;

    private HoldingRegister s1_1sec;
    private HoldingRegister s2_1sec;
    private HoldingRegister s3_1sec;
    private HoldingRegister st_1sec;

    private HoldingRegister fp1_1sec;
    private HoldingRegister fp2_1sec;
    private HoldingRegister fp3_1sec;
    private HoldingRegister fpt_1sec;

    /*
    private HoldingRegister total_active_energy;
    private HoldingRegister total_reative_energy;
    private HoldingRegister total_apparent_energy;
    */

    private HoldingRegister thd_u1_1sec;
    private HoldingRegister thd_u2_1sec;
    private HoldingRegister thd_u3_1sec;

    private HoldingRegister thd_i1_1sec;
    private HoldingRegister thd_i2_1sec;
    private HoldingRegister thd_i3_1sec;

    private HoldingRegister avg_v_1;
    private HoldingRegister avg_v_2;
    private HoldingRegister avg_v_3;

    private HoldingRegister avg_u_1;
    private HoldingRegister avg_u_2;
    private HoldingRegister avg_u_3;

    private HoldingRegister avg_i_1;
    private HoldingRegister avg_i_2;
    private HoldingRegister avg_i_3;

    private HoldingRegister avg_p_1;
    private HoldingRegister avg_p_2;
    private HoldingRegister avg_p_3;
    private HoldingRegister avg_pt;

    private HoldingRegister avg_q_1;
    private HoldingRegister avg_q_2;
    private HoldingRegister avg_q_3;
    private HoldingRegister avg_qt;

    private HoldingRegister avg_s_1;
    private HoldingRegister avg_s_2;
    private HoldingRegister avg_s_3;
    private HoldingRegister avg_st;

    private HoldingRegister avg_fp_1;
    private HoldingRegister avg_fp_2;
    private HoldingRegister avg_fp_3;
    private HoldingRegister avg_fpt;

    private HoldingRegister avg_thd_u1;
    private HoldingRegister avg_thd_u2;
    private HoldingRegister avg_thd_u3;

    private HoldingRegister avg_thd_i1;
    private HoldingRegister avg_thd_i2;
    private HoldingRegister avg_thd_i3;

    private HoldingRegister active_e_plus;
    private HoldingRegister reactive_e_plus;
    private HoldingRegister apparent_e_plus;

    private HoldingRegister active_e_min;
    private HoldingRegister reactive_e_min;
    private HoldingRegister apparent_e_min;

    private HoldingRegister active_e_slot_0;
    private HoldingRegister reactive_e_slot_0;

    private HoldingRegister active_e_slot_1;
    private HoldingRegister reactive_e_slot_1;

    private HoldingRegister active_e_slot_2;
    private HoldingRegister reactive_e_slot_2;

    private HoldingRegister active_e_slot_3;
    private HoldingRegister reactive_e_slot_3;

    private HoldingRegister active_e_slot_4;
    private HoldingRegister reactive_e_slot_4;

    private HoldingRegister active_e_slot_5;
    private HoldingRegister reactive_e_slot_5;

    private HoldingRegister min_v_1;
    private HoldingRegister min_v_2;
    private HoldingRegister min_v_3;

    private HoldingRegister min_u_12;
    private HoldingRegister min_u_23;
    private HoldingRegister min_u_31;

    private HoldingRegister min_i_1;
    private HoldingRegister min_i_2;
    private HoldingRegister min_i_3;

    private HoldingRegister min_p_1;
    private HoldingRegister min_p_2;
    private HoldingRegister min_p_3;
    private HoldingRegister min_pt;

    private HoldingRegister min_q_1;
    private HoldingRegister min_q_2;
    private HoldingRegister min_q_3;
    private HoldingRegister min_qt;

    private HoldingRegister min_s_1;
    private HoldingRegister min_s_2;
    private HoldingRegister min_s_3;
    private HoldingRegister min_st;

    private HoldingRegister min_fpt;

    private HoldingRegister min_thd_u1;
    private HoldingRegister min_thd_u2;
    private HoldingRegister min_thd_u3;

    private HoldingRegister min_thd_i1;
    private HoldingRegister min_thd_i2;
    private HoldingRegister min_thd_i3;

    private HoldingRegister max_v_1;
    private HoldingRegister max_v_2;
    private HoldingRegister max_v_3;

    private HoldingRegister max_u_12;
    private HoldingRegister max_u_23;
    private HoldingRegister max_u_31;

    private HoldingRegister max_i_1;
    private HoldingRegister max_i_2;
    private HoldingRegister max_i_3;

    private HoldingRegister max_p_1;
    private HoldingRegister max_p_2;
    private HoldingRegister max_p_3;
    private HoldingRegister max_pt;

    private HoldingRegister max_q_1;
    private HoldingRegister max_q_2;
    private HoldingRegister max_q_3;
    private HoldingRegister max_qt;

    private HoldingRegister max_s_1;
    private HoldingRegister max_s_2;
    private HoldingRegister max_s_3;
    private HoldingRegister max_st;

    private HoldingRegister max_fpt;

    private HoldingRegister max_thd_u1;
    private HoldingRegister max_thd_u2;
    private HoldingRegister max_thd_u3;

    private HoldingRegister max_thd_i1;
    private HoldingRegister max_thd_i2;
    private HoldingRegister max_thd_i3;

    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }

    protected void init() {

        Type type = null;
        String d = null;

        type = Type.SIGNED_LONG_WORD;
        v_1_1sec  = add( "1.1.32.7.0.255", 0x0004, "V 1 1sec", V, type);
        v_2_1sec  = add( "1.1.52.7.0.255", 0x0006, "V 2 1sec", V, type);
        v_3_1sec  = add( "1.1.72.7.0.255", 0x0008, "V 3 1sec", V, type);

        u12_1sec  = add( "1.1.154.7.0.255", 0x000A, "U12 1sec", V, type);
        u23_1sec  = add( "1.1.155.7.0.255", 0x000C, "U23 1sec", V, type);
        u31_1sec  = add( "1.1.156.7.0.255", 0x000E, "U31 1sec", V, type);

        i1_1sec   = add( "1.1.31.7.0.255", 0x0010, "I1 1sec", A, type);
        i2_1sec   = add( "1.1.51.7.0.255", 0x0012, "I2 1sec", A, type);
        i3_1sec   = add( "1.1.71.7.0.255", 0x0014, "I3 1sec", A, type);

        p1_1sec   = add( "1.1.21.7.0.255", 0x0016, "P1 1sec", W, type);
        p2_1sec   = add( "1.1.41.7.0.255", 0x0018, "P2 1sec", W, type);
        p3_1sec   = add( "1.1.61.7.0.255", 0x001A, "P3 1sec", W, type);
        pt_1sec   = add( "1.1.1.7.0.255",  0x001C, "Pt 1sec", W, type);

        q1_1sec   = add( "1.1.23.7.0.255", 0x001E, "Q1 1sec", VAr, type);
        q2_1sec   = add( "1.1.43.7.0.255", 0x0020, "Q2 1sec", VAr, type);
        q3_1sec   = add( "1.1.63.7.0.255", 0x0022, "Q3 1sec", VAr, type);
        qt_1sec   = add( "1.1.3.7.0.255",  0x0024, "Qt 1sec", VAr, type);

        s1_1sec   = add( "1.1.29.7.0.255", 0x0026, "S1 1sec", VA, type);
        s2_1sec   = add( "1.1.49.7.0.255", 0x0028, "S2 1sec", VA, type);
        s3_1sec   = add( "1.1.69.7.0.255", 0x002A, "S3 1sec", VA, type);
        st_1sec   = add( "1.1.9.7.0.255",  0x002C, "St 1sec", VA, type);

        fp1_1sec  = add( "1.1.33.7.0.255", 0x002E, "FP1 1sec", fpUnit, type);
        fp2_1sec  = add( "1.1.53.7.0.255", 0x0030, "FP2 1sec", fpUnit, type);
        fp3_1sec  = add( "1.1.73.7.0.255", 0x0032, "FP3 1sec", fpUnit, type);
        fpt_1sec  = add( "1.1.13.7.0.255", 0x0034, "FPt 1sec", fpUnit, type);

        /* Not actually mapped, since I don't know how to map these
        total_active_energy   = add( "1.1.1.7.0.255", 0x0038, "Total active energy", kWh);
        total_reative_energy  = add( "1.1.3.7.0.255", 0x003A, "Total reactive energy", kvarh);
        total_apparent_energy = add( "1.1.9.7.0.255", 0x003C, "Total apparent energy", kVAh);
        */

        d = "THD U1 1sec";
        thd_u1_1sec = add( "1.1.157.7.0.255", 0x003E, d, percent, type);
        d = "THD U2 1sec";
        thd_u2_1sec = add( "1.1.158.7.0.255", 0x0040, d, percent, type);
        d = "THD U3 1sec";
        thd_u3_1sec = add( "1.1.159.7.0.255", 0x0042, d, percent, type);

        d = "THD I1 1sec";
        thd_i1_1sec = add( "1.1.160.7.0.255", 0x0044, d, percent, type);
        d = "THD I2 1sec";
        thd_i2_1sec = add( "1.1.161.7.0.255", 0x0046, d, percent, type);
        d = "THD I3 1sec";
        thd_i3_1sec = add( "1.1.162.7.0.255", 0x0048, d, percent, type);


        /* Avergage values integrated x minutes */

        avg_v_1 = add( "1.1.32.5.0.255", 0x004C, "average V 1", V, type);
        avg_v_2 = add( "1.1.52.5.0.255", 0x004E, "average V 2", V, type);
        avg_v_3 = add( "1.1.72.5.0.255", 0x0050, "average V 3", V, type);

        avg_u_1 = add( "1.1.154.5.0.255", 0x0052, "average U12", V, type);
        avg_u_2 = add( "1.1.155.5.0.255", 0x0054, "average U23", V, type);
        avg_u_3 = add( "1.1.156.5.0.255", 0x0056, "average U31", V, type);

        avg_i_1 = add( "1.1.31.5.0.255", 0x0058, "average I1", A, type);
        avg_i_2 = add( "1.1.51.5.0.255", 0x005A, "average I2", A, type);
        avg_i_3 = add( "1.1.71.5.0.255", 0x005C, "average I3", A, type);

        avg_p_1 = add( "1.1.21.5.0.255", 0x005E, "average P1", W, type);
        avg_p_2 = add( "1.1.41.5.0.255", 0x0060, "average P2", W, type);
        avg_p_3 = add( "1.1.61.5.0.255", 0x0062, "average P3", W, type);
        avg_pt  = add( "1.1.1.5.0.255",  0x0064, "average Pt", W, type);

        avg_q_1 = add( "1.1.23.5.0.255", 0x0066, "average Q1", VAr, type);
        avg_q_2 = add( "1.1.43.5.0.255", 0x0068, "average Q2", VAr, type);
        avg_q_3 = add( "1.1.63.5.0.255", 0x006A, "average Q3", VAr, type);
        avg_qt  = add( "1.1.3.5.0.255",  0x006C, "average Qt", VAr, type);

        avg_s_1 = add( "1.1.29.5.0.255", 0x006E, "average S1", VA, type);
        avg_s_2 = add( "1.1.49.5.0.255", 0x0070, "average S2", VA, type);
        avg_s_3 = add( "1.1.69.5.0.255", 0x0072, "average S3", VA, type);
        avg_st     = add( "1.1.9.5.0.255", 0x0074, "average St", VA, type);

        d = "average FP1";
        avg_fp_1   = add( "1.1.33.5.0.255",  0x0076, d, fpUnit, type);
        d = "average FP2";
        avg_fp_2   = add( "1.1.53.5.0.255",  0x0078, d, fpUnit, type);
        d = "average FP3";
        avg_fp_3   = add( "1.1.73.5.0.255",  0x007A, d, fpUnit, type);
        d = "average FPt";
        avg_fpt    = add( "1.1.13.5.0.255",  0x007C, d, fpUnit, type);

        d = "average THD U1";
        avg_thd_u1 = add( "1.1.157.5.0.255", 0x007E, d, percent, type);
        d = "average THD U2";
        avg_thd_u2 = add( "1.1.158.5.0.255", 0x0080, d, percent, type);
        d = "average THD U3";
        avg_thd_u3 = add( "1.1.159.5.0.255", 0x0082, d, percent, type);

        d = "average THD I1";
        avg_thd_i1 = add( "1.1.160.5.0.255", 0x0084, d, percent, type);
        d = "average THD I2";
        avg_thd_i2 = add( "1.1.161.5.0.255", 0x0086, d, percent, type);
        d = "average THD I3";
        avg_thd_i3 = add( "1.1.162.5.0.255", 0x0088, d, percent, type);


        /* Energy meters  */
        type = Type.LONG_WORD;
        d = "Active E+";
        active_e_plus     = add( "1.1.1.8.0.255", 0x008E, d, kWh, type);
        d = "Reactive E+";
        reactive_e_plus   = add( "1.1.3.8.0.255", 0x0090, d, kvarh, type);
        d = "Apparent E+";
        apparent_e_plus   = add( "1.1.9.8.0.255", 0x0092, d, kVAh, type);

        d = "Active E-";
        active_e_min      = add( "1.1.2.8.0.255", 0x0094, d, kWh, type);
        d = "Reactive E-";
        reactive_e_min    = add( "1.1.4.8.0.255", 0x0096, d, kvarh, type);
        d = "Apparent E-";
        apparent_e_min    = add( "1.1.10.8.0.255", 0x0098, d, kVAh, type);

        type = Type.SIGNED_LONG_WORD;
        d = "Active E time slot 0";
        active_e_slot_0   = add( "1.1.1.8.1.255", 0x0094, d, kWh, type);
        d = "Reactive E time slot 0";
        reactive_e_slot_0 = add( "1.1.3.8.1.255", 0x0094, d, kWh, type);;

        d = "Active E time slot 1";
        active_e_slot_1   = add( "1.1.1.8.2.255", 0x009E, d, kWh, type);
        d = "Reactive E time slot 1";
        reactive_e_slot_1 = add( "1.1.3.8.2.255", 0x00A0, d, kvarh, type);

        d = "Active E time slot 2";
        active_e_slot_2   = add( "1.1.1.8.3.255", 0x00A2, d, kWh, type);
        d = "Reactive E time slot 2";
        reactive_e_slot_2 = add( "1.1.3.8.3.255", 0x00A4, d, kvarh, type);

        d = "Active E time slot 3";
        active_e_slot_3   = add( "1.1.1.8.4.255", 0x00A6, d, kWh, type);
        d = "Reactive E time slot 3";
        reactive_e_slot_3 = add( "1.1.3.8.4.255", 0x00A8, d, kvarh, type);

        d = "Active E time slot 4";
        active_e_slot_4   = add( "1.1.1.8.5.255", 0x00AA, d, kWh, type);
        d = "Reactive E time slot 4";
        reactive_e_slot_4 = add( "1.1.3.8.5.255", 0x00AC, d, kvarh, type);

        d = "Active E time slot 5";
        active_e_slot_5   = add( "1.1.1.8.6.255", 0x00AE, d, kWh, type);
        d = "Reactive E time slot 5";
        reactive_e_slot_5 = add( "1.1.3.8.6.255", 0x00B0, d, kvarh, type);


        /* Min. values on V, U, I, P, Q, S and FP */
        type = Type.DATE_AND_SIGNED_LONG_WORD;
        min_v_1    = add( "1.1.32.3.0.255",  0x00B8, "Minimum V 1", V, type );
        min_v_2    = add( "1.1.52.3.0.255",  0x00BE, "Minimum V 2", V, type );
        min_v_3    = add( "1.1.72.3.0.255",  0x00C4, "Minimum V 3", V, type );

        min_u_12   = add( "1.1.154.3.0.255", 0x00CA, "Minimum U12", V, type );
        min_u_23   = add( "1.1.155.3.0.255", 0x00D0, "Minimum U23", V, type );
        min_u_31   = add( "1.1.156.3.0.255", 0x00D6, "Minimum U31", V, type );

        min_i_1    = add( "1.1.31.3.0.255",  0x00DC, "Minimum I1", A, type );
        min_i_2    = add( "1.1.51.3.0.255",  0x00E2, "Minimum I2", A, type );
        min_i_3    = add( "1.1.71.3.0.255",  0x00E8, "Minimum I3", A, type );

        min_p_1    = add( "1.1.21.3.0.255",  0x00EE, "Minimum P1", W, type );
        min_p_2    = add( "1.1.41.3.0.255",  0x00F4, "Minimum P2", W, type );
        min_p_3    = add( "1.1.61.3.0.255",  0x00FA, "Minimum P3", W, type );
        min_pt     = add( "1.1.1.3.0.255",   0x0100, "Minimum Pt", W, type );

        min_q_1    = add( "1.1.23.3.0.255",  0x0106, "Minimum Q1", VAr, type );
        min_q_2    = add( "1.1.43.3.0.255",  0x010C, "Minimum Q2", VAr, type );
        min_q_3    = add( "1.1.63.3.0.255",  0x0112, "Minimum Q3", VAr, type );
        min_qt     = add( "1.1.3.3.0.255",   0x0118, "Minimum Qt", VAr, type );

        min_s_1    = add( "1.1.29.3.0.255",  0x011E, "Minimum S1", VA, type );
        min_s_2    = add( "1.1.49.3.0.255",  0x0124, "Minimum S2", VA, type );
        min_s_3    = add( "1.1.69.3.0.255",  0x012A, "Minimum S3", VA, type );
        min_st     = add( "1.1.9.3.0.255",   0x0130, "Minimum St", VA, type );

        d = "Minimum FPt";
        min_fpt    = add( "1.1.13.3.0.255",  0x0136, d, fpUnit, type );

        d = "Minimum THD U1";
        min_thd_u1 = add( "1.1.157.3.0.255", 0x013C, d, percent, type );
        d = "Minimum THD U2";
        min_thd_u2 = add( "1.1.158.3.0.255", 0x0142, d, percent, type );
        d = "Minimum THD U3";
        min_thd_u3 = add( "1.1.159.3.0.255", 0x014C, d, percent, type );

        d = "Minimum THD I1";
        min_thd_i1 = add( "1.1.160.3.0.255", 0x014E, d, percent, type );
        d = "Minimum THD I2";
        min_thd_i2 = add( "1.1.161.3.0.255", 0x0154, d, percent, type );
        d = "Minimum THD I3";
        min_thd_i3 = add( "1.1.162.3.0.255", 0x015A, d, percent, type );


        /* Max. values on V, U, I, P, Q, S and FP */

        max_v_1    = add( "1.1.32.6.0.255",  0x0160, "Maximum V 1", V, type );
        max_v_2    = add( "1.1.52.6.0.255",  0x0166, "Maximum V 2", V, type );
        max_v_3    = add( "1.1.72.6.0.255",  0x016C, "Maximum V 3", V, type );

        max_u_12   = add( "1.1.154.6.0.255", 0x0172, "Maximum U12", V, type );
        max_u_23   = add( "1.1.155.6.0.255", 0x0178, "Maximum U23", V, type );
        max_u_31   = add( "1.1.156.6.0.255", 0x017E, "Maximum U31", V, type );

        max_i_1    = add( "1.1.31.6.0.255",  0x0184, "Maximum I1", A, type );
        max_i_2    = add( "1.1.51.6.0.255",  0x018A, "Maximum I2", A, type );
        max_i_3    = add( "1.1.71.6.0.255",  0x0190, "Maximum I3", A, type );

        max_p_1    = add( "1.1.21.6.0.255",  0x0196, "Maximum P1", W, type );
        max_p_2    = add( "1.1.41.6.0.255",  0x019C, "Maximum P2", W, type );
        max_p_3    = add( "1.1.61.6.0.255",  0x01A2, "Maximum P3", W, type );
        max_pt     = add( "1.1.1.6.0.255",   0x01A8, "Maximum Pt", W, type );

        max_q_1    = add( "1.1.23.6.0.255",  0x01AE, "Maximum Q1", VAr, type );
        max_q_2    = add( "1.1.43.6.0.255",  0x01B4, "Maximum Q2", VAr, type );
        max_q_3    = add( "1.1.63.6.0.255",  0x01BA, "Maximum Q3", VAr, type );
        max_qt     = add( "1.1.3.6.0.255",   0x01C0, "Maximum Qt", VAr, type );

        max_s_1    = add( "1.1.29.6.0.255",  0x01C6, "Maximum S1", VA, type );
        max_s_2    = add( "1.1.49.6.0.255",  0x01CC, "Maximum S2", VA, type );
        max_s_3    = add( "1.1.69.6.0.255",  0x01D2, "Maximum S3", VA, type );
        max_st     = add( "1.1.9.6.0.255",   0x01D8, "Maximum St", VA, type );

        d = "Maximum FPt";
        max_fpt    = add( "1.1.13.6.0.255",  0x01DE, d, fpUnit, type );

        d = "Maximum THD U1";
        max_thd_u1 = add( "1.1.157.6.0.255", 0x01E4, d, percent, type );
        d = "Maximum THD U2";
        max_thd_u2 = add( "1.1.158.6.0.255", 0x01EA, d, percent, type );
        d = "Maximum THD U3";
        max_thd_u3 = add( "1.1.159.6.0.255", 0x01F0, d, percent, type );

        d = "Maximum THD I1";
        max_thd_i1 = add( "1.1.160.6.0.255", 0x01F6, d, percent, type );
        d = "Maximum THD I2";
        max_thd_i2 = add( "1.1.161.6.0.255", 0x01FC, d, percent, type );
        d = "Maximum THD I3";
        max_thd_i3 = add( "1.1.162.6.0.255", 0x0202, d, percent, type );

    }

    /** create a HoldingRegister (=factory method) */
    private HoldingRegister add(
        String obis, int address, String description, Unit unit, Type type ){

        ObisCode oc   = ObisCode.fromString(obis);
        int wordSize  = type.wordSize();

        HoldingRegister hr =
            new HoldingRegister( address, wordSize, oc, unit, description );

        hr.setRegisterFactory(this);
        hr.setParser(type.toString());

        getRegisters().add( hr );

        return hr;

    }


    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addParser(Type.WORD.toString(),
            new Parser() {
                public Object val(int[] values, AbstractRegister register)
                    throws IOException {

                    dbg( "Parser.val( " + Type.WORD + " )" );
                    BigDecimal bd = toBigDecimal(Type.WORD, values);
                    Quantity q = new Quantity( bd, register.getUnit() );
                    return new RegisterValue( register.getObisCode(), q );
                }
            });

            getParserFactory().addParser(Type.LONG_WORD.toString(),
            new Parser() {
                public Object val(int[] values, AbstractRegister register)
                    throws IOException {

                    dbg( "Parser.val( " + Type.LONG_WORD + " )" );

                    BigDecimal bd = toBigDecimal(Type.LONG_WORD, values);
                    Quantity q = new Quantity( bd, register.getUnit() );
                    return new RegisterValue( register.getObisCode(), q );
                }
            });

            getParserFactory().addParser(Type.SIGNED_LONG_WORD.toString(),
            new Parser() {
                public Object val(int[] values, AbstractRegister register)
                    throws IOException {

                    dbg( "Parser.val( " + Type.SIGNED_LONG_WORD + " )" );

                    BigDecimal bd = toBigDecimal(Type.SIGNED_LONG_WORD, values);
                    Quantity q = new Quantity( bd, register.getUnit() );
                    return new RegisterValue( register.getObisCode(), q );
                }
            });


            getParserFactory().addParser(Type.DATE.toString(),
            new Parser() {
                public Object val(int[] values, AbstractRegister register) {

                    dbg( "Parser.val( " + Type.DATE  + " )" );
                    Date date = toDate(values);
                    return new RegisterValue( register.getObisCode(), date );
                }
            });


            getParserFactory().addParser(Type.DATE_AND_SIGNED_LONG_WORD.toString(),
            new Parser() {
                public Object val(int[] values, AbstractRegister register)
                    throws IOException {

                    dbg( "Parser.val( " + Type.DATE_AND_SIGNED_LONG_WORD + " )" );

                    BigDecimal bd = toBigDecimal(Type.DATE_AND_SIGNED_LONG_WORD, values);
                    Quantity q = new Quantity( bd, register.getUnit() );
                    Date date = toDate(values);
                    return new RegisterValue( register.getObisCode(), q, date );
                }
            });
    }


    BigDecimal toBigDecimal(Type type, ByteArray byteArray) {

        byte[] data = byteArray.getBytes();

        if( (type.intValue() & Type.WORD.intValue()) > 0 ) {
            return new BigDecimal( (data[0] << 8) | data[1]  );
        }

        if( (type.intValue() & Type.LONG_WORD.intValue() ) > 0 ) {

            long l =
                (data[2] & 0xffl ) << 24 |
                (data[3] & 0xffl ) << 16 |
                (data[0] & 0xffl ) << 8  |
                (data[1] & 0xffl ) ;


            return new BigDecimal( l );

        }

        String msg = "RegisterFactory.toBigDecimal() ";
        msg += "unknown type: " + type;
        throw new RuntimeException(  );

    }

    /*
     * A value can consist of (and ONLY of):
     *  - a Number ( = byte, word, float, ...)
     *  - a Date and a Number
     *
     * Because of this simplification, the parsing logic stays simple/short.
     * If the type contains a date flag, the first four bytes are the date.
     * ps. mind the little endians
     *
     */
    BigDecimal toBigDecimal(Type type, int values[]) {

        int [] data = null;

        if( hasDate(type) ) {

            int newLength = values.length - Type.DATE.wordSize();
            data = new int[newLength];
            System.arraycopy(values, Type.DATE.wordSize(), data, 0, newLength);

        } else {

            data = values;

        }

        if( (type.intValue() & Type.WORD.intValue()) > 0 )
            return new BigDecimal( data[0] );

        if( (type.intValue() & Type.LONG_WORD.intValue() ) > 0 ) {

            int i =
                ( ( data[1] & 0x0000ffff ) << 16 ) |
                  ( data[0] & 0x0000ffff );

            return new BigDecimal( i );

        }

        String msg = "RegisterFactory.toBigDecimal() ";
        msg += "unknown type: " + type;
        throw new RuntimeException(  );

    }

    boolean hasDate(Type type) {
        return (type.intValue() & Type.DATE.intValue() ) > 0;
    }


    Date toDate(int[] values) {

        Calendar cal = ProtocolUtils.getCleanCalendar(getModBus().getTimeZone());
        int bcd[] = new int[12];

        bcd[0] = (values[1]&0x0000f000)>>12;
        bcd[1] = (values[1]&0x00000f00)>>8;
        bcd[2] = (values[1]&0x000000f0)>>4;
        bcd[3] = (values[1]&0x0000000f);

        bcd[4] = (values[2]&0x0000f000)>>12;
        bcd[5] = (values[2]&0x00000f00)>>8;
        bcd[6] = (values[2]&0x000000f0)>>4;
        bcd[7] = (values[2]&0x0000000f);

        bcd[8] = (values[3]&0x0000f000)>>12;
        bcd[9] = (values[3]&0x00000f00)>>8;
        bcd[10]= (values[3]&0x000000f0)>>4;
        bcd[11]= (values[3]&0x0000000f);

        dbg( "parsing date mm" + bcd[0] + bcd[1] + " yy" + bcd[2] + bcd[3] + " HH"  + bcd[4] + bcd[5] + " dd" + bcd[6] + bcd[7] + " ss" + bcd[8] + bcd[9] + " mm" + bcd[10] + bcd[11] );

        cal.set(Calendar.MONTH,        ((bcd[0]*10) + bcd[1]) -1);
        cal.set(Calendar.YEAR,         ((bcd[2]*10) + bcd[3])+2000);
        cal.set(Calendar.HOUR_OF_DAY,  (bcd[4]*10) + bcd[5]);
        cal.set(Calendar.DAY_OF_MONTH, (bcd[6]*10) + bcd[7]);
        cal.set(Calendar.SECOND,       (bcd[8]*10) + bcd[9]);
        cal.set(Calendar.MINUTE,       (bcd[10]*10)+ bcd[11]);

        Date result = null;

        if( bcd[0] != 0 || bcd[1] != 0 || bcd[2] != 0 ||
            bcd[3] != 0 || bcd[4] != 0 || bcd[6] != 0 ||
            bcd[7] != 0 || bcd[8] != 0 || bcd[9] != 0 ||
            bcd[10] != 0 || bcd[11] != 0 )

            result = cal.getTime();

        if( debug ) {

            String raw =

                "mm"    + bcd[0] + bcd[1] +
                " yy"   + bcd[2] + bcd[3] +
                " HH"   + bcd[4] + bcd[5] +
                " dd"   + bcd[6] + bcd[7] +
                " ss"   + bcd[8] + bcd[9] +
                " mm"   + bcd[10] + bcd[11];

            dbg( "toDate( " + raw + " ) -> " + result );

        }

        return result;


    }

    Date toPowerStreamDate(ByteArray byteArray) {


        Calendar cal = ProtocolUtils.getCleanCalendar(getModBus().getTimeZone());

        byte[] values = byteArray.getBytes();
        int bcd[] = new int[16];

        bcd[0] = (values[0]&0xf0)>>4;
        bcd[1] = (values[0]&0x0f);
        bcd[2] = (values[1]&0xf0)>>4;
        bcd[3] = (values[1]&0x0f);

        bcd[4] = (values[2]&0xf0)>>4;
        bcd[5] = (values[2]&0x0f);
        bcd[6] = (values[3]&0xf0)>>4;
        bcd[7] = (values[3]&0x0f);

        bcd[8] = (values[4]&0xf0)>>4;
        bcd[9] = (values[4]&0x0f);
        bcd[10]= (values[5]&0xf0)>>4;
        bcd[11]= (values[5]&0x0f);

        bcd[12] = (values[6]&0xf0)>>4;
        bcd[13] = (values[6]&0x0f);
        bcd[14] = (values[7]&0xf0)>>4;
        bcd[15] = (values[7]&0x0f);


        int year = (bcd[2]*10) + bcd[3];
        if( year > 60 )
            year += 1900;
        else
            year += 2000;

        cal.set(Calendar.MONTH,         ((bcd[0]*10) + bcd[1]) -1);
        cal.set(Calendar.YEAR,          year);
        cal.set(Calendar.DAY_OF_MONTH,  (bcd[6]*10)  + bcd[7]);
        cal.set(Calendar.MINUTE,        (bcd[8]*10)  + bcd[9]);
        cal.set(Calendar.HOUR_OF_DAY,   (bcd[10]*10) + bcd[11]);
        cal.set(Calendar.SECOND,        (bcd[14]*10) + bcd[15]);


        Date result = null;

        if( bcd[0] != 0 || bcd[1] != 0 || bcd[2] != 0 ||
            bcd[3] != 0 || bcd[4] != 0 || bcd[6] != 0 ||
            bcd[7] != 0 || bcd[8] != 0 || bcd[9] != 0 ||
            bcd[10] != 0 || bcd[11] != 0 )

            result = cal.getTime();

        if( debug ) {

            String raw =
                "mm"    + bcd[0] + bcd[1] +
                " yy"   + bcd[2] + bcd[3] +
                " dd"   + bcd[6] + bcd[7] +
                " mm"   + bcd[8] + bcd[9] +
                " HH"   + bcd[10] + bcd[11] +
                " ss"   + bcd[14] + bcd[15];

            dbg( "toDate( " + raw + " ) -> " + result );

        }

        return result;

    }


    /** @return short desciption of ALL the possibly available obiscodes */
    public String toString() {

        StringBuffer result;

        try {
            result = new StringBuffer()

            .append( "1 sec instantaeous values: \n")
            .append( toDbgString( v_1_1sec ) ).append( "\n" )
            .append( toDbgString( v_2_1sec ) ).append( "\n" )
            .append( toDbgString( v_3_1sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( u12_1sec ) ).append( "\n" )
            .append(toDbgString(u23_1sec)).append( "\n" )
            .append( toDbgString( u31_1sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( i1_1sec ) ).append( "\n" )
            .append( toDbgString( i2_1sec ) ).append( "\n" )
            .append( toDbgString( i3_1sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( p1_1sec ) ).append( "\n" )
            .append( toDbgString( p2_1sec ) ).append( "\n" )
            .append( toDbgString( p3_1sec ) ).append( "\n" )
            .append( toDbgString( pt_1sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( q1_1sec ) ).append( "\n" )
            .append( toDbgString( q2_1sec ) ).append( "\n" )
            .append( toDbgString( q3_1sec ) ).append( "\n" )
            .append( toDbgString( qt_1sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( s1_1sec ) ).append( "\n" )
            .append( toDbgString( s2_1sec ) ).append( "\n" )
            .append( toDbgString( s3_1sec ) ).append( "\n" )
            .append( toDbgString( st_1sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( fp1_1sec ) ).append( "\n" )
            .append( toDbgString( fp2_1sec ) ).append( "\n" )
            .append( toDbgString( fp3_1sec ) ).append( "\n" )
            .append( toDbgString( fpt_1sec ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( thd_u1_1sec) ).append( "\n" )
            .append( toDbgString( thd_u2_1sec) ).append( "\n" )
            .append( toDbgString( thd_u3_1sec) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( thd_i1_1sec) ).append( "\n" )
            .append( toDbgString( thd_i2_1sec) ).append( "\n" )
            .append( toDbgString( thd_i3_1sec) ).append( "\n" )
            .append( "\n" )

            .append("\n\n\nAverage values integrated x minutes:\n")
            .append( toDbgString( avg_v_1 ) ).append( "\n" )
            .append( toDbgString( avg_v_2 ) ).append( "\n" )
            .append( toDbgString( avg_v_3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_u_1 ) ).append( "\n" )
            .append( toDbgString( avg_u_2 ) ).append( "\n" )
            .append( toDbgString( avg_u_3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_i_1 ) ).append( "\n" )
            .append( toDbgString( avg_i_2 ) ).append( "\n" )
            .append( toDbgString( avg_i_3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_p_1 ) ).append( "\n" )
            .append( toDbgString( avg_p_2 ) ).append( "\n" )
            .append( toDbgString( avg_p_3 ) ).append( "\n" )
            .append( toDbgString( avg_pt ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_q_1 ) ).append( "\n" )
            .append( toDbgString( avg_q_2 ) ).append( "\n" )
            .append( toDbgString( avg_q_3 ) ).append( "\n" )
            .append( toDbgString( avg_qt ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_s_1 ) ).append( "\n" )
            .append( toDbgString( avg_s_2 ) ).append( "\n" )
            .append( toDbgString( avg_s_3 ) ).append( "\n" )
            .append( toDbgString( avg_st ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_fp_1 ) ).append( "\n" )
            .append( toDbgString( avg_fp_2 ) ).append( "\n" )
            .append( toDbgString( avg_fp_3 ) ).append( "\n" )
            .append( toDbgString( avg_fpt ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_thd_u1 ) ).append( "\n" )
            .append( toDbgString( avg_thd_u2 ) ).append( "\n" )
            .append( toDbgString( avg_thd_u3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( avg_thd_i1 ) ).append( "\n" )
            .append( toDbgString( avg_thd_i2 ) ).append( "\n" )
            .append( toDbgString( avg_thd_i3 ) ).append( "\n" )

            .append("\n\n\nEnergy meters:\n")
            .append( toDbgString( active_e_plus ) ).append( "\n" )
            .append( toDbgString( reactive_e_plus ) ).append( "\n" )
            .append( toDbgString( apparent_e_plus ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_e_min ) ).append( "\n" )
            .append( toDbgString( reactive_e_min ) ).append( "\n" )
            .append( toDbgString( apparent_e_min ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_e_slot_0 ) ).append( "\n" )
            .append( toDbgString( reactive_e_slot_0 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_e_slot_1 ) ).append( "\n" )
            .append( toDbgString( reactive_e_slot_1 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_e_slot_2 ) ).append( "\n" )
            .append( toDbgString( reactive_e_slot_2 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_e_slot_3 ) ).append( "\n" )
            .append( toDbgString( reactive_e_slot_3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_e_slot_4 ) ).append( "\n" )
            .append( toDbgString( reactive_e_slot_4 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_e_slot_5 ) ).append( "\n" )
            .append( toDbgString( reactive_e_slot_5 ) ).append( "\n" )

            .append("\n\n\nMin. values \n")
            .append( toDbgString( min_v_1 ) ).append( "\n" )
            .append( toDbgString( min_v_2 ) ).append( "\n" )
            .append( toDbgString( min_v_3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_u_12 ) ).append( "\n" )
            .append( toDbgString( min_u_23 ) ).append( "\n" )
            .append( toDbgString( min_u_31 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_i_1 ) ).append( "\n" )
            .append( toDbgString( min_i_2 ) ).append( "\n" )
            .append( toDbgString( min_i_3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_p_1 ) ).append( "\n" )
            .append( toDbgString( min_p_2 ) ).append( "\n" )
            .append( toDbgString( min_p_3 ) ).append( "\n" )
            .append( toDbgString( min_pt ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_q_1 ) ).append( "\n" )
            .append( toDbgString( min_q_2 ) ).append( "\n" )
            .append( toDbgString( min_q_3 ) ).append( "\n" )
            .append( toDbgString( min_qt ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_s_1 ) ).append( "\n" )
            .append( toDbgString( min_s_2 ) ).append( "\n" )
            .append( toDbgString( min_s_3 ) ).append( "\n" )
            .append( toDbgString( min_st ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_fpt ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_thd_u1 ) ).append( "\n" )
            .append( toDbgString( min_thd_u2 ) ).append( "\n" )
            .append( toDbgString( min_thd_u3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_thd_i1 ) ).append( "\n" )
            .append( toDbgString( min_thd_i2 ) ).append( "\n" )
            .append( toDbgString( min_thd_i3 ) ).append( "\n" )

            .append("\n\n\n Max. values:\n")
            .append( toDbgString( max_v_1 ) ).append( "\n" )
            .append( toDbgString( max_v_2 ) ).append( "\n" )
            .append( toDbgString( max_v_3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_u_12 ) ).append( "\n" )
            .append( toDbgString( max_u_23 ) ).append( "\n" )
            .append( toDbgString( max_u_31 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_i_1 ) ).append( "\n" )
            .append( toDbgString( max_i_2 ) ).append( "\n" )
            .append( toDbgString( max_i_3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_p_1 ) ).append( "\n" )
            .append( toDbgString( max_p_2 ) ).append( "\n" )
            .append( toDbgString( max_p_3 ) ).append( "\n" )
            .append( toDbgString( max_pt ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_q_1 ) ).append( "\n" )
            .append( toDbgString( max_q_2 ) ).append( "\n" )
            .append( toDbgString( max_q_3 ) ).append( "\n" )
            .append( toDbgString( max_qt ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_s_1 ) ).append( "\n" )
            .append( toDbgString( max_s_2 ) ).append( "\n" )
            .append( toDbgString( max_s_3 ) ).append( "\n" )
            .append( toDbgString( max_st ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_fpt ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_thd_u1 ) ).append( "\n" )
            .append( toDbgString( max_thd_u2 ) ).append( "\n" )
            .append( toDbgString( max_thd_u3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( max_thd_i1 ) ).append( "\n" )
            .append( toDbgString( max_thd_i2 ) ).append( "\n" )
            .append( toDbgString( max_thd_i3 ) ).append( "\n" )
            .append( "\n" );


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

    private void dbg(Object o) {
        if( debug ) System.out.println( "" + o);
    }

}
