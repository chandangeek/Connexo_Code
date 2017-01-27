/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.squared.pm800;

import com.energyict.obis.ObisCode;
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
        getRegisters().add(new HoldingRegister(1716,4,ObisCode.fromString("1.1.16.8.0.255")));
        getRegisters().add(new HoldingRegister(1143,1,ObisCode.fromString("1.1.1.7.0.255")).setParser("scale F"));
        getRegisters().add(new HoldingRegister(1147,1,ObisCode.fromString("1.1.3.7.0.255")).setParser("scale F"));
        getRegisters().add(new HoldingRegister(1151,1,ObisCode.fromString("1.1.9.7.0.255")).setParser("scale F"));
        getRegisters().add(new HoldingRegister(1163,1,ObisCode.fromString("1.1.13.7.0.255")).setParser("powerfactor"));
        getRegisters().add(new HoldingRegister(1123,1,ObisCode.fromString("1.1.12.7.0.255")).setParser("scale D"));
        getRegisters().add(new HoldingRegister(1128,1,ObisCode.fromString("1.1.112.7.0.255")).setParser("scale D"));
        getRegisters().add(new HoldingRegister(1105,1,ObisCode.fromString("1.1.11.7.0.255")).setParser("scale A"));
        getRegisters().add(new HoldingRegister(1140,1,ObisCode.fromString("1.1.21.7.0.255")).setParser("scale F"));
        getRegisters().add(new HoldingRegister(1141,1,ObisCode.fromString("1.1.41.7.0.255")).setParser("scale F"));
        getRegisters().add(new HoldingRegister(1142,1,ObisCode.fromString("1.1.61.7.0.255")).setParser("scale F"));
        getRegisters().add(new HoldingRegister(1160,1,ObisCode.fromString("1.1.33.7.0.255")).setParser("powerfactor"));
        getRegisters().add(new HoldingRegister(1161,1,ObisCode.fromString("1.1.53.7.0.255")).setParser("powerfactor"));
        getRegisters().add(new HoldingRegister(1162,1,ObisCode.fromString("1.1.73.7.0.255")).setParser("powerfactor"));
        getRegisters().add(new HoldingRegister(1120,1,ObisCode.fromString("1.1.32.7.0.255")).setParser("scale D"));
        getRegisters().add(new HoldingRegister(1121,1,ObisCode.fromString("1.1.52.7.0.255")).setParser("scale D"));
        getRegisters().add(new HoldingRegister(1122,1,ObisCode.fromString("1.1.72.7.0.255")).setParser("scale D"));
        getRegisters().add(new HoldingRegister(1124,1,ObisCode.fromString("1.1.132.7.0.255")).setParser("scale D"));
        getRegisters().add(new HoldingRegister(1125,1,ObisCode.fromString("1.1.152.7.0.255")).setParser("scale D"));
        getRegisters().add(new HoldingRegister(1126,1,ObisCode.fromString("1.1.172.7.0.255")).setParser("scale D"));
        getRegisters().add(new HoldingRegister(1100,1,ObisCode.fromString("1.1.31.7.0.255")).setParser("scale A"));
        getRegisters().add(new HoldingRegister(1101,1,ObisCode.fromString("1.1.51.7.0.255")).setParser("scale A"));
        getRegisters().add(new HoldingRegister(1102,1,ObisCode.fromString("1.1.71.7.0.255")).setParser("scale A"));
        getRegisters().add(new HoldingRegister(1373,1,ObisCode.fromString("1.1.1.3.0.255")));
        getRegisters().add(new HoldingRegister(1378,1,ObisCode.fromString("1.1.1.6.0.255")));
        
        // Time&Date
        getRegisters().add(new HoldingRegister(3034,3,"present datetime"));

        // Metering Configuration and Status
        getRegisters().add(new HoldingRegister(3200,1,ObisCode.fromString("0.0.96.1.1.255"), "Metering System Type"));
        getRegisters().add(new HoldingRegister(3201,1,ObisCode.fromString("1.1.0.4.2.255"), "CT Ratio, 3-Phase Primary"));
        getRegisters().add(new HoldingRegister(3202,1,ObisCode.fromString("1.1.0.4.5.255"), "CT Ratio, 3-Phase Secondary"));
        getRegisters().add(new HoldingRegister(3205,1,ObisCode.fromString("1.1.0.4.3.255"), "PT Ratio, 3-Phase Primary"));
        getRegisters().add(new HoldingRegister(3206,1,ObisCode.fromString("1.1.0.4.8.255"), "PT Ratio, 3-Phase Primary Scale Factor").setParser("scalefactor"));
        getRegisters().add(new HoldingRegister(3207,1,ObisCode.fromString("1.1.0.4.6.255"), "PT Ratio, 3-Phase Secondary"));
        getRegisters().add(new HoldingRegister(3208,1,ObisCode.fromString("1.1.0.6.2.255"), " Nominal System Frequency"));

        getRegisters().add(new HoldingRegister(3209,1,"scale A").setParser("scalefactor"));
        getRegisters().add(new HoldingRegister(3210,1,"scale B").setParser("scalefactor"));
        getRegisters().add(new HoldingRegister(3212,1,"scale D").setParser("scalefactor"));
        getRegisters().add(new HoldingRegister(3213,1,"scale E").setParser("scalefactor"));
        getRegisters().add(new HoldingRegister(3214,1,"scale F").setParser("scalefactor"));
        
    }
    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val = 0;
                for (int i = 0; i < values.length; i++) {
                    val += values[i] * (long) Math.pow(10, i * 4);
                }
                return BigDecimal.valueOf(val);
            }
        });
        getParserFactory().addDateParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                Calendar cal = ProtocolUtils.getCleanCalendar(getModBus().getTimeZone());
                cal.set(Calendar.MONTH, ((values[0] >> 8) & 0xFF) - 1);
                cal.set(Calendar.DAY_OF_MONTH, (values[0] & 0xFF));
                cal.set(Calendar.YEAR, ((values[1] >> 8) & 0xFF) + 1900);
                cal.set(Calendar.HOUR_OF_DAY, values[1] & 0xFF);
                cal.set(Calendar.MINUTE, ((values[2] >> 8) & 0xFF));
                cal.set(Calendar.SECOND, values[2] & 0xFF);
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
                return BigDecimal.valueOf((short) values[0]);
            }
        });
        getParserFactory().addParser("scale A",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += values[i]*(long)Math.pow(10, i*4);
                }
                return BigDecimal.valueOf(val).multiply(getModBus().getRegisterMultiplier('A')); 
            }
        });
        getParserFactory().addParser("scale B",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += values[i]*(long)Math.pow(10, i*4);
                }
                return BigDecimal.valueOf(val).multiply(getModBus().getRegisterMultiplier('B')); 
            }
        });
        getParserFactory().addParser("scale D",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += values[i]*(long)Math.pow(10, i*4);
                }
                return BigDecimal.valueOf(val).multiply(getModBus().getRegisterMultiplier('D')); 
            }
        });
        getParserFactory().addParser("scale E",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += values[i]*(long)Math.pow(10, i*4);
                }
                return BigDecimal.valueOf(val).multiply(getModBus().getRegisterMultiplier('E')); 
            }
        });
        getParserFactory().addParser("scale F",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += values[i]*(long)Math.pow(10, i*4);
                }
                return BigDecimal.valueOf(val).multiply(getModBus().getRegisterMultiplier('F')); 
            }
        });
    } //private void initParsers()
    
} // public class RegisterFactory extends AbstractRegisterFactory
