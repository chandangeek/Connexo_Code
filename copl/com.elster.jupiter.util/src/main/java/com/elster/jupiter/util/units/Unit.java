package com.elster.jupiter.util.units;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

/**
 * An enumeration of all supported units.
 */
public enum Unit {
	//CIM units
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
    DECIBEL_MILLIWATT("decibel milliwatt", "dBm", Dimension.POWER), // TODO convert to SI
    ENCODED_VALUE("encoded value", "Code", Dimension.DIMENSIONLESS),
    WATT_HOUR_PER_ROTATION("watt hours per rotation", "Wh/rev", Dimension.ENERGY, Constants.BD3600),
    VOLT_AMPERE_REACTIVE_HOUR_PER_ROTATION("volt ampere reactive hours per rotation", "VArh/rev", Dimension.REACTIVE_ENERGY, Constants.BD3600),
    VOLT_AMPERE_HOUR_PER_ROTATION("volt ampere hours per rotation", "VAh/rev", Dimension.APPARENT_ENERGY, Constants.BD3600),
    END_DEVICE_EVENT_CODE("end device event code", "MeCode", Dimension.DIMENSIONLESS), //118
    CUBIC_FEET("cubic feet","ft\u00b3","ft3",Dimension.VOLUME,Constants.CUBIC_METER_PER_CUBIC_FOOT),
    CUBIC_FEET_COMPENSATED("cubic feet compensated","ft\u00b3(compensated)","ft3(compensated)",Dimension.VOLUME,Constants.CUBIC_METER_PER_CUBIC_FOOT),
    CUBIC_FEET_UNCOMPENSATED("cubic feet uncompensated","ft\u00b3(uncompensated)","ft3(uncompensated)",Dimension.VOLUME,Constants.CUBIC_METER_PER_CUBIC_FOOT),
    CUBIC_FEET_PER_HOUR("cubic feet per hour","ft\u00b3/h","ft3/h",Dimension.VOLUME_FLOW,Constants.CUBIC_METER_PER_CUBIC_FOOT,Constants.BD3600),
    CUBIC_FEET_COMPENSATED_PER_HOUR("cubic feet compensated per hour","ft\u00b3(compensated)/h","ft3(compensated)/h",Dimension.VOLUME_FLOW,Constants.CUBIC_METER_PER_CUBIC_FOOT,Constants.BD3600),
    CUBIC_FEET_UNCOMPENSATED_PER_HOUR("cubic feet uncompensated per hour","ft\u00b3(uncompensated)/h","ft3(uncompensated)/h",Dimension.VOLUME_FLOW,Constants.CUBIC_METER_PER_CUBIC_FOOT,Constants.BD3600),
    CUBIC_METER_PER_HOUR("cubic meter per hour","m\u00b3/h","m3/h",Dimension.VOLUME_FLOW,ONE,Constants.BD3600),
    CUBIC_METER_COMPENSATED_PER_HOUR("cubic meter compensated per hour","m\u00b3(compensated)/h","m3(compensated)/h",Dimension.VOLUME_FLOW,ONE,Constants.BD3600),
    CUBIC_METER_UNCOMPENSATED_PER_HOUR("cubic meter uncompensated per hour","m\u00b3(uncompensated)/h","m3(uncompensated)/h",Dimension.VOLUME_FLOW,ONE,Constants.BD3600),
    USGALLON("us gallon","USGal",Dimension.VOLUME,Constants.CUBIC_METER_PER_USGALLON),
    USGALLON_PER_HOUR("us gallon per hour","USGal/h",Dimension.VOLUME_FLOW,Constants.CUBIC_METER_PER_USGALLON,Constants.BD3600),
    IMPERIALGALLON("imperial gallon","ImperialGal",Dimension.VOLUME,Constants.CUBIC_METER_PER_IMPERIALGALLON),
    IMPERIALGALLON_PER_HOUR("imperial gallon per hour","ImperarialGal/h",Dimension.VOLUME_FLOW,Constants.CUBIC_METER_PER_IMPERIALGALLON,Constants.BD3600),
    BRITISH_THERMAL_UNIT("british thermal unit","BTU",Dimension.ENERGY,Constants.JOULE_PER_BTU),
    BRITISH_THERMAL_UNIT_PER_HOUR("british thermal unit per hour","BTU/h",Dimension.POWER,Constants.JOULE_PER_BTU,Constants.BD3600),
    LITRE("litre","L",Dimension.VOLUME,ONE,Constants.BD1000),
    LITRE_PER_HOUR("litre per hour", "L/h", Dimension.VOLUME_FLOW, ONE, Constants.BD3600.multiply(Constants.BD1000)),
    LITRE_COMPENSATED_PER_HOUR("litre compensated per hour","L(compensated)/h",Dimension.VOLUME_FLOW, ONE, Constants.BD3600.multiply(Constants.BD1000)),
    LITRE_UNCOMPENSATED_PER_HOUR("litre uncompensated per hour","L(uncompensated)/h",Dimension.VOLUME_FLOW,ONE,Constants.BD3600.multiply(Constants.BD1000)),   
    PASCAL_GAUGE("pascal gauge","PaG",Dimension.PRESSURE),
    POUND_PER_SQUARE_INCH_ABSOLUTE("pounds per square inch, absolute","ps/A",Dimension.PRESSURE,Constants.NEWTON_PER_POUND,Constants.SQUARE_METER_PER_SQUARE_INCH),
    POUND_PER_SQUARE_INCH_GAUGE("pounds per square inch, gauge","ps/G",Dimension.PRESSURE,Constants.NEWTON_PER_POUND,Constants.SQUARE_METER_PER_SQUARE_INCH),
    LITRE_PER_LITRE("liter per liter","L/L",Dimension.DIMENSIONLESS),
    GRAM_PER_GRAM("gram per gram","g/g",Dimension.DIMENSIONLESS),
    MOL_PER_M3("mol per m3","mol/m\u00b3","mol/m3",Dimension.VOLUME_CONCENTRATION),
    MOL_PER_MOL("mol per mol","mol/mol",Dimension.DIMENSIONLESS),
    MOL_PER_KG("mol per kg","mol/kg",Dimension.MASS_CONCENTRATION),
    METER_PER_METER("meter per meter","m/m",Dimension.DIMENSIONLESS),
    SECOND_PER_SECOND("second per second","s/s",Dimension.DIMENSIONLESS),
    HERZ_PER_HERZ("herz per herz","Hz/Hz",Dimension.DIMENSIONLESS),
    VOLT_PER_VOLT("volt per volt","V/V",Dimension.DIMENSIONLESS),
    AMPERE_PER_AMPERE("ampere per ampere","A/A",Dimension.DIMENSIONLESS),
    WATT_PER_VOLTAMPERE("watt per voltampere","W/VA",Dimension.DIMENSIONLESS),
    REVOLUTIONS("revolutions","rev",Dimension.DIMENSIONLESS), //154
    PASCAL_ABSOLUTE("pascal absolute","PaA",Dimension.PRESSURE),
    LITRE_UNCOMPENSATED("litre uncompensated","L(uncompensated)",Dimension.VOLUME,ONE,Constants.BD1000),
    LITRE_COMPENSATED("litre compensated","L(compensated)",Dimension.VOLUME,ONE,Constants.BD1000),
    KATAL("katal","kat",Dimension.CATALYTIC_ACTIVITY),
    MINUTE("minute", "min", Dimension.TIME, BigDecimal.valueOf(60)),
    HOUR("hour", "h", Dimension.TIME, Constants.BD3600),
    QUANTITY_POWER_45("quantity power 45", "Q45",Dimension.DIMENSIONLESS),
    QUANTITY_POWER_60("quantity power 60", "Q60",Dimension.DIMENSIONLESS),
    QUANTITY_ENERGY_45("quantity energy 45", "Q45h",Dimension.DIMENSIONLESS),
    QUANTITY_ENERGY_60("quantity energy 60", "Q60h",Dimension.DIMENSIONLESS),
    JOULES_PER_KG("joule per kg","J/kg",Dimension.SPECIFIC_ENERGY),
    CUBIC_METER_UNCOMPENSATED("cubic meter uncompensated","m\u00b3(uncompensated)","m3(uncompensated)",Dimension.VOLUME),
    CUBIC_METER_COMPENSATED("cubic meter compensated","m\u00b3(compensated)","m3(compensated)",Dimension.VOLUME),
    WATT_PER_WATT("watt per watt","W/W",Dimension.DIMENSIONLESS), // 168
    THERM("therm", "therm", Dimension.ENERGY, Constants.JOULE_PER_THERM),
    // Other units
    YEAR("year", "a", Dimension.TIME, BigDecimal.valueOf(3600L * 24 * 365)),
    MONTH("month", "mo", Dimension.TIME, BigDecimal.valueOf(3600L * 24 * 30)),
    DAY("day", "d", Dimension.TIME, Constants.BD86400),
    NORMAL_CUBIC_METER("normal cubic meter", "Nm\u00b3", "Nm3", Dimension.VOLUME),
    NORMAL_CUBIC_METER_PER_HOUR("normal cubic meter per hour", "Nm\u00b3/h", "Nm3/h", Dimension.VOLUME_FLOW, ONE, Constants.BD3600, ZERO),
    CUBIC_METER_PER_DAY("cubic meter per day", "m\u00b3/d", "m3/d", Dimension.VOLUME_FLOW, ONE, Constants.BD86400, ZERO),
    NORMAL_CUBIC_METER_PER_DAY("normal cubic meter per day", "Nm\u00b3/d", "Nm3/d", Dimension.VOLUME_FLOW, ONE, Constants.BD86400, ZERO),
    PER_HOUR("per hour", "/h", Dimension.FREQUENCY, ONE, Constants.BD3600),
    MOLE_PER_CENT("mole percent hour", "mol%/", Dimension.DIMENSIONLESS, ONE, Constants.BD100),
    PERCENT("percent", "%", Dimension.DIMENSIONLESS, ONE, Constants.BD100),
    JOULE_PER_NORMAL_CUBIC_METER("joule per normal cubic meter", "J/Nm\u00b3", "J/Nm3", Dimension.ENERGY_DENSITY),
    WATT_HOUR_PER_NORMAL_CUBIC_METER("watt hour per normal cubic meter", "Wh/Nm\u00b3", "Wh/Nm3", Dimension.ENERGY_DENSITY, Constants.BD3600),
    TON("ton", "t", Dimension.MASS, Constants.BD1000),
    KILOGRAM_PER_HOUR("kilogram per hour", "kg/h", Dimension.MASSFLOW, ONE, Constants.BD3600),
    TON_PER_HOUR("ton per hour", "t/h", Dimension.MASSFLOW, Constants.BD1000, Constants.BD3600),
   // TODO verify and check UK AND US units from com.energyict.cbo.BaseUnit
    FOOT("foot", "ft", Dimension.LENGTH, Constants.METER_PER_FOOT),
    FOOT_PER_SECOND("foot per second", "ft/s", Dimension.SPEED, Constants.METER_PER_FOOT),
    CUBIC_FOOT_PER_DAY("cubic foot per day", "cf/d", Dimension.VOLUME_FLOW, Constants.CUBIC_METER_PER_CUBIC_FOOT, Constants.BD86400),
    THERM_PER_HOUR("therm per hour", "thm/h", Dimension.POWER, Constants.JOULE_PER_THERM, Constants.BD3600),
    THERM_PER_DAY("therm per day", "thm/d", Dimension.POWER, Constants.JOULE_PER_THERM, Constants.BD86400);
   
    
    
