package com.energyict.protocolimpl.modbus.spiraxsarco;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.*;
import com.energyict.protocolimpl.modbus.generic.ParserFactory;

import java.util.HashMap;

/**
 * Created by cisac on 11/16/2015.
 */
public class VLM20RegisterFactory extends AbstractRegisterFactory {

    private static final String UNSIGNED_LONG = "5";
    private static final String STRING = "17";
    private static final String FLOAT = "7";
    private final String BCD_32BIT = "9";
    private ParserFactory parserFactory;

    // Map containing the mapping of unit string to EIServer units.
    private HashMap<String, Unit> unitTextHashMap = new HashMap<String, Unit>();
    // Mapping of Register Unit Number to its EIServer Unit -- used to prevent the same unit register is read out multiple times in one comm session.
    private HashMap<String, Unit> unitHashMap = new HashMap<String, Unit>();


    /**
     * Creates a new instance of AbstractRegisterFactory
     *
     * @param modBus
     */
    public VLM20RegisterFactory(Modbus modBus) {
        super(modBus);
    }

    @Override
    protected void init() {
        // options
        setZeroBased(((VLM20) getModBus()).isStartRegistersZeroBased()); // this means that reg2read = reg-1

        // registers
//        getRegisters().add(new InputRegister(100, 2, ObisCode.fromString("0.0.96.0.1.255"), "Serial number").setParser(UNSIGNED_LONG));
        getRegisters().add(new InputRegister(525, 2, ObisCode.fromString("7.0.96.5.1.255"), "Totalizer").setParser(UNSIGNED_LONG));
        getRegisters().add(new InputRegister(2037, 6, ObisCode.fromString("7.0.96.5.0.255"), "Totalizer units").setParser(STRING));
        getRegisters().add(new InputRegister(9, 2, ObisCode.fromString("7.0.61.0.1.255"), "Mass flow").setParser(FLOAT));
        getRegisters().add(new InputRegister(7, 2, ObisCode.fromString("7.0.1.0.2.255"), "Volume flow").setParser(FLOAT));
        getRegisters().add(new InputRegister(5, 2, ObisCode.fromString("7.0.42.0.1.255"), "Pressure").setParser(FLOAT));
        getRegisters().add(new InputRegister(1, 2, ObisCode.fromString("7.0.41.0.1.255"), "Temperature").setParser(FLOAT));
        getRegisters().add(new InputRegister(29, 2, ObisCode.fromString("7.0.44.0.1.255"), Unit.get(BaseUnit.FEET), "Velocity").setParser(FLOAT));
        getRegisters().add(new InputRegister(15, 2, ObisCode.fromString("7.0.45.0.1.255"), "Density").setParser(FLOAT));
        getRegisters().add(new InputRegister(13, 2, ObisCode.fromString("7.0.0.13.8.255"), "Viscosity").setParser(FLOAT));
        getRegisters().add(new InputRegister(31, 2, ObisCode.fromString("7.0.52.0.1.255"), "Reynolds number").setParser(FLOAT));
        getRegisters().add(new InputRegister(25, 2, ObisCode.fromString("1.0.0.6.2.255"), Unit.get(BaseUnit.HERTZ), "Vortex frequency").setParser(FLOAT));
        getRegisters().add(new InputRegister(4532, 1, ObisCode.fromString("1.0.0.6.3.255"), "Gain").setParser(STRING));
        getRegisters().add(new InputRegister(85, 2, ObisCode.fromString("1.0.0.6.4.255"), "Vortex amplitude").setParser(FLOAT));
        getRegisters().add(new InputRegister(27, 2, ObisCode.fromString("1.0.0.6.5.255"), Unit.get(BaseUnit.HERTZ), "Filter setting").setParser(FLOAT));

        //The following registers are available with the energy meter firmware:
        getRegisters().add(new InputRegister(527, 2, ObisCode.fromString("7.1.96.5.1.255"), "Totalizer #2").setParser(UNSIGNED_LONG));
        getRegisters().add(new InputRegister(2043, 6, ObisCode.fromString("7.1.96.5.0.255"), "Totalizer #2 units").setParser(STRING));
        getRegisters().add(new InputRegister(3, 2, ObisCode.fromString("7.1.41.0.1.255"), "Temperature #2").setParser(FLOAT));
        getRegisters().add(new InputRegister(11, 2, ObisCode.fromString("7.0.31.0.1.255"), "Energy flow").setParser(FLOAT));

        //The following registers contain the display units strings:
        getRegisters().add(new InputRegister(2007, 6, ObisCode.fromString("7.0.1.0.0.255"), "Volume flow units").setParser(STRING));
        getRegisters().add(new InputRegister(2001, 6, ObisCode.fromString("7.0.61.0.0.255"), "Mass flow units").setParser(STRING));
        getRegisters().add(new InputRegister(2025, 6, ObisCode.fromString("7.0.41.0.0.255"), "Temperature units").setParser(STRING));
        getRegisters().add(new InputRegister(2019, 6, ObisCode.fromString("7.0.42.0.0.255"), "Pressure units").setParser(STRING));
        getRegisters().add(new InputRegister(2031, 6, ObisCode.fromString("7.0.45.0.0.255"), "Density units").setParser(STRING));
        getRegisters().add(new InputRegister(2013, 6, ObisCode.fromString("7.0.31.0.0.255"), "Energy flow units").setParser(STRING));

        //Discrete Input Definitions
        getRegisters().add(new InputStatusRegister(1, 1, ObisCode.fromString("0.0.97.98.21.255"), "Alarm #1 state").setParser(BCD_32BIT));
        getRegisters().add(new InputStatusRegister(2, 1, ObisCode.fromString("0.0.97.98.22.255"), "Alarm #2 state").setParser(BCD_32BIT));
        getRegisters().add(new InputStatusRegister(3, 1, ObisCode.fromString("0.0.97.98.23.255"), "Alarm #3 state").setParser(BCD_32BIT));

    }

    @Override
    public ParserFactory getParserFactory() {
        if (parserFactory == null) {
            parserFactory = new ParserFactory();
        }
        return parserFactory;
    }

}
