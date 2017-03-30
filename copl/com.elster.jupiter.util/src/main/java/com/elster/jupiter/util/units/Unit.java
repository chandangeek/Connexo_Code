/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.elster.jupiter.util.units.Dimension.ABSORBED_DOSE;
import static com.elster.jupiter.util.units.Dimension.ABSORBED_DOSE_RATE;
import static com.elster.jupiter.util.units.Dimension.ACCELERATION;
import static com.elster.jupiter.util.units.Dimension.ACTION;
import static com.elster.jupiter.util.units.Dimension.AMOUNT_OF_SUBSTANCE;
import static com.elster.jupiter.util.units.Dimension.ANGULAR_ACCELERATION;
import static com.elster.jupiter.util.units.Dimension.ANGULAR_SPEED;
import static com.elster.jupiter.util.units.Dimension.APPARENT_ENERGY;
import static com.elster.jupiter.util.units.Dimension.APPARENT_POWER;
import static com.elster.jupiter.util.units.Dimension.CATALYTIC_ACTIVITY;
import static com.elster.jupiter.util.units.Dimension.CATALYTIC_ACTIVITY_CONCENTRATION;
import static com.elster.jupiter.util.units.Dimension.CURRENCY;
import static com.elster.jupiter.util.units.Dimension.DENSITY;
import static com.elster.jupiter.util.units.Dimension.DIMENSIONLESS;
import static com.elster.jupiter.util.units.Dimension.DOSE_EQUIVALENT;
import static com.elster.jupiter.util.units.Dimension.DYNAMIC_VISCOSITY;
import static com.elster.jupiter.util.units.Dimension.ELECTRICCONDUCTANCE;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_CAPACITANCE;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_CHARGE;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_CHARGE_DENSITY;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_CHARGE_SURFACE_DENSITY;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_CURRENT;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_CURRENT_SQUARED;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_CURRENT_SQUARED_TIME;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_FIELD_STRENGTH;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_INDUCTANCE;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_POTENTIAL;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_POTENTIAL_SQUARED;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_POTENTIAL_SQUARED_TIME;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_RESISTANCE;
import static com.elster.jupiter.util.units.Dimension.ELECTRIC_RESISTIVITY;
import static com.elster.jupiter.util.units.Dimension.ENERGY;
import static com.elster.jupiter.util.units.Dimension.ENERGY_DENSITY;
import static com.elster.jupiter.util.units.Dimension.EXPOSURE;
import static com.elster.jupiter.util.units.Dimension.FORCE;
import static com.elster.jupiter.util.units.Dimension.FREQUENCY;
import static com.elster.jupiter.util.units.Dimension.FREQUENCY_CHANGE_RATE;
import static com.elster.jupiter.util.units.Dimension.FUEL_ECONOMY;
import static com.elster.jupiter.util.units.Dimension.FUEL_EFFICIENCY;
import static com.elster.jupiter.util.units.Dimension.HEAT_CAPACITY;
import static com.elster.jupiter.util.units.Dimension.ILLUMINANCE;
import static com.elster.jupiter.util.units.Dimension.LENGTH;
import static com.elster.jupiter.util.units.Dimension.LUMINOUS_FLUX;
import static com.elster.jupiter.util.units.Dimension.LUMINOUS_INTENSITY;
import static com.elster.jupiter.util.units.Dimension.MAGNETIC_FIELD_STRENGTH;
import static com.elster.jupiter.util.units.Dimension.MAGNETIC_FLUX;
import static com.elster.jupiter.util.units.Dimension.MAGNETIC_FLUX_DENSITY;
import static com.elster.jupiter.util.units.Dimension.MASS;
import static com.elster.jupiter.util.units.Dimension.MASSFLOW;
import static com.elster.jupiter.util.units.Dimension.MASS_CONCENTRATION;
import static com.elster.jupiter.util.units.Dimension.MOLAR_ENERGY;
import static com.elster.jupiter.util.units.Dimension.MOLAR_ENTROPY;
import static com.elster.jupiter.util.units.Dimension.MOMENT_OF_FORCE;
import static com.elster.jupiter.util.units.Dimension.MOMENT_OF_MASS;
import static com.elster.jupiter.util.units.Dimension.PERMEABILITY;
import static com.elster.jupiter.util.units.Dimension.PERMITTIVITY;
import static com.elster.jupiter.util.units.Dimension.POWER;
import static com.elster.jupiter.util.units.Dimension.POWER_RAMP;
import static com.elster.jupiter.util.units.Dimension.PRESSURE;
import static com.elster.jupiter.util.units.Dimension.RADIANCE;
import static com.elster.jupiter.util.units.Dimension.RADIOACTIVITY;
import static com.elster.jupiter.util.units.Dimension.REACTIVE_ENERGY;
import static com.elster.jupiter.util.units.Dimension.REACTIVE_POWER;
import static com.elster.jupiter.util.units.Dimension.RECIPROCAL_LENGTH;
import static com.elster.jupiter.util.units.Dimension.SPECIFIC_ENERGY;
import static com.elster.jupiter.util.units.Dimension.SPECIFIC_HEAT_CAPACITY;
import static com.elster.jupiter.util.units.Dimension.SPECIFIC_VOLUME;
import static com.elster.jupiter.util.units.Dimension.SPEED;
import static com.elster.jupiter.util.units.Dimension.SURFACE;
import static com.elster.jupiter.util.units.Dimension.SURFACE_TENSION;
import static com.elster.jupiter.util.units.Dimension.TEMPERATURE;
import static com.elster.jupiter.util.units.Dimension.THERMAL_CONDUCTIVITY;
import static com.elster.jupiter.util.units.Dimension.TIME;
import static com.elster.jupiter.util.units.Dimension.TURBINE_INERTIA;
import static com.elster.jupiter.util.units.Dimension.VISCOSITY;
import static com.elster.jupiter.util.units.Dimension.VOLUME;
import static com.elster.jupiter.util.units.Dimension.VOLUME_CONCENTRATION;
import static com.elster.jupiter.util.units.Dimension.VOLUME_FLOW;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

