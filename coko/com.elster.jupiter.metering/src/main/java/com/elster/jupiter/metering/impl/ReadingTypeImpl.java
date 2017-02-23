/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.IllegalEnumValueException;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.IllegalMRIDFormatException;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Currency;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.HolderBuilder.first;

public final class ReadingTypeImpl implements PersistenceAware, IReadingType {

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

    static enum Fields {
        mRID, aliasName, description, version, createTime, modTime, userName, equidistant
    }

    // persistent fields
	private String mRID;
	private String aliasName;
	private String fullAliasName;
    private String description;
    private boolean active;
	private long version;
	@SuppressWarnings("unused")
	private Instant createTime;
	@SuppressWarnings("unused")
	private Instant modTime;
	@SuppressWarnings("unused")
	private String userName;
    private boolean equidistant;

	// transient fields
	private MacroPeriod macroPeriod;
	private Aggregate aggregate;
	private TimeAttribute measuringPeriod;
	private Accumulation accumulation;
	private FlowDirection flowDirection;
	private com.elster.jupiter.cbo.Commodity commodity;
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

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
	private transient Optional<ReadingType> calculatedReadingType = Optional.empty();

    @Inject
	ReadingTypeImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

	ReadingTypeImpl init(String mRID, String aliasName) {
		this.mRID = mRID;
		this.aliasName = aliasName;
		this.active = true;
		setTransientFields();
		buildFullAliasName();
        return this;
	}

	static Currency getCurrency(int isoCode, Thesaurus thesaurus) {
		if (isoCode == 0) {
			isoCode = 999;
		}
		for (Currency each : Currency.getAvailableCurrencies()) {
			if (each.getNumericCode() == isoCode) {
				return each;
			}
		}
		throw new IllegalCurrencyCodeException(thesaurus, isoCode);
	}

