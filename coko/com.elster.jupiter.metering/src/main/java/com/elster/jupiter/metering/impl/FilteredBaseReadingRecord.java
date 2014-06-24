package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ProcesStatus;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.collections.KPermutation;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Decorates an IntervalReading by selecting only certain values in a possibly different order.
 */
public class FilteredBaseReadingRecord implements BaseReadingRecord {

    private final BaseReadingRecordImpl filtered;
    private final KPermutation view;

    FilteredBaseReadingRecord(BaseReadingRecordImpl filtered, int... indices) {
        this.filtered = filtered;
        view = new KPermutation(indices);
    }

    @Override
    public ProcesStatus getProcesStatus() {
        return filtered.getProcesStatus();
    }

    @Override
    public void setProcessingFlags(ProcesStatus.Flag... flags) {
        this.filtered.setProcessingFlags(flags);
    }

    @Override
    public ReadingType getReadingType() {
        return filtered.getReadingType();
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return view.perform(filtered.getReadingTypes()).get(offset);
    }

    @Override
    public List<ReadingTypeImpl> getReadingTypes() {
        return view.perform(filtered.getReadingTypes());
    }

    @Override
    public Date getReportedDateTime() {
        return filtered.getReportedDateTime();
    }

    @Override
    public Date getTimeStamp() {
        return filtered.getTimeStamp();
    }

    @Override
    public BigDecimal getValue() {
        return filtered.getValue();
    }

    @Override
    public Quantity getQuantity(int offset) {
        return view.perform(filtered.getQuantities()).get(offset);
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return filtered.getQuantity(readingType);
    }

    @Override
    public List<Quantity> getQuantities() {
        return view.perform(filtered.getQuantities());
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return null;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public Interval getTimePeriod() {
        return null;
    }

}
