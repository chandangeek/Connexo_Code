package com.elster.jupiter.util.units;

import java.math.BigDecimal;
import java.util.*;
import static java.math.BigDecimal.*;

public final class Unit {
	
	final private String name;
	final private String symbol;
	final private String asciiSymbol;
	final private Dimension dimension;
	final private BigDecimal siMultiplier;
	final private BigDecimal siDivisor;
	final private BigDecimal siDelta;
	
	private Unit(String name, String symbol, String asciiSymbol, Dimension dimension , 	BigDecimal siMultiplier , BigDecimal siDivisor , BigDecimal siDelta) {
		this.name = name;
		this.symbol= symbol;
		this.asciiSymbol = asciiSymbol;
		this.dimension = dimension;
		this.siMultiplier = siMultiplier;
		this.siDivisor = siDivisor;
		this.siDelta = siDelta;
	}

	private Unit(String name, String symbol, String asciiSymbol, Dimension dimension, BigDecimal siMultiplier, BigDecimal siDivisor) {
		this(name,symbol,asciiSymbol,dimension,siMultiplier,siDivisor,ZERO);
	}
	
	private Unit(String name, String symbol, String asciiSymbol, Dimension dimension, BigDecimal siMultiplier) {
		this(name,symbol,asciiSymbol,dimension,siMultiplier,ONE);
	}
	
	private Unit(String name, String symbol, Dimension dimension , BigDecimal siMultiplier,BigDecimal siDivisor) {
		this(name,symbol,symbol,dimension,siMultiplier,siDivisor);
	}	
	
	private Unit(String name, String symbol, Dimension dimension, BigDecimal siMultiplier) {
		this(name,symbol,symbol,dimension,siMultiplier);
	}
	
	private Unit(String name, String symbol,String asciiSymbol, Dimension dimension) {
		this(name,symbol,asciiSymbol,dimension,ONE);
	}
	
	private Unit(String name, String symbol, Dimension dimension ) {
		this(name,symbol,symbol,dimension);
	}
	