	static int getCurrencyId(Currency currency) {
		int result = currency.getNumericCode();
		return result == 999 ? 0 : result;
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
			throw new IllegalMRIDFormatException(thesaurus, mRID);
		}
        try {
            macroPeriod = MacroPeriod.get(parse(parts[MACRO_PERIOD]));
            aggregate = Aggregate.get(parse(parts[AGGREGATE]));
            measuringPeriod = TimeAttribute.get(parse(parts[MEASURING_PERIOD]));
            accumulation = Accumulation.get(parse(parts[ACCUMULATION]));
            flowDirection = FlowDirection.get(parse(parts[FLOW_DIRECTION]));
            commodity = com.elster.jupiter.cbo.Commodity.get(parse(parts[COMMODITY]));
            measurementKind = MeasurementKind.get(parse(parts[MEASUREMENT_KIND]));
            interharmonic = asRational(parts,INTERHARMONIC_NUMERATOR,INTERHARMONIC_DENOMINATOR);
            argument = asRational(parts,ARGUMENT_NUMERATOR,ARGUMENT_DENOMINATOR);
            tou = parse(parts[TOU]);
            cpp = parse(parts[CPP]);
            consumptionTier = parse(parts[CONSUMPTION_TIER]);
            phases = Phase.get(parse(parts[PHASES]));
            multiplier = MetricMultiplier.with(parse(parts[MULTIPLIER]));
            unit = ReadingTypeUnit.get(parse(parts[UNIT]));
            currency = getCurrency(parse(parts[CURRENCY]), thesaurus);
		} catch (IllegalEnumValueException | IllegalCurrencyCodeException | NumberFormatException e) {
			throw new IllegalMRIDFormatException(mRID, e, thesaurus);
        }
        equidistant = getIntervalLength().isPresent();
    }

	private int parse(String intString) {
		return Integer.parseInt(intString);
	}

	private RationalNumber asRational(String[] parts , int numeratorOffset , int denominatorOffset) {
		int numerator = parse(parts[numeratorOffset]);
		int denominator = parse(parts[denominatorOffset]);
		if (numerator == 0 && denominator == 0) {
			return RationalNumber.NOTAPPLICABLE;
		} else {
			return new RationalNumber(numerator, denominator);
		}
	}

	public String getName() {
		StringBuilder builder = new StringBuilder();
		Holder<String> connector = first("").andThen(" ");
		if (macroPeriod.isApplicable()) {
			builder.append(connector.get()).append(macroPeriod.getDescription());
		}
		if (aggregate.isApplicable()) {
			builder.append(connector.get()).append(aggregate.getDescription());
		}
		if (measuringPeriod.isApplicable()) {
			builder.append(connector.get()).append(measuringPeriod.getDescription());
		}
		if (accumulation.isApplicable()) {
			builder.append(connector.get()).append(accumulation.getDescription());
		}
		if (flowDirection.isApplicable()) {
			builder.append(connector.get()).append(flowDirection.getDescription());
		}
		if (commodity.isApplicable()) {
			builder.append(connector.get()).append(commodity.getDescription());
		}
		if (measurementKind.isApplicable()) {
			builder.append(connector.get()).append(measurementKind.getDescription());
		}
		if (interharmonic.getDenominator() != 0) {
			builder.append(connector.get()).append("Interharmonic: ").append(interharmonic);
		}
		if (argument.getDenominator() != 0) {
			builder.append(connector.get()).append("Argument: ").append(argument);
		}
		if (tou > 0) {
			builder.append(connector.get()).append("Tou: ").append(tou);
		}
		if (cpp > 0) {
			builder.append(connector.get()).append("Cpp: ").append(cpp);
		}
		if (consumptionTier > 0) {
			builder.append(connector.get()).append("Consumption Tier: ").append(consumptionTier);
		}
		if (phases.isApplicable()) {
			builder.append(connector.get()).append(phases.getDescription());
		}
		if (unit.isApplicable()) {
			builder.append(connector.get()).append('(');
			builder.append(multiplier.getSymbol());
			builder.append(unit.getSymbol());
			builder.append(')');
		}
		if (getCurrencyId(currency) != 0) {
            builder.append(connector.get());
			builder.append('(');
			builder.append(currency.getCurrencyCode());
			builder.append(')');
		}
		return builder.toString();
	}

	@Override
	public ReadingTypeCodeBuilder builder() {
		return
			ReadingTypeCodeBuilder.of(commodity)
				.period(macroPeriod)
				.aggregate(aggregate)
				.period(measuringPeriod)
				.accumulate(accumulation)
				.flow(flowDirection)
				.measure(measurementKind)
				.harmonic((int) interharmonic.getNumerator(), (int) interharmonic.getDenominator())
				.argument((int) argument.getNumerator(), (int) argument.getDenominator())
				.tou(tou)
				.cpp(cpp)
				.tier(consumptionTier)
				.phase(phases)
				.in(multiplier,unit)
				.currency(currency);
	}

	@Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public void persist() {
        dataModel.mapper(ReadingType.class).persist(this);
	}

	@Override
	public Optional<TemporalAmount> getIntervalLength() {
        switch (getMacroPeriod()) {
	        case YEARLY:
                return Optional.of(Period.ofYears(1));
            case MONTHLY:
                return Optional.of(Period.ofMonths(1));
            case DAILY:
                return Optional.of(Period.ofDays(1));
            default:
        }
        if (getMeasuringPeriod() == TimeAttribute.HOUR24) {
            return Optional.of(Period.ofDays(1));
        }
        int minutes = getMeasuringPeriod().getMinutes();
        return minutes == 0 ? Optional.empty() : Optional.of(Duration.ofMinutes(minutes));
    }

	boolean hasMacroPeriod() {
		return !macroPeriod.equals(MacroPeriod.NOTAPPLICABLE);
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
    	return equidistant;
    }

	@Override
	public Optional<ReadingType> getCalculatedReadingType() {
		if (isCumulative() && !calculatedReadingType.isPresent()) {
			ReadingTypeCodeBuilder builder = this.builder();
			builder.accumulate(Accumulation.DELTADELTA);
			calculatedReadingType = dataModel.mapper(ReadingType.class).getOptional(builder.code());
		}
		return calculatedReadingType;
	}

    @Override
    public Optional<ReadingType> getBulkReadingType() {
        if (!isCumulative()){
            ReadingTypeCodeBuilder builder = this.builder();
            builder.accumulate(Accumulation.BULKQUANTITY);
            return dataModel.mapper(ReadingType.class).getOptional(builder.code());
        }
        return Optional.empty();
    }

	@Override
	public boolean isBulkQuantityReadingType(ReadingType readingType) {
		ReadingTypeImpl other = (ReadingTypeImpl) readingType;
		return
			this.macroPeriod.equals(other.macroPeriod) &&
			this.aggregate.equals(other.aggregate) &&
			this.measuringPeriod.equals(other.measuringPeriod) &&
			this.accumulation.equals(Accumulation.DELTADELTA) &&
			(other.accumulation.isCumulative()) &
			this.flowDirection.equals(other.flowDirection) &&
			this.commodity.equals(other.commodity) &&
			this.measurementKind.equals(other.measurementKind) &&
			this.interharmonic.equals(other.interharmonic) &&
			this.argument.equals(other.argument) &&
			this.tou == other.tou &&
			this.cpp == other.cpp &&
			this.consumptionTier == other.consumptionTier &&
			this.phases.equals(other.phases) &&
			this.multiplier.equals(other.multiplier) &&
			this.unit.equals(other.unit) &&
			this.currency.equals(other.currency);
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
    public com.elster.jupiter.cbo.Commodity getCommodity() {
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

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void activate() {
        this.active = true;
    }

    @Override
    public void deactivate() {
        this.active = false;
    }

    @Override
    public void update() {
        this.buildFullAliasName();
        dataModel.mapper(ReadingType.class).update(this);
    }

    static TimeAttribute extractTimeAttribute(String mRID) {
		String[] parts = mRID.split("\\.");
		if (parts.length != MRID_FIELD_COUNT) {
			throw new IllegalArgumentException();
		}
		return TimeAttribute.values()[Integer.parseInt(parts[2])];
	}

	@Override
    public Quantity toQuantity(BigDecimal value) {
    	if (value == null) {
    		return null;
    	} else {
    		return this.unit.getUnit().amount(value,this.multiplier.getMultiplier());
    	}
    }

    private void buildFullAliasName() {
		List<String> fullAliasNameElements = new ArrayList<>();
		if (!this.getMeasuringPeriod().equals(TimeAttribute.NOTAPPLICABLE)) {
			fullAliasNameElements.add(ReadingTypeTranslationKeys.MeasuringPeriod.getFullAliasNameElement(this.getMeasuringPeriod(), this.thesaurus));
		} else if (this.recurringMacroPeriods().contains(this.getMacroPeriod())) {
			fullAliasNameElements.add(ReadingTypeTranslationKeys.MacroPeriod.getFullAliasNameElement(this.getMacroPeriod(), this.thesaurus));
		}
		fullAliasNameElements.add(ReadingTypeTranslationKeys.Commodity.getFullAliasNameElement(this.getCommodity(), this.thesaurus));
		fullAliasNameElements.add(ReadingTypeTranslationKeys.Accumulation.getFullAliasNameElement(this.getAccumulation(), this.thesaurus));
		fullAliasNameElements.add(this.getAliasName());
		if (this.getUnit().isApplicable()) {
			fullAliasNameElements.add(ReadingTypeTranslationKeys.UnitWithMultiplier.getFullAliasNameElement(this.getMultiplier(), this.getUnit(), this.thesaurus));
		}
		if (this.getPhases().isApplicable()) {
			fullAliasNameElements.add(ReadingTypeTranslationKeys.Phase.getFullAliasNameElement(this.getPhases(), this.thesaurus));
		}
		if (this.getTou() != 0) {
			fullAliasNameElements.add(ReadingTypeTranslationKeys.TimeOfUse.getFullAliasNameElement(this.getTou(), this.thesaurus));
		}
		fullAliasName = fullAliasNameElements.stream().filter(s -> !Checks.is(s).emptyOrOnlyWhiteSpace()).collect(Collectors.joining(" "));
	}

	private Set<MacroPeriod> recurringMacroPeriods() {
        return EnumSet.of(MacroPeriod.DAILY, MacroPeriod.MONTHLY, MacroPeriod.YEARLY);
    }

    @Override
    public String getFullAliasName() {
	    if (this.fullAliasName == null) {
		    this.buildFullAliasName();
	    }
	    return fullAliasName;
    }

	@Override
	public String toString() {
		return getMRID();
	}
}
