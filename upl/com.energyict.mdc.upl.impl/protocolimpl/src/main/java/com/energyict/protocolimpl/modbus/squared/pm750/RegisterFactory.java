/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.squared.pm750;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 *
 * @author Koen
 */
public class RegisterFactory extends AbstractRegisterFactory {
    
    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }
    
    protected void init() {
        // options
        setZeroBased(true); // this means that reg2read = reg-1
        
        // registers
        getRegisters().add(new HoldingRegister(1000,2,ObisCode.fromString("1.1.16.8.0.255"),Unit.get("kWh")));
        getRegisters().add(new HoldingRegister(1006,2,ObisCode.fromString("1.1.1.7.0.255"),Unit.get("kW")));
        getRegisters().add(new HoldingRegister(1010,2,ObisCode.fromString("1.1.3.7.0.255"),Unit.get("kvar")));
        getRegisters().add(new HoldingRegister(1008,2,ObisCode.fromString("1.1.9.7.0.255"),Unit.get("kVA")));
        getRegisters().add(new HoldingRegister(1012,2,ObisCode.fromString("1.1.13.7.0.255")));
        getRegisters().add(new HoldingRegister(1014,2,ObisCode.fromString("1.1.12.7.0.255")));
        getRegisters().add(new HoldingRegister(1016,2,ObisCode.fromString("1.1.112.7.0.255")));
        getRegisters().add(new HoldingRegister(1018,2,ObisCode.fromString("1.1.11.7.0.255")));
        getRegisters().add(new HoldingRegister(1066,2,ObisCode.fromString("1.1.21.7.0.255"),Unit.get("kW")));
        getRegisters().add(new HoldingRegister(1068,2,ObisCode.fromString("1.1.41.7.0.255"),Unit.get("kW")));
        getRegisters().add(new HoldingRegister(1070,2,ObisCode.fromString("1.1.61.7.0.255"),Unit.get("kW")));
        //getRegisters().add(new HoldingRegister(1160,1,ObisCode.fromString("1.1.33.7.0.255")).setParser("powerfactor"));
        //getRegisters().add(new HoldingRegister(1161,1,ObisCode.fromString("1.1.53.7.0.255")).setParser("powerfactor"));
        //getRegisters().add(new HoldingRegister(1162,1,ObisCode.fromString("1.1.73.7.0.255")).setParser("powerfactor"));
        getRegisters().add(new HoldingRegister(1054,2,ObisCode.fromString("1.1.32.7.0.255")));
        getRegisters().add(new HoldingRegister(1056,2,ObisCode.fromString("1.1.52.7.0.255")));
        getRegisters().add(new HoldingRegister(1058,2,ObisCode.fromString("1.1.72.7.0.255")));
        getRegisters().add(new HoldingRegister(1060,2,ObisCode.fromString("1.1.132.7.0.255")));
        getRegisters().add(new HoldingRegister(1062,2,ObisCode.fromString("1.1.152.7.0.255")));
        getRegisters().add(new HoldingRegister(1064,2,ObisCode.fromString("1.1.172.7.0.255")));
        getRegisters().add(new HoldingRegister(1034,2,ObisCode.fromString("1.1.31.7.0.255")));
        getRegisters().add(new HoldingRegister(1036,2,ObisCode.fromString("1.1.51.7.0.255")));
        getRegisters().add(new HoldingRegister(1038,2,ObisCode.fromString("1.1.71.7.0.255")));
        //getRegisters().add(new HoldingRegister(1122,1,ObisCode.fromString("1.1.1.4.0.255")));
        getRegisters().add(new HoldingRegister(1104,2,ObisCode.fromString("1.1.1.3.0.255"),Unit.get("kW")));
        getRegisters().add(new HoldingRegister(1154,2,ObisCode.fromString("1.1.1.6.0.255"),Unit.get("kW")));

        // Register Listing—Setup and Status
        getRegisters().add(new HoldingRegister(4120,1,ObisCode.fromString("1.1.0.4.2.255"), "CT Ratio - Primary").setParser("integer"));
        getRegisters().add(new HoldingRegister(4121,1,ObisCode.fromString("1.1.0.4.5.255"), "CT Ratio - Secondary").setParser("integer"));
        getRegisters().add(new HoldingRegister(4122,1,ObisCode.fromString("1.1.0.4.3.255"), "PT Ratio - Primary").setParser("integer"));
        getRegisters().add(new HoldingRegister(4123,1,ObisCode.fromString("1.1.0.4.8.255"), "PT Ratio - Scale").setParser("integer"));
        getRegisters().add(new HoldingRegister(4124,1,ObisCode.fromString("1.1.0.4.6.255"), "PT Ratio - Secondary").setParser("integer"));
        getRegisters().add(new HoldingRegister(4125,1,ObisCode.fromString("1.1.0.6.2.255"), "Service Frequency").setParser("integer"));

//        getRegisters().add(new HoldingRegister(4000,2,ObisCode.fromString("1.1.16.8.0.255"),Unit.get("kWh")).setParser("scale E"));
//        getRegisters().add(new HoldingRegister(4006,1,ObisCode.fromString("1.1.1.7.0.255"),Unit.get("kW")).setParser("scale W"));
//        getRegisters().add(new HoldingRegister(4008,1,ObisCode.fromString("1.1.3.7.0.255"),Unit.get("kvar")).setParser("scale W"));
//        getRegisters().add(new HoldingRegister(4007,1,ObisCode.fromString("1.1.9.7.0.255"),Unit.get("kVA")).setParser("scale W"));
//        getRegisters().add(new HoldingRegister(4048,1,ObisCode.fromString("1.1.13.7.0.255")).setParser("powerfactor"));
//        getRegisters().add(new HoldingRegister(4010,1,ObisCode.fromString("1.1.12.7.0.255")).setParser("scale V"));
//        getRegisters().add(new HoldingRegister(4011,1,ObisCode.fromString("1.1.112.7.0.255")).setParser("scale V"));
//        getRegisters().add(new HoldingRegister(4012,1,ObisCode.fromString("1.1.11.7.0.255")).setParser("scale I"));
//        getRegisters().add(new HoldingRegister(4036,1,ObisCode.fromString("1.1.21.7.0.255"),Unit.get("kW")).setParser("scale W"));
//        getRegisters().add(new HoldingRegister(4037,1,ObisCode.fromString("1.1.41.7.0.255"),Unit.get("kW")).setParser("scale W"));
//        getRegisters().add(new HoldingRegister(4038,1,ObisCode.fromString("1.1.61.7.0.255"),Unit.get("kW")).setParser("scale W"));
//        //getRegisters().add(new HoldingRegister(1160,1,ObisCode.fromString("1.1.33.7.0.255")).setParser("powerfactor"));
//        //getRegisters().add(new HoldingRegister(1161,1,ObisCode.fromString("1.1.53.7.0.255")).setParser("powerfactor"));
//        //getRegisters().add(new HoldingRegister(1162,1,ObisCode.fromString("1.1.73.7.0.255")).setParser("powerfactor"));
//        getRegisters().add(new HoldingRegister(4030,1,ObisCode.fromString("1.1.32.7.0.255")).setParser("scale V"));
//        getRegisters().add(new HoldingRegister(4031,1,ObisCode.fromString("1.1.52.7.0.255")).setParser("scale V"));
//        getRegisters().add(new HoldingRegister(4032,1,ObisCode.fromString("1.1.72.7.0.255")).setParser("scale V"));
//        getRegisters().add(new HoldingRegister(4033,1,ObisCode.fromString("1.1.132.7.0.255")).setParser("scale V"));
//        getRegisters().add(new HoldingRegister(4034,1,ObisCode.fromString("1.1.152.7.0.255")).setParser("scale V"));
//        getRegisters().add(new HoldingRegister(4035,1,ObisCode.fromString("1.1.172.7.0.255")).setParser("scale V"));
//        getRegisters().add(new HoldingRegister(4020,1,ObisCode.fromString("1.1.31.7.0.255")).setParser("scale I"));
//        getRegisters().add(new HoldingRegister(4021,1,ObisCode.fromString("1.1.51.7.0.255")).setParser("scale I"));
//        getRegisters().add(new HoldingRegister(4022,1,ObisCode.fromString("1.1.71.7.0.255")).setParser("scale I"));
//        //getRegisters().add(new HoldingRegister(1122,1,ObisCode.fromString("1.1.1.4.0.255")));
//        getRegisters().add(new HoldingRegister(4055,1,ObisCode.fromString("1.1.1.3.0.255"),Unit.get("kW")).setParser("scale W"));
//        getRegisters().add(new HoldingRegister(4080,1,ObisCode.fromString("1.1.1.6.0.255"),Unit.get("kW")).setParser("scale W"));
//        
//        // Time&Date
//        //getRegisters().add(new HoldingRegister(3034,3,"present datetime"));
//
//        getRegisters().add(new HoldingRegister(4105,1,"scale I").setParser("scalefactor"));
//        getRegisters().add(new HoldingRegister(4106,1,"scale V").setParser("scalefactor"));
//        getRegisters().add(new HoldingRegister(4107,1,"scale W").setParser("scalefactor"));
//        getRegisters().add(new HoldingRegister(4108,1,"scale E").setParser("scalefactor"));
        
    }
    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                int val = 0;
                for (int i = 0; i < values.length; i++) {
                    val += (values[i] << ((values.length - 1 - i) * 16));
                }
                try {
                    return new BigDecimal("" + Float.intBitsToFloat(val));
                } catch (NumberFormatException e) {
                    return BigDecimal.valueOf(0);
                }
            }
        });

        getParserFactory().addParser("integer", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                int val = 0;
                for (int i = 0; i < values.length; i++) {
                    val += (values[i] << ((values.length - 1 - i) * 16));
                }
                return val;
            }
        });
        
        getParserFactory().addDateParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                Calendar cal = ProtocolUtils.getCleanCalendar(getModBus().getTimeZone());
                cal.set(Calendar.MONTH,((values[0]>>8)&0xFF)-1);
                cal.set(Calendar.DAY_OF_MONTH,(values[0]&0xFF));
                cal.set(Calendar.YEAR,((values[1]>>8)&0xFF)+1900);
                cal.set(Calendar.HOUR_OF_DAY,values[1]&0xFF);
                cal.set(Calendar.MINUTE,((values[2]>>8)&0xFF));
                cal.set(Calendar.SECOND,values[2]&0xFF);
                return cal.getTime();
            }
        });
        getParserFactory().addParser("powerfactor",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                val = values[0]&0x7FFF;
                if ((values[0]&0x8000) != 0)
                    val *= (-1);
                return BigDecimal.valueOf(val).movePointLeft(3);
            }
        });
        getParserFactory().addParser("scalefactor",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                return BigDecimal.valueOf((short)values[0]);
            }
        });
        getParserFactory().addParser("scale I",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<((values.length-1-i)*16));
                }
                return BigDecimal.valueOf(val).multiply(getModBus().getRegisterMultiplier('I')); 
            }
        });
        getParserFactory().addParser("scale V",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<((values.length-1-i)*16));
                }
                return BigDecimal.valueOf(val).multiply(getModBus().getRegisterMultiplier('V')); 
            }
        });
        getParserFactory().addParser("scale W",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<((values.length-1-i)*16));
                }
                val = ParseUtils.signExtend(val,16*values.length);
                return BigDecimal.valueOf(val).multiply(getModBus().getRegisterMultiplier('W')); 
            }
        });
        getParserFactory().addParser("scale E",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<((values.length-1-i)*16));
                }
                val = ParseUtils.signExtend(val,16*values.length);
                return BigDecimal.valueOf(val).multiply(getModBus().getRegisterMultiplier('E')); 
            }
        });
    } //private void initParsers()
    
} // public class RegisterFactory extends AbstractRegisterFactory