	public static Unit valueOf(String symbol) {
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
	
	private static final BigDecimal BD3600 = BigDecimal.valueOf(3600);
	private static final BigDecimal BD86400 = BigDecimal.valueOf(86400);  // seconds per 24h day
	private static final BigDecimal BD100 = BigDecimal.valueOf(100);
	private static final BigDecimal BD1000 = BigDecimal.valueOf(1000);
	private static final BigDecimal METERPERFOOT = BigDecimal.valueOf(3048,4);
	private static final BigDecimal SQUAREMETERPERSQUAREFOOT = METERPERFOOT.multiply(METERPERFOOT);
	private static final BigDecimal CUBICMETERPERCUBICFOOT = SQUAREMETERPERSQUAREFOOT.multiply(METERPERFOOT);
	private static final BigDecimal JOULEPERTHERM = BigDecimal.valueOf(105505585257348L,6);
	
	public static final Unit UNITLESS = new Unit("unitless","",Dimension.DIMENSIONLESS);
	public static final Unit METER = new Unit("meter","m",Dimension.LENGTH);
	public static final Unit KILOGRAM = new Unit("kilogram","kg",Dimension.MASS);
	public static final Unit GRAM = new Unit("gram","g","g",Dimension.MASS,ONE,BD1000,ZERO);
	public static final Unit SECOND = new Unit("second","s",Dimension.TIME);
	public static final Unit AMPERE = new Unit("ampere","A",Dimension.ELECTRICCURRENT);
	public static final Unit KELVIN = new Unit("kelvin","K",Dimension.TEMPERATURE);
	public static final Unit MOLE = new Unit("mole","mol",Dimension.AMOUNTOFSUBSTANCE);
	public static final Unit CANDELA = new Unit("candela","cd",Dimension.LUMINOUSINTENSITY);
	public static final Unit ROTATIONSPERSECOND = new Unit("rotations per second","rev/s",Dimension.FREQUENCY);
	public static final Unit DEGREES = new Unit("degrees","\u00b0","deg",Dimension.DIMENSIONLESS);
	public static final Unit RADIAN = new Unit("radian","rad",Dimension.DIMENSIONLESS);
	public static final Unit STERADIAN = new Unit("steradian","sr",Dimension.DIMENSIONLESS);
	public static final Unit GRAY = new Unit("gray","Gy",Dimension.ABSORBEDDOSE);
	public static final Unit BECQUEREL = new Unit("becquerel","Bq",Dimension.RADIOACTIVITY);
	public static final Unit DEGREESCELSIUS = new Unit("degrees Celsius","\u00b0C","deg C", Dimension.TEMPERATURE,ONE,ONE,BigDecimal.valueOf(27315, 2));
	public static final Unit SIEVERT = new Unit("sievert","Sv",Dimension.DOSEEQUIVALENT);
	public static final Unit FARAD = new Unit("farad","F",Dimension.ELECTRICCAPACITANCE);
	public static final Unit COULOMB = new Unit("coulomb","C",Dimension.ELECTRICCHARGE);
	public static final Unit HENRY = new Unit("henry","H",Dimension.ELECTRICINDUCTANCE);
	public static final Unit VOLT = new Unit("volt","V",Dimension.ELECTRICPOTENTIAL);
	public static final Unit OHM = new Unit("ohm","\u03a9","ohm",Dimension.ELECTRICRESISTANCE);
	public static final Unit JOULE = new Unit("joule","J",Dimension.ENERGY);
	public static final Unit NEWTON = new Unit("newton","N",Dimension.FORCE);
	public static final Unit HERTZ = new Unit("hertz","Hz",Dimension.FREQUENCY);
	public static final Unit LUX = new Unit("lux","lx",Dimension.ILLUMINANCE);
	public static final Unit LUMEN = new Unit("lumen","Lm",Dimension.LUMINOUSFLUX);
	public static final Unit WEBER = new Unit("weber","Wb",Dimension.MAGNETICFLUX);
	public static final Unit TESLA = new Unit("tesla","T",Dimension.MAGNETICFLUXDENSITY);
	public static final Unit WATT = new Unit("watt","W",Dimension.POWER);
	public static final Unit PASCAL = new Unit("pascal","Pa",Dimension.PRESSURE);
	public static final Unit SQUAREMETER = new Unit("square meter","m\u00b2","m2",Dimension.SURFACE);
	public static final Unit CUBICMETER = new Unit("cubic meter","m\u00b3","m3",Dimension.VOLUME);
	public static final Unit METERPERSECOND = new Unit("meter per second","m/s",Dimension.SPEED);
	public static final Unit METERPERSECONDSQUARED = new Unit("meter per second squared","m/s\u00b2","m/s2",Dimension.ACCELERATION);
	public static final Unit CUBICMETERPERSECOND = new Unit("cubic meter per second","m\u00b3/s","m3/s",Dimension.VOLUMEFLOW);
	public static final Unit METERPERCUBICMETER = new Unit("meters per cubic meter","m/m\u00b3","m/m3",Dimension.FUELEFFICIENCY);
	public static final Unit KILOGRAMMETER = new Unit("kilogram meter","M",Dimension.MOMENTOFMASS);
	public static final Unit KILOGRAMPERCUBICMETER = new Unit("kilogram per cubic meter","kg/m\u00b3","kg/m3",Dimension.DENSITY);
	public static final Unit METERSQUAREDPERSECOND = new Unit("meter squared per second","m\u00b2/s","m2/s",Dimension.VISCOSITY);
	public static final Unit WATTPERMETERKELVIN = new Unit("watt per meter kelvin","W/mK",Dimension.THERMALCONDUCTIVITY);
	public static final Unit JOULEPERKELVIN = new Unit("joule per kelvin","J/K",Dimension.HEATCAPACITY);
	public static final Unit PARTSPERMILLION = new Unit("parts per million","ppm",Dimension.DIMENSIONLESS);
	public static final Unit SIEMENS = new Unit("siemens","S",Dimension.ELECTRICCONDUCTANCE);
	public static final Unit RADIANSPERSECOND = new Unit("radians per second","rad/s",Dimension.ANGULARSPEED);
	public static final Unit VOLTAMPERE = new Unit("volt ampere","VA",Dimension.APPARENTPOWER);
	public static final Unit VOLTAMPEREREACTIVE = new Unit("volt ampere reactive","VAr",Dimension.REACTIVEPOWER);
	public static final Unit PHASEANGLE = new Unit("phase angle","\u03b8-Deg","theta degrees",Dimension.DIMENSIONLESS);
	public static final Unit POWERFACTOR = new Unit("power factor","Cos \u03b8","cos theta",Dimension.DIMENSIONLESS);
	public static final Unit VOLTSECONDS = new Unit("volt seconds","Vs",Dimension.MAGNETICFLUX);
	public static final Unit VOLTSQUARED = new Unit("volt squared","V\u00b2","V2",Dimension.ELECTRICPOTENTIALSQUARED);
	public static final Unit AMPERESECONDS = new Unit("ampere seconds","As",Dimension.ELECTRICCHARGE);
	public static final Unit AMPERESQUARED = new Unit("ampere squared","A\u00b2","A2",Dimension.ELECTRICCURRENTSQUARED);
	public static final Unit AMPERESQUAREDSECOND = new Unit("ampere squared second","A\u00b2s","A2s",Dimension.ELECTRICCURRENTSQUAREDTIME);
	public static final Unit VOLTAMPEREHOUR = new Unit("volt ampere hours","VAh",Dimension.APPARENTENERGY,BD3600);
	public static final Unit WATTHOUR = new Unit("watt hours","Wh",Dimension.ENERGY,BD3600);
	public static final Unit VOLTAMPEREREACTIVEHOUR = new Unit("volt ampere reactive hours","VArh",Dimension.REACTIVEENERGY,BD3600);
	public static final Unit VOLTPERHERTZ = new Unit("volts per hertz","V/Hz",Dimension.MAGNETICFLUX);
	public static final Unit HERTZPERSECOND = new Unit("hertz per second","Hz/s",Dimension.FREQUENCYCHANGERATE);
	public static final Unit CHARACTERS = new Unit("characters","char",Dimension.DIMENSIONLESS);
	public static final Unit CHARACTERSPERSECOND = new Unit("characters per second","char/s",Dimension.FREQUENCY);
	public static final Unit KILOGRAMMETERSQUARED = new Unit("kilogram meter squared","kgm\u00b2","kgm2",Dimension.TURBINEINERTIA);
	public static final Unit DECIBEL = new Unit("decibel","dB",Dimension.DIMENSIONLESS);
	public static final Unit MONEY = new Unit("money","\u00A4","money",Dimension.CURRENCY);
	public static final Unit QUANTITYPOWER = new Unit("quantity power","Q",Dimension.DIMENSIONLESS); // TODO dimension
	public static final Unit QUANTITYENERGY = new Unit("quantity energy","Qh",Dimension.DIMENSIONLESS); // TODO dimension
	public static final Unit OHMMETER = new Unit("ohm meter","\u03a9m","ohmm", Dimension.ELECTRICRESISTIVITY);
	public static final Unit AMPEREPERMETER = new Unit("ampere per meter","A/m",Dimension.MAGNETICFIELDSTRENGTH);
	public static final Unit VOLTSQUAREDHOUR = new Unit("volt squared hour","V\u00b2h","V2h",Dimension.ELECTRICPOTENTIALSQUAREDTIME,BD3600);
	public static final Unit AMPERESQUAREDHOUR = new Unit("ampere squared hour","A\u00b2h","A2h",Dimension.ELECTRICCURRENTSQUAREDTIME,BD3600);
	public static final Unit AMPEREHOUR = new Unit("ampere hour","Ah",Dimension.ELECTRICCHARGE,BD3600);
	public static final Unit WATTHOURPERCUBICMETER = new Unit("watt hour per cubic meter", "Wh/m\u00b3","Wh/m3",Dimension.ENERGYDENSITY,BD3600);
	public static final Unit TIMESTAMP = new Unit("timestamp", "timeStamp",Dimension.DIMENSIONLESS);
	public static final Unit BOOLEAN = new Unit("boolean", "status",Dimension.DIMENSIONLESS);
	public static final Unit BOOLEANARRAY = new Unit("boolean array", "statuses",Dimension.DIMENSIONLESS);
	public static final Unit COUNT = new Unit("count", "Count",Dimension.DIMENSIONLESS);
	public static final Unit DECIBELMILLIWATT = new Unit("decibel milliwatt", "dBm",Dimension.POWER); // TO DO convert to SI
	public static final Unit ENCODEDVALUE = new Unit("encoded value", "Code",Dimension.DIMENSIONLESS);
	public static final Unit WATTHOURPERROTATION = new Unit("watt hours per rotation","Wh/rev",Dimension.ENERGY,BD3600);
	public static final Unit VOLTAMPEREREACTIVEHOURPERROTATION = new Unit("volt ampere reactive hours per rotation","VArh/rev",Dimension.REACTIVEENERGY,BD3600);
	public static final Unit VOLTAMPEREHOURPERROTATION = new Unit("volt ampere hours per rotation","VAh/rev",Dimension.APPARENTENERGY,BD3600);
	public static final Unit ENDDEVICEEVENTCODE = new Unit("end device event code","MeCode",Dimension.DIMENSIONLESS);
	
	public static final Unit YEAR = new Unit("year","a",Dimension.TIME,BigDecimal.valueOf(3600L*24*365));
	public static final Unit MONTH = new Unit("month","mo",Dimension.TIME,BigDecimal.valueOf(3600L*24*30));
	public static final Unit DAY = new Unit("day","d",Dimension.TIME,BD86400);
	public static final Unit HOUR = new Unit("hour","h",Dimension.TIME,BD3600);
	public static final Unit MINUTE = new Unit("minute","min",Dimension.TIME,BigDecimal.valueOf(60));
	public static final Unit NORMALCUBICMETER = new Unit("normal cubic meter","Nm\u00b3","Nm3",Dimension.VOLUME);
	public static final Unit CUBICMETERPERHOUR = new Unit("cubic meter per hour","m\u00b3/h","m3/h",Dimension.VOLUMEFLOW,ONE,BD3600,ZERO);
	public static final Unit NORMALCUBICMETERPERHOUR = new Unit("normal cubic meter per hour","Nm\u00b3/h","Nm3/h",Dimension.VOLUMEFLOW,ONE,BD3600,ZERO);
	public static final Unit CUBICMETERPERDAY = new Unit("cubic meter per day","m\u00b3/d","m3/d",Dimension.VOLUMEFLOW,ONE,BD86400,ZERO);
	public static final Unit NORMALCUBICMETERPERDAY = new Unit("normal cubic meter per day","Nm\u00b3/d","Nm3/d",Dimension.VOLUMEFLOW,ONE,BD86400,ZERO);
	public static final Unit LITER = new Unit("liter","l",Dimension.VOLUME,ONE,BD1000);
	
	// following units still need to be added to UNITS map
	
	public static final Unit PERHOUR = new Unit("per hour","/h",Dimension.FREQUENCY,ONE,BD3600);
	public static final Unit MOLEPERCENT = new Unit("mole percent hour","mol%/",Dimension.DIMENSIONLESS,ONE,BD100);
	public static final Unit PERCENT = new Unit("percent","%",Dimension.DIMENSIONLESS,ONE,BD100);
	public static final Unit JOULEPERNORMALCUBICMETER = new Unit("joule per normal cubic meter","J/Nm\u00b3","J/Nm3",Dimension.ENERGYDENSITY);
	public static final Unit WATTHOURPERNORMALCUBICMETER = new Unit("watt hour per normal cubic meter","Wh/Nm\u00b3","Wh/Nm3",Dimension.ENERGYDENSITY,BD3600);
	public static final Unit TON = new Unit("ton","t",Dimension.MASS,BD1000);
	public static final Unit KILOGRAMPERHOUR = new Unit("kilogram per hour","kg/h",Dimension.MASSFLOW,ONE,BD3600);
	public static final Unit TONPERHOUR = new Unit("ton per hour","t/h",Dimension.MASSFLOW,BD1000,BD3600);
	public static final Unit LITERPERHOUR = new Unit("liter per hour","l/h",Dimension.VOLUMEFLOW,ONE,BD3600.multiply(BD1000));
	// TODO verify and check UK AND US units from com.energyict.cbo.BaseUnit
	public static final Unit FOOT = new Unit("foot","ft",Dimension.LENGTH,METERPERFOOT);
	public static final Unit FOOTPERSECOND = new Unit("foot per second","ft/s",Dimension.SPEED,METERPERFOOT);
	public static final Unit CUBICFOOT = new Unit("cubic foot","cf",Dimension.VOLUME,CUBICMETERPERCUBICFOOT);
	public static final Unit CUBICFOOTPERHOUR = new Unit("cubic foot per hour","cf/h",Dimension.VOLUMEFLOW,CUBICMETERPERCUBICFOOT,BD3600);
	public static final Unit CUBICFOOTPERDAY = new Unit("cubic foot per day","cf/d",Dimension.VOLUMEFLOW,CUBICMETERPERCUBICFOOT,BD86400);
	public static final Unit THERM = new Unit("therm" ,"thm", Dimension.ENERGY,JOULEPERTHERM);
	public static final Unit THERMPERHOUR = new Unit("therm per hour" ,"thm/h", Dimension.POWER, JOULEPERTHERM , BD3600);
	public static final Unit THERMPERDAY = new Unit("therm per day" ,"thm/d" , Dimension.POWER, JOULEPERTHERM , BD86400);
	
	
	private static final Map<String,Unit> UNITS = new HashMap<>();
	
	static {
		UNITS.put(UNITLESS.asciiSymbol,UNITLESS);
		UNITS.put(METER.asciiSymbol,METER);
		UNITS.put(KILOGRAM.asciiSymbol,KILOGRAM);
		UNITS.put(GRAM.asciiSymbol,GRAM);
		UNITS.put(SECOND.asciiSymbol,SECOND);
		UNITS.put(AMPERE.asciiSymbol,AMPERE);
		UNITS.put(KELVIN.asciiSymbol,KELVIN);
		UNITS.put(MOLE.asciiSymbol,MOLE);
		UNITS.put(CANDELA.asciiSymbol,CANDELA);
		UNITS.put(ROTATIONSPERSECOND.asciiSymbol,ROTATIONSPERSECOND);
		UNITS.put(DEGREES.asciiSymbol,DEGREES);
		UNITS.put(RADIAN.asciiSymbol,RADIAN);
		UNITS.put(STERADIAN.asciiSymbol,STERADIAN);
		UNITS.put(GRAY.asciiSymbol,GRAY);
		UNITS.put(BECQUEREL.asciiSymbol,BECQUEREL);
		UNITS.put(DEGREESCELSIUS.asciiSymbol,DEGREESCELSIUS);
		UNITS.put(SIEVERT.asciiSymbol,SIEVERT);
		UNITS.put(FARAD.asciiSymbol,FARAD);
		UNITS.put(COULOMB.asciiSymbol,COULOMB);
		UNITS.put(HENRY.asciiSymbol,HENRY);
		UNITS.put(VOLT.asciiSymbol,VOLT);
		UNITS.put(OHM.asciiSymbol,OHM);
		UNITS.put(JOULE.asciiSymbol,JOULE);
		UNITS.put(NEWTON.asciiSymbol,NEWTON);
		UNITS.put(HERTZ.asciiSymbol,HERTZ);
		UNITS.put(LUX.asciiSymbol,LUX);
		UNITS.put(LUMEN.asciiSymbol,LUMEN);
		UNITS.put(WEBER.asciiSymbol,WEBER);
		UNITS.put(TESLA.asciiSymbol,TESLA);
		UNITS.put(WATT.asciiSymbol,WATT);
		UNITS.put(PASCAL.asciiSymbol,PASCAL);
		UNITS.put(SQUAREMETER.asciiSymbol,SQUAREMETER);
		UNITS.put(CUBICMETER.asciiSymbol,CUBICMETER);
		UNITS.put(METERPERSECOND.asciiSymbol,METERPERSECOND);
		UNITS.put(METERPERSECONDSQUARED.asciiSymbol,METERPERSECONDSQUARED);
		UNITS.put(CUBICMETERPERSECOND.asciiSymbol,CUBICMETERPERSECOND);
		UNITS.put(METERPERCUBICMETER.asciiSymbol,METERPERCUBICMETER);
		UNITS.put(KILOGRAMMETER.asciiSymbol,KILOGRAMMETER);
		UNITS.put(KILOGRAMPERCUBICMETER.asciiSymbol,KILOGRAMPERCUBICMETER);
		UNITS.put(METERSQUAREDPERSECOND.asciiSymbol,METERSQUAREDPERSECOND);
		UNITS.put(WATTPERMETERKELVIN.asciiSymbol,WATTPERMETERKELVIN);
		UNITS.put(JOULEPERKELVIN.asciiSymbol,JOULEPERKELVIN);
		UNITS.put(PARTSPERMILLION.asciiSymbol,PARTSPERMILLION);
		UNITS.put(SIEMENS.asciiSymbol,SIEMENS);
		UNITS.put(RADIANSPERSECOND.asciiSymbol,RADIANSPERSECOND);
		UNITS.put(VOLTAMPERE.asciiSymbol,VOLTAMPERE);
		UNITS.put(VOLTAMPEREREACTIVE.asciiSymbol,VOLTAMPEREREACTIVE);
		UNITS.put(PHASEANGLE.asciiSymbol,PHASEANGLE);
		UNITS.put(POWERFACTOR.asciiSymbol,POWERFACTOR);
		UNITS.put(VOLTSECONDS.asciiSymbol,VOLTSECONDS);
		UNITS.put(VOLTSQUARED.asciiSymbol,VOLTSQUARED);
		UNITS.put(AMPERESECONDS.asciiSymbol,AMPERESECONDS);
		UNITS.put(AMPERESQUARED.asciiSymbol,AMPERESQUARED);
		UNITS.put(AMPERESQUAREDSECOND.asciiSymbol,AMPERESQUAREDSECOND);
		UNITS.put(VOLTAMPEREHOUR.asciiSymbol,VOLTAMPEREHOUR);
		UNITS.put(WATTHOUR.asciiSymbol,WATTHOUR);
		UNITS.put(VOLTAMPEREREACTIVEHOUR.asciiSymbol,VOLTAMPEREREACTIVEHOUR);
		UNITS.put(VOLTPERHERTZ.asciiSymbol,VOLTPERHERTZ);
		UNITS.put(HERTZPERSECOND.asciiSymbol,HERTZPERSECOND);
		UNITS.put(CHARACTERS.asciiSymbol,CHARACTERS);
		UNITS.put(CHARACTERSPERSECOND.asciiSymbol,CHARACTERSPERSECOND);
		UNITS.put(KILOGRAMMETERSQUARED.asciiSymbol,KILOGRAMMETERSQUARED);
		UNITS.put(DECIBEL.asciiSymbol,DECIBEL);
		UNITS.put(MONEY.asciiSymbol,MONEY);
		UNITS.put(QUANTITYPOWER.asciiSymbol,QUANTITYPOWER);
		UNITS.put(QUANTITYENERGY.asciiSymbol,QUANTITYENERGY);
		UNITS.put(OHMMETER.asciiSymbol,OHMMETER);
		UNITS.put(AMPEREPERMETER.asciiSymbol,AMPEREPERMETER);
		UNITS.put(VOLTSQUAREDHOUR.asciiSymbol,VOLTSQUAREDHOUR);
		UNITS.put(AMPERESQUAREDHOUR.asciiSymbol,AMPERESQUAREDHOUR);
		UNITS.put(AMPEREHOUR.asciiSymbol,AMPEREHOUR);
		UNITS.put(WATTHOURPERCUBICMETER.asciiSymbol,WATTHOURPERCUBICMETER);
		UNITS.put(TIMESTAMP.asciiSymbol,TIMESTAMP);
		UNITS.put(BOOLEAN.asciiSymbol,BOOLEAN);
		UNITS.put(BOOLEANARRAY.asciiSymbol,BOOLEANARRAY);
		UNITS.put(COUNT.asciiSymbol,COUNT);
		UNITS.put(DECIBELMILLIWATT.asciiSymbol,DECIBELMILLIWATT);
		UNITS.put(ENCODEDVALUE.asciiSymbol,ENCODEDVALUE);
		UNITS.put(WATTHOURPERROTATION.asciiSymbol,WATTHOURPERROTATION);
		UNITS.put(VOLTAMPEREREACTIVEHOURPERROTATION.asciiSymbol,VOLTAMPEREREACTIVEHOURPERROTATION);
		UNITS.put(VOLTAMPEREHOURPERROTATION.asciiSymbol,VOLTAMPEREHOURPERROTATION);
		UNITS.put(ENDDEVICEEVENTCODE.asciiSymbol,ENDDEVICEEVENTCODE);		
		UNITS.put(YEAR.asciiSymbol,YEAR);
		UNITS.put(MONTH.asciiSymbol,MONTH);
		UNITS.put(DAY.asciiSymbol,DAY);
		UNITS.put(HOUR.asciiSymbol,HOUR);
		UNITS.put(MINUTE.asciiSymbol,MINUTE);
		UNITS.put(NORMALCUBICMETER.asciiSymbol,NORMALCUBICMETER);
		UNITS.put(CUBICMETERPERHOUR.asciiSymbol,CUBICMETERPERHOUR);
		UNITS.put(NORMALCUBICMETERPERHOUR.asciiSymbol,NORMALCUBICMETERPERHOUR);
		UNITS.put(CUBICMETERPERDAY.asciiSymbol,CUBICMETERPERDAY);
		UNITS.put(NORMALCUBICMETERPERDAY.asciiSymbol,NORMALCUBICMETERPERDAY);
		UNITS.put(LITER.asciiSymbol,LITER);		
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
	
	public Quantity amount(BigDecimal value,int exponent) {
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
			if (each.dimension.equals(dimension) && each.isSICompliant())
				return each;
		}
		throw new IllegalArgumentException("" + dimension);
	}
	
	BigDecimal siValue(BigDecimal value) {
		BigDecimal newValue = value.multiply(siMultiplier);
		newValue = newValue.divide(siDivisor,newValue.scale() + siDivisor.precision() + 6,BigDecimal.ROUND_HALF_UP);
		newValue = newValue.add(siDelta);
		return newValue.stripTrailingZeros();		
	}
	
	public static void main(String[] args) {
		List<Unit> units = new ArrayList<>(Unit.available());	
		System.out.println(units.size());
		Set<Unit> printed = new HashSet<>();
		for (int i = 0 ; i < units.size(); i++) {
			Unit each = units.get(i);
			if (!printed.contains(each)) {
				System.out.println(each.getName() + ": " + each.getSymbol() + " (" + each.getSymbol(true) + ") SI: " + each.isSICompliant());
				for (int j = i+1 ; j < units.size(); j++) {
					Unit other = units.get(j);
					if (other.hasSameDimensions(each)) {
						printed.add(other);
						System.out.println("\t" + other.getName() + ": " + other.getSymbol() + " (" + other.getSymbol(true) + ")SI: " + other.isSICompliant());
					}
				}
			}
		}
	}
	
}