/**
 * An enumeration of all supported units.
 */
public enum Unit {
	//CIM units
    UNITLESS("unitless", "", DIMENSIONLESS),
    METER("meter", "m", LENGTH),
    KILOGRAM("kilogram", "kg", MASS),
    GRAM("gram", "g", MASS, -3),
    SECOND("second", "s", TIME),
    AMPERE("ampere", "A", ELECTRIC_CURRENT),
    KELVIN("kelvin", "K", TEMPERATURE),
    MOLE("mole", "mol", AMOUNT_OF_SUBSTANCE),
    CANDELA("candela", "cd", LUMINOUS_INTENSITY),
    ROTATIONS_PER_SECOND("rotations per second", "rev/s", FREQUENCY),
    DEGREES("degrees", "\u00b0", "deg", DIMENSIONLESS),
    RADIAN("radian", "rad", DIMENSIONLESS),
    STERADIAN("steradian", "sr", DIMENSIONLESS),
    GRAY("gray", "Gy", ABSORBED_DOSE),
    BECQUEREL("becquerel", "Bq", RADIOACTIVITY),
    DEGREES_CELSIUS("degrees Celsius", "\u00b0C", "deg C", TEMPERATURE, ONE, ONE, BigDecimal.valueOf(27315, 2)),
    SIEVERT("sievert", "Sv", DOSE_EQUIVALENT),
    FARAD("farad", "F", ELECTRIC_CAPACITANCE),
    COULOMB("coulomb", "C", ELECTRIC_CHARGE),
    HENRY("henry", "H", ELECTRIC_INDUCTANCE),
    VOLT("volt", "V", ELECTRIC_POTENTIAL),
    OHM("ohm", "\u03a9", "ohm", ELECTRIC_RESISTANCE),
    JOULE("joule", "J", ENERGY),
    NEWTON("newton", "N", FORCE),
    HERTZ("hertz", "Hz", FREQUENCY),
    LUX("lux", "lx", ILLUMINANCE),
    LUMEN("lumen", "Lm", LUMINOUS_FLUX),
    WEBER("weber", "Wb", MAGNETIC_FLUX),
    TESLA("tesla", "T", MAGNETIC_FLUX_DENSITY),
    WATT("watt", "W", POWER),
    PASCAL("pascal", "Pa", PRESSURE),
    SQUARE_METER("square meter", "m\u00b2", "m2", SURFACE),
    CUBIC_METER("cubic meter", "m\u00b3", "m3", VOLUME),
    METER_PER_SECOND("meter per second", "m/s", SPEED),
    METER_PER_SECOND_SQUARED("meter per second squared", "m/s\u00b2", "m/s2", ACCELERATION),
    CUBIC_METER_PER_SECOND("cubic meter per second", "m\u00b3/s", "m3/s", VOLUME_FLOW),
    METER_PER_CUBIC_METER("meters per cubic meter", "m/m\u00b3", "m/m3", FUEL_EFFICIENCY),
    GRAM_METER("gram meter", "gm", MOMENT_OF_MASS,-3),
    GRAM_PER_CUBIC_METER("gram per cubic meter", "g/m\u00b3", "g/m3", DENSITY,-3),
    METER_SQUARED_PER_SECOND("meter squared per second", "m\u00b2/s", "m2/s", VISCOSITY),
    WATT_PER_METER_KELVIN("watt per meter kelvin", "W/(mK)", THERMAL_CONDUCTIVITY),
    JOULE_PER_KELVIN("joule per kelvin", "J/K", HEAT_CAPACITY),
    SIEMENS("siemens", "S", ELECTRICCONDUCTANCE),
    RADIANS_PER_SECOND("radians per second", "rad/s", ANGULAR_SPEED),
    VOLT_AMPERE("volt ampere", "VA", APPARENT_POWER),
    VOLT_AMPERE_REACTIVE("volt ampere reactive", "VAr", REACTIVE_POWER),
    PHASE_ANGLE("phase angle", "\u03b8-Deg", "theta degrees", DIMENSIONLESS),
    POWER_FACTOR("power factor", "Cos \u03b8", "cos theta", DIMENSIONLESS),
    VOLT_SECONDS("volt seconds", "Vs", MAGNETIC_FLUX),
    VOLT_SQUARED("volt squared", "V\u00b2", "V2", ELECTRIC_POTENTIAL_SQUARED),
    AMPERE_SECONDS("ampere seconds", "As", ELECTRIC_CHARGE),
    AMPERE_SQUARED("ampere squared", "A\u00b2", "A2", ELECTRIC_CURRENT_SQUARED),
    AMPERE_SQUARED_SECOND("ampere squared second", "A\u00b2s", "A2s", ELECTRIC_CURRENT_SQUARED_TIME),
    VOLT_AMPERE_HOUR("volt ampere hours", "VAh", APPARENT_ENERGY, Constants.BD3600),
    WATT_HOUR("watt hours", "Wh", ENERGY, Constants.BD3600),
    VOLT_AMPERE_REACTIVE_HOUR("volt ampere reactive hours", "VArh", REACTIVE_ENERGY, Constants.BD3600),
    VOLT_PER_HERTZ("volts per hertz", "V/Hz", MAGNETIC_FLUX),
    HERTZ_PER_SECOND("hertz per second", "Hz/s", FREQUENCY_CHANGE_RATE),
    CHARACTERS("characters", "char", DIMENSIONLESS),
    CHARACTERS_PER_SECOND("characters per second", "char/s", FREQUENCY),
    GRAM_METER_SQUARED("gram meter squared", "gm\u00b2", "gm2", TURBINE_INERTIA,-3),
    BEL("bel", "B", DIMENSIONLESS),
    MONEY("money", "\u00A4", "money", CURRENCY),
    WATT_PER_SECOND("watt per second","W/s",POWER_RAMP),
    LITRE_PER_SECOND("littre per second","L/s", VOLUME_FLOW, -3),
    QUANTITY_POWER("quantity power", "Q", DIMENSIONLESS), // TODO dimension
    QUANTITY_ENERGY("quantity energy", "Qh", DIMENSIONLESS), // TODO dimension
    OHM_METER("ohm meter", "\u03a9m", "ohmm", ELECTRIC_RESISTIVITY),
    AMPERE_PER_METER("ampere per meter", "A/m", MAGNETIC_FIELD_STRENGTH),
    VOLT_SQUARED_HOUR("volt squared hour", "V\u00b2h", "V2h", ELECTRIC_POTENTIAL_SQUARED_TIME, Constants.BD3600),
    AMPERE_SQUARED_HOUR("ampere squared hour", "A\u00b2h", "A2h", ELECTRIC_CURRENT_SQUARED_TIME, Constants.BD3600),
    AMPERE_HOUR("ampere hour", "Ah", ELECTRIC_CHARGE, Constants.BD3600),
    WATT_HOUR_PER_CUBIC_METER("watt hour per cubic meter", "Wh/m\u00b3", "Wh/m3", ENERGY_DENSITY, Constants.BD3600),
    TIMESTAMP("timestamp", "timeStamp", DIMENSIONLESS),
    BOOLEAN("boolean", "status", DIMENSIONLESS),
    BOOLEAN_ARRAY("boolean array", "statuses", DIMENSIONLESS),
    COUNT("count", "Count", DIMENSIONLESS),
    BEL_MILLIWATT("bel milliwatt", "Bm", POWER,ONE,ZERO) {
    	// set siDivisor to ZERO to indicate si conversion overrule
    	@Override
    	Quantity siValue(BigDecimal value) {
    		return Unit.WATT.amount(BigDecimal.valueOf(Math.pow(10.0,value.doubleValue()-3.0)));
    	}
    }, // TODO convert to SI
    ENCODED_VALUE("encoded value", "Code", DIMENSIONLESS),
    WATT_HOUR_PER_ROTATION("watt hours per rotation", "Wh/rev", ENERGY, Constants.BD3600),
    VOLT_AMPERE_REACTIVE_HOUR_PER_ROTATION("volt ampere reactive hours per rotation", "VArh/rev", REACTIVE_ENERGY, Constants.BD3600),
    VOLT_AMPERE_HOUR_PER_ROTATION("volt ampere hours per rotation", "VAh/rev", APPARENT_ENERGY, Constants.BD3600),
    END_DEVICE_EVENT_CODE("end device event code", "MeCode", DIMENSIONLESS),
    CUBIC_FEET("cubic feet","ft\u00b3","ft3",VOLUME,Constants.CUBIC_METER_PER_CUBIC_FOOT),
    CUBIC_FEET_COMPENSATED("cubic feet compensated","ft\u00b3(compensated)","ft3(compensated)",VOLUME,Constants.CUBIC_METER_PER_CUBIC_FOOT),
    CUBIC_FEET_UNCOMPENSATED("cubic feet uncompensated","ft\u00b3(uncompensated)","ft3(uncompensated)",VOLUME,Constants.CUBIC_METER_PER_CUBIC_FOOT),
    CUBIC_FEET_PER_HOUR("cubic feet per hour","ft\u00b3/h","ft3/h",VOLUME_FLOW,Constants.CUBIC_METER_PER_CUBIC_FOOT,Constants.BD3600),
    CUBIC_FEET_COMPENSATED_PER_HOUR("cubic feet compensated per hour","ft\u00b3(compensated)/h","ft3(compensated)/h",VOLUME_FLOW,Constants.CUBIC_METER_PER_CUBIC_FOOT,Constants.BD3600),
    CUBIC_FEET_UNCOMPENSATED_PER_HOUR("cubic feet uncompensated per hour","ft\u00b3(uncompensated)/h","ft3(uncompensated)/h",VOLUME_FLOW,Constants.CUBIC_METER_PER_CUBIC_FOOT,Constants.BD3600),
    CUBIC_METER_PER_HOUR("cubic meter per hour","m\u00b3/h","m3/h",VOLUME_FLOW,ONE,Constants.BD3600),
    CUBIC_METER_COMPENSATED_PER_HOUR("cubic meter compensated per hour","m\u00b3(compensated)/h","m3(compensated)/h",VOLUME_FLOW,ONE,Constants.BD3600),
    CUBIC_METER_UNCOMPENSATED_PER_HOUR("cubic meter uncompensated per hour","m\u00b3(uncompensated)/h","m3(uncompensated)/h",VOLUME_FLOW,ONE,Constants.BD3600),
    USGALLON("us gallon","USGal",VOLUME,Constants.CUBIC_METER_PER_USGALLON),
    USGALLON_PER_HOUR("us gallon per hour","USGal/h",VOLUME_FLOW,Constants.CUBIC_METER_PER_USGALLON,Constants.BD3600),
    IMPERIALGALLON("imperial gallon","ImperialGal",VOLUME,Constants.CUBIC_METER_PER_IMPERIALGALLON),
    IMPERIALGALLON_PER_HOUR("imperial gallon per hour","ImperialGal/h",VOLUME_FLOW,Constants.CUBIC_METER_PER_IMPERIALGALLON,Constants.BD3600),
    BRITISH_THERMAL_UNIT("british thermal unit","BTU",ENERGY,Constants.JOULE_PER_BTU),
    BRITISH_THERMAL_UNIT_PER_HOUR("british thermal unit per hour","BTU/h",POWER,Constants.JOULE_PER_BTU,Constants.BD3600),
    LITRE("litre","L",VOLUME,-3),
    LITRE_PER_HOUR("litre per hour", "L/h", VOLUME_FLOW, ONE, Constants.BD3600.scaleByPowerOfTen(3)),
    LITRE_COMPENSATED_PER_HOUR("litre compensated per hour","L(compensated)/h",VOLUME_FLOW, ONE, Constants.BD3600.scaleByPowerOfTen(3)),
    LITRE_UNCOMPENSATED_PER_HOUR("litre uncompensated per hour","L(uncompensated)/h",VOLUME_FLOW,ONE,Constants.BD3600.scaleByPowerOfTen(3)),
    PASCAL_GAUGE("pascal gauge","PaG",PRESSURE),
    POUND_PER_SQUARE_INCH_ABSOLUTE("pounds per square inch, absolute","ps/A",PRESSURE,Constants.NEWTON_PER_POUND,Constants.SQUARE_METER_PER_SQUARE_INCH),
    POUND_PER_SQUARE_INCH_GAUGE("pounds per square inch, gauge","ps/G",PRESSURE,Constants.NEWTON_PER_POUND,Constants.SQUARE_METER_PER_SQUARE_INCH),
    LITRE_PER_LITRE("liter per liter","L/L",DIMENSIONLESS),
    GRAM_PER_GRAM("gram per gram","g/g",DIMENSIONLESS),
    MOL_PER_M3("mol per m3","mol/m\u00b3","mol/m3",VOLUME_CONCENTRATION),
    MOL_PER_MOL("mol per mol","mol/mol",DIMENSIONLESS),
    MOL_PER_KG("mol per kg","mol/kg",MASS_CONCENTRATION),
    METER_PER_METER("meter per meter","m/m",DIMENSIONLESS),
    SECOND_PER_SECOND("second per second","s/s",DIMENSIONLESS),
    HERZ_PER_HERZ("herz per herz","Hz/Hz",DIMENSIONLESS),
    VOLT_PER_VOLT("volt per volt","V/V",DIMENSIONLESS),
    AMPERE_PER_AMPERE("ampere per ampere","A/A",DIMENSIONLESS),
    WATT_PER_VOLTAMPERE("watt per voltampere","W/VA",DIMENSIONLESS),
    REVOLUTIONS("revolutions","rev",DIMENSIONLESS),
    PASCAL_ABSOLUTE("pascal absolute","PaA",PRESSURE),
    LITRE_UNCOMPENSATED("litre uncompensated","L(uncompensated)",VOLUME,-3),
    LITRE_COMPENSATED("litre compensated","L(compensated)",VOLUME,-3),
    KATAL("katal","kat",CATALYTIC_ACTIVITY),
    MINUTE("minute", "min", TIME, BigDecimal.valueOf(60)),
    HOUR("hour", "h", TIME, Constants.BD3600),
    QUANTITY_POWER_45("quantity power 45", "Q45",DIMENSIONLESS),
    QUANTITY_POWER_60("quantity power 60", "Q60",DIMENSIONLESS),
    QUANTITY_ENERGY_45("quantity energy 45", "Q45h",DIMENSIONLESS),
    QUANTITY_ENERGY_60("quantity energy 60", "Q60h",DIMENSIONLESS),
    JOULES_PER_KG("joule per kg","J/kg",SPECIFIC_ENERGY),
    CUBIC_METER_UNCOMPENSATED("cubic meter uncompensated","m\u00b3(uncompensated)","m3(uncompensated)",VOLUME),
    CUBIC_METER_COMPENSATED("cubic meter compensated","m\u00b3(compensated)","m3(compensated)",VOLUME),
    WATT_PER_WATT("watt per watt","W/W",DIMENSIONLESS),
    THERM("therm", "therm", ENERGY, Constants.JOULE_PER_THERM),
    WAVENUMBER("wave number","/m",RECIPROCAL_LENGTH),
    CUBIC_METER_PER_KILOGRAM("cubic meter per kilogram","m\u00b3/kg","m3/kg",SPECIFIC_VOLUME),
    PASCAL_SECOND("pascal second","Pas",DYNAMIC_VISCOSITY),
    NEWTON_METER("newton meter","Nm",MOMENT_OF_FORCE),
    NEWTON_PER_METER("newton per meter","N/m",SURFACE_TENSION),
    RADIANS_PER_SECOND_SQUARED("radians per second squared","rad/s\u00b2","rad/s2",ANGULAR_ACCELERATION),
    WATT_PER_SQUARED_METER("watt per squared meter","W/m\u00b2","W/m2",RADIANCE),
    JOULE_PER_KILOGRAM_KELVIN("Joule per kilogram kelvin","J/(kgK)",SPECIFIC_HEAT_CAPACITY),
    JOULE_PER_CUBIC_METER("Joule per cubic meter","J/m\u00b3","J/m3",ENERGY_DENSITY),
    VOLT_PER_METER("Volt per meter","V/m",ELECTRIC_FIELD_STRENGTH),
    COULOMB_PER_CUBIC_METER("Coulomb per cubic meter","C/m\u00b3","C/m3",ELECTRIC_CHARGE_DENSITY),
    COULOMB_PER_SQUARE_METER("Coulomb per square meter","C/m\u00b2","C/m2",ELECTRIC_CHARGE_SURFACE_DENSITY),
    FARAD_PER_METER("Farad per meter","F/m",PERMITTIVITY),
    HENRY_PER_METER("Henry per meter","H/m",PERMEABILITY),
    JOULE_PER_MOLE("Joule per mole","J/mol",MOLAR_ENERGY),
    JOULE_PER_MOLE_KELVIN("Joule per mole kelvin","J/(molK)",MOLAR_ENTROPY),
    COULOMB_PER_KILOGRAM("Coulomb per kilogram","C/kg",EXPOSURE),
    GRAY_PER_SECOND("Gray per second","Gy/s",ABSORBED_DOSE_RATE),
    WATT_PER_STERADIAN("Watt per steradian","W/sr",POWER),
    WATT_PER_SQUARE_METER_STERADIAN("Watt per square meter steradian","W/(m\u00b2sr)","W/(m2sr)",RADIANCE),
    KATAL_PER_CUBIC_METER("Katal per cubic meter","kat/m\u00b3","kat/m3",CATALYTIC_ACTIVITY_CONCENTRATION),
    VOLT_HOUR("Volt hour","Vh",MAGNETIC_FLUX,ONE,Constants.BD3600),
    ANGLEMINUTE("Angle minute","'",DIMENSIONLESS),
    ANGLESECOND("Angle second","\"",DIMENSIONLESS),
    HECTARE("hectare","ha",SURFACE,4),
    NEPER("Neper","Np",DIMENSIONLESS),
    ELECTRON_VOLT("electronvolt","eV",ENERGY,BigDecimal.valueOf(1602176L, 25)),
    DALTON("dalton","Da",MASS,BigDecimal.valueOf(1660538L,33)),
    UNIFIED_MASS("unified mass","u",MASS,BigDecimal.valueOf(1660538L,33)),
    ASTRONOMICAL_UNIT("astronomical unit","ua",LENGTH,BigDecimal.valueOf(1495978L,-5)),
    LIGHT_SPEED("light speed","c\u2080","c0",SPEED,BigDecimal.valueOf(299192458L)),
    REDUCED_PLANCK("atomic unit of action","\u210F","(h-)",ACTION,BigDecimal.valueOf(1054571L,40)),
    ELECTRON_MASS("electron mass","m\u2091","me",MASS,BigDecimal.valueOf(9109382,37)),
    NATURAL_UNIT_TIME("natural unit of time","\u210F/(m\u2091c\u2080\u00b2)","(h-)/(mec02)",TIME,BigDecimal.valueOf(1288087L,27)),
    ELECTRON_CHARGE("electron charge","e",ELECTRIC_CHARGE,BigDecimal.valueOf(1602176L,25)),
    BOHR_RADIUS("bohr radius","a\u2080","a0",LENGTH,BigDecimal.valueOf(529177L,16)),
    HARTREE("hartree","Eh","Eh",ENERGY,BigDecimal.valueOf(4359744,24)),
    ATOMIC_TIME("atomic unit of time","\u210F/Eh","(h-)/Eh",TIME,BigDecimal.valueOf(2418883L,23)),
    BAR("bar","bar",PRESSURE,5),
    MM_MERCURY("millimeter of mercury","mmHg",PRESSURE,BigDecimal.valueOf(1333L,1)),
    ANGSTROM("angstrom","\u212B","Angstrom",LENGTH,-10),
    NAUTICAL_MILE("nautical mile","M",LENGTH,BigDecimal.valueOf(1852L)),
    BARN("barn","b",SURFACE,-28),
    KNOT("knot","kn",SPEED,BigDecimal.valueOf(1852L),Constants.BD3600),
    CURIE("curie","Ci",RADIOACTIVITY,BigDecimal.valueOf(37L,11)),
    ROENTGEN("roentgen","R",EXPOSURE,BigDecimal.valueOf(258,6)),
    RAD("rad","rd",ABSORBED_DOSE,-2),
    REM("rem","rem",DOSE_EQUIVALENT,-2),
    INCH("inch","in",LENGTH,Constants.METER_PER_INCH),
    FOOT("foot","ft",LENGTH,Constants.METER_PER_FOOT),
    ROD("rod","rod",LENGTH,BigDecimal.valueOf(50292L,4)),
    FURLONG("furlong","fur",LENGTH,BigDecimal.valueOf(201168L,3)),
    MILE("mile","mi",LENGTH,Constants.METER_PER_MILE),
    SQUARE_FOOT("square foot","ft\u00B2","ft2",SURFACE,Constants.SQUARE_METER_PER_SQUARE_FOOT),
    SQUARE_YARD("square yard","yd\u00B2","yd2",SURFACE,BigDecimal.valueOf(83612736L,8)),
    SQUARE_ROD("square rod","rod\u00B2","rod2",SURFACE,BigDecimal.valueOf(2529285264L,8)),
    ACRE("acre","acre",SURFACE,BigDecimal.valueOf(40468564224L,7)),
    SQUARE_MILE("square mile","mi\u00B2","mi2",SURFACE,BigDecimal.valueOf(2589988110336L,6)),
    TOWNSHIP("township","township",SURFACE,BigDecimal.valueOf(15539928662016L,6)),
    CUBIC_YARD("cubic yard","yd\u00B3","yd3",VOLUME,BigDecimal.valueOf(764554857984L,12)),
    LINK("link","li",LENGTH,BigDecimal.valueOf(201168L,6)),
    CHAIN("chain","ch",LENGTH,BigDecimal.valueOf(201168,4)),
    US_LIQUID_PINT("us liquid pint","US liq pt",VOLUME,BigDecimal.valueOf(473176473,12)),
    US_LIQUID_QUART("us liquid quart","US liq qt",VOLUME,BigDecimal.valueOf(946352946,12)),
    FLUIDDRAM("Apothecaries fluid dram","fl dr ap",VOLUME,BigDecimal.valueOf(36966911953125L,19)),
    FLUIDOUNCE("Apothecaries fluid once","fl oz ap",VOLUME,BigDecimal.valueOf(295735295625L,16)),
    US_DRY_PINT("us dry pint","US dry pt",VOLUME,BigDecimal.valueOf(5506104713575L,16)),
    US_DRY_QUART("us dry quart","US dry qt",VOLUME,BigDecimal.valueOf(1101220942715L,15)),
    US_PECK("us peck","US pk",VOLUME,BigDecimal.valueOf(880976754172L,14)),
    US_BUSHEL("us bushel","Us bushel",VOLUME,BigDecimal.valueOf(3523907016688L,14)),
    GRAIN("grain","gr",MASS,BigDecimal.valueOf(6479891L,11)),
    AVOIRDUPOIS_DRAM("Avoirdupois dram","avdp dr",MASS,BigDecimal.valueOf(17718451953125L,16)),
    AVOIRDUPOIS_OUNCE("Avoirdupois ounce","avdp oz",MASS,BigDecimal.valueOf(28349523125L,12)),
    AVOIRDUPOIS_POUND("Avoirdupois pound","avdp lb",MASS,BigDecimal.valueOf(45359237L,8)),
    AVOIRDUPOIS_HUNDREDWEIGHT("Avoirdupois hundred weight","short cwt",MASS,BigDecimal.valueOf(45359237L,6)),
    AVOIRDUPOIS_TON("Avoirdupois ton","short ton",MASS,BigDecimal.valueOf(90718474L,5)),
    AVOIRDUPOIS_GROSS("Avoirdupois gross","long cwt",MASS,BigDecimal.valueOf(5080234544L,8)),
    AVOIRDUPOIS_LONG_TON("Avoirdupois long ton","long ton",MASS,BigDecimal.valueOf(10160469088L,7)),
    TROY_PENNY_WEIGHT("Troy pennyweight","dwt",MASS,BigDecimal.valueOf(155517384L,11)),
    TROY_OUNCE("Troy ounce","oz t",MASS,BigDecimal.valueOf(311034768L,10)),
    TROY_POUND("Troy pound","lb t",MASS,BigDecimal.valueOf(3732417216L,10)),
    APOTHECARIES_SCRUPLE("Apothecaries scruple","s ap",MASS,BigDecimal.valueOf(12959782L,10)),
    APOTHECARIES_DRAM("Apothecaries dram","dr ap",MASS,BigDecimal.valueOf(38879346L,10)),
    APOTHECARIES_OUNCE("Apothecaries ounce","oz ap",MASS,BigDecimal.valueOf(311034768L,10)),
    APOTHECARIES_POUND("Apothecaries pound","lb ap",MASS,BigDecimal.valueOf(3732417216L,10)),
    MILE_PER_IMPERIAL_GALLON("Mile per imperial gallon","mpg (Imp)", FUEL_EFFICIENCY, Constants.METER_PER_MILE,Constants.CUBIC_METER_PER_IMPERIALGALLON),
    MILE_PER_US_GALLON("Mile per US gallon","mpg (US)", FUEL_EFFICIENCY, Constants.METER_PER_MILE,Constants.CUBIC_METER_PER_USGALLON),
    MILE_PER_US_GALLON_EQUIVALENT("Mile per US gallon equivalent", "MPGe (US)", FUEL_EFFICIENCY, Constants.CUBIC_METER_PER_USGALLON),
    LITER_PER_100_KM("Litre per 100 km","l/(100km)",FUEL_ECONOMY,-8),
    WATT_HOUR_PER_MILE("Watt hour per mile","Wh/mi", FORCE , Constants.BD3600, Constants.METER_PER_MILE),
    WATT_HOUR_PER_100_MILE("Watt hour per 100 mile","Wh/(100mi)", FORCE , Constants.BD3600, Constants.METER_PER_MILE.scaleByPowerOfTen(2)),
    DEGREES_FAHRENHEIT("Degrees Fahrenheit","\u00b0F","deg F", TEMPERATURE, BigDecimal.valueOf(5L),BigDecimal.valueOf(9L),BigDecimal.valueOf(255372222222222L,12)),
    DAY("day", "d", TIME, Constants.BD86400),
    // Other units
    YEAR("year", "a", TIME, BigDecimal.valueOf(3600L * 24 * 365)),
    MONTH("month", "mo", TIME, BigDecimal.valueOf(3600L * 24 * 30)),
    NORMAL_CUBIC_METER("normal cubic meter", "Nm\u00b3", "Nm3", VOLUME),
    NORMAL_CUBIC_METER_PER_HOUR("normal cubic meter per hour", "Nm\u00b3/h", "Nm3/h", VOLUME_FLOW, ONE, Constants.BD3600, ZERO),
    CUBIC_METER_PER_DAY("cubic meter per day", "m\u00b3/d", "m3/d", VOLUME_FLOW, ONE, Constants.BD86400, ZERO),
    NORMAL_CUBIC_METER_PER_DAY("normal cubic meter per day", "Nm\u00b3/d", "Nm3/d", VOLUME_FLOW, ONE, Constants.BD86400, ZERO),
    PER_HOUR("per hour", "/h", FREQUENCY, ONE, Constants.BD3600),
    MOLE_PER_CENT("mole percent", "mol%", DIMENSIONLESS, -2),
    PERCENT("percent", "%", DIMENSIONLESS, -2),
    JOULE_PER_NORMAL_CUBIC_METER("joule per normal cubic meter", "J/Nm\u00b3", "J/Nm3", ENERGY_DENSITY),
    WATT_HOUR_PER_NORMAL_CUBIC_METER("watt hour per normal cubic meter", "Wh/Nm\u00b3", "Wh/Nm3", ENERGY_DENSITY, Constants.BD3600),
    TON("ton", "t", MASS, 3),
    KILOGRAM_PER_HOUR("kilogram per hour", "kg/h", MASSFLOW, ONE, Constants.BD3600),
    TON_PER_HOUR("ton per hour", "t/h", MASSFLOW, BigDecimal.valueOf(1000L), Constants.BD3600),
    FOOT_PER_SECOND("foot per second", "ft/s", SPEED, Constants.METER_PER_FOOT),
    CUBIC_FOOT_PER_DAY("cubic foot per day", "cf/d", VOLUME_FLOW, Constants.CUBIC_METER_PER_CUBIC_FOOT, Constants.BD86400),
    THERM_PER_HOUR("therm per hour", "thm/h", POWER, Constants.JOULE_PER_THERM, Constants.BD3600),
    THERM_PER_DAY("therm per day", "thm/d", POWER, Constants.JOULE_PER_THERM, Constants.BD86400),
    VOLT_SQUARED_SECOND("Volt squared second","V\u00b2s","V2s",ELECTRIC_POTENTIAL_SQUARED_TIME),
    JOULE_SECOND("Joule second","Js",ACTION),
    GRAM_PER_SECOND("kilogram per second", "g/s", MASSFLOW),
    CUBIC_METER_PER_METER("cubic meter per meter" , "m\u00B3/m","m3/m",FUEL_ECONOMY);



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

