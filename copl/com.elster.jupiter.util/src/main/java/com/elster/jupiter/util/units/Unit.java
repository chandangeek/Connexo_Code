package com.elster.jupiter.util.units;

import java.math.BigDecimal;
import java.util.*;

import static java.math.BigDecimal.*;

/**
 *
 */
public enum Unit {

    UNITLESS("unitless", "", Dimension.DIMENSIONLESS),
    METER("meter", "m", Dimension.LENGTH),
    KILOGRAM("kilogram", "kg", Dimension.MASS),
    GRAM("gram", "g", "g", Dimension.MASS, ONE, Constants.BD1000, ZERO),
    SECOND("second", "s", Dimension.TIME),
    AMPERE("ampere", "A", Dimension.ELECTRIC_CURRENT),
    KELVIN("kelvin", "K", Dimension.TEMPERATURE),
    MOLE("mole", "mol", Dimension.AMOUNT_OF_SUBSTANCE),
    CANDELA("candela", "cd", Dimension.LUMINOUS_INTENSITY),
    ROTATIONS_PER_SECOND("rotations per second", "rev/s", Dimension.FREQUENCY),
    DEGREES("degrees", "\u00b0", "deg", Dimension.DIMENSIONLESS),
    RADIAN("radian", "rad", Dimension.DIMENSIONLESS),
    STERADIAN("steradian", "sr", Dimension.DIMENSIONLESS),
    GRAY("gray", "Gy", Dimension.ABSORBED_DOSE),
    BECQUEREL("becquerel", "Bq", Dimension.RADIOACTIVITY),
    DEGREES_CELSIUS("degrees Celsius", "\u00b0C", "deg C", Dimension.TEMPERATURE, ONE, ONE, BigDecimal.valueOf(27315, 2)),
    SIEVERT("sievert", "Sv", Dimension.DOSE_EQUIVALENT),
    FARAD("farad", "F", Dimension.ELECTRIC_CAPACITANCE),
    COULOMB("coulomb", "C", Dimension.ELECTRIC_CHARGE),
    HENRY("henry", "H", Dimension.ELECTRIC_INDUCTANCE),
    VOLT("volt", "V", Dimension.ELECTRIC_POTENTIAL),
    OHM("ohm", "\u03a9", "ohm", Dimension.ELECTRIC_RESISTANCE),
    JOULE("joule", "J", Dimension.ENERGY),
    NEWTON("newton", "N", Dimension.FORCE),
    HERTZ("hertz", "Hz", Dimension.FREQUENCY),
    LUX("lux", "lx", Dimension.ILLUMINANCE),
    LUMEN("lumen", "Lm", Dimension.LUMINOUS_FLUX),
    WEBER("weber", "Wb", Dimension.MAGNETIC_FLUX),
    TESLA("tesla", "T", Dimension.MAGNETIC_FLUX_DENSITY),
    WATT("watt", "W", Dimension.POWER),
    PASCAL("pascal", "Pa", Dimension.PRESSURE),
    SQUARE_METER("square meter", "m\u00b2", "m2", Dimension.SURFACE),
    CUBIC_METER("cubic meter", "m\u00b3", "m3", Dimension.VOLUME),
    METER_PER_SECOND("meter per second", "m/s", Dimension.SPEED),
    METER_PER_SECOND_SQUARED("meter per second squared", "m/s\u00b2", "m/s2", Dimension.ACCELERATION),
    CUBIC_METER_PER_SECOND("cubic meter per second", "m\u00b3/s", "m3/s", Dimension.VOLUME_FLOW),
    METER_PER_CUBIC_METER("meters per cubic meter", "m/m\u00b3", "m/m3", Dimension.FUEL_EFFICIENCY),
    KILOGRAM_METER("kilogram meter", "M", Dimension.MOMENT_OF_MASS),
    KILOGRAM_PER_CUBIC_METER("kilogram per cubic meter", "kg/m\u00b3", "kg/m3", Dimension.DENSITY),
    METER_SQUARED_PER_SECOND("meter squared per second", "m\u00b2/s", "m2/s", Dimension.VISCOSITY),
    WATT_PER_METER_KELVIN("watt per meter kelvin", "W/mK", Dimension.THERMAL_CONDUCTIVITY),
    JOULE_PER_KELVIN("joule per kelvin", "J/K", Dimension.HEAT_CAPACITY),
    PARTS_PER_MILLION("parts per million", "ppm", Dimension.DIMENSIONLESS),
    SIEMENS("siemens", "S", Dimension.ELECTRICCONDUCTANCE),
    RADIANS_PER_SECOND("radians per second", "rad/s", Dimension.ANGULAR_SPEED),
    VOLT_AMPERE("volt ampere", "VA", Dimension.APPARENT_POWER),
    VOLT_AMPERE_REACTIVE("volt ampere reactive", "VAr", Dimension.REACTIVE_POWER),
    PHASE_ANGLE("phase angle", "\u03b8-Deg", "theta degrees", Dimension.DIMENSIONLESS),
    POWER_FACTOR("power factor", "Cos \u03b8", "cos theta", Dimension.DIMENSIONLESS),
    VOLT_SECONDS("volt seconds", "Vs", Dimension.MAGNETIC_FLUX),
    VOLT_SQUARED("volt squared", "V\u00b2", "V2", Dimension.ELECTRIC_POTENTIAL_SQUARED),
    AMPERE_SECONDS("ampere seconds", "As", Dimension.ELECTRIC_CHARGE),
    AMPERE_SQUARED("ampere squared", "A\u00b2", "A2", Dimension.ELECTRIC_CURRENT_SQUARED),
    AMPERE_SQUARED_SECOND("ampere squared second", "A\u00b2s", "A2s", Dimension.ELECTRIC_CURRENT_SQUARED_TIME),
    VOLT_AMPERE_HOUR("volt ampere hours", "VAh", Dimension.APPARENT_ENERGY, Constants.BD3600),
    WATT_HOUR("watt hours", "Wh", Dimension.ENERGY, Constants.BD3600),
    VOLT_AMPERE_REACTIVE_HOUR("volt ampere reactive hours", "VArh", Dimension.REACTIVE_ENERGY, Constants.BD3600),
    VOLT_PER_HERTZ("volts per hertz", "V/Hz", Dimension.MAGNETIC_FLUX),
    HERTZ_PER_SECOND("hertz per second", "Hz/s", Dimension.FREQUENCY_CHANGE_RATE),
    CHARACTERS("characters", "char", Dimension.DIMENSIONLESS),
    CHARACTERS_PER_SECOND("characters per second", "char/s", Dimension.FREQUENCY),
    KILOGRAM_METER_SQUARED("kilogram meter squared", "kgm\u00b2", "kgm2", Dimension.TURBINE_INERTIA),
    DECIBEL("decibel", "dB", Dimension.DIMENSIONLESS),
    MONEY("money", "\u00A4", "money", Dimension.CURRENCY),
    QUANTITY_POWER("quantity power", "Q", Dimension.DIMENSIONLESS), // TODO dimension
    QUANTITY_ENERGY("quantity energy", "Qh", Dimension.DIMENSIONLESS), // TODO dimension
    OHM_METER("ohm meter", "\u03a9m", "ohmm", Dimension.ELECTRIC_RESISTIVITY),
    AMPERE_PER_METER("ampere per meter", "A/m", Dimension.MAGNETIC_FIELD_STRENGTH),
    VOLT_SQUARED_HOUR("volt squared hour", "V\u00b2h", "V2h", Dimension.ELECTRIC_POTENTIAL_SQUARED_TIME, Constants.BD3600),
    AMPERE_SQUARED_HOUR("ampere squared hour", "A\u00b2h", "A2h", Dimension.ELECTRIC_CURRENT_SQUARED_TIME, Constants.BD3600),
    AMPERE_HOUR("ampere hour", "Ah", Dimension.ELECTRIC_CHARGE, Constants.BD3600),
    WATT_HOUR_PER_CUBIC_METER("watt hour per cubic meter", "Wh/m\u00b3", "Wh/m3", Dimension.ENERGY_DENSITY, Constants.BD3600),
    TIMESTAMP("timestamp", "timeStamp", Dimension.DIMENSIONLESS),
    BOOLEAN("boolean", "status", Dimension.DIMENSIONLESS),
    BOOLEAN_ARRAY("boolean array", "statuses", Dimension.DIMENSIONLESS),
    COUNT("count", "Count", Dimension.DIMENSIONLESS),
    DECIBEL_MILLIWATT("decibel milliwatt", "dBm", Dimension.POWER), // TO DO convert to SI
    ENCODED_VALUE("encoded value", "Code", Dimension.DIMENSIONLESS),
    WATT_HOUR_PER_ROTATION("watt hours per rotation", "Wh/rev", Dimension.ENERGY, Constants.BD3600),
    VOLT_AMPERE_REACTIVE_HOUR_PER_ROTATION("volt ampere reactive hours per rotation", "VArh/rev", Dimension.REACTIVE_ENERGY, Constants.BD3600),
    VOLT_AMPERE_HOUR_PER_ROTATION("volt ampere hours per rotation", "VAh/rev", Dimension.APPARENT_ENERGY, Constants.BD3600),
    END_DEVICE_EVENT_CODE("end device event code", "MeCode", Dimension.DIMENSIONLESS),

