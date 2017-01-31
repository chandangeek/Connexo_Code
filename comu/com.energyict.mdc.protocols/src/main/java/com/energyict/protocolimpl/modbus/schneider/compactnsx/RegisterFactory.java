/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.modbus.schneider.compactnsx;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.Parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * @author gna
 *
 */
public class RegisterFactory extends AbstractRegisterFactory {

	/**
	 * @param modBus
	 */
	public RegisterFactory(Modbus modBus) {
		super(modBus);
	}

	protected void init() {

		setZeroBased(true);	// means that reg2read = reg-1

		// Voltage registers
        getRegisters().add(new HoldingRegister(1000,1,ObisCode.fromString("1.1.32.7.0.255"))); // RMS Phase-to-Phase voltage V12
        getRegisters().add(new HoldingRegister(1001,1,ObisCode.fromString("1.1.52.7.0.255"))); // RMS Phase-to-Phase voltage V23
        getRegisters().add(new HoldingRegister(1002,1,ObisCode.fromString("1.1.72.7.0.255"))); // RMS Phase-to-Phase voltage V31
        getRegisters().add(new HoldingRegister(1003,1,ObisCode.fromString("1.1.132.7.0.255"))); // RMS Phase-to-Neutral voltage V1N
        getRegisters().add(new HoldingRegister(1004,1,ObisCode.fromString("1.1.152.7.0.255"))); // RMS Phase-to-Neutral voltage V2N
        getRegisters().add(new HoldingRegister(1005,1,ObisCode.fromString("1.1.172.7.0.255"))); // RMS Phase-to-Neutral voltage V3N
        getRegisters().add(new HoldingRegister(1006,1,ObisCode.fromString("1.1.128.7.0.255"))); // Total average Line-to-Line
        getRegisters().add(new HoldingRegister(1007,1,ObisCode.fromString("1.1.129.7.0.255"))); // Total average Line-to-Neutral
        getRegisters().add(new HoldingRegister(1145,1,ObisCode.fromString("1.1.12.6.0.255"))); // Maximum of V12, V23, V31
        getRegisters().add(new HoldingRegister(1146,1,ObisCode.fromString("1.1.12.3.0.255"))); // Minimum of V12, V23, V31

        // Current registers
        getRegisters().add(new HoldingRegister(1016,1,ObisCode.fromString("1.1.31.7.0.255"))); // RMS current on phase 1:L1
        getRegisters().add(new HoldingRegister(1017,1,ObisCode.fromString("1.1.51.7.0.255"))); // RMS current on phase 2:L2
        getRegisters().add(new HoldingRegister(1018,1,ObisCode.fromString("1.1.71.7.0.255"))); // RMS current on phase 3:L3
        getRegisters().add(new HoldingRegister(1020,1,ObisCode.fromString("1.1.11.6.0.255"))); // Maximum I1, I2, I3
        getRegisters().add(new HoldingRegister(1026,1,ObisCode.fromString("1.1.11.3.0.255"))); // Minimum I1, I2, I3
        getRegisters().add(new HoldingRegister(1027,1,ObisCode.fromString("1.1.130.7.0.255"))); // Total average I1, I2, I3

        // Energy registers
        getRegisters().add(new HoldingRegister(2000,2,ObisCode.fromString("1.1.1.8.0.255"),Unit.get("kWh")).setParser("DINT")); // Active energy
        getRegisters().add(new HoldingRegister(2004,2,ObisCode.fromString("1.1.3.8.0.255"), Unit.get("kWh")).setParser("DINT")); // Reactive energy
        getRegisters().add(new HoldingRegister(2024,2,ObisCode.fromString("1.1.9.8.0.255"), Unit.get("kWh")).setParser("DINT")); // Apparent energy

        // Active power
        getRegisters().add(new HoldingRegister(1034,1,ObisCode.fromString("1.1.21.7.0.255"),Unit.get("kW")).setParser("PowerSign")); // Active power Phase1
        getRegisters().add(new HoldingRegister(1035,1,ObisCode.fromString("1.1.41.7.0.255"),Unit.get("kW")).setParser("PowerSign")); // Active power Phase2
        getRegisters().add(new HoldingRegister(1036,1,ObisCode.fromString("1.1.61.7.0.255"),Unit.get("kW")).setParser("PowerSign")); // Active power Phase3
        getRegisters().add(new HoldingRegister(1037,1,ObisCode.fromString("1.1.1.7.0.255"),Unit.get("kW")).setParser("PowerSign")); // Active power Total

        // Reactive power
        getRegisters().add(new HoldingRegister(1038,1,ObisCode.fromString("1.1.23.7.0.255"),Unit.get("kvar")).setParser("PowerSign")); // Reactive power Phase1
        getRegisters().add(new HoldingRegister(1039,1,ObisCode.fromString("1.1.43.7.0.255"),Unit.get("kvar")).setParser("PowerSign")); // Reactive power Phase2
        getRegisters().add(new HoldingRegister(1040,1,ObisCode.fromString("1.1.63.7.0.255"),Unit.get("kvar")).setParser("PowerSign")); // Reactive power Phase3
        getRegisters().add(new HoldingRegister(1041,1,ObisCode.fromString("1.1.3.7.0.255"),Unit.get("kvar")).setParser("PowerSign")); // Reactive power Total

        // Apparent power
        getRegisters().add(new HoldingRegister(1045,1,ObisCode.fromString("1.1.9.7.0.255"),Unit.get("VA"))); // Apparent power Total

        // Power Factors
        getRegisters().add(new HoldingRegister(1046,1,ObisCode.fromString("1.1.33.7.0.255"),Unit.get("")).setParser("PowerFactorSign")); // powerfactor phase A
        getRegisters().add(new HoldingRegister(1047,1,ObisCode.fromString("1.1.53.7.0.255"),Unit.get("")).setParser("PowerFactorSign")); // powerfactor phase B
        getRegisters().add(new HoldingRegister(1048,1,ObisCode.fromString("1.1.73.7.0.255"),Unit.get("")).setParser("PowerFactorSign")); // powerfactor phase C
        getRegisters().add(new HoldingRegister(1049,1,ObisCode.fromString("1.1.13.7.0.255"),Unit.get("")).setParser("PowerFactorSign")); // Total power factor

        // Signers
        getRegisters().add(new HoldingRegister(3316, 1, "PowerSign"));
        getRegisters().add(new HoldingRegister(3318, 1, "PowerFactorSign"));

        // Clock
        getRegisters().add(new HoldingRegister(8023,4, "Date"));

        // Buffer
        getRegisters().add(new HoldingRegister(8000,20, "Buffer"));
        getRegisters().add(new HoldingRegister(8021,1,"CommandStatus").setParser("IntegerParser"));


	}

