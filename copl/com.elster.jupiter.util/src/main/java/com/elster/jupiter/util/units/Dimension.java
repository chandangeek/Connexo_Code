/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import com.elster.jupiter.util.HasName;

public enum Dimension implements HasName {
	DIMENSIONLESS("dimensionless",0,0,0,0),
	LENGTH("length",1,0,0,0),
	MASS("mass",0,1,0,0),
	TIME("time",0,0,1,0),
	ELECTRIC_CURRENT("electric current",0,0,0,1),
	TEMPERATURE("temperature",0,0,0,0,1,0,0),
	AMOUNT_OF_SUBSTANCE("amount of substance",0,0,0,0,0,1,0),
	LUMINOUS_INTENSITY("luminous intensity",0,0,0,0,0,0,1),
	SURFACE("surface",2,0,0,0),
	VOLUME("volume",3,0,0,0),
	FREQUENCY("frequency",0,0,-1,0),
	ANGULAR_SPEED("angular speed",0,0,-1,0),
	RADIOACTIVITY("radioactivity",0,0,-1,0),
	SPEED("speed",1,0,-1,0),
	ACCELERATION("acceleration",1,0,-2,0),
	FORCE("force",1,1,-2,0),
	ENERGY("energy",2,1,-2,0),
	APPARENT_ENERGY("apparent energy",2,1,-2,0),
	REACTIVE_ENERGY("reactive energy",2,1,-2,0),
	POWER("power",2,1,-3,0),
	APPARENT_POWER("apparent power",2,1,-3,0),
	REACTIVE_POWER("reactive power",2,1,-3,0),
	POWER_RAMP("power ramp",2,1,-4,0),
	ELECTRIC_CHARGE("electric charge",0,0,1,1),
	ELECTRIC_POTENTIAL("electric potential",2,1,-3,-1),
	ELECTRIC_RESISTANCE("electric resistance", 2,1,-3,-2),
	ELECTRIC_CAPACITANCE("electric capacitance",-2,-1,4,2),
	ELECTRIC_INDUCTANCE("electric inductance",2,1,-2,-2),
	ELECTRICCONDUCTANCE("electric conductance",-2,-1,3,2),
	ELECTRIC_POTENTIAL_SQUARED("electric potential squared",4,2,-6,-2),
	ELECTRIC_POTENTIAL_SQUARED_TIME("electric potential squared time",4,2,-5,-2),
	ELECTRIC_CURRENT_SQUARED("electric current squared",0,0,0,2),
	ELECTRIC_CURRENT_SQUARED_TIME("electric current squared time",0,0,1,2),
	ELECTRIC_RESISTIVITY("electric resistivity", 3,1,-3,-2),
	MAGNETIC_FIELD_STRENGTH("magnetic field strength",-1,0,0,1),
	MAGNETIC_FLUX("magnetic flux",2,1,-2,-1),
	MAGNETIC_FLUX_DENSITY("magnetic flux density",0,1,-2,-1),
	ILLUMINANCE("illuminance",-2,0,0,0,0,0,1),
	LUMINOUS_FLUX("luminous flux",0,0,0,0,0,0,1),
	PRESSURE("pressure",-1,1,-2,0),
	VOLUME_FLOW("volume flow",3,0,-1,0),
	FUEL_EFFICIENCY("fuel efficiency",-2,0,0,0),
	FUEL_ECONOMY("fuel economy",2,0,0,0),
	MOMENT_OF_MASS("moment of mass",1,1,0,0),
	MOMENT_OF_FORCE("moment of force",2,1,-2,0),
	SURFACE_TENSION("surface tension",0,1,-2,0),
	DENSITY("density",-3,1,0,0),
	VISCOSITY("viscosity",2,0,1,0),
	THERMAL_CONDUCTIVITY("thermal conductivity",1,1,-3,0,1,0,0),
	HEAT_CAPACITY("heat capacity",2,1,-2,0,1,0,0),
	FREQUENCY_CHANGE_RATE("frequence change rate",0,0,-2,0),
	TURBINE_INERTIA("turbine inertia",2,1,0,0),
	CURRENCY("currency",0,0,0,0,0,0,0),
	ENERGY_DENSITY("energy density",-1,1,-2,0),
	ABSORBED_DOSE("absorbed dose",2,0,-2,0),
	DOSE_EQUIVALENT("dose equivalent",2,0,-2,0),
	MASSFLOW("mass flow dose",0,1,-1,0),
	VOLUME_CONCENTRATION("volume concentration",-3,0,0,0,0,1,0),
	MASS_CONCENTRATION("mass concentration",0,-1,0,0,0,1,0),
	CATALYTIC_ACTIVITY("catalytic activity",0,0,-1,0,0,1,0),
	CATALYTIC_ACTIVITY_CONCENTRATION("catalytic activity concentration",-3,0,-1,0,0,1,0),
	SPECIFIC_ENERGY("specific energy",2,0,-2,0),
	RECIPROCAL_LENGTH("reciprocal length",-1,0,0,0),
	SPECIFIC_VOLUME("specific volumne",3,-1,0,0),
	DYNAMIC_VISCOSITY("dynamic viscosity",-1,1,-1,0),
	ANGULAR_ACCELERATION("angular acceleration",0,0,-2,0),
	SPECIFIC_HEAT_CAPACITY("specific heat capacity",2,0,-2,0,-1,0,0),
	ELECTRIC_FIELD_STRENGTH("electric field strength",1,1,-3,-1),
	ELECTRIC_CHARGE_DENSITY("electric charge density",-3,0,1,1),
	ELECTRIC_CHARGE_SURFACE_DENSITY("electric charge surface density",-2,0,1,1),
	PERMITTIVITY("permittivity",-3,-1,4,2),
	PERMEABILITY("permeability",1,1,-2,-2),
	MOLAR_ENERGY("molar energy",2,1,-2,0,0,-1,0),
	MOLAR_ENTROPY("molar entropy",2,1,-2,0,-1,-1,0),
	EXPOSURE("exposure",0,-1,1,1),
	ABSORBED_DOSE_RATE("absorbed dose rate",2,0,-3,0),
	RADIANCE("radiance",0,1,-3,0),
	ACTION("action",2,1,-1,0);

