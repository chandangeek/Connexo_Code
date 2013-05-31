package com.elster.jupiter.util.units;

public final class Dimension {
	
	final private String name;
	final private int lengthDimension;
	final private int massDimension;
	final private int timeDimension;
	final private int currentDimension;
	final private int temperatureDimension;
	final private int amountDimension;
	final private int luminousIntensityDimension;
	
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
	public static final Dimension ELECTRICCURRENT = new Dimension("electric current",0,0,0,1);
	public static final Dimension TEMPERATURE = new Dimension("temperature",0,0,0,0,1,0,0);
	public static final Dimension AMOUNTOFSUBSTANCE = new Dimension("amount of substance",0,0,0,0,0,1,0);
	public static final Dimension LUMINOUSINTENSITY = new Dimension("luminous intensity",0,0,0,0,0,0,1);
	public static final Dimension SURFACE = new Dimension("surface",2,0,0,0);
	public static final Dimension VOLUME = new Dimension("volume",3,0,0,0);
	public static final Dimension FREQUENCY = new Dimension("frequency",0,0,-1,0);
	public static final Dimension ANGULARSPEED = new Dimension("angular speed",0,0,-1,0);
	public static final Dimension RADIOACTIVITY = new Dimension("radioactivity",0,0,-1,0);
	public static final Dimension SPEED = new Dimension("speed",1,0,-1,0);
	public static final Dimension ACCELERATION = new Dimension("acceleration",1,0,-2,0);
	public static final Dimension FORCE = new Dimension("force",1,1,-2,0);
	public static final Dimension ENERGY = new Dimension("energy",2,1,-2,0);
	public static final Dimension APPARENTENERGY = new Dimension("apparent energy",2,1,-2,0);
	public static final Dimension REACTIVEENERGY = new Dimension("reactive energy",2,1,-2,0);
	public static final Dimension POWER = new Dimension("power",2,1,-3,0);
	public static final Dimension APPARENTPOWER = new Dimension("apparent power",2,1,-3,0);
	public static final Dimension REACTIVEPOWER = new Dimension("reactive power",2,1,-3,0);
	public static final Dimension ELECTRICCHARGE = new Dimension("electric charge",0,0,1,1);
	public static final Dimension ELECTRICPOTENTIAL = new Dimension("elecric pontential",2,1,-3,-1);
	public static final Dimension ELECTRICRESISTANCE = new Dimension("electric resistance", 2,1,-3,-2);
	public static final Dimension ELECTRICCAPACITANCE = new Dimension("electric capacitance",-2,-1,4,2);
	public static final Dimension ELECTRICINDUCTANCE = new Dimension("electric inductance",2,1,-2,-2);
	public static final Dimension ELECTRICCONDUCTANCE = new Dimension("electric conductance",-2,-1,3,2);
	public static final Dimension ELECTRICPOTENTIALSQUARED = new Dimension("electric potential squared",4,2,-6,-2);
	public static final Dimension ELECTRICPOTENTIALSQUAREDTIME = new Dimension("electric potential squared time",4,2,-5,-2);
	public static final Dimension ELECTRICCURRENTSQUARED = new Dimension("electric current squared",0,0,0,2);
	public static final Dimension ELECTRICCURRENTSQUAREDTIME = new Dimension("electric current squared time",0,0,1,2);
	public static final Dimension ELECTRICRESISTIVITY = new Dimension("electric resistivity", 3,1,-3,-2);
	public static final Dimension MAGNETICFIELDSTRENGTH = new Dimension("magnetic field strength",-1,0,0,1);
	public static final Dimension MAGNETICFLUX = new Dimension("magnetic flux",2,1,-2,-1);
	public static final Dimension MAGNETICFLUXDENSITY = new Dimension("magnetic flux density",0,1,-2,-1);
	public static final Dimension ILLUMINANCE = new Dimension("illuminance",-2,0,0,0,0,0,1);
	public static final Dimension LUMINOUSFLUX = new Dimension("luminous flux",0,0,0,0,0,0,1);
	public static final Dimension PRESSURE = new Dimension("pressure",-1,1,-2,0);
	public static final Dimension VOLUMEFLOW = new Dimension("volume flow",3,0,-1,0);
	public static final Dimension FUELEFFICIENCY = new Dimension("fuel efficiency",-2,0,0,0);
	public static final Dimension MOMENTOFMASS = new Dimension("moment of mass",1,1,0,0);
	public static final Dimension DENSITY = new Dimension("density",-3,1,0,0);
	public static final Dimension VISCOSITY = new Dimension("viscosity",2,0,1,0);
	public static final Dimension THERMALCONDUCTIVITY = new Dimension("thermal conductivity",1,1,-3,0,1,0,0);
	public static final Dimension HEATCAPACITY = new Dimension("heat capacity",2,1,-2,0,1,0,0);
	public static final Dimension FREQUENCYCHANGERATE = new Dimension("frequence change rate",0,0,-2,0);
	public static final Dimension TURBINEINERTIA = new Dimension("turbine inertia",2,1,0,0);
	public static final Dimension CURRENCY = new Dimension("currency",0,0,0,0,0,0,0);
	public static final Dimension ENERGYDENSITY = new Dimension("energy density",-1,1,-2,0);
	public static final Dimension ABSORBEDDOSE = new Dimension("absorbed dose",2,0,-2,0);
	public static final Dimension DOSEEQUIVALENT = new Dimension("absorbed dose",2,0,-2,0);
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