    YEAR("year", "a", Dimension.TIME, BigDecimal.valueOf(3600L * 24 * 365)),
    MONTH("month", "mo", Dimension.TIME, BigDecimal.valueOf(3600L * 24 * 30)),
    DAY("day", "d", Dimension.TIME, Constants.BD86400),
    HOUR("hour", "h", Dimension.TIME, Constants.BD3600),
    MINUTE("minute", "min", Dimension.TIME, BigDecimal.valueOf(60)),
    NORMAL_CUBIC_METER("normal cubic meter", "Nm\u00b3", "Nm3", Dimension.VOLUME),
    CUBIC_METER_PER_HOUR("cubic meter per hour", "m\u00b3/h", "m3/h", Dimension.VOLUME_FLOW, ONE, Constants.BD3600, ZERO),
    NORMAL_CUBIC_METER_PER_HOUR("normal cubic meter per hour", "Nm\u00b3/h", "Nm3/h", Dimension.VOLUME_FLOW, ONE, Constants.BD3600, ZERO),
    CUBIC_METER_PER_DAY("cubic meter per day", "m\u00b3/d", "m3/d", Dimension.VOLUME_FLOW, ONE, Constants.BD86400, ZERO),
    NORMAL_CUBIC_METER_PER_DAY("normal cubic meter per day", "Nm\u00b3/d", "Nm3/d", Dimension.VOLUME_FLOW, ONE, Constants.BD86400, ZERO),
    LITER("liter", "l", Dimension.VOLUME, ONE, Constants.BD1000),

