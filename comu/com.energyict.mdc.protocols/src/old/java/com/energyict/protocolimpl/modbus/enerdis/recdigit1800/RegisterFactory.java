/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.enerdis.recdigit1800;

import com.energyict.mdc.common.ApplicationException;
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
 * Responsibilities:
 *  - Accesspoint to Registers
 *  - All parsing
 *
 * @author fbo
 * @beginchanges
 * GNA|25042008| changed default timeZone to meterTimezone
 */

class RegisterFactory extends AbstractRegisterFactory {

    private final static boolean debug = false;

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

    RecDigit1800 recDigit;

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

    private HoldingRegister min_i_ph1;
    private HoldingRegister min_i_ph2;
    private HoldingRegister min_i_ph3;

    private HoldingRegister min_v_ph1;
    private HoldingRegister min_v_ph2;
    private HoldingRegister min_v_ph3;

    private HoldingRegister min_p_ph1;
    private HoldingRegister min_p_ph2;
    private HoldingRegister min_p_ph3;

    private HoldingRegister min_p_tot;

    private HoldingRegister min_q_ph1;
    private HoldingRegister min_q_ph2;
    private HoldingRegister min_q_ph3;

    private HoldingRegister min_q_tot;

    private HoldingRegister min_fp;

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

    private HoldingRegister min_u_ph1;
    private HoldingRegister min_u_ph2;
    private HoldingRegister min_u_ph3;

    private HoldingRegister active_energy_tou_0;
    private HoldingRegister active_energy_tou_1;
    private HoldingRegister active_energy_tou_2;
    private HoldingRegister active_energy_tou_3;
    private HoldingRegister active_energy_tou_4;
    private HoldingRegister active_energy_tou_5;

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

