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

package com.energyict.protocolimpl.modbus.ge.pqm2;

//import com.energyict.concentrator.core.*;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;

import java.io.ByteArrayOutputStream;
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
        //setZeroBased(true); // this means that reg2read = reg-1

        // registers
        getRegisters().add(new HoldingRegister(0x230,4,"clock"));
        getRegisters().add(new HoldingRegister(0,3,"firmware version"));
        getRegisters().add(new HoldingRegister(0x0180,1,"UserDefined1"));
        getRegisters().add(new HoldingRegister(0x0450,1, ObisCode.fromString("1.1.82.8.0.255"),"Pulse1 input high"));
        getRegisters().add(new HoldingRegister(0x0451,1,ObisCode.fromString("1.2.82.8.0.255"),"Pulse1 input low"));
        getRegisters().add(new HoldingRegister(0x0452,1,ObisCode.fromString("1.3.82.8.0.255"),"Pulse2 input high"));
        getRegisters().add(new HoldingRegister(0x0453,1,ObisCode.fromString("1.4.82.8.0.255"),"Pulse2 input low"));
        getRegisters().add(new HoldingRegister(0x0454,1,ObisCode.fromString("1.5.82.8.0.255"),"Pulse3 input high"));
        getRegisters().add(new HoldingRegister(0x0455,1,ObisCode.fromString("1.6.82.8.0.255"),"Pulse3 input low"));
        getRegisters().add(new HoldingRegister(0x0456,1,ObisCode.fromString("1.7.82.8.0.255"),"Pulse4 input high"));
        getRegisters().add(new HoldingRegister(0x0457,1,ObisCode.fromString("1.8.82.8.0.255"),"Pulse4 input low"));
        getRegisters().add(new HoldingRegister(0x0440,1,ObisCode.fromString("1.1.14.7.0.255"), Unit.get(BaseUnit.HERTZ),"Supply frequency").setParser("F1_unsigned16bitIntegerScale-2"));

        getRegisters().add(new HoldingRegister(0x03d0,2,ObisCode.fromString("1.1.1.8.0.255"),Unit.get("kWh")).setParser("F3_unsigned32bitInteger"));
        getRegisters().add(new HoldingRegister(0x03d2,2,ObisCode.fromString("1.1.2.8.0.255"),Unit.get("kWh")).setParser("F3_unsigned32bitInteger"));
        getRegisters().add(new HoldingRegister(0x02f0,2,ObisCode.fromString("1.1.1.7.0.255"),Unit.get("kW")).setParser("F4_signed32bitIntegerScale-2"));
        getRegisters().add(new HoldingRegister(0x02f2,2,ObisCode.fromString("1.1.3.7.0.255"),Unit.get("kvar")).setParser("F4_signed32bitIntegerScale-2"));
        getRegisters().add(new HoldingRegister(0x02f4,2,ObisCode.fromString("1.1.9.7.0.255"),Unit.get("kVA")).setParser("F3_unsigned32bitIntegerScale-2"));
        getRegisters().add(new HoldingRegister(0x02F6,1,ObisCode.fromString("1.1.13.7.0.255")).setParser("F2_signed16bitIntegerScale-2"));

        getRegisters().add(new HoldingRegister(0x28E,2,ObisCode.fromString("1.1.12.7.0.255"),Unit.get(BaseUnit.VOLT)).setParser("F3_unsigned32bitInteger"));
        getRegisters().add(new HoldingRegister(0x286,2,ObisCode.fromString("1.1.112.7.0.255"),Unit.get(BaseUnit.VOLT)).setParser("F3_unsigned32bitInteger"));
        getRegisters().add(new HoldingRegister(0x243,1,ObisCode.fromString("1.1.11.7.0.255"),Unit.get(BaseUnit.AMPERE)).setParser("F1_unsigned16bitInteger"));
        getRegisters().add(new HoldingRegister(0x2F7,2,ObisCode.fromString("1.1.21.7.0.255")).setParser("F4_signed32bitIntegerScale-2")); // Power phase A
        getRegisters().add(new HoldingRegister(0x2FE,2,ObisCode.fromString("1.1.41.7.0.255")).setParser("F4_signed32bitIntegerScale-2")); // Power phase B
        getRegisters().add(new HoldingRegister(0x305,2,ObisCode.fromString("1.1.61.7.0.255")).setParser("F4_signed32bitIntegerScale-2")); // Power phase C
        getRegisters().add(new HoldingRegister(0x2FD,1,ObisCode.fromString("1.1.33.7.0.255")).setParser("F2_signed16bitIntegerScale-2")); // Power factor phase A
        getRegisters().add(new HoldingRegister(0x304,1,ObisCode.fromString("1.1.53.7.0.255")).setParser("F2_signed16bitIntegerScale-2")); // Power factor phase B
        getRegisters().add(new HoldingRegister(0x30B,1,ObisCode.fromString("1.1.73.7.0.255")).setParser("F2_signed16bitIntegerScale-2")); // Power factor phase C
        getRegisters().add(new HoldingRegister(0x288,2,ObisCode.fromString("1.1.32.7.0.255")).setParser("F3_unsigned32bitInteger")); // VAB
        getRegisters().add(new HoldingRegister(0x28A,2,ObisCode.fromString("1.1.52.7.0.255")).setParser("F3_unsigned32bitInteger")); // VBC
        getRegisters().add(new HoldingRegister(0x28C,2,ObisCode.fromString("1.1.72.7.0.255")).setParser("F3_unsigned32bitInteger")); // VAC
        getRegisters().add(new HoldingRegister(0x280,2,ObisCode.fromString("1.1.132.7.0.255"),Unit.get(BaseUnit.VOLT)).setParser("F3_unsigned32bitInteger")); // VAN
        getRegisters().add(new HoldingRegister(0x282,2,ObisCode.fromString("1.1.152.7.0.255"),Unit.get(BaseUnit.VOLT)).setParser("F3_unsigned32bitInteger")); // VBN
        getRegisters().add(new HoldingRegister(0x284,2,ObisCode.fromString("1.1.172.7.0.255"),Unit.get(BaseUnit.VOLT)).setParser("F3_unsigned32bitInteger")); // VCN
        getRegisters().add(new HoldingRegister(0x240,1,ObisCode.fromString("1.1.31.7.0.255")).setParser("F1_unsigned16bitInteger")); // IA
        getRegisters().add(new HoldingRegister(0x241,1,ObisCode.fromString("1.1.51.7.0.255")).setParser("F1_unsigned16bitInteger")); // IB
        getRegisters().add(new HoldingRegister(0x242,1,ObisCode.fromString("1.1.71.7.0.255")).setParser("F1_unsigned16bitInteger")); // IC

        getRegisters().add(new HoldingRegister(0x404,2,ObisCode.fromString("1.1.1.4.0.255"),Unit.get("kW")).setParser("F4_signed32bitIntegerScale-2")); // Average demand
