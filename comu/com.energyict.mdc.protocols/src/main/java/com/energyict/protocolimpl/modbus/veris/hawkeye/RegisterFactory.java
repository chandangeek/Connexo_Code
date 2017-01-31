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

package com.energyict.protocolimpl.modbus.veris.hawkeye;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;

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

        getRegisters().add(new HoldingRegister(257,2, ObisCode.fromString("1.1.16.8.0.255"), Unit.get("kWh"))); // energy
        getRegisters().add(new HoldingRegister(261,2,ObisCode.fromString("1.1.1.7.0.255"),Unit.get("kW"))); // demand power
        getRegisters().add(new HoldingRegister(263,2,ObisCode.fromString("1.1.3.7.0.255"),Unit.get("var"))); // reactive power
        getRegisters().add(new HoldingRegister(265,2,ObisCode.fromString("1.1.9.7.0.255"),Unit.get("VA"))); // apparent power
        getRegisters().add(new HoldingRegister(267,2,ObisCode.fromString("1.1.13.7.0.255"),Unit.get(""))); // PF
        getRegisters().add(new HoldingRegister(269,2,ObisCode.fromString("1.1.12.7.0.255"))); // total line to line
        getRegisters().add(new HoldingRegister(271,2,ObisCode.fromString("1.1.112.7.0.255"))); // total line to neutral
        getRegisters().add(new HoldingRegister(273,2,ObisCode.fromString("1.1.11.7.0.255"))); // Current
        getRegisters().add(new HoldingRegister(275,2,ObisCode.fromString("1.1.21.7.0.255"),Unit.get("kW"))); // power phase A
        getRegisters().add(new HoldingRegister(277,2,ObisCode.fromString("1.1.41.7.0.255"),Unit.get("kW"))); // power phase B
        getRegisters().add(new HoldingRegister(279,2,ObisCode.fromString("1.1.61.7.0.255"),Unit.get("kW"))); // power phase C
        getRegisters().add(new HoldingRegister(281,2,ObisCode.fromString("1.1.33.7.0.255"),Unit.get(""))); // powerfactor phase A
        getRegisters().add(new HoldingRegister(283,2,ObisCode.fromString("1.1.53.7.0.255"),Unit.get(""))); // powerfactor phase B
        getRegisters().add(new HoldingRegister(285,2,ObisCode.fromString("1.1.73.7.0.255"),Unit.get(""))); // powerfactor phase C
        getRegisters().add(new HoldingRegister(287,2,ObisCode.fromString("1.1.32.7.0.255"))); // AB
        getRegisters().add(new HoldingRegister(289,2,ObisCode.fromString("1.1.52.7.0.255"))); // BC
        getRegisters().add(new HoldingRegister(291,2,ObisCode.fromString("1.1.72.7.0.255"))); // AC
        getRegisters().add(new HoldingRegister(293,2,ObisCode.fromString("1.1.132.7.0.255"))); // ANeutral
        getRegisters().add(new HoldingRegister(295,2,ObisCode.fromString("1.1.152.7.0.255"))); // BNeutral
        getRegisters().add(new HoldingRegister(297,2,ObisCode.fromString("1.1.172.7.0.255"))); // CNeutral
        getRegisters().add(new HoldingRegister(299,2,ObisCode.fromString("1.1.31.7.0.255"))); // Current phase A
        getRegisters().add(new HoldingRegister(301,2,ObisCode.fromString("1.1.51.7.0.255"))); // Current phase B
        getRegisters().add(new HoldingRegister(303,2,ObisCode.fromString("1.1.71.7.0.255"))); // Current phase C
        getRegisters().add(new HoldingRegister(305,2,ObisCode.fromString("1.1.1.4.0.255"),Unit.get("kW"))); // power average demand
        getRegisters().add(new HoldingRegister(307,2,ObisCode.fromString("1.1.1.3.0.255"),Unit.get("kW"))); // power min demand
        getRegisters().add(new HoldingRegister(309,2,ObisCode.fromString("1.1.1.6.0.255"),Unit.get("kW"))); // power max demand



    }

    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                int val=(values[0]<<16)+values[1];
                return new BigDecimal(""+Float.intBitsToFloat(val));
            }
        });
    } //private void initParsers()

} // public class RegisterFactory extends AbstractRegisterFactory