    Unit(String name, String symbol, Dimension dimension, int powerOfTen) {
        this(name, symbol, symbol, dimension, powerOfTen);
    }

    Unit(String name, String symbol, String asciiSymbol, Dimension dimension, int powerOfTen) {
        this(name, symbol, asciiSymbol, dimension, ONE.scaleByPowerOfTen(powerOfTen));
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
        BigDecimal METER_PER_MILE = BigDecimal.valueOf(1609344L,3);
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
    boolean isCoherentSiUnit() {
        return siMultiplier.equals(BigDecimal.ONE) && siDivisor.equals(ONE) && siDelta.equals(BigDecimal.ZERO) && !isDimensionLess();
    }

    private boolean isPowerOfTen() {
    	if (!siDelta.equals(ZERO)) {
    		return false;
    	}
    	return isPowerOfTen(siMultiplier) && isPowerOfTen(siDivisor);
    }

    private int powerOfTen() {
    	return powerOfTen(siMultiplier) - powerOfTen(siDivisor);
    }

    private boolean isPowerOfTen(BigDecimal value) {
    	return value.stripTrailingZeros().unscaledValue().equals(BigInteger.ONE);
    }

    private int powerOfTen(BigDecimal value) {
    	return -value.stripTrailingZeros().scale();
    }

    public static Unit getSIUnit(Dimension dimension) {
    	Unit candidate = null;
        for (Unit each : values()) {
            if (each.dimension.equals(dimension)) {
            	if (each.isCoherentSiUnit()) {
            		return each;
            	}
            	if (candidate == null && each.isPowerOfTen()) {
            		candidate = each;
            	}
            }
        }
        if (candidate == null) {
        	for (Dimension alternateDimension : Dimension.values()) {
        		if (alternateDimension != dimension && alternateDimension.hasSameDimensions(dimension)) {
        			return Unit.getSIUnit(alternateDimension);
        		}
        	}
        	throw new IllegalArgumentException(dimension.toString());
        } else {
        	return candidate;
        }
    }

    Quantity siValue(BigDecimal value) {
        BigDecimal newValue = value.multiply(siMultiplier);
        newValue = newValue.divide(siDivisor, newValue.scale() + siDivisor.precision() + EXTRA_PRECISION, BigDecimal.ROUND_HALF_UP);
        newValue = newValue.add(siDelta).stripTrailingZeros();
        Unit unit = Unit.getSIUnit(dimension);
        return unit.amount(newValue,-unit.powerOfTen());
    }

}