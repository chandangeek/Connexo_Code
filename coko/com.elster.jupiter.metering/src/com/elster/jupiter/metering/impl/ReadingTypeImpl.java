package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.UtcInstant;
import com.elster.jupiter.cbo.*;

public class ReadingTypeImpl implements ReadingType , PersistenceAware {
	// persistent fields
	private String mRID;
	private String aliasName;
	@SuppressWarnings("unused")
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
		if (parts.length != 11) {
			throw new IllegalArgumentException(mRID);
		}
		timeAttribute = TimeAttribute.get(parse(parts[0]));
		dataQualifier = DataQualifier.get(parse(parts[1]));
		accumulation = Accumulation.get(parse(parts[2]));
		flowDirection = FlowDirection.get(parse(parts[3]));
		unitOfMeasureCategory = UnitOfMeasureCategory.get(parse(parts[4]),parse(parts[5]));
		measurementCategory = MeasurementCategory.get(parse(parts[6]),parse(parts[7]));
		phase = Phase.get(parse(parts[8]));
		metricMultiplier = MetricMultiplier.get(parse(parts[9]));
		baseUnit = ReadingTypeUnit.get(parse(parts[10]));
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
	
	public void persist() {
		Bus.getOrmClient().getReadingTypeFactory().persist(this);
	}

	IntervalLength getIntervalLength() {
		return IntervalLength.forCimCode(timeAttribute.getId());
	}
	
	@Override
	public boolean equals(Object other) {
		if  (other instanceof ReadingTypeImpl) {
			ReadingTypeImpl o = (ReadingTypeImpl) other;
			return this.mRID.equals(o.mRID);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
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
}
