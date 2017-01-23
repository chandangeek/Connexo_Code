package com.energyict.protocolimpl.landisgyr.maxsys2510;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

import java.util.TreeMap;

class UnitOfMeasureCode {

    private static TreeMap codes = new TreeMap();

    private int id;
    private String description;
    private Unit unit;

    private UnitOfMeasureCode() {
    }

    private UnitOfMeasureCode(int id, String description, Unit unit) {
        this.id = id;
        this.description = description;
        this.unit = unit;
        codes.put(new Integer(id), this);
    }

    private static UnitOfMeasureCode cr(int id, String description, Unit unit) {
        return new UnitOfMeasureCode(id, description, unit);
    }

    private static UnitOfMeasureCode cr(int id, String description, int unitId) {
        return new UnitOfMeasureCode(id, description, Unit.get(unitId));
    }

    private static UnitOfMeasureCode cr(int id, String description, String unit) {
        Unit aUnit = null;
        if (unit != null)
            aUnit = Unit.get(unit);
        else
            aUnit = Unit.getUndefined();
        return cr(id, description, aUnit);
    }

    static {
        cr(0, "No unit of measure code defined.", Unit.getUndefined());
        cr(1, "Kilowatt hours", "kWh");
        cr(2, "Kilowatt demand", "kW");
        cr(3, "Kilovar hours", "kvarh");
        cr(4, "Kilovar demand", "kvar");
        cr(5, "Temperature, degrees F", BaseUnit.FAHRENHEIT);
        cr(6, "KQ demand at 60 degrees", Unit.getUndefined());
        cr(7, "Volts squared hours", "V2h");
        cr(8, "KQ hours at 60 degrees", Unit.getUndefined());
        cr(9, "Timing Pulses", Unit.getUndefined());
        cr(10, "Amps squared hours", Unit.getUndefined());
        cr(11, "Volts", "V");
        cr(12, "Amps", "A");
        cr(13, "Temperature, degrees C", "\u00B0C");
        cr(14, "Dew point", Unit.getUndefined());
        cr(15, "Amplitude", Unit.getUndefined());
        cr(16, "Temperature hours, degrees F", Unit.getUndefined());
        cr(17, "Temperature hours, degrees C", Unit.getUndefined());
        cr(18, "kQh at 45 degree phase", Unit.getUndefined());
        cr(20, "Hundred cubic feet", Unit.getUndefined());
        cr(21, "Cubic feet per minute", Unit.getUndefined());
        cr(22, "Cubic feet per hour", Unit.getUndefined());
        cr(23, "Thermal units", Unit.getUndefined());
        cr(24, "Pounds per square inch", Unit.getUndefined());
        cr(60, "Kilowatt demand", "kW");
        cr(61, "Kilowatt demand Channel 1", "kW");
        cr(62, "Kilowatt demand Channel 2", "kW");
        cr(63, "Kilowatt demand Channel 3", "kW");
        cr(64, "Kilowatt demand Channel 4", "kW");
        cr(65, "Kilowatt hours all totalized", "kWh");
        cr(66, "Degree days, Fahrenheit", "\u00B0F");
        cr(67, "Degree days, Celsius", "\u00B0C");
        cr(68, "Kilovar demand", "kvar");
        cr(69, "Kilovolt-amp demand", "kva");
        cr(70, "Sum of kilowatt demand squared", Unit.getUndefined());
        cr(71, "Weighted Variance", Unit.getUndefined());
        cr(72, "Sample Size", Unit.getUndefined());
        cr(82, "Kilovolt-amp hours", "kvah");
        cr(84, "KQ demand at 45 degrees", Unit.getUndefined());
        cr(85, "Power factor", Unit.getUndefined());
        cr(86, "Power factor averaged between demand resets", Unit.getUndefined());
        cr(87, "Thermal KVA, 10 ** -1/tc", Unit.getUndefined());
        cr(88, "Thermal KVA, e ** -1/tc", Unit.getUndefined());
        cr(89, "Thermal KW, 10 ** -1/tc", Unit.getUndefined());
        cr(90, "Thermal KW, e ** -1/tc", Unit.getUndefined());
        cr(91, "Thermal KVAR, 10 ** -1/tc", Unit.getUndefined());
        cr(92, "Thermal KVAR, e ** -1/tc", Unit.getUndefined());
        cr(93, "Thermal VOLTS, 10 ** -1/tc", Unit.getUndefined());
        cr(94, "Thermal VOLTS, e ** -1/tc", Unit.getUndefined());
        cr(95, "Thermal AMPS, 10 ** -1/tc", Unit.getUndefined());
        cr(96, "Thermal AMPS, e ** -1/tc ", Unit.getUndefined());
        cr(97, "Percent ratio (A/B) * 100 ", Unit.getUndefined());
        cr(98, "Hertz-hour ", Unit.getUndefined());
        cr(99, "Hertz ", "Hz");
        cr(100, "V**2h_RT 1-second volts squared hours", "V2");
        cr(101, "A**2h_RT 1-second amperes squared hours", "A2");
        cr(102, "V**2_RT 1-second volts squared", "V2");
        cr(103, "A**2_RT 1-second amperes squared", "A2");
        cr(104, "V_RT 1-second volts", "V");
        cr(105, "A_RT 1-second amperes", "A");
        cr(106, "kWh_RT 1-second kilowatt hours", "kWh");
        cr(107, "kvarh_RT 1-second kilovar hours", "kvarh");
        cr(108, "kVAh_RT 1-second kVA hours", "kVah");
        cr(109, "kW_RT 1-second kilowatts", "kW");
        cr(110, "kvar_RT 1-second kilovars", "kvar");
        cr(111, "kVA_RT 1-second kVA", "kVA");
        cr(112, "SQRT_PERCNT_RATIO:Square root of the ratio for V**2 and A**2", Unit.getUndefined());
        cr(113, "SUM - specifies the summation transform", Unit.getUndefined());
        cr(114, "KVAH_ST   2-second update kVAh transform", Unit.getUndefined());
        cr(115, "KVA_ST  2-second update kVA rate based on KVAH_ST", Unit.getUndefined());
        cr(116, "PWR_FCTR_ST: 2-second update power factor based on KVA_ST", Unit.getUndefined());
        cr(117, "AVG_PWR_FCTR_ST: 2-second update power factor absolute value", Unit.getUndefined());
        cr(118, "PERCNT_RATIO_ST  2-second update percent ratio", Unit.getUndefined());
        cr(119, "SQRT_PERCNT_RATIO_ST 2-second update sqrt percent ratio ", Unit.getUndefined());
        cr(120, "Volt hours (square root of V2H)", Unit.getUndefined());
        cr(121, "Amp hours (square root of I2H)", Unit.getUndefined());
        cr(122, "Difference transforms (A - B)", Unit.getUndefined());
        cr(123, "Product transform (A * B)", Unit.getUndefined());
        cr(124, "Quotient transform (A / B)", Unit.getUndefined());
        cr(125, "Data Gates transform", Unit.getUndefined());
        cr(126, "Square root transform (A ** (1/2))", Unit.getUndefined());
        cr(240, "CUST_EXT_INPUT: specifies that this aux input, and any following, is for customer use", Unit
                .getUndefined());

    }

    static UnitOfMeasureCode get(int id) {
        return (UnitOfMeasureCode) codes.get(new Integer(id));
    }

    String getDescription() {
        return description;
    }

    Unit getUnit() {
        return unit;
    }

    public String toString() {
        return "UnitOfMeasureCode " + id + ", " + description + ", " + unit;
    }

}
