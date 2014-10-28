package com.energyict.protocolimpl.modbus.socomec.countis.e44;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author sva
 * @since 8/10/2014 - 10:00
 */
public class RegisterFactory extends AbstractRegisterFactory {

    public static final String LP_R1_AREA = "LP R1 Area";
    public static final String LP_R2_AREA = "LP R2 Area";
    public static final String CurrentDateTime = "currentDateTime";
    public static final String JbusTableVersion = "JBUS Table Version";
    public static final String ProductSoftwareVersion = "Product software version";

    private static final String AsciiParser = "AsciiParser";
    private static final String Ascii16Parser = "Ascii16Parser";
    private static final String UnsignedValueParser = "UnsignedValueParser";
    private static final String UnsignedValueCheckNotAvailableParser = "UnsignedValueCheckNotAvailableParser";
    private static final String SignedValueParser = "SignedValueParser";
    private static final String SignedValueCheckNotAvailableParser = "SignedValueCheckNotAvailableParser";

    private static final int CENTI_SCALE = -2;
    private static final int MILLI_SCALE = -3;
    private static final int KILO_SCALE = 3;
    private static final int DECA_SCALE = 1;
    private static final int MEGA_SCALE = 6;
    private static final boolean BIG_ENDIAN_ENCODING = true;

    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }

    @Override
    protected void init() {
        setZeroBased(false);

        // Product identification (Start address 0xC350 - size 66)
        getRegisters().add(new HoldingRegister(0xC350, 4, ObisCode.fromString("1.0.96.0.0.255"), Unit.get(BaseUnit.UNITLESS), "SOCO").setParser(Ascii16Parser));
        getRegisters().add(new HoldingRegister(0xC354, 1, ObisCode.fromString("1.0.96.1.0.255"), Unit.get(BaseUnit.UNITLESS), "Product order ID").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0xC355, 1, ObisCode.fromString("1.0.96.2.0.255"), Unit.get(BaseUnit.UNITLESS), "Product ID").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0xC356, 1, ObisCode.fromString("1.0.96.3.0.255"), Unit.get(BaseUnit.UNITLESS), JbusTableVersion).setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0xC357, 1, ObisCode.fromString("1.0.96.4.0.255"), Unit.get(BaseUnit.UNITLESS), ProductSoftwareVersion).setParser(UnsignedValueParser));

        // Meteorology affected by current and voltage transformers (Start address 0xC550 - size 62)
        getRegisters().add(new HoldingRegister(0xC552, 2, ObisCode.fromString("1.0.128.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Phase to Phase Voltage: U12").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC554, 2, ObisCode.fromString("1.0.129.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Phase to Phase Voltage: U23").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC556, 2, ObisCode.fromString("1.0.130.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Phase to Phase Voltage: U31").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC558, 2, ObisCode.fromString("1.0.32.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Simple voltage: V1").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC55A, 2, ObisCode.fromString("1.0.52.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Simple voltage: V2").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC55C, 2, ObisCode.fromString("1.0.72.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Simple voltage: V3").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC560, 2, ObisCode.fromString("1.0.31.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current: I1 ").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC562, 2, ObisCode.fromString("1.0.51.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current: I2").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC564, 2, ObisCode.fromString("1.0.71.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current: I3").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC566, 2, ObisCode.fromString("1.0.91.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Neutral Current: In").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC568, 2, ObisCode.fromString("1.0.1.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC56A, 2, ObisCode.fromString("1.0.3.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC56C, 2, ObisCode.fromString("1.0.9.7.0.255"), Unit.get(BaseUnit.VOLTAMPERE, DECA_SCALE), "Apparent Power").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC56E, 2, ObisCode.fromString("1.0.13.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC570, 2, ObisCode.fromString("1.0.21.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power phase 1").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC572, 2, ObisCode.fromString("1.0.41.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power phase 2").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC574, 2, ObisCode.fromString("1.0.61.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power phase 3").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC576, 2, ObisCode.fromString("1.0.23.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power phase 1").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC578, 2, ObisCode.fromString("1.0.43.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power phase 2").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC57A, 2, ObisCode.fromString("1.0.63.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power phase 3").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC57C, 2, ObisCode.fromString("1.0.29.7.0.255"), Unit.get(BaseUnit.VOLTAMPERE, DECA_SCALE), "Apparent Power phase 1").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC57E, 2, ObisCode.fromString("1.0.49.7.0.255"), Unit.get(BaseUnit.VOLTAMPERE, DECA_SCALE), "Apparent Power phase 2").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC580, 2, ObisCode.fromString("1.0.69.7.0.255"), Unit.get(BaseUnit.VOLTAMPERE, DECA_SCALE), "Apparent Power phase 3").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC582, 2, ObisCode.fromString("1.0.33.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor phase 1").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC584, 2, ObisCode.fromString("1.0.53.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor phase 2").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC586, 2, ObisCode.fromString("1.0.73.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor phase 3").setParser(SignedValueCheckNotAvailableParser));

        // Energies (Start address 0x650 - size 65)
        getRegisters().add(new HoldingRegister(0xC652, 2, ObisCode.fromString("1.0.1.8.0.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Total Positive Active Energy").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC654, 2, ObisCode.fromString("1.0.3.8.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Total Positive Reactive Energy").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC658, 2, ObisCode.fromString("1.0.2.8.0.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Total Negative Active Energy").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC65C, 2, ObisCode.fromString("1.0.1.8.1.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Partial Positive Active Energy").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC65E, 2, ObisCode.fromString("1.0.3.8.1.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Partial Positive Reactive Energy").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC662, 2, ObisCode.fromString("1.0.2.8.1.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Partial Negative Active Energy").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC68A, 2, ObisCode.fromString("1.0.131.5.0.255"), Unit.get(BaseUnit.SECOND), "Last date for Record average P/Q/S in second since 01/01/2000").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC68C, 1, ObisCode.fromString("1.0.1.5.0.255"), Unit.get(BaseUnit.WATT), "Last average (P+) (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));

        // Energies per tariff (Start address 0xC6A0 - size 50)
        getRegisters().add(new HoldingRegister(0xC6A0, 1, ObisCode.fromString("1.0.96.5.0.255"), Unit.get(BaseUnit.UNITLESS), "Number of tariffs supported").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0xC6A1, 1, ObisCode.fromString("1.0.96.6.0.255"), Unit.get(BaseUnit.UNITLESS), "Tariff number in progress").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0xC6A2, 2, ObisCode.fromString("1.0.1.8.1.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Positive Active Energy Tariff 1").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6A4, 2, ObisCode.fromString("1.0.1.8.2.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Positive Active Energy Tariff 2").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6A6, 2, ObisCode.fromString("1.0.1.8.3.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Positive Active Energy Tariff 3").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6A8, 2, ObisCode.fromString("1.0.1.8.4.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Positive Active Energy Tariff 4").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6AA, 2, ObisCode.fromString("1.0.1.8.5.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Positive Active Energy Tariff 5").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6AC, 2, ObisCode.fromString("1.0.1.8.6.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Positive Active Energy Tariff 6").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6AE, 2, ObisCode.fromString("1.0.1.8.7.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Positive Active Energy Tariff 7").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6B0, 2, ObisCode.fromString("1.0.1.8.8.255"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Positive Active Energy Tariff 8").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6B2, 2, ObisCode.fromString("1.0.3.8.1.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Positive Reactive Energy Tariff 1").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6B4, 2, ObisCode.fromString("1.0.3.8.2.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Positive Reactive Energy Tariff 2").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6B6, 2, ObisCode.fromString("1.0.3.8.3.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Positive Reactive Energy Tariff 3").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6B8, 2, ObisCode.fromString("1.0.3.8.4.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Positive Reactive Energy Tariff 4").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6BA, 2, ObisCode.fromString("1.0.3.8.5.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Positive Reactive Energy Tariff 5").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6BC, 2, ObisCode.fromString("1.0.3.8.6.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Positive Reactive Energy Tariff 6").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6BE, 2, ObisCode.fromString("1.0.3.8.7.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Positive Reactive Energy Tariff 7").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC6C0, 2, ObisCode.fromString("1.0.3.8.8.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE), "Positive Reactive Energy Tariff 8").setParser(UnsignedValueCheckNotAvailableParser));

        // Statistics affected by current and voltage transformers (Start address 0xC750 - size 70)
        getRegisters().add(new HoldingRegister(0xC786, 2, ObisCode.fromString("1.0.1.6.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Max/avg P+").setParser(UnsignedValueCheckNotAvailableParser));

        // Meteorology not affected by current and voltage transformers (Start address 0xC850 - size 35)
        getRegisters().add(new HoldingRegister(0xC851, 1, ObisCode.fromString("1.1.128.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Phase to Phase Voltage: U12 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC852, 1, ObisCode.fromString("1.1.129.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Phase to Phase Voltage: U23 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC853, 1, ObisCode.fromString("1.1.130.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Phase to Phase Voltage: U31 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC854, 1, ObisCode.fromString("1.1.32.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Simple voltage: V1 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC855, 1, ObisCode.fromString("1.1.52.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Simple voltage: V2 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC856, 1, ObisCode.fromString("1.1.72.7.0.255"), Unit.get(BaseUnit.VOLT, CENTI_SCALE), "Simple voltage: V3 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC858, 1, ObisCode.fromString("1.1.31.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current: I1 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC859, 1, ObisCode.fromString("1.1.51.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current: I2 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC85A, 1, ObisCode.fromString("1.1.71.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current: I3 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC85B, 1, ObisCode.fromString("1.1.91.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Neutral Current: In (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC85C, 1, ObisCode.fromString("1.1.1.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC85D, 1, ObisCode.fromString("1.1.3.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC85E, 1, ObisCode.fromString("1.1.9.7.0.255"), Unit.get(BaseUnit.VOLTAMPERE, DECA_SCALE), "Apparent Power (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC85F, 1, ObisCode.fromString("1.1.13.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC860, 1, ObisCode.fromString("1.1.21.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power phase 1 (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC861, 1, ObisCode.fromString("1.1.41.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power phase 2 (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC862, 1, ObisCode.fromString("1.1.61.7.0.255"), Unit.get(BaseUnit.WATT, DECA_SCALE), "Active Power phase 3 (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC863, 1, ObisCode.fromString("1.1.23.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power phase 1 (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC864, 1, ObisCode.fromString("1.1.43.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power phase 2 (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC865, 1, ObisCode.fromString("1.1.63.7.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, DECA_SCALE), "Reactive Power phase 3 (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC866, 1, ObisCode.fromString("1.1.29.7.0.255"), Unit.get(BaseUnit.VOLTAMPERE, DECA_SCALE), "Apparent Power phase 1 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC867, 1, ObisCode.fromString("1.1.49.7.0.255"), Unit.get(BaseUnit.VOLTAMPERE, DECA_SCALE), "Apparent Power phase 2 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC868, 1, ObisCode.fromString("1.1.69.7.0.255"), Unit.get(BaseUnit.VOLTAMPERE, DECA_SCALE), "Apparent Power phase 3 (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC869, 1, ObisCode.fromString("1.1.33.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor phase 1 (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC86A, 1, ObisCode.fromString("1.1.53.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor phase 2 (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC86B, 1, ObisCode.fromString("1.1.73.7.0.255"), Unit.get(BaseUnit.UNITLESS, MILLI_SCALE), "Power Factor phase 3 (not affected by CT and VT)").setParser(SignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC86F, 1, ObisCode.fromString("1.1.1.8.0.255"), Unit.get(BaseUnit.WATTHOUR, MEGA_SCALE), "Total Positive Active Energy (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC870, 1, ObisCode.fromString("1.1.3.8.0.255"), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, MEGA_SCALE), "Total Positive Reactive Energy (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xC871, 1, ObisCode.fromString("1.1.2.8.0.255"), Unit.get(BaseUnit.WATTHOUR, MEGA_SCALE), "Total Negative Active Energy (not affected by CT and VT)").setParser(UnsignedValueCheckNotAvailableParser));

        // Network Setting (Start address 0xE000 - size 12)
        getRegisters().add(new HoldingRegister(0xE000, 1, ObisCode.fromString("1.0.96.7.0.255"), Unit.get(BaseUnit.UNITLESS), "Network type").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xE001, 1, ObisCode.fromString("1.0.96.8.0.255"), Unit.get(BaseUnit.UNITLESS), "Current Transformer secondary").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xE002, 1, ObisCode.fromString("1.0.96.9.0.255"), Unit.get(BaseUnit.AMPERE), "Current Transformer primary").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0xE00B, 1, ObisCode.fromString("1.0.96.10.0.255"), Unit.get(BaseUnit.SECOND), "Synchronisation Top for P+/-  Q+/-").setParser(UnsignedValueCheckNotAvailableParser));

        // Hour/Date setting (Start address 0xE100 - size 6)
        getRegisters().add(new HoldingRegister(0xE100, 6, CurrentDateTime));

        // Load curve P+ (Start address 0xF000)
        getRegisters().add(new HoldingRegister(0xF000, 1, LP_R1_AREA));
        getRegisters().add(new HoldingRegister(0xF010, 122, LP_R2_AREA));

        // Table Overflow (Start address 0x9000 - size 11)
        getRegisters().add(new HoldingRegister(0x9000, 2, ObisCode.fromString("1.0.1.53.0.255"), Unit.get(BaseUnit.WATTHOUR, DECA_SCALE), "Value of the meter overflow").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x9002, 1, ObisCode.fromString("1.0.1.128.0.255"), Unit.get(BaseUnit.UNITLESS), "Nb Overflow Total Ea +").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x9003, 1, ObisCode.fromString("1.0.3.128.0.255"), Unit.get(BaseUnit.UNITLESS), "Nb Overflow Total Er +").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x9004, 1, ObisCode.fromString("1.0.2.128.0.255"), Unit.get(BaseUnit.UNITLESS), "Nb Overflow Total Ea -").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x9007, 1, ObisCode.fromString("1.0.1.128.1.255"), Unit.get(BaseUnit.UNITLESS), "Nb Overflow Total Ea T1").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x9008, 1, ObisCode.fromString("1.0.1.128.2.255"), Unit.get(BaseUnit.UNITLESS), "Nb Overflow Total Ea T2").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x9009, 1, ObisCode.fromString("1.0.1.128.3.255"), Unit.get(BaseUnit.UNITLESS), "Nb Overflow Total Ea T3").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x900A, 1, ObisCode.fromString("1.0.1.28.4.255"), Unit.get(BaseUnit.UNITLESS), "Nb Overflow Total Ea T4").setParser(UnsignedValueParser));

        // Temporal Active Index (Start address 0x9020 - size 24)
        getRegisters().add(new HoldingRegister(0x9020, 2, ObisCode.fromString("1.0.1.8.0.0"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea + for day n-1").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0x9022, 2, ObisCode.fromString("1.0.2.8.0.0"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea - for day n-1").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0x9024, 2, ObisCode.fromString("1.0.1.8.0.1"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea + for day n").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0x9026, 2, ObisCode.fromString("1.0.2.8.0.1"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea - for day n").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0x9028, 2, ObisCode.fromString("1.0.1.8.0.2"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea + for Week n-1").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0x902A, 2, ObisCode.fromString("1.0.2.8.0.2"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea - for Week n-1").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0x902C, 2, ObisCode.fromString("1.0.1.8.0.3"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea + for Week n").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0x902E, 2, ObisCode.fromString("1.0.2.8.0.3"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea - for Week n").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0x9030, 2, ObisCode.fromString("1.0.1.8.0.4"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea + for Month n-1").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0x9032, 2, ObisCode.fromString("1.0.2.8.0.4"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea - for Month n-1").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0x9034, 2, ObisCode.fromString("1.0.1.8.0.5"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea + for Month n").setParser(UnsignedValueCheckNotAvailableParser));
        getRegisters().add(new HoldingRegister(0x9036, 2, ObisCode.fromString("1.0.2.8.0.5"), Unit.get(BaseUnit.WATTHOUR, KILO_SCALE), "Ea - for Month n").setParser(UnsignedValueCheckNotAvailableParser));

        // Current without CT (Start address 0x9E80 - size 6)
        getRegisters().add(new HoldingRegister(0x9E80, 2, ObisCode.fromString("1.2.31.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current: I1  (not affected by CT and VT)").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x9E82, 2, ObisCode.fromString("1.2.51.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current: I2 (not affected by CT and VT)").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x9E84, 2, ObisCode.fromString("1.2.71.7.0.255"), Unit.get(BaseUnit.AMPERE, MILLI_SCALE), "Current: I3 (not affected by CT and VT)").setParser(UnsignedValueParser));

        // Indus Mode and MID status (Start address 0x9E60 - size 2)
        getRegisters().add(new HoldingRegister(0x9E60, 1, ObisCode.fromString("1.0.96.11.0.255"), Unit.get(BaseUnit.UNITLESS), "Indus mode").setParser(UnsignedValueParser));
        getRegisters().add(new HoldingRegister(0x9E61, 1, ObisCode.fromString("1.0.96.12.0.255"), Unit.get(BaseUnit.UNITLESS), "MID status").setParser(UnsignedValueParser));

        // COM Board Product ID (Start address 0x9EA0 - size 1
        getRegisters().add(new HoldingRegister(0x9EA0,1, ObisCode.fromString("1.0.96.13.0.255"), Unit.get(BaseUnit.UNITLESS), "Product ID").setParser(UnsignedValueParser));
    }

    @Override
    protected void initParsers() {
        getParserFactory().addParser(UnsignedValueParser, getUnsignedValueParser(true));
        getParserFactory().addParser(UnsignedValueCheckNotAvailableParser, getUnsignedValueCheckNotAvailableParser(true));
        getParserFactory().addParser(SignedValueParser, getSignedValueParser(true));
        getParserFactory().addParser(SignedValueCheckNotAvailableParser, getSignedValueCheckNotAvailableParser(true));
        getParserFactory().addParser(AsciiParser, getAsciiParser(BIG_ENDIAN_ENCODING, false));
        getParserFactory().addParser(Ascii16Parser, getAsciiParser(BIG_ENDIAN_ENCODING, true));
    }

    private Parser getUnsignedValueParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 1 || values.length == 2 || values.length == 4) {
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getUnsignedBigIntegerFromBytes(intBitsArray);
                        return new BigDecimal(bigInteger);
                    } else {
                        throw new ModbusException("ParserFactory, UnsignedValueParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (IOException e) {
                    throw new ModbusException("ParserFactory, UnsignedValueParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getUnsignedValueCheckNotAvailableParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 1 || values.length == 2 || values.length == 4) {
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getUnsignedBigIntegerFromBytes(intBitsArray);
                        if (((values.length == 1) && bigInteger.longValue() == Long.parseLong("FFFF", 16)) ||
                                ((values.length == 2) && bigInteger.longValue() == Long.parseLong("FFFFFFFF", 16))) {
                            bigInteger = new BigInteger("0");   // The value is not available
                        }
                        return new BigDecimal(bigInteger);
                    } else {
                        throw new ModbusException("ParserFactory, UnsignedValueParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (IOException e) {
                    throw new ModbusException("ParserFactory, UnsignedValueParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getSignedValueParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 1 | values.length == 2 | values.length == 4) {           // Signed register
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getSignedBigIntegerFromBytes(intBitsArray);
                        return new BigDecimal(bigInteger);
                    } else {
                        throw new ModbusException("ParserFactory, SignedValueParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (IOException e) {
                    throw new ModbusException("ParserFactory, SignedValueParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getSignedValueCheckNotAvailableParser(final boolean bigEndianEncoding) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) throws ModbusException {
                try {
                    if (values.length == 1 | values.length == 2 | values.length == 4) {           // Signed register
                        byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                        BigInteger bigInteger = ProtocolTools.getSignedBigIntegerFromBytes(intBitsArray);
                        if (((values.length == 1) && bigInteger.longValue() == Long.parseLong("7FFF", 16)) ||
                                ((values.length == 2) && bigInteger.longValue() == Long.parseLong("7FFFFFFF", 16))) {
                            bigInteger = new BigInteger("0");   // The value is not available
                        }
                        return new BigDecimal(bigInteger);
                    } else {
                        throw new ModbusException("ParserFactory, SignedValueParser, received data has invalid length (" + values.length + ")");
                    }
                } catch (IOException e) {
                    throw new ModbusException("ParserFactory, SignedValueParser, failed to parse the value: " + e.getMessage());
                }
            }
        };
    }

    private Parser getAsciiParser(final boolean bigEndianEncoding, final boolean isAscii16) {
        return new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                byte[] intBitsArray = getByteArrayFromValue(values, bigEndianEncoding);
                BigInteger bigInteger = ProtocolTools.getUnsignedBigIntegerFromBytes(intBitsArray);
                if (((values.length == 1) && bigInteger.longValue() == Long.parseLong("FFFF", 16)) ||
                        ((values.length == 2) && bigInteger.longValue() == Long.parseLong("FFFFFFFF", 16))) {
                    return "";   // The value is not available
                }

                String asciiString = "";
                if (bigEndianEncoding) {
                    for (int i = 0; i < values.length; i++) {
                        if (!isAscii16) {
                            asciiString += (char) (values[i] >> 8 & 0xFF);
                        }
                        asciiString += (char) (values[i] & 0xFF);
                    }
                } else {
                    for (int i = (values.length - 1); i >= 0; i--) {
                        if (!isAscii16) {
                            asciiString += (char) (values[i] >> 8 & 0xFF);
                        }
                        asciiString += (char) (values[i] & 0xFF);
                    }
                }
                return asciiString.trim();
            }
        };
    }

    private byte[] getByteArrayFromValue(int[] values, boolean bigEndianEncoding) {
        byte[] byteArray = new byte[values.length * 2];

        if (bigEndianEncoding) {
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                byteArray[index] = (byte) (values[i] >> 8 & 0xFF);
                byteArray[index + 1] = (byte) values[i];
                index += 2;
            }
        } else {
            int index = (values.length * 2) - 2;
            for (int i = 0; i < values.length; i++) {
                byteArray[index] = (byte) (values[i] >> 8 & 0xFF);
                byteArray[index + 1] = (byte) values[i];
                index -= 2;
            }
        }

        return byteArray;
    }
}
