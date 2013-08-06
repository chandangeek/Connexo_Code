package com.elster.jupiter.metering.rest;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.DataQualifier;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementCategory;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cbo.UnitOfMeasureCategory;
import com.elster.jupiter.util.time.UtcInstant;

public class ReadingTypeInfo {

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

}