	private final String name;
	private final int lengthDimension;
	private final int massDimension;
	private final int timeDimension;
	private final int currentDimension;
	private final int temperatureDimension;
	private final int amountDimension;
	private final int luminousIntensityDimension;

	Dimension(String name, int lengthD, int massD, int timeD, int currentD, int temperatureD,int amountD,int luminousIntensityD) {
		this.name = name;
		this.lengthDimension = lengthD;
		this.massDimension = massD;
		this.timeDimension = timeD;
		this.currentDimension = currentD;
		this.temperatureDimension = temperatureD;
		this.amountDimension = amountD;
		this.luminousIntensityDimension = luminousIntensityD;
	}

	Dimension(String name, int lengthD, int massD, int timeD, int currentD) {
		this(name,lengthD,massD,timeD,currentD,0,0,0);
	}

	public String getName() {
		return name;
	}

	public int getLengthDimension() {
		return lengthDimension;
	}

	public int getMassDimension() {
		return massDimension;
	}

	public int getTimeDimension() {
		return timeDimension;
	}

	public int getCurrentDimension() {
		return currentDimension;
	}

	public int getTemperatureDimension() {
		return temperatureDimension;
	}

	public int getAmountDimension() {
		return amountDimension;
	}

	public int getLuminousIntensityDimension() {
		return luminousIntensityDimension;
	}

	public String toString() {
		return getName();
	}

	public boolean isDimensionLess() {
		return
			lengthDimension == 0 && massDimension == 0 &&
			timeDimension == 0 && currentDimension == 0 &&
			temperatureDimension == 0 && amountDimension == 0 &&
			luminousIntensityDimension == 0;
	}

	public boolean hasSameDimensions(Dimension other) {
		return
			lengthDimension == other.lengthDimension && massDimension ==  other.massDimension &&
			timeDimension == other.timeDimension && currentDimension == other.currentDimension &&
			temperatureDimension == other.temperatureDimension && amountDimension == other.amountDimension &&
			luminousIntensityDimension == other.luminousIntensityDimension;
	}

	public int[] getDimensions() {
		return new int[] { lengthDimension , massDimension , timeDimension , currentDimension , temperatureDimension , amountDimension , luminousIntensityDimension };
	}

}