    protected void initParsers() {

        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
            	long val = 0;
            	for(int i = 0; i < values.length; i++){
            		val += values[i]<<(16*i);
            	}
                BigDecimal bd = new BigDecimal(""+val);
                return bd;
//                return bd.multiply(getModBus().getRegisterMultiplier(register.getReg()));
            }
        });

    	getParserFactory().addParser("IntegerParser", new Parser() {
    		public Object val(int[] values, AbstractRegister register) throws IOException {
    			return values[0];
    		}
    	});

        // 32-bit big-endian signed integer parser
        getParserFactory().addParser("DINT", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                if (values.length == 2) {
                    byte[] intBitsArray = new byte[]{(byte) (values[0] >> 8 & 0xFF), (byte) (values[0] & 0xFF),
                            (byte) (values[1] >> 8 & 0xFF), (byte) (values[1] & 0xFF)};
                    try {
                        int val = ProtocolUtils.getInt(intBitsArray, 0, 4);
                        return new BigDecimal(String.valueOf(val));
                    } catch (IOException e) {
                    }
                }
                return BigDecimal.ZERO;
            }
        });

        getParserFactory().addParser("PowerSign",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
            	long val = 0;
            	for(int i = 0; i < values.length; i++){
            		val += values[i]<<(16*i);
            	}
            	if((val != 0x8000) && (val != 0x80000000)){
            		BigDecimal bd;
            		if((Integer)getModBus().getRegisterFactory().findRegister("PowerSign").objectValueWithParser("IntegerParser") == 1){
            			bd = new BigDecimal("-" + val);
            		} else {
            			bd = new BigDecimal("+" + val);
            		}
                    return bd.movePointLeft(1);
            	} else {
                    getModBus().getLogger().info("Register " + register.getObisCode() + " is not accessible when System Type is 30 or 31");
            		throw new ModbusException("Not supported when Systemtype is 30 or 31",(short) 0,0x83,0x02);
            	}
            }
        });

        getParserFactory().addParser("PowerFactorSign",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
            	long val = 0;
            	for(int i = 0; i < values.length; i++){
            		val += values[i]<<(16*i);
            	}
            	if((val != 0x8000) && (val != 0x80000000)){
            		BigDecimal bd;
            		if((Integer)getModBus().getRegisterFactory().findRegister("PowerFactorSign").objectValueWithParser("IntegerParser") == 1){
            			bd = new BigDecimal("-" + val);
            		} else {
            			bd = new BigDecimal("+" + val);
            		}
                    return bd.movePointLeft(2);
            	} else {
                    getModBus().getLogger().info("Register " + register.getObisCode() + " is not accessible when System Type is 30 or 31");
            		throw new ModbusException("Not supported when Systemtype is 30 or 31",(short) 0,0x83,0x02);
            	}
            }
        });

        getParserFactory().addDateParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                Calendar cal = ProtocolUtils.getCleanCalendar(getModBus().getTimeZone());
                cal.set(Calendar.MONTH, ((values[0]>>8)&0xFF) - 1);
                cal.set(Calendar.DAY_OF_MONTH, (values[0])&0xFF);
                cal.set(Calendar.YEAR, 2000 + ((values[1]>>8)&0xFF));
                cal.set(Calendar.HOUR_OF_DAY, (values[1]&0xFF));
                cal.set(Calendar.MINUTE, ((values[2]>>8)&0xFF));
                cal.set(Calendar.SECOND, (values[2])&0xFF);
                cal.set(Calendar.MILLISECOND, values[3]);
                return cal.getTime();
            }
        });

    }

}
