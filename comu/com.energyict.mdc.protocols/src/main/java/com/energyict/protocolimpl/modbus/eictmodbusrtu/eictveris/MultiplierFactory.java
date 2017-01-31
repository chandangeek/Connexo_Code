/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MultiplierFactory.java
 *
 * Created on 26 september 2007, 11:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris;

import com.energyict.mdc.common.Unit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author kvds
 */
public class MultiplierFactory {

    static List list = new ArrayList();
    static {
        list.add(new Multiplier(1, Unit.get("kWh"),new BigDecimal("0.007812500"),new BigDecimal("0.031250000"),new BigDecimal("0.062500000"),new BigDecimal("0.125000000"),new BigDecimal("0.250000000")));
        list.add(new Multiplier(2,Unit.get("kWh"),new BigDecimal("512.000000000"),new BigDecimal("2048.000000000"),new BigDecimal("4096.000000000"),new BigDecimal("8192.000000000"),new BigDecimal("16384.000000000")));
        list.add(new Multiplier(3,Unit.get("kW"),new BigDecimal("0.004000000"),new BigDecimal("0.016000000"),new BigDecimal("0.032000000"),new BigDecimal("0.064000000"),new BigDecimal("0.128000000")));
        list.add(new Multiplier(4,Unit.get("var"),new BigDecimal("0.004000000"),new BigDecimal("0.016000000"),new BigDecimal("0.032000000"),new BigDecimal("0.064000000"),new BigDecimal("0.128000000")));
        list.add(new Multiplier(5,Unit.get("VA"),new BigDecimal("0.004000000"),new BigDecimal("0.016000000"),new BigDecimal("0.032000000"),new BigDecimal("0.064000000"),new BigDecimal("0.128000000")));
        list.add(new Multiplier(6,Unit.get(""),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518")));
        list.add(new Multiplier(7,Unit.get("V"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000")));
        list.add(new Multiplier(8,Unit.get("V"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000")));
        list.add(new Multiplier(9,Unit.get("A"),new BigDecimal("0.003906300"),new BigDecimal("0.015625000"),new BigDecimal("0.031250000"),new BigDecimal("0.062500000"),new BigDecimal("0.125000000")));
        list.add(new Multiplier(10,Unit.get("kW"),new BigDecimal("0.001000000"),new BigDecimal("0.004000000"),new BigDecimal("0.008000000"),new BigDecimal("0.016000000"),new BigDecimal("0.032000000")));
        list.add(new Multiplier(11,Unit.get("kW"),new BigDecimal("0.001000000"),new BigDecimal("0.004000000"),new BigDecimal("0.008000000"),new BigDecimal("0.016000000"),new BigDecimal("0.032000000")));
        list.add(new Multiplier(12,Unit.get("kW"),new BigDecimal("0.001000000"),new BigDecimal("0.004000000"),new BigDecimal("0.008000000"),new BigDecimal("0.016000000"),new BigDecimal("0.032000000")));
        list.add(new Multiplier(13,Unit.get(""),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518")));
        list.add(new Multiplier(14,Unit.get(""),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518")));
        list.add(new Multiplier(15,Unit.get(""),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518"),new BigDecimal("0.000030518")));
        list.add(new Multiplier(16,Unit.get("V"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000")));
        list.add(new Multiplier(17,Unit.get("V"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000")));
        list.add(new Multiplier(18,Unit.get("V"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000"),new BigDecimal("0.031250000")));
        list.add(new Multiplier(19,Unit.get("V"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000")));
        list.add(new Multiplier(20,Unit.get("V"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000")));
        list.add(new Multiplier(21,Unit.get("V"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000"),new BigDecimal("0.015625000")));
        list.add(new Multiplier(22,Unit.get("A"),new BigDecimal("0.003906300"),new BigDecimal("0.015625000"),new BigDecimal("0.031250000"),new BigDecimal("0.062500000"),new BigDecimal("0.125000000")));
        list.add(new Multiplier(23,Unit.get("A"),new BigDecimal("0.003906300"),new BigDecimal("0.015625000"),new BigDecimal("0.031250000"),new BigDecimal("0.062500000"),new BigDecimal("0.125000000")));
        list.add(new Multiplier(24,Unit.get("A"),new BigDecimal("0.003906300"),new BigDecimal("0.015625000"),new BigDecimal("0.031250000"),new BigDecimal("0.062500000"),new BigDecimal("0.125000000")));
        list.add(new Multiplier(25,Unit.get("kW"),new BigDecimal("0.004000000"),new BigDecimal("0.016000000"),new BigDecimal("0.032000000"),new BigDecimal("0.064000000"),new BigDecimal("0.128000000")));
        list.add(new Multiplier(26,Unit.get("kW"),new BigDecimal("0.004000000"),new BigDecimal("0.016000000"),new BigDecimal("0.032000000"),new BigDecimal("0.064000000"),new BigDecimal("0.128000000")));
        list.add(new Multiplier(27,Unit.get("kW"),new BigDecimal("0.004000000"),new BigDecimal("0.016000000"),new BigDecimal("0.032000000"),new BigDecimal("0.064000000"),new BigDecimal("0.128000000")));

    }

    String firmwareVersion;
    // 0=100A; 1=300/400A; 2=800A, 3=1600A, 4=2400A
    int meterType;

    /** Creates a new instance of MultiplierFactory */
    public MultiplierFactory(String firmwareVersion) {
        this.firmwareVersion=firmwareVersion;
    }

    public void init() throws IOException {

        if (firmwareVersion.indexOf("50")>=0)
            meterType=0;
        else if (firmwareVersion.indexOf("100")>=0)
            meterType=0;
        else if (firmwareVersion.indexOf("300/400")>=0)
            meterType=1;
        else if (firmwareVersion.indexOf("300")>=0)
            meterType=1;
        else if (firmwareVersion.indexOf("400")>=0)
            meterType=1;
        else if (firmwareVersion.indexOf("600")>=0)
            meterType=2;
        else if (firmwareVersion.indexOf("800")>=0)
            meterType=2;
        else if (firmwareVersion.indexOf("1600")>=0)
            meterType=3;
        else if (firmwareVersion.indexOf("2400")>=0)
            meterType=4;
        else
            throw new IOException("MultiplierFactory, error, no supported metertype for firmware version "+firmwareVersion);
    }

    public BigDecimal findMultiplier(int address) throws IOException {

        Iterator it = list.iterator();
        while(it.hasNext()) {

            Multiplier multiplier = (Multiplier)it.next();
            if (multiplier.getAddress()==address) {
                if (meterType == 0)
                    return multiplier.getMul100A();
                if (meterType == 1)
                    return multiplier.getMul300_400A();
                if (meterType == 2)
                    return multiplier.getMul800A();
                if (meterType == 3)
                    return multiplier.getMul1600A();
                if (meterType == 4)
                    return multiplier.getMul2400A();
            }
        }

        throw new IOException("MultiplierFactory, error, no register found for address "+address);
    }

}
