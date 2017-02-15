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

package com.energyict.protocolimpl.modbus.socomec.a40;

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

	final static String slotInfo 		= "slotInfo";
	final static String productCode 	= "productCode";
	final static String profileInterval	= "profileInterval";
	final static String channelInfos	= "channelInfos";
	final static String aePointer		= "activeEnergyPointer";
	final static String rePointer		= "reactiveEnergyPointer";
	final static String dtLastUpdate	= "dateTimeLastProfileUpdate";
	final static String currentDateTime = "currentDateTime";


    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }

    protected void init() {
        // options
        setZeroBased(false); // this means that reg2read = reg-1

        // registers
        getRegisters().add(new HoldingRegister(1835,2,ObisCode.fromString("1.1.16.8.0.255"),Unit.get("kWh")).setParser("decimal10000")); 	// active Energy+
        getRegisters().add(new HoldingRegister(1837,2,ObisCode.fromString("1.1.36.8.0.255"),Unit.get("kvarh")).setParser("decimal10000")); 	// reactive Energy+
        getRegisters().add(new HoldingRegister(1803,1,ObisCode.fromString("1.1.1.7.0.255"),Unit.get("kW")).setParser("power"));				// Active Power
        getRegisters().add(new HoldingRegister(1804,1,ObisCode.fromString("1.1.3.7.0.255"),Unit.get("kvar")).setParser("power"));			// Reactive Power
        getRegisters().add(new HoldingRegister(1805,1,ObisCode.fromString("1.1.9.7.0.255"),Unit.get("kVA")).setParser("power"));			// Apparent Power
        getRegisters().add(new HoldingRegister(1806,1,ObisCode.fromString("1.1.13.7.0.255")).setParser("powerfactor"));						// PowerFactor
        getRegisters().add(new HoldingRegister(1858,1,ObisCode.fromString("1.1.12.7.0.255")).setParser("voltage"));// A40					// Voltage any phase
        getRegisters().add(new HoldingRegister(1859,1,ObisCode.fromString("1.1.112.7.0.255")).setParser("voltage")); // A40					//
        getRegisters().add(new HoldingRegister(1857,1,ObisCode.fromString("1.1.11.7.0.255")).setParser("current")); // A40					// Current any phase
        getRegisters().add(new HoldingRegister(1807,1,ObisCode.fromString("1.1.21.7.0.255"))); // A40										// Active power ph1
        getRegisters().add(new HoldingRegister(1808,1,ObisCode.fromString("1.1.41.7.0.255"))); // A40										// Active power ph2
        getRegisters().add(new HoldingRegister(1809,1,ObisCode.fromString("1.1.61.7.0.255"))); // A40										// Active power ph3
        getRegisters().add(new HoldingRegister(1810,1,ObisCode.fromString("1.1.23.7.0.255"))); // A40										// Reactive power ph1
        getRegisters().add(new HoldingRegister(1811,1,ObisCode.fromString("1.1.43.7.0.255"))); // A40										// Reactive power ph2
        getRegisters().add(new HoldingRegister(1812,1,ObisCode.fromString("1.1.63.7.0.255"))); // A40										// Reactive power ph3
        getRegisters().add(new HoldingRegister(1816,1,ObisCode.fromString("1.1.33.7.0.255")).setParser("powerfactor")); // A40				// PowerFactor ph1
        getRegisters().add(new HoldingRegister(1817,1,ObisCode.fromString("1.1.53.7.0.255")).setParser("powerfactor")); // A40				// PowerFactor ph2
        getRegisters().add(new HoldingRegister(1818,1,ObisCode.fromString("1.1.73.7.0.255")).setParser("powerfactor")); // A40				// PowerFactor ph3
        getRegisters().add(new HoldingRegister(1796,1,ObisCode.fromString("1.1.32.7.0.255")).setParser("voltage"));							// Voltage ph1
        getRegisters().add(new HoldingRegister(1797,1,ObisCode.fromString("1.1.52.7.0.255")).setParser("voltage"));							// Voltage ph2
        getRegisters().add(new HoldingRegister(1798,1,ObisCode.fromString("1.1.72.7.0.255")).setParser("voltage"));							// Voltage ph3
        getRegisters().add(new HoldingRegister(1799,1,ObisCode.fromString("1.1.132.7.0.255")).setParser("voltage"));						//
        getRegisters().add(new HoldingRegister(1800,1,ObisCode.fromString("1.1.152.7.0.255")).setParser("voltage"));						//
        getRegisters().add(new HoldingRegister(1801,1,ObisCode.fromString("1.1.172.7.0.255")).setParser("voltage"));						//
        getRegisters().add(new HoldingRegister(1792,1,ObisCode.fromString("1.1.31.7.0.255")).setParser("current"));							// Current ph1
        getRegisters().add(new HoldingRegister(1793,1,ObisCode.fromString("1.1.51.7.0.255")).setParser("current"));							// Current ph2
        getRegisters().add(new HoldingRegister(1794,1,ObisCode.fromString("1.1.71.7.0.255")).setParser("current"));							// Current ph3
        getRegisters().add(new HoldingRegister(1822,1,ObisCode.fromString("1.1.1.4.0.255"))); // A40										// Average Active Power
        //getRegisters().add(new HoldingRegister(1104,1,ObisCode.fromString("1.1.1.3.0.255")));
        getRegisters().add(new HoldingRegister(1830,1,ObisCode.fromString("1.1.1.6.0.255"),Unit.get("kW")).setParser("power"));				// Max. Demand Active Power

        getRegisters().add(new HoldingRegister(257,1,productCode));

        getRegisters().add(new HoldingRegister(513,1,"ctSec"));
        getRegisters().add(new HoldingRegister(514,1,"ctPrim"));

        getRegisters().add(new HoldingRegister(3072, 6, currentDateTime));

        // LoadProfile related registers
        getRegisters().add(new HoldingRegister(258,4,slotInfo));
        getRegisters().add(new HoldingRegister(580,1,profileInterval));
        getRegisters().add(new HoldingRegister(575,4,channelInfos));
        getRegisters().add(new HoldingRegister(12288, 1, aePointer));
        getRegisters().add(new HoldingRegister(12289, 1, rePointer));
        getRegisters().add(new HoldingRegister(12290, 3, dtLastUpdate));


        // mMINT modbus <-> INCOM interface  does allow reading the registerconfiguration registers for the word order by using address 247 or 248.
        // we use a custom property to change the order because this reading of interface configuration registers is not possible within the framework.
        //getRegisters().add(new HoldingRegister(2002,1,"fpwordorder").setParser("fixedpoint"));
    }
    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<(i*16));
                }
                return new BigDecimal(val);
            }
        });

        getParserFactory().addParser("decimal10000",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += values[i]*(long)Math.pow(10, i*4);
                }
                return BigDecimal.valueOf(val);
            }
        });

        getParserFactory().addParser("power",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<(i*16));
                }
                val = ParseUtils.signExtend(val, values.length*16);

                BigDecimal bd = new BigDecimal(val);
                bd = bd.multiply(getModBus().getRegisterMultiplier(MultiplierFactory.CT));
                bd = bd.movePointLeft(3);
                return bd;
            }
        });
        getParserFactory().addParser("voltage",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<(i*16));
                }
                BigDecimal bd = new BigDecimal(val);
                bd = bd.movePointLeft(getModBus().getRegisterMultiplier(MultiplierFactory.VSHIFT).intValue());
                return bd;
            }
        });
        getParserFactory().addParser("current",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=0;
                for (int i=0;i<values.length;i++) {
                    val += (values[i]<<(i*16));
                }
                BigDecimal bd = new BigDecimal(val);
                bd = bd.multiply(getModBus().getRegisterMultiplier(MultiplierFactory.CT));
                bd = bd.movePointLeft(3);
                return bd;
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
                val = ParseUtils.signExtend((long)values[0]&0xFFFF, 16);
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
