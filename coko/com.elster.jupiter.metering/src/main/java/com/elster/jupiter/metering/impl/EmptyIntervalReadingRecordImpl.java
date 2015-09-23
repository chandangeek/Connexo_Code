package com.elster.jupiter.metering.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.util.units.Quantity;
import com.google.common.collect.Range;

public class EmptyIntervalReadingRecordImpl extends IntervalReadingRecordImpl {
	
	private Instant timeStamp;
    private ReadingType readingType;

    EmptyIntervalReadingRecordImpl(ReadingType readingType, Instant timeStamp) {
		super(null,null);
		this.timeStamp=timeStamp;
		this.readingType=readingType;
	}

	@Override
	public ProfileStatus getProfileStatus() {
		return null;//new ProfileStatus(getEntry().getLong(1));
	}

	@Override
	int getReadingTypeOffset() {
		return -1;
	}
	
	@Override
	public IntervalReadingRecord filter(ReadingType readingType) {
		return new FilteredIntervalReadingRecord(this, getIndex(readingType));
	}
	
	@Override
    public Instant getTimeStamp() {
        return timeStamp;
    }

    @Override
    public Instant getReportedDateTime() {
        return null;
    }

    @Override
    public BigDecimal getValue() {
        return null;
    }
    
    @Override
    public List<Quantity> getQuantities() {
        // do not use ImmutableList.builder() as getQuantity(i) can return null;
        List<Quantity> result = new ArrayList<>();
        return result;
    }

    @Override
    public Quantity getQuantity(int offset) {
        return null;
    }
    
    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return null;
    }

    @Override
    public ReadingType getReadingType() {
        return readingType;
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return readingType;
    }

    @Override
    public List<ReadingTypeImpl> getReadingTypes() {
        List<ReadingTypeImpl> ret = new ArrayList<>();
        ret.add((ReadingTypeImpl) readingType);
        return ret;
    }

    @Override
    public ProcessStatus getProcesStatus() {
        return null;
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
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
    public Optional<Range<Instant>> getTimePeriod() {
        return Optional.empty();
    }
    
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        List<? extends ReadingQualityRecord> ret = new ArrayList<>();
        return ret;
    }

}