//        getRegisters().add(new HoldingRegister(0x30C,1,ObisCode.fromString("1.1.1.3.0.255"),Unit.get("kW")).setParser("F4_signed32bitInteger")); // Minimum demand
        getRegisters().add(new HoldingRegister(0x40E,2,ObisCode.fromString("1.1.1.6.0.255"),Unit.get("kW")).setParser("F4_signed32bitIntegerScale-2")); // Maximum demand

        getRegisters().add(new HoldingRegister(0x0000,1,"ProductDeviceCode").setParser("F1_unsigned16bitInteger")); // returns 73
        getRegisters().add(new HoldingRegister(0x0020,4,"SerialNumber").setParser("F10")); // returns 73
    }

    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<((values.length-1-i)*16));
                }
                return BigDecimal.valueOf(val);
            }
        });

        getParserFactory().addParser("F4_signed32bitIntegerScale-2",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<((values.length-1-i)*16));
                }
                val = ParseUtils.signExtend(val, values.length*16);
                BigDecimal bd = new BigDecimal(val);
                bd = bd.movePointLeft(2);
                return bd;
            }
        });
        getParserFactory().addParser("F10",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (int i=0;i<values.length;i++) {
                    baos.write((values[i]>>8)&0xff);
                    baos.write((values[i])&0xff);
                }
                return new String(baos.toByteArray());
            }
        });

        getParserFactory().addParser("F4_signed32bitInteger",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<((values.length-1-i)*16));
                }
                val = ParseUtils.signExtend(val, values.length*16);
                BigDecimal bd = new BigDecimal(val);
                return bd;
            }
        });

        getParserFactory().addParser("F3_unsigned32bitIntegerScale-2",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<((values.length-1-i)*16));
                }
                return BigDecimal.valueOf(val).movePointLeft(2);
            }
        });
        getParserFactory().addParser("F3_unsigned32bitInteger",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<((values.length-1-i)*16));
                }
                return BigDecimal.valueOf(val);
            }
        });

        getParserFactory().addParser("F2_signed16bitIntegerScale-3",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                val = ParseUtils.signExtend((long)values[0]&0xFFFF, 16);
                return BigDecimal.valueOf(val).movePointLeft(3);
            }
        });

        getParserFactory().addParser("F2_signed16bitIntegerScale-2",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                val = ParseUtils.signExtend((long)values[0]&0xFFFF, 16);
                return BigDecimal.valueOf(val).movePointLeft(2);
            }
        });
        getParserFactory().addParser("F2_signed16bitInteger",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                val = ParseUtils.signExtend((long)values[0]&0xFFFF, 16);
                return BigDecimal.valueOf(val);
            }
        });


        getParserFactory().addParser("F1_unsigned16bitInteger",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                return BigDecimal.valueOf((long)values[0]&0xFFFF);
            }
        });
        getParserFactory().addParser("F1_unsigned16bitIntegerScale-2",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                return BigDecimal.valueOf((long)values[0]&0xFFFF).movePointLeft(2);
            }
        });
        getParserFactory().addParser("F1_unsigned16bitIntegerScale-3",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                return BigDecimal.valueOf((long)values[0]&0xFFFF).movePointLeft(3);
            }
        });

        getParserFactory().addDateParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                Calendar cal = ProtocolUtils.getCleanCalendar(getModBus().getTimeZone());
                cal.set(Calendar.HOUR_OF_DAY,((values[0]>>8)&0xFF));
                cal.set(Calendar.MINUTE,(values[0]&0xFF));
                cal.set(Calendar.SECOND,values[1]/1000);
                cal.set(Calendar.MONTH,((values[2]>>8)&0xFF)-1);
                cal.set(Calendar.DAY_OF_MONTH,(values[2]&0xFF));
                cal.set(Calendar.YEAR,values[3]);
                return cal.getTime();
            }
        });

        getParserFactory().addParser("firmware version",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                return "Product device code="+values[0]+", Hardware version code="+values[1]+", Main software version code="+values[2];
            }
        });
    } //private void initParsers()

} // public class RegisterFactory extends AbstractRegisterFactory