    // following units still need to be added to UNITS map

    PER_HOUR("per hour", "/h", Dimension.FREQUENCY, ONE, Constants.BD3600),
    MOLE_PER_CENT("mole percent hour", "mol%/", Dimension.DIMENSIONLESS, ONE, Constants.BD100),
    PERCENT("percent", "%", Dimension.DIMENSIONLESS, ONE, Constants.BD100),
    JOULE_PER_NORMAL_CUBIC_METER("joule per normal cubic meter", "J/Nm\u00b3", "J/Nm3", Dimension.ENERGY_DENSITY),
    WATT_HOUR_PER_NORMAL_CUBIC_METER("watt hour per normal cubic meter", "Wh/Nm\u00b3", "Wh/Nm3", Dimension.ENERGY_DENSITY, Constants.BD3600),
    TON("ton", "t", Dimension.MASS, Constants.BD1000),
    KILOGRAM_PER_HOUR("kilogram per hour", "kg/h", Dimension.MASSFLOW, ONE, Constants.BD3600),
    TON_PER_HOUR("ton per hour", "t/h", Dimension.MASSFLOW, Constants.BD1000, Constants.BD3600),
    LITER_PER_HOUR("liter per hour", "l/h", Dimension.VOLUME_FLOW, ONE, Constants.BD3600.multiply(Constants.BD1000)),
    // TODO verify and check UK AND US units from com.energyict.cbo.BaseUnit
    FOOT("foot", "ft", Dimension.LENGTH, Constants.METER_PER_FOOT),
    FOOT_PER_SECOND("foot per second", "ft/s", Dimension.SPEED, Constants.METER_PER_FOOT),
    CUBIC_FOOT("cubic foot", "cf", Dimension.VOLUME, Constants.CUBIC_METER_PER_CUBIC_FOOT),
    CUBIC_FOOT_PER_HOUR("cubic foot per hour", "cf/h", Dimension.VOLUME_FLOW, Constants.CUBIC_METER_PER_CUBIC_FOOT, Constants.BD3600),
    CUBIC_FOOT_PER_DAY("cubic foot per day", "cf/d", Dimension.VOLUME_FLOW, Constants.CUBIC_METER_PER_CUBIC_FOOT, Constants.BD86400),
    THERM("therm", "thm", Dimension.ENERGY, Constants.JOULE_PER_THERM),
    THERM_PER_HOUR("therm per hour", "thm/h", Dimension.POWER, Constants.JOULE_PER_THERM, Constants.BD3600),
    THERM_PER_DAY("therm per day", "thm/d", Dimension.POWER, Constants.JOULE_PER_THERM, Constants.BD86400);

