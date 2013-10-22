package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.cbo.*;

public final class ReadingTypeImpl implements ReadingType , PersistenceAware {

    private static final int MRID_FIELD_COUNT = 11;
    private static final int TIME_INDEX = 0;
    private static final int DATA_QUALIFIER_INDEX = 1;
    private static final int ACCUMULATION_INDEX = 2;
    private static final int FLOW_DIRECTION_INDEX = 3;
    private static final int UNIT_OF_MEASURE_INDEX2 = 5;
    private static final int UNIT_OF_MEASURE_INDEX1 = 4;
    private static final int MEASUREMENT_CATEGORY_INDEX1 = 6;
    private static final int MEASUREMENT_CATEGORY_INDEX2 = 7;
    private static final int PHASE_INDEX = 8;
    private static final int METRIC_MULTIPLIER_INDEX = 9;
    private static final int BASE_UNIT_INDEX = 10;
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
	private TimeAttribute timeAttribute;
	private DataQualifier dataQualifier;
	private Accumulation accumulation;
	private FlowDirection flowDirection;
	private UnitOfMeasureCategory unitOfMeasureCategory;
	private MeasurementCategory measurementCategory;
	private Phase phase;
	private MetricMultiplier metricMultiplier; 
	private ReadingTypeUnit baseUnit;
	
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
		timeAttribute = TimeAttribute.get(parse(parts[TIME_INDEX]));
		dataQualifier = DataQualifier.get(parse(parts[DATA_QUALIFIER_INDEX]));
		accumulation = Accumulation.get(parse(parts[ACCUMULATION_INDEX]));
		flowDirection = FlowDirection.get(parse(parts[FLOW_DIRECTION_INDEX]));
		unitOfMeasureCategory = UnitOfMeasureCategory.get(parse(parts[UNIT_OF_MEASURE_INDEX1]),parse(parts[UNIT_OF_MEASURE_INDEX2]));
		measurementCategory = MeasurementCategory.get(parse(parts[MEASUREMENT_CATEGORY_INDEX1]),parse(parts[MEASUREMENT_CATEGORY_INDEX2]));
		phase = Phase.get(parse(parts[PHASE_INDEX]));
		metricMultiplier = MetricMultiplier.get(parse(parts[METRIC_MULTIPLIER_INDEX]));
		baseUnit = ReadingTypeUnit.get(parse(parts[BASE_UNIT_INDEX]));
	}
		
	private int parse(String intString) {
		return Integer.parseInt(intString);
	}
	
	public String getName() {
		StringBuilder builder = new StringBuilder();
		String connector = "";
		if (timeAttribute.isApplicable()) {
			builder.append(connector);
			builder.append(timeAttribute.getDescription());
			connector = " ";
		}
		if (dataQualifier.isApplicable()) {
			builder.append(connector);
			builder.append(dataQualifier.getDescription());
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
		if (unitOfMeasureCategory.isApplicable()) {
			builder.append(connector);
			builder.append(unitOfMeasureCategory.getDescription());
			connector = " ";
		}
		if (measurementCategory.isApplicable()) {
			builder.append(connector);
			builder.append(measurementCategory.getDescription());
			connector = " ";
		}
		if (phase.isApplicable()) {
			builder.append(connector);
			builder.append(phase.getDescription());
			connector = " ";
		}
		if (baseUnit.isApplicable()) {
			builder.append(" (");
			builder.append(metricMultiplier.getSymbol());
			builder.append(baseUnit.getSymbol());
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

	IntervalLength getIntervalLength() {
		return IntervalLength.forCimCode(timeAttribute.getId());
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
	public boolean isCumulativeReadingType(ReadingType readingType) {
		ReadingTypeImpl other = (ReadingTypeImpl) readingType;
		return 
			this.timeAttribute == other.timeAttribute &&			
			this.accumulation == Accumulation.INTERVALDATA &&
			other.accumulation == Accumulation.CUMULATIVE;
	}

    @Override
    public long getVersion() {
        return version;
    }
}
