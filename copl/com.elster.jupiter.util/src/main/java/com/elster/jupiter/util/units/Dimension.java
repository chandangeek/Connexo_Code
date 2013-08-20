package com.elster.jupiter.util.units;

import com.elster.jupiter.util.HasName;

public final class Dimension implements HasName {
	
	private final String name;
	private final int lengthDimension;
	private final int massDimension;
	private final int timeDimension;
	private final int currentDimension;
	private final int temperatureDimension;
	private final int amountDimension;
	private final int luminousIntensityDimension;
	
	private Dimension(String name, int lengthD, int massD, int timeD, int currentD, int temperatureD,int amountD,int luminousIntensityD) {
		this.name = name;
		this.lengthDimension = lengthD;
		this.massDimension = massD;
		this.timeDimension = timeD;
		this.currentDimension = currentD;
		this.temperatureDimension = temperatureD;
		this.amountDimension = amountD;
		this.luminousIntensityDimension = luminousIntensityD;
	}
	
	private Dimension(String name, int lengthD, int massD, int timeD, int currentD) {
		this(name,lengthD,massD,timeD,currentD,0,0,0);
	}
	
	public static final Dimension DIMENSIONLESS = new Dimension("dimensionless",0,0,0,0);
	public static final Dimension LENGTH = new Dimension("length",1,0,0,0);
	public static final Dimension MASS = new Dimension("mass",0,1,0,0);
	public static final Dimension TIME = new Dimension("time",0,0,1,0);
	public static final Dimension ELECTRIC_CURRENT = new Dimension("electric current",0,0,0,1);
	public static final Dimension TEMPERATURE = new Dimension("temperature",0,0,0,0,1,0,0);
	public static final Dimension AMOUNT_OF_SUBSTANCE = new Dimension("amount of substance",0,0,0,0,0,1,0);
	public static final Dimension LUMINOUS_INTENSITY = new Dimension("luminous intensity",0,0,0,0,0,0,1);
	public static final Dimension SURFACE = new Dimension("surface",2,0,0,0);
	public static final Dimension VOLUME = new Dimension("volume",3,0,0,0);
	public static final Dimension FREQUENCY = new Dimension("frequency",0,0,-1,0);
	public static final Dimension ANGULAR_SPEED = new Dimension("angular speed",0,0,-1,0);
	public static final Dimension RADIOACTIVITY = new Dimension("radioactivity",0,0,-1,0);
	public static final Dimension SPEED = new Dimension("speed",1,0,-1,0);
	public static final Dimension ACCELERATION = new Dimension("acceleration",1,0,-2,0);
	public static final Dimension FORCE = new Dimension("force",1,1,-2,0);
	public static final Dimension ENERGY = new Dimension("energy",2,1,-2,0);
	public static final Dimension APPARENT_ENERGY = new Dimension("apparent energy",2,1,-2,0);
	public static final Dimension REACTIVE_ENERGY = new Dimension("reactive energy",2,1,-2,0);
	public static final Dimension POWER = new Dimension("power",2,1,-3,0);
	public static final Dimension APPARENT_POWER = new Dimension("apparent power",2,1,-3,0);
	public static final Dimension REACTIVE_POWER = new Dimension("reactive power",2,1,-3,0);
	public static final Dimension ELECTRIC_CHARGE = new Dimension("electric charge",0,0,1,1);
	public static final Dimension ELECTRIC_POTENTIAL = new Dimension("elecric pontential",2,1,-3,-1);
	public static final Dimension ELECTRIC_RESISTANCE = new Dimension("electric resistance", 2,1,-3,-2);
	public static final Dimension ELECTRIC_CAPACITANCE = new Dimension("electric capacitance",-2,-1,4,2);
	public static final Dimension ELECTRIC_INDUCTANCE = new Dimension("electric inductance",2,1,-2,-2);
	public static final Dimension ELECTRICCONDUCTANCE = new Dimension("electric conductance",-2,-1,3,2);
	public static final Dimension ELECTRIC_POTENTIAL_SQUARED = new Dimension("electric potential squared",4,2,-6,-2);
	public static final Dimension ELECTRIC_POTENTIAL_SQUARED_TIME = new Dimension("electric potential squared time",4,2,-5,-2);
	public static final Dimension ELECTRIC_CURRENT_SQUARED = new Dimension("electric current squared",0,0,0,2);
	public static final Dimension ELECTRIC_CURRENT_SQUARED_TIME = new Dimension("electric current squared time",0,0,1,2);
	public static final Dimension ELECTRIC_RESISTIVITY = new Dimension("electric resistivity", 3,1,-3,-2);
	public static final Dimension MAGNETIC_FIELD_STRENGTH = new Dimension("magnetic field strength",-1,0,0,1);
	public static final Dimension MAGNETIC_FLUX = new Dimension("magnetic flux",2,1,-2,-1);
	public static final Dimension MAGNETIC_FLUX_DENSITY = new Dimension("magnetic flux density",0,1,-2,-1);
	public static final Dimension ILLUMINANCE = new Dimension("illuminance",-2,0,0,0,0,0,1);
	public static final Dimension LUMINOUS_FLUX = new Dimension("luminous flux",0,0,0,0,0,0,1);
	public static final Dimension PRESSURE = new Dimension("pressure",-1,1,-2,0);
	public static final Dimension VOLUME_FLOW = new Dimension("volume flow",3,0,-1,0);
	public static final Dimension FUEL_EFFICIENCY = new Dimension("fuel efficiency",-2,0,0,0);
	public static final Dimension MOMENT_OF_MASS = new Dimension("moment of mass",1,1,0,0);
	public static final Dimension DENSITY = new Dimension("density",-3,1,0,0);
	public static final Dimension VISCOSITY = new Dimension("viscosity",2,0,1,0);
	public static final Dimension THERMAL_CONDUCTIVITY = new Dimension("thermal conductivity",1,1,-3,0,1,0,0);
	public static final Dimension HEAT_CAPACITY = new Dimension("heat capacity",2,1,-2,0,1,0,0);
	public static final Dimension FREQUENCY_CHANGE_RATE = new Dimension("frequence change rate",0,0,-2,0);
	public static final Dimension TURBINE_INERTIA = new Dimension("turbine inertia",2,1,0,0);
	public static final Dimension CURRENCY = new Dimension("currency",0,0,0,0,0,0,0);
	public static final Dimension ENERGY_DENSITY = new Dimension("energy density",-1,1,-2,0);
	public static final Dimension ABSORBED_DOSE = new Dimension("absorbed dose",2,0,-2,0);
	public static final Dimension DOSE_EQUIVALENT = new Dimension("absorbed dose",2,0,-2,0);
	public static final Dimension MASSFLOW = new Dimension("mass flow dose",0,1,-1,0);
		
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
	
	
}