    private final String name;
    private final String symbol;
    private final String asciiSymbol;
    private final Dimension dimension;
    private final BigDecimal siMultiplier;
    private final BigDecimal siDivisor;
    private final BigDecimal siDelta;

    Unit(String name, String symbol, String asciiSymbol, Dimension dimension, BigDecimal siMultiplier, BigDecimal siDivisor, BigDecimal siDelta) {
        this.name = name;
        this.symbol = symbol;
        this.asciiSymbol = asciiSymbol;
        this.dimension = dimension;
        this.siMultiplier = siMultiplier;
        this.siDivisor = siDivisor;
        this.siDelta = siDelta;
    }

    Unit(String name, String symbol, String asciiSymbol, Dimension dimension, BigDecimal siMultiplier, BigDecimal siDivisor) {
        this(name, symbol, asciiSymbol, dimension, siMultiplier, siDivisor, ZERO);
    }

    Unit(String name, String symbol, String asciiSymbol, Dimension dimension, BigDecimal siMultiplier) {
        this(name, symbol, asciiSymbol, dimension, siMultiplier, ONE);
    }

    Unit(String name, String symbol, Dimension dimension, BigDecimal siMultiplier, BigDecimal siDivisor) {
        this(name, symbol, symbol, dimension, siMultiplier, siDivisor);
    }

    Unit(String name, String symbol, Dimension dimension, BigDecimal siMultiplier) {
        this(name, symbol, symbol, dimension, siMultiplier);
    }

    Unit(String name, String symbol, String asciiSymbol, Dimension dimension) {
        this(name, symbol, asciiSymbol, dimension, ONE);
    }

    Unit(String name, String symbol, Dimension dimension) {
        this(name, symbol, symbol, dimension);
    }

    public static Unit unitForSymbol(String symbol) {
        for (Unit unit : UNITS.values()) {
            if (unit.getSymbol().equals(symbol)) {
                return unit;
            }
        }
        throw new IllegalArgumentException(symbol);
    }

    public static Unit get(String asciiSymbol) {
        Unit unit = UNITS.get(asciiSymbol);
        if (unit == null) {
            throw new IllegalArgumentException(asciiSymbol);
        } else {
            return unit;
        }
    }

    public static Collection<Unit> available() {
        return UNITS.values();
    }

    private interface Constants {
        BigDecimal BD3600 = BigDecimal.valueOf(3600);
        BigDecimal BD86400 = BigDecimal.valueOf(86400);  // seconds per 24h day
        BigDecimal BD100 = BigDecimal.valueOf(100);
        BigDecimal BD1000 = BigDecimal.valueOf(1000);
        BigDecimal METER_PER_FOOT = BigDecimal.valueOf(3048, 4);
        BigDecimal SQUARE_METER_PER_SQUARE_FOOT = METER_PER_FOOT.multiply(METER_PER_FOOT);
        BigDecimal CUBIC_METER_PER_CUBIC_FOOT = SQUARE_METER_PER_SQUARE_FOOT.multiply(METER_PER_FOOT);
        BigDecimal JOULE_PER_THERM = BigDecimal.valueOf(105505585257348L, 6);
    }

    private static final Map<String, Unit> UNITS = fillUnits();

