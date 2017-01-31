/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.modbus.socomec.countis.ci;

import com.energyict.mdc.common.ObisCode;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Defines all available registers for the Countis Ci
 *
 * @author gna
 * @since 10-dec-2009
 *
 */
public class RegisterFactory extends AbstractRegisterFactory {

	public static final String currentDateTime = "currentDateTime";
	public static final String pulseParser = "pulseParser";
	public static final String energyParser = "energyParser";
	public static final String scalerInput1 = "scalerInput1";
	public static final String scalerInput2 = "scalerInput2";
	public static final String scalerInput3 = "scalerInput3";
	public static final String scalerInput4 = "scalerInput4";
	public static final String scalerInput5 = "scalerInput5";
	public static final String scalerInput6 = "scalerInput6";
	public static final String scalerInput7 = "scalerInput7";
	public static final String[] energyScalers = {scalerInput1, scalerInput2, scalerInput3,
		scalerInput4, scalerInput5, scalerInput6, scalerInput7};

	/**
	 * @param modBus
	 */
	public RegisterFactory(Modbus modBus) {
		super(modBus);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void init() {

        setZeroBased(false); // this means that reg2read = reg-1
     // registers

        getRegisters().add(new HoldingRegister(0x0300,2,ObisCode.fromString("1.1.82.7.0.255"),"Pulse1 input 1").setParser(pulseParser));
        getRegisters().add(new HoldingRegister(0x0302,2,ObisCode.fromString("1.2.82.7.0.255"),"Pulse1 input 2").setParser(pulseParser));
        getRegisters().add(new HoldingRegister(0x0304,2,ObisCode.fromString("1.3.82.7.0.255"),"Pulse2 input 3").setParser(pulseParser));
        getRegisters().add(new HoldingRegister(0x0306,2,ObisCode.fromString("1.4.82.7.0.255"),"Pulse2 input 4").setParser(pulseParser));
        getRegisters().add(new HoldingRegister(0x0308,2,ObisCode.fromString("1.5.82.7.0.255"),"Pulse3 input 5").setParser(pulseParser));
        getRegisters().add(new HoldingRegister(0x030A,2,ObisCode.fromString("1.6.82.7.0.255"),"Pulse3 input 6").setParser(pulseParser));
        getRegisters().add(new HoldingRegister(0x030C,2,ObisCode.fromString("1.7.82.7.0.255"),"Pulse4 input 7").setParser(pulseParser));

        getRegisters().add(new HoldingRegister(0x0300,2,ObisCode.fromString("1.1.1.7.0.255"),"Active power+ (QI+QIV) channel 1").setParser(energyParser));
        getRegisters().add(new HoldingRegister(0x0302,2,ObisCode.fromString("1.2.1.7.0.255"),"Active power+ (QI+QIV) channel 2").setParser(energyParser));
        getRegisters().add(new HoldingRegister(0x0304,2,ObisCode.fromString("1.3.1.7.0.255"),"Active power+ (QI+QIV) channel 3").setParser(energyParser));
        getRegisters().add(new HoldingRegister(0x0306,2,ObisCode.fromString("1.4.1.7.0.255"),"Active power+ (QI+QIV) channel 4").setParser(energyParser));
        getRegisters().add(new HoldingRegister(0x0308,2,ObisCode.fromString("1.5.1.7.0.255"),"Active power+ (QI+QIV) channel 5").setParser(energyParser));
        getRegisters().add(new HoldingRegister(0x030A,2,ObisCode.fromString("1.6.1.7.0.255"),"Active power+ (QI+QIV) channel 6").setParser(energyParser));
        getRegisters().add(new HoldingRegister(0x030C,2,ObisCode.fromString("1.7.1.7.0.255"),"Active power+ (QI+QIV) channel 7").setParser(energyParser));

//        getRegisters().add(new HoldingRegister(0x0107, 7, energyScalers));
        getRegisters().add(new HoldingRegister(0x0107,1,scalerInput1));
        getRegisters().add(new HoldingRegister(0x0108,1,scalerInput2));
        getRegisters().add(new HoldingRegister(0x0109,1,scalerInput3));
        getRegisters().add(new HoldingRegister(0x010A,1,scalerInput4));
        getRegisters().add(new HoldingRegister(0x010B,1,scalerInput5));
        getRegisters().add(new HoldingRegister(0x010C,1,scalerInput6));
        getRegisters().add(new HoldingRegister(0x010D,1,scalerInput7));

        getRegisters().add(new HoldingRegister(1536, 6, currentDateTime)); 	// Current DateTime
	}

	/**
	 * {@inheritDoc}
	 */
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

        getParserFactory().addParser(pulseParser, new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                long val= values[1];
                val += values[0]<<16;
                return new BigDecimal(val);
            }
        });

        getParserFactory().addParser(energyParser, new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val= values[1];
                val += values[0]<<16;
                return BigDecimal.valueOf(val).multiply(getModBus().getRegisterMultiplier(register.getObisCode().getB()));
            }
        });
    }

}