    private static final int EXTRA_PRECISION = 6;

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
        for (Unit unit : values()) {
            if (unit.getSymbol().equals(symbol)) {
                return unit;
            }
        }
        throw new IllegalArgumentException(symbol);
    }

    public static Unit get(String asciiSymbol) {
        for (Unit unit : values()) {
            if (unit.getAsciiSymbol().equals(asciiSymbol)) {
                return unit;
            }
        }
        throw new IllegalArgumentException(asciiSymbol);
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
        BigDecimal CUBIC_METER_PER_USGALLON = BigDecimal.valueOf(378541178L,11);
        BigDecimal CUBIC_METER_PER_IMPERIALGALLON = BigDecimal.valueOf(454609188L,11);
        BigDecimal JOULE_PER_BTU = BigDecimal.valueOf(105505585L,5);
        BigDecimal NEWTON_PER_POUND = BigDecimal.valueOf(4448222L,6);
        BigDecimal METER_PER_INCH = BigDecimal.valueOf(254,4);
        BigDecimal SQUARE_METER_PER_SQUARE_INCH = METER_PER_INCH.multiply(METER_PER_INCH);
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

    /**
     * <a href="http://en.wikipedia.org/wiki/Coherence_(units_of_measurement)>See wikipedia article</a>
     * @return
     */
    public boolean isCoherentSiUnit() {
        return siMultiplier.equals(BigDecimal.ONE) && siDivisor.equals(BigDecimal.ONE) && siDelta.equals(BigDecimal.ZERO) && !isDimensionLess();
    }

    public static Unit getSIUnit(Dimension dimension) {
        for (Unit each : values()) {
            if (each.dimension.equals(dimension) && each.isCoherentSiUnit()) {
                return each;
            }
        }
        throw new IllegalArgumentException(dimension.toString());
    }

    BigDecimal siValue(BigDecimal value) {
        BigDecimal newValue = value.multiply(siMultiplier);
        newValue = newValue.divide(siDivisor, newValue.scale() + siDivisor.precision() + EXTRA_PRECISION, BigDecimal.ROUND_HALF_UP);
        newValue = newValue.add(siDelta);
        return newValue.stripTrailingZeros();
    }

}