    static Map<String, Unit> fillUnits() {
        Map<String, Unit> units = new HashMap<>();
        units.put(UNITLESS.asciiSymbol, UNITLESS);
        units.put(METER.asciiSymbol, METER);
        units.put(KILOGRAM.asciiSymbol, KILOGRAM);
        units.put(GRAM.asciiSymbol, GRAM);
        units.put(SECOND.asciiSymbol, SECOND);
        units.put(AMPERE.asciiSymbol, AMPERE);
        units.put(KELVIN.asciiSymbol, KELVIN);
        units.put(MOLE.asciiSymbol, MOLE);
        units.put(CANDELA.asciiSymbol, CANDELA);
        units.put(ROTATIONS_PER_SECOND.asciiSymbol, ROTATIONS_PER_SECOND);
        units.put(DEGREES.asciiSymbol, DEGREES);
        units.put(RADIAN.asciiSymbol, RADIAN);
        units.put(STERADIAN.asciiSymbol, STERADIAN);
        units.put(GRAY.asciiSymbol, GRAY);
        units.put(BECQUEREL.asciiSymbol, BECQUEREL);
        units.put(DEGREES_CELSIUS.asciiSymbol, DEGREES_CELSIUS);
        units.put(SIEVERT.asciiSymbol, SIEVERT);
        units.put(FARAD.asciiSymbol, FARAD);
        units.put(COULOMB.asciiSymbol, COULOMB);
        units.put(HENRY.asciiSymbol, HENRY);
        units.put(VOLT.asciiSymbol, VOLT);
        units.put(OHM.asciiSymbol, OHM);
        units.put(JOULE.asciiSymbol, JOULE);
        units.put(NEWTON.asciiSymbol, NEWTON);
        units.put(HERTZ.asciiSymbol, HERTZ);
        units.put(LUX.asciiSymbol, LUX);
        units.put(LUMEN.asciiSymbol, LUMEN);
        units.put(WEBER.asciiSymbol, WEBER);
        units.put(TESLA.asciiSymbol, TESLA);
        units.put(WATT.asciiSymbol, WATT);
        units.put(PASCAL.asciiSymbol, PASCAL);
        units.put(SQUARE_METER.asciiSymbol, SQUARE_METER);
        units.put(CUBIC_METER.asciiSymbol, CUBIC_METER);
        units.put(METER_PER_SECOND.asciiSymbol, METER_PER_SECOND);
        units.put(METER_PER_SECOND_SQUARED.asciiSymbol, METER_PER_SECOND_SQUARED);
        units.put(CUBIC_METER_PER_SECOND.asciiSymbol, CUBIC_METER_PER_SECOND);
        units.put(METER_PER_CUBIC_METER.asciiSymbol, METER_PER_CUBIC_METER);
        units.put(KILOGRAM_METER.asciiSymbol, KILOGRAM_METER);
        units.put(KILOGRAM_PER_CUBIC_METER.asciiSymbol, KILOGRAM_PER_CUBIC_METER);
        units.put(METER_SQUARED_PER_SECOND.asciiSymbol, METER_SQUARED_PER_SECOND);
        units.put(WATT_PER_METER_KELVIN.asciiSymbol, WATT_PER_METER_KELVIN);
        units.put(JOULE_PER_KELVIN.asciiSymbol, JOULE_PER_KELVIN);
        units.put(PARTS_PER_MILLION.asciiSymbol, PARTS_PER_MILLION);
        units.put(SIEMENS.asciiSymbol, SIEMENS);
        units.put(RADIANS_PER_SECOND.asciiSymbol, RADIANS_PER_SECOND);
        units.put(VOLT_AMPERE.asciiSymbol, VOLT_AMPERE);
        units.put(VOLT_AMPERE_REACTIVE.asciiSymbol, VOLT_AMPERE_REACTIVE);
        units.put(PHASE_ANGLE.asciiSymbol, PHASE_ANGLE);
        units.put(POWER_FACTOR.asciiSymbol, POWER_FACTOR);
        units.put(VOLT_SECONDS.asciiSymbol, VOLT_SECONDS);
        units.put(VOLT_SQUARED.asciiSymbol, VOLT_SQUARED);
        units.put(AMPERE_SECONDS.asciiSymbol, AMPERE_SECONDS);
        units.put(AMPERE_SQUARED.asciiSymbol, AMPERE_SQUARED);
        units.put(AMPERE_SQUARED_SECOND.asciiSymbol, AMPERE_SQUARED_SECOND);
        units.put(VOLT_AMPERE_HOUR.asciiSymbol, VOLT_AMPERE_HOUR);
        units.put(WATT_HOUR.asciiSymbol, WATT_HOUR);
        units.put(VOLT_AMPERE_REACTIVE_HOUR.asciiSymbol, VOLT_AMPERE_REACTIVE_HOUR);
        units.put(VOLT_PER_HERTZ.asciiSymbol, VOLT_PER_HERTZ);
        units.put(HERTZ_PER_SECOND.asciiSymbol, HERTZ_PER_SECOND);
        units.put(CHARACTERS.asciiSymbol, CHARACTERS);
        units.put(CHARACTERS_PER_SECOND.asciiSymbol, CHARACTERS_PER_SECOND);
        units.put(KILOGRAM_METER_SQUARED.asciiSymbol, KILOGRAM_METER_SQUARED);
        units.put(DECIBEL.asciiSymbol, DECIBEL);
        units.put(MONEY.asciiSymbol, MONEY);
        units.put(QUANTITY_POWER.asciiSymbol, QUANTITY_POWER);
        units.put(QUANTITY_ENERGY.asciiSymbol, QUANTITY_ENERGY);
        units.put(OHM_METER.asciiSymbol, OHM_METER);
        units.put(AMPERE_PER_METER.asciiSymbol, AMPERE_PER_METER);
        units.put(VOLT_SQUARED_HOUR.asciiSymbol, VOLT_SQUARED_HOUR);
        units.put(AMPERE_SQUARED_HOUR.asciiSymbol, AMPERE_SQUARED_HOUR);
        units.put(AMPERE_HOUR.asciiSymbol, AMPERE_HOUR);
        units.put(WATT_HOUR_PER_CUBIC_METER.asciiSymbol, WATT_HOUR_PER_CUBIC_METER);
        units.put(TIMESTAMP.asciiSymbol, TIMESTAMP);
        units.put(BOOLEAN.asciiSymbol, BOOLEAN);
        units.put(BOOLEAN_ARRAY.asciiSymbol, BOOLEAN_ARRAY);
        units.put(COUNT.asciiSymbol, COUNT);
        units.put(DECIBEL_MILLIWATT.asciiSymbol, DECIBEL_MILLIWATT);
        units.put(ENCODED_VALUE.asciiSymbol, ENCODED_VALUE);
        units.put(WATT_HOUR_PER_ROTATION.asciiSymbol, WATT_HOUR_PER_ROTATION);
        units.put(VOLT_AMPERE_REACTIVE_HOUR_PER_ROTATION.asciiSymbol, VOLT_AMPERE_REACTIVE_HOUR_PER_ROTATION);
        units.put(VOLT_AMPERE_HOUR_PER_ROTATION.asciiSymbol, VOLT_AMPERE_HOUR_PER_ROTATION);
        units.put(END_DEVICE_EVENT_CODE.asciiSymbol, END_DEVICE_EVENT_CODE);
        units.put(YEAR.asciiSymbol, YEAR);
        units.put(MONTH.asciiSymbol, MONTH);
        units.put(DAY.asciiSymbol, DAY);
        units.put(HOUR.asciiSymbol, HOUR);
        units.put(MINUTE.asciiSymbol, MINUTE);
        units.put(NORMAL_CUBIC_METER.asciiSymbol, NORMAL_CUBIC_METER);
        units.put(CUBIC_METER_PER_HOUR.asciiSymbol, CUBIC_METER_PER_HOUR);
        units.put(NORMAL_CUBIC_METER_PER_HOUR.asciiSymbol, NORMAL_CUBIC_METER_PER_HOUR);
        units.put(CUBIC_METER_PER_DAY.asciiSymbol, CUBIC_METER_PER_DAY);
        units.put(NORMAL_CUBIC_METER_PER_DAY.asciiSymbol, NORMAL_CUBIC_METER_PER_DAY);
        units.put(LITER.asciiSymbol, LITER);
        return units;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getAsciiSymbol() {
        return asciiSymbol;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public BigDecimal getSiMultiplier() {
        return siMultiplier;
    }

    public BigDecimal getSiDivisor() {
        return siDivisor;
    }

    public BigDecimal getSiDelta() {
        return siDelta;
    }

    public String getSymbol(boolean asciiOnly) {
        return asciiOnly ? asciiSymbol : symbol;
    }

    public Quantity amount(BigDecimal value) {
        return new Quantity(this, value);
    }

    public Quantity amount(BigDecimal value, int exponent) {
        return new Quantity(this, value, exponent);
    }

    public String toString() {
        return getSymbol();
    }

    public boolean isDimensionLess() {
        return dimension.isDimensionLess();
    }

    public boolean hasSameDimensions(Unit other) {
        return this.dimension.hasSameDimensions(other.dimension);
    }

    public boolean isSICompliant() {
        return
                siMultiplier.equals(BigDecimal.ONE) && siDivisor.equals(BigDecimal.ONE) && siDelta.equals(BigDecimal.ZERO) && !isDimensionLess();
    }

    public static Unit getSIUnit(Dimension dimension) {
        for (Unit each : available()) {
            if (each.dimension.equals(dimension) && each.isSICompliant()) {
                return each;
            }
        }
        throw new IllegalArgumentException("" + dimension);
    }

    BigDecimal siValue(BigDecimal value) {
        BigDecimal newValue = value.multiply(siMultiplier);
        newValue = newValue.divide(siDivisor, newValue.scale() + siDivisor.precision() + 6, BigDecimal.ROUND_HALF_UP);
        newValue = newValue.add(siDelta);
        return newValue.stripTrailingZeros();
    }

}
