package com.energyict.protocolimpl.modbus.emco;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.CoilStatusRegister;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.generic.ParserFactory;

/**
 * Created by cisac on 11/5/2015.
 */
public class FP93BRegisterFactory extends AbstractRegisterFactory {
    private final String FLOAT_32BIT = "135";
    private final String BCD_32BIT = "9";

    protected static final String CurrentDateTime = "CurrentDateTime";
    private ParserFactory parserFactory;

    public FP93BRegisterFactory(Modbus modBus) {
        super(modBus);
    }

    @Override
    protected void init() {
        // options
        setZeroBased(((FP93B) getModBus()).isStartRegistersZeroBased()); // this means that reg2read = reg-1

        // registers
        getRegisters().add(new HoldingRegister(1, 2, ObisCode.fromString("6.0.10.0.1.255"), "Heat Flow").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(3, 2, ObisCode.fromString("7.0.61.0.1.255"), "Mass Flow").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(5, 2, ObisCode.fromString("7.0.1.0.1.255"), "STD Volume Flow").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(7, 2, ObisCode.fromString("7.0.1.0.2.255"), "Volume Flow").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(9, 2, ObisCode.fromString("7.0.41.0.1.255"), "Temperature 1").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(11, 2, ObisCode.fromString("7.1.41.0.1.255"), "Temperature 2").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(13, 2, ObisCode.fromString("7.2.41.0.1.255"), "Delta Temperature").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(15, 2, ObisCode.fromString("7.0.42.0.1.255"), "Process Pressure").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(17, 2, ObisCode.fromString("7.0.42.0.2.255"), "Diff. Pressure").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(19, 2, ObisCode.fromString("7.0.45.0.1.255"), "Density").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(21, 2, ObisCode.fromString("5.0.0.4.1.255"), "Specific Enthalpy").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(23, 2, ObisCode.fromString("6.0.10.0.2.255"), "Heat Total").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(25, 2, ObisCode.fromString("7.0.61.0.2.255"), "Mass Total").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(27, 2, ObisCode.fromString("7.0.1.0.3.255"), "STD Volume Total").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(29, 2, ObisCode.fromString("7.0.1.0.4.255"), "Volume Total").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(31, 2, ObisCode.fromString("6.0.10.0.3.255"), "Heat Grand Total").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(33, 2, ObisCode.fromString("7.0.61.0.3.255"), "Mass Grand Total").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(35, 2, ObisCode.fromString("7.0.1.0.5.255"), "STD Volume Grand Total").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(37, 2, ObisCode.fromString("7.0.1.0.6.255"), "Volume Grand Total").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(39, 2, ObisCode.fromString("0.0.97.98.21.255"), "Alarm Point 1").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(41, 2, ObisCode.fromString("0.0.97.98.22.255"), "Alarm Point 2").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(43, 2, ObisCode.fromString("0.0.97.98.23.255"), "Alarm Point 3").setParser(FLOAT_32BIT));
        getRegisters().add(new HoldingRegister(45, 6, CurrentDateTime));
//        getRegisters().add(new HoldingRegister(45, 1, ObisCode.fromString("0.0.1.0.0.255"), "Year").setParser(INTEGER));
//        getRegisters().add(new HoldingRegister(46, 1, ObisCode.fromString("0.0.1.0.1.255"), "Month").setParser(INTEGER));
//        getRegisters().add(new HoldingRegister(47, 1, ObisCode.fromString("0.0.1.0.2.255"), "Day").setParser(INTEGER));
//        getRegisters().add(new HoldingRegister(48, 1, ObisCode.fromString("0.0.1.0.3.255"), "Hours").setParser("3"));
//        getRegisters().add(new HoldingRegister(49, 1, ObisCode.fromString("0.0.1.0.4.255"), "Min").setParser(INTEGER));
//        getRegisters().add(new HoldingRegister(50, 1, ObisCode.fromString("0.0.1.0.5.255"), "Sec").setParser(INTEGER));

        //coils
        getRegisters().add(new CoilStatusRegister(1, 1, ObisCode.fromString("0.0.97.98.1.255"), "System Alarm Power Failure"));
        getRegisters().add(new CoilStatusRegister(2, 1, ObisCode.fromString("0.1.97.98.1.255"), "System Alarm Watchdog").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(3, 1, ObisCode.fromString("0.2.97.98.1.255"), "System Alarm Communication Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(4, 1, ObisCode.fromString("0.3.97.98.1.255"), "System Alarm Calibration Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(5, 1, ObisCode.fromString("0.4.97.98.1.255"), "System Alarm Print Buffer Full").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(6, 1, ObisCode.fromString("0.5.97.98.1.255"), "System Alarm Totalizer Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(7, 1, ObisCode.fromString("0.6.97.98.1.255"), "Sensor/Process Alarm Wet Steam Alarm").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(8, 1, ObisCode.fromString("0.7.97.98.1.255"), "Sensor/Process Alarm Off Fluid Table").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(9, 1, ObisCode.fromString("0.8.97.98.0.255"), "Sensor/Process Alarm Flow In Over Range").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(10, 1, ObisCode.fromString("0.8.97.98.1.255"), "Sensor/Process Alarm Input 1 Over Range").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(11, 1, ObisCode.fromString("0.8.97.98.2.255"), "Sensor/Process Alarm Input 2 Over Range").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(12, 1, ObisCode.fromString("0.9.97.98.0.255"), "Sensor/Process Alarm Flow Loop Broken").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(13, 1, ObisCode.fromString("0.9.97.98.1.255"), "Sensor/Process Alarm Loop 1 Broken").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(14, 1, ObisCode.fromString("0.9.97.98.2.255"), "Sensor/Process Alarm Loop 2 Broken").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(15, 1, ObisCode.fromString("0.10.97.98.0.255"), "Sensor/Process Alarm RTD 1 Open").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(16, 1, ObisCode.fromString("0.10.97.98.1.255"), "Sensor/Process Alarm RTD 1 Short").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(17, 1, ObisCode.fromString("0.10.97.98.2.255"), "Sensor/Process Alarm RTD 2 Open").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(18, 1, ObisCode.fromString("0.10.97.98.3.255"), "Sensor/Process Alarm RTD 2 Short").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(19, 1, ObisCode.fromString("0.11.97.98.0.255"), "Sensor/Process Alarm Pulse Out Overrun").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(20, 1, ObisCode.fromString("0.12.97.98.0.255"), "Sensor/Process Alarm Iout 1 Out Of Range").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(21, 1, ObisCode.fromString("0.12.97.98.1.255"), "Sensor/Process Alarm Iout 2 Out Of Range").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(22, 1, ObisCode.fromString("0.13.97.98.0.255"), "Sensor/Process Alarm Relay 1 Hi Alarm").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(23, 1, ObisCode.fromString("0.13.97.98.1.255"), "Sensor/Process Alarm Relay 1 Lo Alarm").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(24, 1, ObisCode.fromString("0.13.97.98.2.255"), "Sensor/Process Alarm Relay 2 Hi Alarm").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(25, 1, ObisCode.fromString("0.13.97.98.3.255"), "Sensor/Process Alarm Relay 2 Lo Alarm").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(26, 1, ObisCode.fromString("0.13.97.98.4.255"), "Sensor/Process Alarm Relay 3 Hi Alarm").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(27, 1, ObisCode.fromString("0.13.97.98.5.255"), "Sensor/Process Alarm Relay 3 Lo Alarm").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(28, 1, ObisCode.fromString("0.0.97.97.0.255"), "Service Test 24Vdc Out Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(29, 1, ObisCode.fromString("0.0.97.97.4.255"), "Service Test Pulse In Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(30, 1, ObisCode.fromString("0.2.97.97.1.255"), "Service Test Input 1 Vin Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(31, 1, ObisCode.fromString("0.0.97.97.2.255"), "Service Test Input 1 Iin Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(32, 1, ObisCode.fromString("0.1.97.97.2.255"), "Service Test Input 2 Iin Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(33, 1, ObisCode.fromString("0.0.97.97.3.255"), "Service Test Input 2 RTD Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(34, 1, ObisCode.fromString("0.2.97.97.2.255"), "Service Test Input 3 Iin Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(35, 1, ObisCode.fromString("0.1.97.97.3.255"), "Service Test Input 3 RTD Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(36, 1, ObisCode.fromString("0.1.97.97.4.255"), "Service Test Pulse Out Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(37, 1, ObisCode.fromString("0.0.97.97.5.255"), "Service Test Iout 1 Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(38, 1, ObisCode.fromString("0.1.97.97.5.255"), "Service Test Iout 2 Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(39, 1, ObisCode.fromString("0.0.97.97.6.255"), "Service Test Relay 1 Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(40, 1, ObisCode.fromString("0.1.97.97.6.255"), "Service Test Relay 2 Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(41, 1, ObisCode.fromString("0.0.97.97.7.255"), "Service Test RS-232 Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(42, 1, ObisCode.fromString("0.0.97.97.8.255"), "Self Test A/D Malfunction").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(43, 1, ObisCode.fromString("0.0.97.97.9.255"), "Self Test Program Error").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(44, 1, ObisCode.fromString("0.0.97.97.10.255"), "Self Test Setup Data Lost").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(45, 1, ObisCode.fromString("0.0.97.97.11.255"), "Self Test Time Clock Lost").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(46, 1, ObisCode.fromString("0.0.97.97.12.255"), "Self Test Display Malfunction").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(47, 1, ObisCode.fromString("0.0.97.97.13.255"), "Self Test Ram Malfunction").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(48, 1, ObisCode.fromString("0.0.96.2.0.255"), "Language Select").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(49, 1, ObisCode.fromString("0.0.97.98.98.255"), "Reset Totalizers").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(50, 1, ObisCode.fromString("0.0.97.98.99.255"), "Reset All Error Codes").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(51, 1, ObisCode.fromString("0.0.97.98.24.255"), "Reset Alarm 1").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(52, 1, ObisCode.fromString("0.0.97.98.25.255"), "Reset Alarm 2").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(53, 1, ObisCode.fromString("0.0.97.98.26.255"), "Reset Alarm 3").setParser(BCD_32BIT));
        getRegisters().add(new CoilStatusRegister(54, 1, ObisCode.fromString("0.0.96.3.0.255"), "Print Transaction Document").setParser(BCD_32BIT));

    }

    @Override
    public ParserFactory getParserFactory() {
        if (parserFactory == null) {
            parserFactory = new ParserFactory();
        }
        return parserFactory;
    }

    protected void initParsers() {
        // No initialization needed
    }

}
