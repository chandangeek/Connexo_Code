package com.elster.jupiter.cbo;

import com.elster.jupiter.util.units.Unit;

public enum ReadingTypeUnit {
	NOTAPPLICABLE(0,Unit.UNITLESS),
	METER (2,Unit.METER),
	GRAM (3,Unit.GRAM),
	ROTATIONSPERSECOND (4,Unit.ROTATIONS_PER_SECOND),
	AMPERE (5,Unit.AMPERE),
	KELVIN (6,Unit.KELVIN),
	MOLE (7,Unit.MOLE),
	CANDELA (8,Unit.CANDELA),
	DEGREES (9,Unit.DEGREES),
	RADIAN (10,Unit.RADIAN),
	STERADIAN (11,Unit.STERADIAN),
	GRAY(21,Unit.GRAY),
	BECQUEREL (22,Unit.BECQUEREL),
	DEGREESCELSIUS (23,Unit.DEGREES_CELSIUS),
	SIEVERT (24,Unit.SIEVERT),
	FARAD (25,Unit.FARAD),
	COULOMB (26,Unit.COULOMB),
	SECOND (27,Unit.SECOND),
	HENRY (28,Unit.HENRY),
	VOLT (29,Unit.VOLT),
	OHM (30,Unit.OHM),
	JOULE (31,Unit.JOULE),
	NEWTON (32,Unit.NEWTON),
	HERTZ (33,Unit.HERTZ),
	LUX (34,Unit.LUX),
	LUMEN (35,Unit.LUMEN),
	WEBER (36,Unit.WEBER),
	TESLA (37,Unit.TESLA),
	WATT (38,Unit.WATT),
	PASCAL (39,Unit.PASCAL),
	SQUAREMETER(41,Unit.SQUARE_METER),
	CUBICMETER (42,Unit.CUBIC_METER),
	METERPERSECOND (43,Unit.METER_PER_SECOND),
	METERPERSECONDSQUARED (44,Unit.METER_PER_SECOND_SQUARED),
	CUBICMETERPERSECOND (45,Unit.CUBIC_METER_PER_SECOND),
	METERPERCUBICMETER (46,Unit.METER_PER_CUBIC_METER),
	KILOGRAMMETER (47,Unit.KILOGRAM_METER),
	KILOGRAMPERCUBICMETER (48,Unit.KILOGRAM_PER_CUBIC_METER),
	METERSQUAREDPERSECOND (49,Unit.METER_SQUARED_PER_SECOND),
	WATTPERMETERKELVIN (50,Unit.WATT_PER_METER_KELVIN),
	JOULEPERKELVIN (51,Unit.JOULE_PER_KELVIN),
	PARTSPERMILLION (52,Unit.PARTS_PER_MILLION),
	SIEMENS (53,Unit.SIEMENS),
	RADIANSPERSECOND (54,Unit.RADIANS_PER_SECOND),
	VOLTAMPERE (61,Unit.VOLT_AMPERE),
	VOLTAMPEREREACTIVE (63,Unit.VOLT_AMPERE_REACTIVE),
	PHASEANGLE (64,Unit.PHASE_ANGLE),
	POWERFACTOR (65,Unit.POWER_FACTOR),
	VOLTSECONDS (66,Unit.VOLT_SECONDS),
	VOLTSQUARED (67,Unit.VOLT_SQUARED),
	AMPERESECONDS (68,Unit.AMPERE_SECONDS),
	AMPERESQUARED (69,Unit.AMPERE_SQUARED),
	AMPERESQUAREDSECOND (70,Unit.AMPERE_SQUARED_SECOND),
	VOLTAMPEREHOUR (71,Unit.VOLT_AMPERE_HOUR),
	WATTHOUR (72,Unit.WATT_HOUR),
	VOLTAMPEREREACTIVEHOUR (73,Unit.VOLT_AMPERE_REACTIVE),
	VOLTPERHERZ (74,Unit.VOLT_PER_HERTZ),
	HERZPERSECOND (75,Unit.HERTZ_PER_SECOND),
	CHARACTERS (76,Unit.CHARACTERS),
	CHARACTERSPERSECOND (77,Unit.CHARACTERS_PER_SECOND),
	KILOGRAMMETERSQUARED (78,Unit.KILOGRAM_METER_SQUARED),
	DECIBEL (79,Unit.DECIBEL),
	MONEY (80,Unit.MONEY),
	QUANTITYPOWER(100,Unit.QUANTITY_POWER),
	QUANTITYENERGY (101,Unit.QUANTITY_ENERGY),
	OHMMETER (102,Unit.OHM_METER),
	AMPEREPERMETER (103,Unit.AMPERE_PER_METER),
	VOLTSQUAREDHOUR (104,Unit.VOLT_SQUARED_HOUR),
	AMPERESQUAREDHOUR (105,Unit.AMPERE_SQUARED_HOUR),
	AMPEREHOUR (106,Unit.AMPERE_HOUR),
	WATTHOURPERCUBICMETER (107,Unit.WATT_HOUR_PER_CUBIC_METER),
	TIMESTAMP (108,Unit.TIMESTAMP),
	BOOLEAN (109,Unit.BOOLEAN),
	BOOLEANARRAY (110,Unit.BOOLEAN_ARRAY),
	COUNT (111,Unit.COUNT),
	DECIBELMILLIWATT (113,Unit.DECIBEL_MILLIWATT),
	ENCODEDVALUE (114,Unit.ENCODED_VALUE),
	WATTHOURPERROTATION (115,Unit.WATT_HOUR_PER_ROTATION),
	VOLTAMPEREREACTIVEHOURPERROTATION (116,Unit.VOLT_AMPERE_REACTIVE_HOUR_PER_ROTATION),
	VOLTAMPEREHOURPERROTATION (117,Unit.VOLT_AMPERE_HOUR_PER_ROTATION),
	ENDDEVICEEVENTCODE (118,Unit.END_DEVICE_EVENT_CODE),
	CUBICFEET(119,Unit.CUBIC_FEET),
	CUBICFEETCOMPENSATED(120,Unit.CUBIC_FEET_COMPENSATED),
	CUBICFEETUNCOMPENSATED(121,Unit.CUBIC_FEET_UNCOMPENSATED),
	CUBICFEETPERHOUR(122,Unit.CUBIC_FEET),
	CUBICFEETCOMPENSATEDPERHOUR(123,Unit.CUBIC_FEET_COMPENSATED_PER_HOUR),
	CUBICFEETUNCOMPENSATEDPERHHOUR(124,Unit.CUBIC_FEET_UNCOMPENSATED_PER_HOUR),
	CUBICMETERPERHOUR(125,Unit.CUBIC_METER_PER_HOUR),
	CUBICMETERPERHOURUNCOMPENSATED(126,Unit.CUBIC_METER_UNCOMPENSATED_PER_HOUR),
	CUBICMETERPERHOURCOMPENSATED(127,Unit.CUBIC_METER_COMPENSATED_PER_HOUR),
	USGALLON(128,Unit.USGALLON),
	USGALLONPERHOUR(129,Unit.USGALLON_PER_HOUR),
	IMPERIALGALLON(130,Unit.IMPERIALGALLON),
	IMPERIALGALLONPERHOUR(131,Unit.IMPERIALGALLON_PER_HOUR),
	BRITISHTHERMALUNIT(132,Unit.BRITISH_THERMAL_UNIT),
	BRITISHTHERMALUNITPERHOUR(133,Unit.BRITISH_THERMAL_UNIT_PER_HOUR),
	LITRE(134,Unit.LITRE),
	PASCALGUAGE(140,Unit.PASCAL_GAUGE),
	POUNDPERSQUAREINCHABSOLUTE(141,Unit.POUND_PER_SQUARE_INCH_ABSOLUTE),
	POUNDPERSQUAREINCHGAUGE(142,Unit.POUND_PER_SQUARE_INCH_GAUGE),
	LITREPERLITRE(143,Unit.UNITLESS),
	GPERG(144,Unit.UNITLESS),
	MOLPERM3(145,Unit.MOL_PER_M3),
	MOLPERMOL(146,Unit.UNITLESS),
	MOLPERKG(147,Unit.MOL_PER_KG),
	METERPERMETER(148,Unit.METER_PER_METER),
	SECONDPERSECOND(149,Unit.SECOND_PER_SECOND),
	HERZPERHERZ(150,Unit.HERZ_PER_HERZ),
	VOLTPERVOLT(151,Unit.VOLT_PER_VOLT),
	AMPEREPERAMPERE(152,Unit.AMPERE_PER_AMPERE),
	WATTPERVOLTAMPERE(153,Unit.WATT_PER_VOLTAMPERE),
	REVOLUTIONS(154,Unit.REVOLUTIONS),
	WATTPERWATT(155,Unit.WATT_PER_WATT),
	LITREUNCOMPENSATED(156,Unit.LITRE_UNCOMPENSATED),
	LITRECOMPENSATED(157,Unit.LITRE_COMPENSATED),
	KATAL(158,Unit.KATAL),
	MINUTE(159,Unit.MINUTE),
	HOUR(160,Unit.HOUR),
	QUANTITYPOWER45(161,Unit.QUANTITY_POWER_45),
	QUANTITYPOWER60(162,Unit.QUANTITY_POWER_60),
	QUANTITYENERGY45(163,Unit.QUANTITY_ENERGY_45),
	QUANTITYENERGY60(164,Unit.QUANTITY_ENERGY_60),
	JOULEPERKG(165,Unit.JOULES_PER_KG),
	CUBICMETERUNCOMPENSATED(166,Unit.CUBIC_METER_UNCOMPENSATED),
	CUBICMETERCOMPENSATED(166,Unit.CUBIC_METER_COMPENSATED),
	THERM(169,Unit.THERM);
	
	private final int cimCode;
	private final Unit unit;
	
	private ReadingTypeUnit(int cimCode , Unit unit) {
		this.cimCode = cimCode;
		this.unit = unit;
	}
	
	public Unit getUnit() {
		return unit;
	}
	
	public String getSymbol() {
		return unit.getSymbol();
	}
	
	public int getCimCode() {
		return cimCode;		
	}
	
	@Override
	public String toString() {
		return getSymbol();
	}
	
	public boolean isApplicable() {
		return cimCode != 0;
	}
	
	public static ReadingTypeUnit get(int id) {
		for (ReadingTypeUnit each : values()) {
			if (each.cimCode == id) {
				return each;
			}
		}
		throw new IllegalArgumentException("" + id);
	}
	

}
