package com.elster.jupiter.metering.impl;

import java.util.Currency;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.cbo.*;
import com.google.common.base.Optional;

public final class ReadingTypeImpl implements ReadingType , PersistenceAware {

    private static final int MRID_FIELD_COUNT = 18;
    private static final int MACRO_PERIOD = 0;
    private static final int AGGREGATE = 1;
    private static final int MEASURING_PERIOD = 2;
    private static final int ACCUMULATION = 3;
    private static final int FLOW_DIRECTION = 4;
    private static final int COMMODITY = 5;
    private static final int MEASUREMENT_KIND = 6;
    private static final int INTERHARMONIC_NUMERATOR = 7;
    private static final int INTERHARMONIC_DENOMINATOR = 8;
    private static final int ARGUMENT_NUMERATOR = 9;
    private static final int ARGUMENT_DENOMINATOR = 10;
    private static final int TOU = 11;
    private static final int CPP = 12;
    private static final int CONSUMPTION_TIER = 13;
    private static final int PHASES = 14;
    private static final int MULTIPLIER = 15;
    private static final int UNIT = 16;
    private static final int CURRENCY = 17;
    
    // persistent fields
	private String mRID;
	private String aliasName;
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	// transient fields
	private MacroPeriod macroPeriod;
	private Aggregate aggregate;
	private TimeAttribute measuringPeriod;
	private Accumulation accumulation;
	private FlowDirection flowDirection;
	private Commodity commodity;
	private MeasurementKind measurementKind;
	private RationalNumber interharmonic;
	private RationalNumber argument;
	private int tou;
	private int cpp;
	private int consumptionTier;
	private Phase phases;
	private MetricMultiplier multiplier;
	private ReadingTypeUnit unit;
	private Currency currency;
	
	@SuppressWarnings("unused")
	private ReadingTypeImpl() {		
	}
	
	public ReadingTypeImpl(String mRID, String aliasName) {
		this.mRID = mRID;
		this.aliasName = aliasName;
		setTransientFields();
	}
	
	@Override 
	public void postLoad() {
		setTransientFields();
	}
	
	@Override
	public String getMRID() {
		return mRID;
	}
	
	@Override
	public String getAliasName() {
		return aliasName;
	}
	
	private void setTransientFields() {
		String[] parts = mRID.split("\\.");
		if (parts.length != MRID_FIELD_COUNT) {
			throw new IllegalArgumentException(mRID);
		}
		macroPeriod = MacroPeriod.get(parse(parts[MACRO_PERIOD]));
		aggregate = Aggregate.get(parse(parts[AGGREGATE]));
		measuringPeriod = TimeAttribute.get(parse(parts[MEASURING_PERIOD]));
		accumulation = Accumulation.get(parse(parts[ACCUMULATION]));
		flowDirection = FlowDirection.get(parse(parts[FLOW_DIRECTION]));
		commodity = Commodity.get(parse(parts[COMMODITY]));
		measurementKind = MeasurementKind.get(parse(parts[MEASUREMENT_KIND]));
		interharmonic = asRational(parts,INTERHARMONIC_NUMERATOR,INTERHARMONIC_DENOMINATOR);
		argument = asRational(parts,ARGUMENT_NUMERATOR,ARGUMENT_DENOMINATOR);
		tou = parse(parts[TOU]);
		cpp = parse(parts[CPP]);
		consumptionTier = parse(parts[CONSUMPTION_TIER]);
		phases = Phase.get(parse(parts[PHASES]));
		multiplier = MetricMultiplier.get(parse(parts[MULTIPLIER]));
		unit = ReadingTypeUnit.get(parse(parts[UNIT]));
		currency = getCurrency(parse(parts[CURRENCY]));
	}
		
	
	private Currency getCurrency(int numericCode) {
		for (Currency each : Currency.getAvailableCurrencies()) {
			if (each.getNumericCode() == numericCode) {
				return each;
			}
		}
		throw new IllegalArgumentException("Invalid currency code " + numericCode);
	}
	private int parse(String intString) {
		return Integer.parseInt(intString);
	}
	
	private RationalNumber asRational(String[] parts , int numeratorOffset , int denominatorOffset) {
		int numerator = parse(parts[numeratorOffset]);
		int denominator = parse(parts[denominatorOffset]);
		if (numerator == 0 && denominator == 0) {
			return null;
		} else {
			return new RationalNumber(numerator, denominator);
		}
	}
	
