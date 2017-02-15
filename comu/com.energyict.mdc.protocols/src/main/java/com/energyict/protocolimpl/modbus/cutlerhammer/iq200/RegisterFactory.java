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

package com.energyict.protocolimpl.modbus.cutlerhammer.iq200;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;

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
        getRegisters().add(new HoldingRegister(6263,2, ObisCode.fromString("1.1.16.8.0.255"), Unit.get("kWh")).setParser("fixedpoint"));
        getRegisters().add(new HoldingRegister(4651,2,ObisCode.fromString("1.1.1.7.0.255")));
        getRegisters().add(new HoldingRegister(4653,2,ObisCode.fromString("1.1.3.7.0.255")));
        getRegisters().add(new HoldingRegister(4655,2,ObisCode.fromString("1.1.9.7.0.255")));
        getRegisters().add(new HoldingRegister(4657,2,ObisCode.fromString("1.1.13.7.0.255")));
        //getRegisters().add(new HoldingRegister(4629,2,ObisCode.fromString("1.1.12.7.0.255")));
        //getRegisters().add(new HoldingRegister(4637,2,ObisCode.fromString("1.1.112.7.0.255")));
        //getRegisters().add(new HoldingRegister(4621,2,ObisCode.fromString("1.1.11.7.0.255")));
        getRegisters().add(new HoldingRegister(4667,2,ObisCode.fromString("1.1.21.7.0.255")));
        getRegisters().add(new HoldingRegister(4669,2,ObisCode.fromString("1.1.41.7.0.255")));
        getRegisters().add(new HoldingRegister(4671,2,ObisCode.fromString("1.1.61.7.0.255")));
        getRegisters().add(new HoldingRegister(4685,2,ObisCode.fromString("1.1.33.7.0.255")));
        getRegisters().add(new HoldingRegister(4687,2,ObisCode.fromString("1.1.53.7.0.255")));
        getRegisters().add(new HoldingRegister(4689,2,ObisCode.fromString("1.1.73.7.0.255")));
        getRegisters().add(new HoldingRegister(4623,2,ObisCode.fromString("1.1.32.7.0.255")));
        getRegisters().add(new HoldingRegister(4625,2,ObisCode.fromString("1.1.52.7.0.255")));
        getRegisters().add(new HoldingRegister(4627,2,ObisCode.fromString("1.1.72.7.0.255")));
        getRegisters().add(new HoldingRegister(4631,2,ObisCode.fromString("1.1.132.7.0.255")));
        getRegisters().add(new HoldingRegister(4633,2,ObisCode.fromString("1.1.152.7.0.255")));
        getRegisters().add(new HoldingRegister(4635,2,ObisCode.fromString("1.1.172.7.0.255")));
        getRegisters().add(new HoldingRegister(4611,2,ObisCode.fromString("1.1.31.7.0.255")));
        getRegisters().add(new HoldingRegister(4613,2,ObisCode.fromString("1.1.51.7.0.255")));
        getRegisters().add(new HoldingRegister(4615,2,ObisCode.fromString("1.1.71.7.0.255")));
        //getRegisters().add(new HoldingRegister(1122,1,ObisCode.fromString("1.1.1.4.0.255")));
        //getRegisters().add(new HoldingRegister(1104,2,ObisCode.fromString("1.1.1.3.0.255"),Unit.get("kW")));
        getRegisters().add(new HoldingRegister(4697,2,ObisCode.fromString("1.1.1.6.0.255")));


        getRegisters().add(new HoldingRegister(6255,2,"productid").setParser("fixedpoint"));


        // mMINT modbus <-> INCOM interface  does allow reading the registerconfiguration registers for the word order by using address 247 or 248.
        // we use a custom property to change the order because this reading of interface configuration registers is not possible within the framework.
        //getRegisters().add(new HoldingRegister(2002,1,"fpwordorder").setParser("fixedpoint"));
    }
    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                int val=0;
                for (int i=0;i<values.length;i++) {
                    if (getModBus().getRegisterOrderFloatingPoint()!=0)
                        val += (values[i]<<(i*16));
                    else
                        val += (values[i]<<((values.length-1-i)*16));
                }
                try {
                    return new BigDecimal(""+Float.intBitsToFloat(val));
                }
                catch(NumberFormatException e) {
                    return BigDecimal.valueOf(0);
                }
            }
        });
        getParserFactory().addParser("fixedpoint",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    if (getModBus().getRegisterOrderFixedPoint()!=0)
                        val += (values[i]<<(i*16));
                    else
                        val += (values[i]<<((values.length-1-i)*16));
                }
                return new BigDecimal(val);
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