    private HoldingRegister max_u_ph1;
    private HoldingRegister max_u_ph2;
    private HoldingRegister max_u_ph3;


    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
        recDigit = (RecDigit1800) modBus;
    }

    protected void init() {

        String d;
        String o;

        d = "1sec line t neutral voltage V Ph1";
        v_ph1_1_sec = add( "1.1.32.7.0.255", 0x0012, d, V, Type.WORD);
        d = "1sec line t neutral voltage V Ph2";
        v_ph2_1_sec = add( "1.1.52.7.0.255", 0x0014, d, V, Type.WORD);
        d = "1sec line t neutral voltage V Ph3";
        v_ph3_1_sec = add( "1.1.72.7.0.255", 0x0016, d, V, Type.WORD);

        d = "1sec current I Ph1";
        i_ph1_1_sec = add( "1.1.31.7.0.255", 0x0018, d, A, Type.WORD);

        d = "1sec current I Ph2";
        i_ph2_1_sec = add( "1.1.51.7.0.255", 0x001A, d, A, Type.WORD);
        d = "1sec current I Ph3";
        i_ph3_1_sec = add( "1.1.71.7.0.255", 0x001C, d, A, Type.WORD);

        d = "1sec active power P Ph1";
        p_ph1_1_sec = add( "1.1.21.7.0.255", 0x001E, d, W, Type.LONG_WORD);
        d = "1sec active power P Ph2";
        p_ph2_1_sec = add( "1.1.41.7.0.255", 0x0022, d, W, Type.LONG_WORD);
        d = "1sec active power P Ph3";
        p_ph3_1_sec = add( "1.1.61.7.0.255", 0x0026, d, W, Type.LONG_WORD);

        d = "1sec active power P tot";
        p_tot_1_sec = add( "1.1.1.7.0.255", 0x002A, d, W, Type.LONG_WORD);

        d = "FP1 1sec";
        fp_ph1_1_sec  = add( "1.1.33.7.0.255", 0x002E, d, fpUnit, Type.BYTE);
        d = "FP2 1sec";
        fp_ph2_1_sec  = add( "1.1.53.7.0.255", 0x002F, d, fpUnit, Type.BYTE);
        d = "FP3 1sec";
        fp_ph3_1_sec  = add( "1.1.73.7.0.255", 0x0030, d, fpUnit, Type.BYTE);

        d = "FPt 1sec";
        fp_tot_1_sec  = add( "1.1.13.7.0.255", 0x0031, d, fpUnit, Type.BYTE);

        d = "1sec reactive power Q Ph1";
        q_ph1_1_sec = add( "1.1.23.7.0.255", 0x0032, d, VAr, Type.LONG_WORD);
        d = "1sec reactive power Q Ph2";
        q_ph2_1_sec = add( "1.1.43.7.0.255", 0x0036, d, VAr, Type.LONG_WORD);
        d = "1sec reactive power Q Ph3";
        q_ph3_1_sec = add( "1.1.63.7.0.255", 0x003A, d, VAr, Type.LONG_WORD);

        d = "1sec reactive power Q totale";
        q_tot_1_sec = add( "1.1.3.7.0.255", 0x003E, d, VAr, Type.LONG_WORD);

        d = "Minimum Of I Ph 1";
        min_i_ph1 = add( "1.1.31.3.0.255", 0x0221, d, A, Type.DATE_AND_WORD);
        d = "Minimum Of I Ph 2";
        min_i_ph2 = add( "1.1.51.3.0.255", 0x022A, d, A, Type.DATE_AND_WORD);
        d = "Minimum Of I Ph 3";
        min_i_ph3 = add( "1.1.71.3.0.255", 0x0232, d, A, Type.DATE_AND_WORD);

        d = "Minimum Of V Ph 1";
        min_v_ph1 = add( "1.1.32.3.0.255", 0x023A, d, V, Type.DATE_AND_WORD);
        d = "Minimum Of V Ph 2";
        min_v_ph2 = add( "1.1.52.3.0.255", 0x0242, d, V, Type.DATE_AND_WORD);
        d = "Minimum Of V Ph 3";
        min_v_ph3 = add( "1.1.72.3.0.255", 0x024A, d, V, Type.DATE_AND_WORD);

        d = "Minimum Of P Ph 1";
        o = "1.1.21.3.0.255";
        min_p_ph1 = add( o, 0x0252, d, V, Type.DATE_AND_LONG_WORD);
        d = "Minimum Of P Ph 2";
        o = "1.1.41.3.0.255";
        min_p_ph2 = add( o, 0x025C, d, V, Type.DATE_AND_LONG_WORD);
        d = "Minimum Of P Ph 3";
        o = "1.1.61.3.0.255";
        min_p_ph3 = add( o, 0x0266, d, V, Type.DATE_AND_LONG_WORD);

        d = "Minimum Of P totale";
        o = "1.1.1.3.0.255";
        min_p_tot = add( o, 0x0270, d, V, Type.DATE_AND_LONG_WORD);

        d = "Minimum Of Q Ph 1";
        o = "1.1.23.3.0.255";
        min_q_ph1 = add( o, 0x027A, d, VAr, Type.DATE_AND_LONG_WORD);
        d = "Minimum Of Q Ph 2";
        o = "1.1.43.3.0.255";
        min_q_ph2 = add( o, 0x0284, d, VAr, Type.DATE_AND_LONG_WORD);
        d = "Minimum Of Q Ph 3";
        o = "1.1.63.3.0.255";
        min_q_ph3 = add( o, 0x028E, d, VAr, Type.DATE_AND_LONG_WORD);

        d = "Minimum Of Q totale";
        o = "1.1.3.3.0.255";
        min_q_tot = add( o, 0x029E, d, VAr, Type.DATE_AND_LONG_WORD);

        d = "Minimum FP";
        min_fp = add( "1.1.13.3.0.255", 0x02A2, d, VAr, Type.DATE_AND_WORD);

        d = "Average voltage line to neutral V Ph1";
        avg_v_ph1 = add( "1.1.32.5.0.255", 0x02B2, d, V, Type.WORD);
        d = "Average voltage line to neutral V Ph2";
        avg_v_ph2 = add( "1.1.52.5.0.255", 0x02B4, d, V, Type.WORD);
        d = "Average voltage line to neutral V Ph3";
        avg_v_ph3 = add( "1.1.72.5.0.255", 0x02B6, d, V, Type.WORD);

        d = "Average current I Ph1";
        avg_i_ph1 = add( "1.1.31.5.0.255", 0x02B8, d, A, Type.WORD);
        d = "Average current I Ph2";
        avg_i_ph2 = add( "1.1.51.5.0.255", 0x02BA, d, A, Type.WORD);
        d = "Average current I Ph3";
        avg_i_ph3 = add( "1.1.71.5.0.255", 0x02BC, d, A, Type.WORD);

        d = "Average active power P Ph1";
        avg_p_ph1 = add( "1.1.21.5.0.255", 0x02BE, d, W, Type.LONG_WORD);
        d = "Average active power P Ph2";
        avg_p_ph2 = add( "1.1.41.5.0.255", 0x02C2, d, W, Type.LONG_WORD);
        d = "Average active power P Ph3";
        avg_p_ph3 = add( "1.1.61.5.0.255", 0x02C6, d, W, Type.LONG_WORD);

        d = "Average active power P totale";
        avg_p_tot = add( "1.1.1.5.0.255", 0x02CA, d, W, Type.LONG_WORD);

        d = "Average power factor FP Ph1";
        avg_fp_ph1 = add( "1.1.33.5.0.255", 0x02CE, d, fpUnit, Type.BYTE);
        d = "Average power factor FP Ph2";
        avg_fp_ph2 = add( "1.1.53.5.0.255", 0x02CF, d, fpUnit, Type.BYTE);
        d = "Average power factor FP Ph3";
        avg_fp_ph3 = add( "1.1.73.5.0.255", 0x02D0, d, fpUnit, Type.BYTE);

        d = "Average power factor P global";
        avg_fp_global = add( "1.1.13.5.0.255", 0x02D1, d, fpUnit, Type.BYTE);

        d = "Average reactive power Q Ph1";
        avg_q_ph1 = add( "1.1.23.5.0.255", 0x02D2, d, VAr, Type.LONG_WORD);
        d = "Average reactive power Q Ph2";
        avg_q_ph2 = add( "1.1.43.5.0.255", 0x02D6, d, VAr, Type.LONG_WORD);
        d = "Average reactive power Q Ph3";
        avg_q_ph3 = add( "1.1.63.5.0.255", 0x02DA, d, VAr, Type.LONG_WORD);

        d = "Average reactive power Q totale";
        avg_q_tot = add( "1.1.3.5.0.255", 0x02DE, d, VAr, Type.LONG_WORD);

        d = "Active energy meter Phase1";
        o = "1.1.21.8.0.255";
        active_energy_ph1 = add( o, 0x02E2, d, kWh, Type.DOUBLE_LONG_WORD);
        d = "Reactive energy meter Phase1";
        o = "1.1.23.8.0.255";
        reactive_energy_ph1 = add( o, 0x02EA, d, kvarh, Type.DOUBLE_LONG_WORD);

        d = "Active energy meter Phase2";
        o = "1.1.41.8.0.255";
        active_energy_ph2 = add( o, 0x02F2, d, kWh, Type.DOUBLE_LONG_WORD);
        d = "Reactive energy meter Phase2";
        o = "1.1.43.8.0.255";
        reactive_energy_ph2 = add( o, 0x02FA, d, kvarh, Type.DOUBLE_LONG_WORD);

        d = "Active energy meter Phase3";
        o = "1.1.61.8.0.255";
        active_energy_ph3 = add( o, 0x0302, d, kWh, Type.DOUBLE_LONG_WORD);
        d = "Reactive energy meter Phase3";
        o = "1.1.63.8.0.255";
        reactive_energy_ph3 = add( o, 0x030A, d, kvarh, Type.DOUBLE_LONG_WORD);

        d = "Active energy meter totale";
        o = "1.1.1.8.0.255";
        active_energy_tot = add( o, 0x0312, d, kWh, Type.DOUBLE_LONG_WORD);
        d = "Reactive energy meter totale";
        o = "1.1.3.8.0.255";
        reactive_energy_tot = add( o, 0x031A, d, kvarh, Type.DOUBLE_LONG_WORD);

        d = "Minimum of U Ph1";
        min_u_ph1 = add( "1.1.154.3.0.255", 0x0312, d, kWh, Type.DATE_AND_WORD);
        d = "Minimum of U Ph2";
        min_u_ph2 = add( "1.1.155.3.0.255", 0x0312, d, kWh, Type.DATE_AND_WORD);
        d = "Minimum of U Ph3";
        min_u_ph3 = add( "1.1.156.3.0.255", 0x0312, d, kWh, Type.DATE_AND_WORD);

        d = "Active energy meter time interval 0";
        o = "1.1.1.8.1.255";
        active_energy_tou_0 = add( o, 0x03B2, d, kWh, Type.DOUBLE_LONG_WORD);
        d = "Active energy meter time interval 1";
        o = "1.1.1.8.2.255";
        active_energy_tou_1 = add( o, 0x03BA, d, kWh, Type.DOUBLE_LONG_WORD);
        d = "Active energy meter time interval 2";
        o = "1.1.1.8.3.255";
        active_energy_tou_2 = add( o, 0x03C2, d, kWh, Type.DOUBLE_LONG_WORD);
        d = "Active energy meter time interval 3";
        o = "1.1.1.8.4.255";
        active_energy_tou_3 = add( o, 0x03CA, d, kWh, Type.DOUBLE_LONG_WORD);
        d = "Active energy meter time interval 4";
        o = "1.1.1.8.5.255";
        active_energy_tou_4 = add( o, 0x03D2, d, kWh, Type.DOUBLE_LONG_WORD);
        d = "Active energy meter time interval 5";
        o = "1.1.1.8.6.255";
        active_energy_tou_5 = add( o, 0x03DA, d, kWh, Type.DOUBLE_LONG_WORD);

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

        d = "Maximum U Ph 1";
        max_u_ph1 = add( "1.1.154.6.0.255", 0x17F0, d, V, Type.DATE_AND_WORD);
        d = "Maximum U Ph 2";
        max_u_ph2 = add( "1.1.155.6.0.255", 0x17F8, d, V, Type.DATE_AND_WORD);
        d = "Maximum U Ph 3";
        max_u_ph3 = add( "1.1.156.6.0.255", 0x1800, d, V, Type.DATE_AND_WORD);


    }

    /** create a HoldingRegister (=factory method) */
    private HoldingRegister add(
        String obis, int address, String description, Unit unit, Type type ){

        ObisCode oc   = ObisCode.fromString(obis);
        int wordSize  = type.wordSize();

        HoldingRegister hr =
            new HoldingRegister(
                address, wordSize, oc, unit, description);
        hr.setRegisterFactory(this);
        hr.setParser(type.toString());

        getRegisters().add( hr );

        return hr;

    }

    protected void initParsers() {


        getParserFactory().addParser(Type.BYTE.toString(),
        new Parser() {
            public Object val(int[] values, AbstractRegister register)
                throws IOException {

                dbg( "Parser.val( " + Type.BYTE + " )" );
                BigDecimal bd = toBigDecimal(Type.BYTE, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );

                return new RegisterValue( register.getObisCode(), q );

            }
        });

        getParserFactory().addParser(Type.OCTET.toString(),
        new Parser() {
            public Object val(int[] values, AbstractRegister register)
                throws IOException {

                dbg( "Parser.val( " + Type.OCTET + " )" );
                BigDecimal bd = toBigDecimal(Type.OCTET, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                return new RegisterValue( register.getObisCode(), q );
            }
        });

        getParserFactory().addParser(Type.WORD.toString(),
        new Parser() {
            public Object val(int[] values, AbstractRegister register)
                throws IOException {

                dbg( "Parser.val( " + Type.WORD + " )" );
                BigDecimal bd = toBigDecimal(Type.WORD, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                return new RegisterValue( register.getObisCode(), q );
            }
        });

        getParserFactory().addParser(Type.LONG_WORD.toString(),
        new Parser() {
            public Object val(int[] values, AbstractRegister register)
                throws IOException {

                dbg( "Parser.val( " + Type.LONG_WORD + " )" );

                BigDecimal bd = toBigDecimal(Type.LONG_WORD, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                return new RegisterValue( register.getObisCode(), q );
            }
        });

        getParserFactory().addParser(Type.DOUBLE_LONG_WORD.toString(),
        new Parser() {
            public Object val(int[] values, AbstractRegister register)
                throws IOException {

                dbg( "Parser.val( " + Type.DOUBLE_LONG_WORD + " )" );

                BigDecimal bd = toBigDecimal(Type.DOUBLE_LONG_WORD, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                return new RegisterValue( register.getObisCode(), q );
            }
        });

        getParserFactory().addParser(Type.REAL_NUMBER.toString(),
        new Parser() {
            public Object val(int[] values, AbstractRegister register) {

                dbg( "Parser.val( " + Type.REAL_NUMBER + " )" );

                try {
                    BigDecimal bd = toBigDecimal(Type.REAL_NUMBER, values);
                    Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                    return new RegisterValue( register.getObisCode(), q );
                } catch( IOException ioe ){
                    throw new ApplicationException(ioe);
                }
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

        getParserFactory().addParser(Type.DATE_AND_WORD.toString(),
        new Parser() {
            public Object val(int[] values, AbstractRegister register)
                throws IOException {

                dbg( "Parser.val( " + Type.DATE_AND_WORD  + " )" );

                BigDecimal bd = toBigDecimal(Type.DATE_AND_WORD, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                Date date = toDate(values);
                return new RegisterValue( register.getObisCode(), q, date );
                }
        });

        getParserFactory().addParser(Type.DATE_AND_LONG_WORD.toString(),
        new Parser() {
            public Object val(int[] values, AbstractRegister register)
                throws IOException {

                dbg( "Parser.val( " + Type.DATE_AND_LONG_WORD  + " )" );

                BigDecimal bd = toBigDecimal(Type.DATE_AND_LONG_WORD, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                Date date = toDate(values);
                return new RegisterValue( register.getObisCode(), q, date );
            }
        });
    }



    Date toDate(int[] values) {

        Calendar cal = ProtocolUtils.getCleanCalendar(getModBus().getTimeZone());
        int bcd[] = new int[12];

        bcd[0] = (values[0]&0x0000f000)>>12;
        bcd[1] = (values[0]&0x00000f00)>>8;
        bcd[2] = (values[0]&0x000000f0)>>4;
        bcd[3] = (values[0]&0x0000000f);

        bcd[4] = (values[1]&0x0000f000)>>12;
        bcd[5] = (values[1]&0x00000f00)>>8;
        bcd[6] = (values[1]&0x000000f0)>>4;
        bcd[7] = (values[1]&0x0000000f);

        bcd[8] = (values[2]&0x0000f000)>>12;
        bcd[9] = (values[2]&0x00000f00)>>8;
        bcd[10]= (values[2]&0x000000f0)>>4;
        bcd[11]= (values[2]&0x0000000f);

        int year = (bcd[2]*10) + bcd[3];
        if( year > 60 )
            year += 1900;
        else
            year += 2000;

        cal.set(Calendar.MONTH,         ((bcd[0]*10) + bcd[1]) -1);
        cal.set(Calendar.YEAR,          year);
        cal.set(Calendar.HOUR_OF_DAY,   (bcd[4]*10) + bcd[5]);
        cal.set(Calendar.DAY_OF_MONTH,  (bcd[6]*10) + bcd[7]);
        cal.set(Calendar.SECOND,        (bcd[8]*10) + bcd[9]);
        cal.set(Calendar.MINUTE,        (bcd[10]*10)+ bcd[11]);

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

        if( (type.intValue() & Type.BYTE.intValue()) > 0 ) {
            /* cast NOT bitwise and, since it is a SIGNED byte */
            return new BigDecimal( (byte)values[0] );
        }

        if( (type.intValue() & Type.WORD.intValue()) > 0 )
            return new BigDecimal( data[0] );

        if( (type.intValue() & Type.LONG_WORD.intValue() ) > 0 ) {

            int i =
                ( ( data[1] & 0x0000ffff ) << 16 ) |
                  ( data[0] & 0x0000ffff );

            return new BigDecimal( i );

        }

        if( (type.intValue() & Type.DOUBLE_LONG_WORD.intValue()) > 0 ) {

            long i =
                (data[3] & 0x000000000000ffffl) << 48 |
                (data[2] & 0x000000000000ffffl) << 32 |
                (data[1] & 0x000000000000ffffl) << 16 |
                (data[0] & 0x000000000000ffffl);

            return new BigDecimal(i);

        }

        if( ( type.intValue() & Type.REAL_NUMBER.intValue()) > 0 ) {

            int i =
                ( ( data[1] & 0x0000ffff ) << 16 ) |
                  ( data[0] & 0x0000ffff );

            return new BigDecimal( Float.intBitsToFloat(i) );

        }

        String msg = "RegisterFactory.toBigDecimal() ";
        msg += "unknown type: " + type;
        throw new RuntimeException(  );

    }

    BigDecimal scaleEnergy( BigDecimal amount ) throws IOException {
        int round = BigDecimal.ROUND_HALF_UP;
        int scale = amount.scale() + 3;

        BigDecimal bd = amount.divide(recDigit.getKP(), scale, round);
        bd = bd.multiply(recDigit.getCtRatio());
        bd = bd.multiply(recDigit.getPtRatio());
        bd = bd.divide(new BigDecimal( 3600000 ), scale, round);

        return bd;
    }

    Quantity scale( Quantity q ) throws IOException {

        Unit unit = q.getUnit();
        BigDecimal amount = q.getAmount();
        int round = BigDecimal.ROUND_HALF_UP;
        int scale = amount.scale() + 3;

        if(  V.equals( unit ) ) {

            BigDecimal bd = amount.divide(recDigit.getKU(), scale, round);
            bd = bd.multiply(recDigit.getPtRatio());

            return new Quantity( bd, unit );

        }

        if( A.equals( unit ) ) {

            BigDecimal bd = amount.divide(recDigit.getKI(), scale, round);
            bd = bd.multiply(recDigit.getCtRatio());

            return new Quantity( bd, unit );

        }

        if( W.equals(unit) || VAr.equals(unit) ) {

            BigDecimal bd = amount.divide(recDigit.getKP(), scale, round);
            bd = bd.multiply(recDigit.getCtRatio());
            bd = bd.multiply(recDigit.getPtRatio());

            return new Quantity( bd, unit );

        }

        if( kWh.equals(unit) || kvarh.equals(unit) ) {

            BigDecimal bd = amount.divide(recDigit.getKP(), scale, round);
            bd = bd.multiply(recDigit.getCtRatio());
            bd = bd.multiply(recDigit.getPtRatio());
            bd = bd.divide(new BigDecimal( 3600000 ), scale, round);

            return new Quantity( bd, unit );

        }

        return q;

    }


    boolean hasDate(Type type) {
        return (type.intValue() & Type.DATE.intValue() ) > 0;
    }

    /** @return short desciption of ALL the possibly available obiscodes */
    public String toString() {

        StringBuffer result;

        try {
            result = new StringBuffer()

            .append( "1 sec instantaious values: \n")
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
            .append(  toDbgString( min_i_ph1 ) ).append( "\n" )
            .append(  toDbgString( min_i_ph2 ) ).append( "\n" )
            .append(  toDbgString( min_i_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append(  toDbgString( min_v_ph1 ) ).append( "\n" )
            .append(  toDbgString( min_v_ph2 ) ).append( "\n" )
            .append(  toDbgString( min_v_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_p_ph1 ) ).append( "\n" )
            .append( toDbgString( min_p_ph2 ) ).append( "\n" )
            .append( toDbgString( min_p_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_p_tot ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_q_ph1 ) ).append( "\n" )
            .append( toDbgString( min_q_ph2 ) ).append( "\n" )
            .append( toDbgString( min_q_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_q_tot ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( min_fp ) ).append( "\n" )
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
            .append( toDbgString( min_u_ph1 ) ).append( "\n" )
            .append( toDbgString( min_u_ph2 ) ).append( "\n" )
            .append( toDbgString( min_u_ph3 ) ).append( "\n" )
            .append( "\n" )
            .append( toDbgString( active_energy_tou_0 ) ).append( "\n" )
            .append( toDbgString( active_energy_tou_1 ) ).append( "\n" )
            .append( toDbgString( active_energy_tou_2 ) ).append( "\n" )
            .append( toDbgString( active_energy_tou_3 ) ).append( "\n" )
            .append( toDbgString( active_energy_tou_4 ) ).append( "\n" )
            .append( toDbgString( active_energy_tou_5 ) ).append( "\n" )
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
            .append( toDbgString( max_u_ph1 ) ).append( "\n" )
            .append( toDbgString( max_u_ph2 ) ).append( "\n" )
            .append( toDbgString( max_u_ph3 ) ).append( "\n" );
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

    private void dbg(Object o) {
        if( debug ) System.out.println( "" + o);
    }

}
