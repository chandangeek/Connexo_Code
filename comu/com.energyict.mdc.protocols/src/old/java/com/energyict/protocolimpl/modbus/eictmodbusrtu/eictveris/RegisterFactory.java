/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;

import java.io.IOException;
import java.math.BigDecimal;

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

        //getRegisters().add(new HoldingRegister(257,2,ObisCode.fromString("1.1.16.8.0.254"),Unit.get("kWh")).setParser("FloatingPoint")); // consumption
        getRegisters().add(new HoldingRegister(1,2, ObisCode.fromString("1.1.16.8.0.255"), Unit.get("kWh")).setParser("EnergyParser")); // consumption
        getRegisters().add(new HoldingRegister(3,1,ObisCode.fromString("1.1.1.7.0.255"),Unit.get("kW"))); // demand
        getRegisters().add(new HoldingRegister(4,1,ObisCode.fromString("1.1.3.7.0.255"),Unit.get("var"))); // reactive power
        getRegisters().add(new HoldingRegister(5,1,ObisCode.fromString("1.1.9.7.0.255"),Unit.get("VA"))); // apparent power
        getRegisters().add(new HoldingRegister(6,1,ObisCode.fromString("1.1.13.7.0.255"),Unit.get(""))); // PF
        getRegisters().add(new HoldingRegister(7,1,ObisCode.fromString("1.1.12.7.0.255"))); // total line to line
        getRegisters().add(new HoldingRegister(8,1,ObisCode.fromString("1.1.112.7.0.255"))); // total line to neutral
        getRegisters().add(new HoldingRegister(9,1,ObisCode.fromString("1.1.11.7.0.255"))); // Current
        getRegisters().add(new HoldingRegister(9,1,ObisCode.fromString("1.1.21.7.0.255"),Unit.get("kW"))); // demand phase A
        getRegisters().add(new HoldingRegister(10,1,ObisCode.fromString("1.1.41.7.0.255"),Unit.get("kW"))); // demand phase B
        getRegisters().add(new HoldingRegister(11,1,ObisCode.fromString("1.1.61.7.0.255"),Unit.get("kW"))); // demand phase C
        getRegisters().add(new HoldingRegister(13,1,ObisCode.fromString("1.1.33.7.0.255"),Unit.get(""))); // powerfactor phase A
        getRegisters().add(new HoldingRegister(14,1,ObisCode.fromString("1.1.53.7.0.255"),Unit.get(""))); // powerfactor phase B
        getRegisters().add(new HoldingRegister(15,1,ObisCode.fromString("1.1.73.7.0.255"),Unit.get(""))); // powerfactor phase C
        getRegisters().add(new HoldingRegister(16,1,ObisCode.fromString("1.1.32.7.0.255"))); // AB
        getRegisters().add(new HoldingRegister(17,1,ObisCode.fromString("1.1.52.7.0.255"))); // BC
        getRegisters().add(new HoldingRegister(18,1,ObisCode.fromString("1.1.72.7.0.255"))); // AC
        getRegisters().add(new HoldingRegister(19,1,ObisCode.fromString("1.1.132.7.0.255"))); // ANeutral
        getRegisters().add(new HoldingRegister(20,1,ObisCode.fromString("1.1.152.7.0.255"))); // BNeutral
        getRegisters().add(new HoldingRegister(21,1,ObisCode.fromString("1.1.172.7.0.255"))); // CNeutral
        getRegisters().add(new HoldingRegister(22,1,ObisCode.fromString("1.1.31.7.0.255"))); // Current phase A
        getRegisters().add(new HoldingRegister(23,1,ObisCode.fromString("1.1.51.7.0.255"))); // Current phase B
        getRegisters().add(new HoldingRegister(24,1,ObisCode.fromString("1.1.71.7.0.255"))); // Current phase C
        getRegisters().add(new HoldingRegister(25,1,ObisCode.fromString("1.1.1.4.0.255"),Unit.get("kW"))); // power average demand
        getRegisters().add(new HoldingRegister(26,1,ObisCode.fromString("1.1.1.3.0.255"),Unit.get("kW"))); // power min demand
        getRegisters().add(new HoldingRegister(27,1,ObisCode.fromString("1.1.1.6.0.255"),Unit.get("kW"))); // power max demand



    }

    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                BigDecimal bd = new BigDecimal(""+values[0]);
                return bd.multiply(getModBus().getRegisterMultiplier(register.getReg()));
            }
        });

        getParserFactory().addParser("EnergyParser",new Parser() {
            public Object val(int[] values, AbstractRegister register) throws IOException {
                long val=(values[1]<<16)+values[0];
                BigDecimal bd = new BigDecimal(""+val);
                return bd.multiply(getModBus().getRegisterMultiplier(register.getReg()));
            }
        });

        getParserFactory().addParser("FloatingPoint",new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                int val=(values[0]<<16)+values[1];
                return new BigDecimal(""+Float.intBitsToFloat(val));
            }
        });

    } //private void initParsers()

} // public class RegisterFactory extends AbstractRegisterFactory
