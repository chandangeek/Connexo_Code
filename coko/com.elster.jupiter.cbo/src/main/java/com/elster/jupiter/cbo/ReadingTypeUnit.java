/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import com.elster.jupiter.util.units.Unit;

import java.util.Optional;

public enum ReadingTypeUnit {
	NOTAPPLICABLE(0, "Not applicable", Unit.UNITLESS),
	METER(2, "Meter", Unit.METER),
	GRAM(3, "Gram", Unit.GRAM),
	ROTATIONSPERSECOND(4, "Rotations per second", Unit.ROTATIONS_PER_SECOND),
	AMPERE(5, "Ampere", Unit.AMPERE),
	KELVIN(6, "Kelvin", Unit.KELVIN),
	MOLE(7, "Mole", Unit.MOLE),
	CANDELA(8, "Candela", Unit.CANDELA),
	DEGREES(9, "Degrees", Unit.DEGREES),
	RADIAN(10, "Radian", Unit.RADIAN),
	STERADIAN(11, "Steradian", Unit.STERADIAN),
	GRAY(21, "Gray", Unit.GRAY),
	BECQUEREL(22, "Becquerel", Unit.BECQUEREL),
	DEGREESCELSIUS(23, "Degrees Celsius", Unit.DEGREES_CELSIUS),
	SIEVERT(24, "Sievert", Unit.SIEVERT),
	FARAD(25, "Farad", Unit.FARAD),
	COULOMB(26, "Coulomb", Unit.COULOMB),
	SECOND(27, "Second", Unit.SECOND),
	HENRY(28, "Henry", Unit.HENRY),
	VOLT(29, "Volt", Unit.VOLT),
	OHM(30, "Ohm", Unit.OHM),
	JOULE(31, "Joule", Unit.JOULE),
	NEWTON(32, "Newton", Unit.NEWTON),
	HERTZ(33, "Hertz", Unit.HERTZ),
	LUX(34, "Lux", Unit.LUX),
	LUMEN(35, "Lumen", Unit.LUMEN),
	WEBER(36, "Weber", Unit.WEBER),
	TESLA(37, "Tesla", Unit.TESLA),
	WATT(38, "Watt", Unit.WATT),
	PASCAL(39, "Pascal", Unit.PASCAL),
	SQUAREMETER(41, "Square meter", Unit.SQUARE_METER),
	CUBICMETER(42, "Cubic meter", Unit.CUBIC_METER),
	METERPERSECOND(43, "Meter per second", Unit.METER_PER_SECOND),
	METERPERSECONDSQUARED(44, "Meter per second squared", Unit.METER_PER_SECOND_SQUARED),
	CUBICMETERPERSECOND(45, "Cubic meter per second", Unit.CUBIC_METER_PER_SECOND),
	METERPERCUBICMETER(46, "Meter per cubic meter", Unit.METER_PER_CUBIC_METER),
	GRAMMETER(47, "Gram meter", Unit.GRAM_METER),
	GRAMPERCUBICMETER(48, "Gram per cubic meter", Unit.GRAM_PER_CUBIC_METER),
	METERSQUAREDPERSECOND(49, "Meter squared per second", Unit.METER_SQUARED_PER_SECOND),
	WATTPERMETERKELVIN(50, "Watt per meter kelvin", Unit.WATT_PER_METER_KELVIN),
	JOULEPERKELVIN(51, "Joule per kelvin", Unit.JOULE_PER_KELVIN),
	SIEMENS(53, "Siemens", Unit.SIEMENS),
	RADIANSPERSECOND(54, "Radians per second", Unit.RADIANS_PER_SECOND),
	VOLTAMPERE(61, "Volt ampere", Unit.VOLT_AMPERE),
	VOLTAMPEREREACTIVE(63, "Volt ampere reactive", Unit.VOLT_AMPERE_REACTIVE),
	PHASEANGLE(64, "Phase angle", Unit.PHASE_ANGLE),
	POWERFACTOR(65, "Power factor", Unit.POWER_FACTOR),
	VOLTSECONDS(66, "Volt seconds", Unit.VOLT_SECONDS),
	VOLTSQUARED(67, "Volt squared", Unit.VOLT_SQUARED),
	AMPERESECONDS(68, "Ampere seconds", Unit.AMPERE_SECONDS),
	AMPERESQUARED(69, "Ampere squared", Unit.AMPERE_SQUARED),
	AMPERESQUAREDSECOND(70, "Ampere squared second", Unit.AMPERE_SQUARED_SECOND),
	VOLTAMPEREHOUR(71, "Volt ampere hours", Unit.VOLT_AMPERE_HOUR),
	WATTHOUR(72, "Watt hours", Unit.WATT_HOUR),
	VOLTAMPEREREACTIVEHOUR(73, "Volt ampere reactive hours", Unit.VOLT_AMPERE_REACTIVE_HOUR),
	VOLTPERHERZ(74, "Volts per hertz", Unit.VOLT_PER_HERTZ),
	HERZPERSECOND(75, "Hertz per second", Unit.HERTZ_PER_SECOND),
	CHARACTERS(76, "Characters", Unit.CHARACTERS),
	CHARACTERSPERSECOND(77, "Characters per second", Unit.CHARACTERS_PER_SECOND),
	GRAMMETERSQUARED(78, "Gram meter squared", Unit.GRAM_METER_SQUARED),
	BEL(79, "Bel", Unit.BEL),
	MONEY(80, "Money", Unit.MONEY),
	WATTPERSECOND(81, "watt per second", Unit.WATT_PER_SECOND),
	LITREPERSECOND(82, "Litre per second", Unit.LITRE_PER_SECOND),
	QUANTITYPOWER(100, "Quantity power", Unit.QUANTITY_POWER),
	QUANTITYENERGY(101, "Quantity energy", Unit.QUANTITY_ENERGY),
	OHMMETER(102, "Ohm meter", Unit.OHM_METER),
	AMPEREPERMETER(103, "Ampere per meter", Unit.AMPERE_PER_METER),
	VOLTSQUAREDHOUR(104, "Volt squared hour", Unit.VOLT_SQUARED_HOUR),
	AMPERESQUAREDHOUR(105, "Ampere squared hour", Unit.AMPERE_SQUARED_HOUR),
	AMPEREHOUR(106, "Ampere hour", Unit.AMPERE_HOUR),
	WATTHOURPERCUBICMETER(107, "Watt hour per cubic meter", Unit.WATT_HOUR_PER_CUBIC_METER),
	TIMESTAMP(108, "Time stamp", Unit.TIMESTAMP),
	BOOLEAN(109, "Boolean", Unit.BOOLEAN),
	BOOLEANARRAY(110, "Boolean array", Unit.BOOLEAN_ARRAY),
	COUNT(111, "Count", Unit.COUNT),
	BELMILLIWATT(113, "Bel milliwatt", Unit.BEL_MILLIWATT),
	ENCODEDVALUE(114, "Encoded value", Unit.ENCODED_VALUE),
	WATTHOURPERROTATION(115, "Watt hours per rotation", Unit.WATT_HOUR_PER_ROTATION),
	VOLTAMPEREREACTIVEHOURPERROTATION(116, "Volt ampere reactive hours per rotation", Unit.VOLT_AMPERE_REACTIVE_HOUR_PER_ROTATION),
	VOLTAMPEREHOURPERROTATION(117, "Volt ampere hours per rotation", Unit.VOLT_AMPERE_HOUR_PER_ROTATION),
	ENDDEVICEEVENTCODE(118, "End device event code", Unit.END_DEVICE_EVENT_CODE),
	CUBICFEET(119, "Cubic feet", Unit.CUBIC_FEET),
	CUBICFEETCOMPENSATED(120, "Cubic feet compensated", Unit.CUBIC_FEET_COMPENSATED),
	CUBICFEETUNCOMPENSATED(121, "Cubic feet uncompensated", Unit.CUBIC_FEET_UNCOMPENSATED),
	CUBICFEETPERHOUR(122, "Cubic feet per hour", Unit.CUBIC_FEET_PER_HOUR),
	CUBICFEETCOMPENSATEDPERHOUR(123, "Cubic feet compensated per hour", Unit.CUBIC_FEET_COMPENSATED_PER_HOUR),
	CUBICFEETUNCOMPENSATEDPERHHOUR(124, "Cubic feet uncompensated per hour", Unit.CUBIC_FEET_UNCOMPENSATED_PER_HOUR),
	CUBICMETERPERHOUR(125, "Cubic meter per hour", Unit.CUBIC_METER_PER_HOUR),
	CUBICMETERPERHOURCOMPENSATED(126, "Cubic meter compensated per hour", Unit.CUBIC_METER_COMPENSATED_PER_HOUR),
	CUBICMETERPERHOURUNCOMPENSATED(127, "Cubic meter uncompensated per hour", Unit.CUBIC_METER_UNCOMPENSATED_PER_HOUR),
	USGALLON(128, "US gallon", Unit.USGALLON),
	USGALLONPERHOUR(129, "US gallon per hour", Unit.USGALLON_PER_HOUR),
	IMPERIALGALLON(130, "Imperial gallon", Unit.IMPERIALGALLON),
	IMPERIALGALLONPERHOUR(131, "Imperial gallon per hour", Unit.IMPERIALGALLON_PER_HOUR),
	BRITISHTHERMALUNIT(132, "British thermal unit", Unit.BRITISH_THERMAL_UNIT),
	BRITISHTHERMALUNITPERHOUR(133, "British thermal unit per hour", Unit.BRITISH_THERMAL_UNIT_PER_HOUR),
	LITRE(134, "Litre", Unit.LITRE),
	LITREPERHOUR(137, "Litre per hour", Unit.LITRE_PER_HOUR),
	LITREPERHOURCOMPENSATED(138, "Litre per hour compensated", Unit.LITRE_COMPENSATED_PER_HOUR),
	LITREPERHOURUNCOMPENSATED(139, "Litre per hour uncompensated", Unit.LITRE_UNCOMPENSATED_PER_HOUR),
	PASCALGUAGE(140, "Pascal gauge", Unit.PASCAL_GAUGE),
	POUNDPERSQUAREINCHABSOLUTE(141, "Pounds per square inch, absolute", Unit.POUND_PER_SQUARE_INCH_ABSOLUTE),
	POUNDPERSQUAREINCHGAUGE(142, "Pounds per square inch, gauge", Unit.POUND_PER_SQUARE_INCH_GAUGE),
	LITREPERLITRE(143, "Litre per litre", Unit.UNITLESS),
	GPERG(144, "Gram per gram", Unit.UNITLESS),
	MOLPERM3(145, "Mol per cubic meter", Unit.MOL_PER_M3),
	MOLPERMOL(146, "Mol per mol", Unit.UNITLESS),
	MOLPERKG(147, "Mol per kg", Unit.MOL_PER_KG),
	METERPERMETER(148, "Meter per meter", Unit.METER_PER_METER),
	SECONDPERSECOND(149, "Second per second", Unit.SECOND_PER_SECOND),
	HERZPERHERZ(150, "Herz per herz", Unit.HERZ_PER_HERZ),
	VOLTPERVOLT(151, "Volt per volt", Unit.VOLT_PER_VOLT),
	AMPEREPERAMPERE(152, "Ampere per ampere", Unit.AMPERE_PER_AMPERE),
	WATTPERVOLTAMPERE(153, "Watt per volt ampere", Unit.WATT_PER_VOLTAMPERE),
	REVOLUTIONS(154, "Revolutions", Unit.REVOLUTIONS),
	PASCALABSOLUTE(155, "Pascal absolute", Unit.PASCAL_ABSOLUTE),
	LITREUNCOMPENSATED(156, "Litre uncompensated", Unit.LITRE_UNCOMPENSATED),
	LITRECOMPENSATED(157, "Litre compensated", Unit.LITRE_COMPENSATED),
	KATAL(158, "Katal", Unit.KATAL),
	MINUTE(159, "Minute", Unit.MINUTE),
	HOUR(160, "Hour", Unit.HOUR),
	QUANTITYPOWER45(161, "Quantity power 45", Unit.QUANTITY_POWER_45),
	QUANTITYPOWER60(162, "Quantity power 60", Unit.QUANTITY_POWER_60),
	QUANTITYENERGY45(163, "Quantity energy 45", Unit.QUANTITY_ENERGY_45),
	QUANTITYENERGY60(164, "Quantity energy 60", Unit.QUANTITY_ENERGY_60),
	JOULEPERKG(165, "Joule per kg", Unit.JOULES_PER_KG),
	CUBICMETERUNCOMPENSATED(166, "Cubic meter uncompensated", Unit.CUBIC_METER_UNCOMPENSATED),
	CUBICMETERCOMPENSATED(167, "Cubic meter compensated", Unit.CUBIC_METER_COMPENSATED),
	WATTPERWATT(168, "Watt per watt", Unit.WATT_PER_WATT),
	THERM(169, "Therm", Unit.THERM),
	REFRACTIVEINDEX(170, "Refractive index", Unit.UNITLESS),
	RELATIVEPERMEABILITY(171, "Relative permeability", Unit.UNITLESS),
	NEPER(172, "Neper", Unit.NEPER),
	WAVENUMBER(173, "Wave number", Unit.WAVENUMBER),
	CUBICMETERPERKILOGRAM(174, "Cubic meter per kilogram", Unit.CUBIC_METER_PER_KILOGRAM),
	PASCALSECOND(175, "Pascal second", Unit.PASCAL_SECOND),
	NEWTONMETER(176, "Newton meter", Unit.NEWTON_METER),
	NEWTONPERMETER(177, "Newton per meter", Unit.NEWTON_PER_METER),
	RADIANSPERSECONDSQUARED(178, "Radians per second squared", Unit.RADIANS_PER_SECOND_SQUARED),
	WATTPERSQUAREDMETER(179, "Watt per squared meter", Unit.WATT_PER_SQUARED_METER),
	JOULEPERKILOGRAMKELVIN(180, "Joule per kilogram kelvin", Unit.JOULE_PER_KILOGRAM_KELVIN),
	JOULEPERCUBICMETER(181, "Joule per cubic meter", Unit.JOULE_PER_CUBIC_METER),
	VOLTPERMETER(182, "Volt per meter", Unit.VOLT_PER_METER),
	COULOMBPERCUBICMETER(183, "Coulomb per cubic meter", Unit.COULOMB_PER_CUBIC_METER),
	COULOMBPERSQUAREMETER(184, "Coulomb per square meter", Unit.COULOMB_PER_SQUARE_METER),
	FARADPERMETER(185, "Farad per meter", Unit.FARAD_PER_METER),
	HENRYPERMETER(186, "Henry per meter", Unit.HENRY_PER_METER),
	JOULEPERMOLE(187, "Joule per mole", Unit.JOULE_PER_MOLE),
	JOULEPERMOLEKELVIN(188, "Joule per mole kelvin", Unit.JOULE_PER_MOLE_KELVIN),
	COULOMBPERKILOGRAM(189, "Coulomb per kilogram", Unit.COULOMB_PER_KILOGRAM),
	GRAYPERSECOND(190, "Gray per second", Unit.GRAY_PER_SECOND),
	WATTPERSTERADIAN(191, "Watt per steradian", Unit.WATT_PER_STERADIAN),
	WATTPERSQUAREMETERSTERADIAN(192, "Watt per square meter steradian", Unit.WATT_PER_SQUARE_METER_STERADIAN),
	KATALPERCUBICMETER(193, "Katal per cubic meter", Unit.KATAL_PER_CUBIC_METER),
	DAY(195, "Day", Unit.DAY),
	ANGLEMIN(196, "Angle minute", Unit.ANGLEMINUTE),
	ANGLESECOND(197, "Angle second", Unit.ANGLESECOND),
	HECTARE(198, "Hectare", Unit.HECTARE),
	TON(199, "Ton", Unit.TON),
	ELECTRONVOLT(200, "Electron volt", Unit.ELECTRON_VOLT),
	DALTON(201, "Dalton", Unit.DALTON),
	UNIFIEDMASS(202, "Unified mass", Unit.UNIFIED_MASS),
	ASTRONOMICALUNIT(203, "Astronomical unit", Unit.ASTRONOMICAL_UNIT),
	LIGHTSPEED(204, "Light speed", Unit.LIGHT_SPEED),
	NATURALACTION(205, "Natural action", Unit.REDUCED_PLANCK),
	NATURALMASS(206, "Natural mass", Unit.ELECTRON_MASS),
	NATURALTIME(207, "Namtural time", Unit.NATURAL_UNIT_TIME),
	ELECTRONCHARGE(208, "Electron charge", Unit.ELECTRON_CHARGE),
	ELECTRONMASS(209, "Electron  mass", Unit.ELECTRON_MASS),
	ATOMICACTION(210, "Atomic action", Unit.REDUCED_PLANCK),
	BOHRRADIUS(211, "Bohr radius", Unit.BOHR_RADIUS),
	HARTREE(212, "Hartree", Unit.HARTREE),
	ATOMICTIME(213, "Atomic time", Unit.ATOMIC_TIME),
	BAR(214, "Bar", Unit.BAR),
	MMMERCURY(215, "Millimeter of mercury", Unit.MM_MERCURY),
	ANGSTROM(216, "Angstrem", Unit.ANGSTROM),
	NAUTICALMILE(217, "Nautical mile", Unit.NAUTICAL_MILE),
	BARN(218, "Barn", Unit.BARN),
	KNOT(219, "Knot", Unit.KNOT),
	CURIE(220, "Curie", Unit.CURIE),
	ROENTGEN(221, "Rentgen", Unit.ROENTGEN),
	RAD(222, "Rad", Unit.RAD),
	REM(223, "Rem", Unit.REM),
	INCH(224, "Inch", Unit.INCH),
	FOOT(225, "Foot", Unit.FOOT),
	ROD(226, "Rod", Unit.ROD),
	FURLONG(227, "Furlong", Unit.FURLONG),
	MILE(228, "Mile", Unit.MILE),
	SQUAREFOOT(229, "Square foot", Unit.SQUARE_FOOT),
	SQUAREYARD(230, "Square yard", Unit.SQUARE_YARD),
	SQUAREROD(231, "Square rod", Unit.SQUARE_ROD),
	ACRE(232, "Acre", Unit.ACRE),
	SQUAREMILE(233, "Square mile", Unit.SQUARE_MILE),
	SECTIONOFLAND(234, "Section of land", Unit.SQUARE_MILE),
	TOWNSHIP(235, "Township", Unit.TOWNSHIP),
	CUBICYARD(237, "Cubic yard", Unit.CUBIC_YARD),
	LINK(238, "Link", Unit.LINK),
	CHAIN(239, "Chain", Unit.CHAIN),
	USLIQUIDPINT(240, "US liquid pint", Unit.US_LIQUID_PINT),
	USLIQUIDQUART(241, "US liquid quart", Unit.US_LIQUID_QUART),
	FLUIDDREAM(242, "Apothecaries fluid dram", Unit.FLUIDDRAM),
	FLUIDOUNCE(243, "Apothecaries fluid once", Unit.FLUIDOUNCE),
	USDRYPINT(244, "US dry pint", Unit.US_DRY_PINT),
	USDRYQUART(245, "US dry quart", Unit.US_DRY_QUART),
	USPECK(246, "US peck", Unit.US_PECK),
	USBUSHEL(247, "US bushel", Unit.US_BUSHEL),
	GRAIN(248, "Grain", Unit.GRAIN),
	AVOIRDUPOISDRAM(249, "Avoirdupois dram", Unit.AVOIRDUPOIS_DRAM),
	AVOIRDUPOISONCE(250, "Avoirdupois ounce", Unit.AVOIRDUPOIS_OUNCE),
	AVOIRDUPOISPOUND(251, "Avoirdupois pound", Unit.AVOIRDUPOIS_POUND),
	AVOIRDUPOISHUNDREDWEIGHT(252, "Avoirdupois hundred weight", Unit.AVOIRDUPOIS_HUNDREDWEIGHT),
	AVOIRDUPOISTON(253, "Avoirdupois ton", Unit.AVOIRDUPOIS_TON),
	AVOIRDUPOISGROSS(254, "Avoirdupois gross", Unit.AVOIRDUPOIS_GROSS),
	AVOIRDUPOISLONGTON(255, "Avoirdupois long ton", Unit.AVOIRDUPOIS_LONG_TON),
	TROYPENNYWEIGHT(256, "Troy pennyweight", Unit.TROY_PENNY_WEIGHT),
	TROYOUNCE(257, "Troy ounce", Unit.TROY_OUNCE),
	TROYPOUND(258, "Troy pound", Unit.TROY_POUND),
	APOTHECARIESSCRUPLE(259, "Apothecaries scruple", Unit.APOTHECARIES_SCRUPLE),
	APOTHECARIESDRAM(260, "Apothecaries dram", Unit.APOTHECARIES_DRAM),
	APOTHECARIESOUNCE(261, "Apothecaries ounce", Unit.APOTHECARIES_OUNCE),
	APOTHECARIESPOUND(262, "Apothecaries pound", Unit.APOTHECARIES_POUND),
	MILEPERIMPERIALGALLON(263, "Mile per imperial gallon", Unit.MILE_PER_IMPERIAL_GALLON),
	MILEPERUSGALLON(264, "Mile per US gallon", Unit.MILE_PER_US_GALLON),
	MILEPERUSGALLONEQUIVALENT(265, "Mile per US gallon equivalent", Unit.MILE_PER_US_GALLON_EQUIVALENT),
	LITREPER100KM(266, "Litre per 100 km", Unit.LITER_PER_100_KM),
	WATTHOURPERMILE(267, "Watt hour per mile", Unit.WATT_HOUR_PER_MILE),
	WATTHOURPER100MILE(268, "Watt hour per 100 mile", Unit.WATT_HOUR_PER_100_MILE),
	DEGREESFAHRENHEIT(279, "Degrees Fahrenheit", Unit.DEGREES_FAHRENHEIT),
	VOLTHOUR(280, "Volt hour", Unit.VOLT_HOUR),
	HUMIDITY(281, "Humidity", Unit.UNITLESS),
	SKYCOVER(282, "Sky cover", Unit.UNITLESS);

	private final int id;
	private final String name;
	private final Unit unit;

	ReadingTypeUnit(int id, String name, Unit unit) {
		this.id = id;
		this.name = name;
		this.unit = unit;
	}
	
	public Unit getUnit() {
		return unit;
	}
	
	public String getSymbol() {
		return unit.getSymbol();
	}
	
	public int getId() {
		return id;		
	}

	public String getName() {
	return name;
}

	@Override
	public String toString() {
		return getSymbol();
	}
	
	public boolean isApplicable() {
		return id != 0;
	}

	public static ReadingTypeUnit get(int id) {
		for (ReadingTypeUnit each : values()) {
			if (each.id == id) {
				return each;
			}
		}
		throw new IllegalEnumValueException(ReadingTypeUnit.class, id);
	}

	public static Optional<ReadingTypeUnit> get(Unit unit) {
		for (ReadingTypeUnit each : values()) {
			if (each.unit.equals(unit)) {
				return Optional.of(each);
			}
		}
		return Optional.empty();
	}
}