	public String getName() {
		StringBuilder builder = new StringBuilder();
		String connector = "";
		if (macroPeriod.isApplicable()) {
			builder.append(connector);
			builder.append(macroPeriod.getDescription());
			connector = " ";
		}
		if (aggregate.isApplicable()) {
			builder.append(connector);
			builder.append(aggregate.getDescription());
			connector = " ";
		}
		if (measuringPeriod.isApplicable()) {
			builder.append(connector);
			builder.append(measuringPeriod.getDescription());
			connector = " ";
		}
		if (accumulation.isApplicable()) {
			builder.append(connector);
			builder.append(accumulation.getDescription());
			connector = " ";
		}
		if (flowDirection.isApplicable()) {
			builder.append(connector);
			builder.append(flowDirection.getDescription());
			connector = " ";
		}
		if (commodity.isApplicable()) {
			builder.append(connector);
			builder.append(commodity.getDescription());
			connector = " ";
		}
		if (measurementKind.isApplicable()) {
			builder.append(connector);
			builder.append(measurementKind.getDescription());
			connector = " ";
		}
		if (interharmonic != null) {
			builder.append(connector);
			builder.append("Interharmonic: ");
			builder.append(interharmonic);
			connector = " ";
		}
		if (argument != null) {
			builder.append(connector);
			builder.append("Argument: ");
			builder.append(argument);
			connector = " ";
		}
		if (tou > 0) {
			builder.append(connector);
			builder.append("Tou: ");
			builder.append(tou);
			connector = " ";
		}
		if (cpp > 0) {
			builder.append(connector);
			builder.append("Cpp: ");
			builder.append(cpp);
				connector = " ";
		}
		if (consumptionTier > 0) {
			builder.append(connector);
			builder.append("Consumption Tier: ");
			builder.append(consumptionTier);
			connector = " ";
		}
		if (phases.isApplicable()) {
			builder.append(connector);
			builder.append(phases.getDescription());
			connector = " ";
		}
		if (unit.isApplicable()) {
			builder.append(" (");
			builder.append(multiplier.getSymbol());
			builder.append(unit.getSymbol());
			builder.append(")");
		}
		if (currency != null) {
			builder.append(" (");
			builder.append(currency.getCurrencyCode());
			builder.append(")");	
		}
		return builder.toString();
	}

    @Override
    public String getDescription() {
        return getName(); // TODO : description should be a field
    }

    public void persist() {
		Bus.getOrmClient().getReadingTypeFactory().persist(this);
	}

	Optional<IntervalLength> getIntervalLength() {
		return IntervalLength.from(this);
	}

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReadingTypeImpl)) {
            return false;
        }

        return mRID.equals(((ReadingTypeImpl) o).mRID);

    }

    @Override
    public final int hashCode() {
        return mRID.hashCode();
    }

    @Override 
    public boolean isRegular() {
    	return getIntervalLength() != null;
    }
    
    @Override
	public boolean isCumulativeReadingType(ReadingType readingType) {
		ReadingTypeImpl other = (ReadingTypeImpl) readingType;
		return 
			this.measuringPeriod == other.measuringPeriod &&			
			this.accumulation == Accumulation.DELTADELTA &&
			other.accumulation == Accumulation.BULKQUANTITY;
	}

    @Override
    public long getVersion() {
        return version;
    }

    @Override 
	public MacroPeriod getMacroPeriod() {
		return macroPeriod;
	}
    
    @Override
	public Aggregate getAggregate() {
		return aggregate;
	}
    
    @Override
	public TimeAttribute getMeasuringPeriod() {
		return measuringPeriod;
	}
    
    @Override
	public Accumulation getAccumulation() {
		return accumulation;
	}

    @Override
	public FlowDirection getFlowDirection() {
		return flowDirection;
	}

    @Override
    public Commodity getCommodity() {
		return commodity;
	}
    
    @Override
	public MeasurementKind getMeasurementKind() {
		return measurementKind;
	}

    @Override
    public RationalNumber getInterharmonic() {
		return interharmonic;
	}
    
    @Override	
    public RationalNumber getArgument() {
		return argument;
	}

    @Override
	public int getTou() {
		return tou;
	}

    @Override
    public int getCpp() {
		return cpp;
	}
    
    @Override
	public int getConsumptionTier() {
		return consumptionTier;
	}

    @Override
    public Phase getPhases() {
		return phases;
	}

    @Override
	public MetricMultiplier getMultiplier() {
		return multiplier;
	}

    @Override
    public ReadingTypeUnit getUnit() {
		return unit;
	}

    @Override
    public Currency getCurrency() {
		return currency;
	}

}
