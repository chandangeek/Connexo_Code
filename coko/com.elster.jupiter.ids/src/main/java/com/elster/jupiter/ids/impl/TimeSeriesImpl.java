package com.elster.jupiter.ids.impl;

import static com.elster.jupiter.ids.IntervalLengthUnit.MINUTE;
import static com.elster.jupiter.ids.IntervalLengthUnit.MONTH;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import com.elster.jupiter.ids.IntervalLengthUnit;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public final class TimeSeriesImpl implements TimeSeries {

    private static final int MINUTES_PER_HOUR = 60;
    // persistent fields
	private long id;
	private UtcInstant firstTime;
	private UtcInstant lastTime;
	private UtcInstant lockTime;
	private String timeZoneName;
	private boolean regular;
	private int intervalLength;
	private IntervalLengthUnit intervalLengthUnit;
	private int offset;
	private long version;
	private UtcInstant createTime;
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	// association
	private Reference<Vault> vault = ValueReference.absent();
	private Reference<RecordSpec> recordSpec = ValueReference.absent();
	
	// cached values
	private TimeZone timeZone;	
	
	private final DataModel dataModel;
	
    @Inject
	TimeSeriesImpl(DataModel dataModel) {
    	this.dataModel = dataModel;
	}

	TimeSeriesImpl init(Vault vault , RecordSpec recordSpec, TimeZone timeZone) {
		this.vault.set(Objects.requireNonNull(vault));
		this.recordSpec.set(Objects.requireNonNull(recordSpec));
		this.timeZone = timeZone;
		this.timeZoneName = timeZone.getID();
		this.regular = false;
		return this;
	}

    TimeSeriesImpl init(Vault vault , RecordSpec recordSpec, TimeZone timeZone, int intervalLength, IntervalLengthUnit intervalLengthUnit, int offsetInHours) {
		init(vault,recordSpec,timeZone);
        this.regular = true;
        validate(intervalLength, intervalLengthUnit);
		this.intervalLength = intervalLength;
		this.intervalLengthUnit = intervalLengthUnit;
		this.offset = offsetInHours;
		return this;
	}
    
    private void validate(int intervalLength, IntervalLengthUnit intervalLengthUnit) {
        if (IntervalLengthUnit.MINUTE.equals(intervalLengthUnit)) {
            if (MINUTES_PER_HOUR % intervalLength != 0) {
                throw new IllegalArgumentException("Only minute interval lengths that are divisors of one hour are supported.");
            }
        } else if (intervalLength != 1) {
            throw new IllegalArgumentException("For Day and Month only 1 as length is supported.");
        }
    }

    @Override
	public long getId() {
		return id;
	}

	@Override
	public Date getFirstDateTime() {
		return firstTime == null ? null : firstTime.toDate();
	}

	@Override
	public Date getLastDateTime() {
		return lastTime == null ? null : lastTime.toDate();
	}

	@Override
	public Date getLockDateTime() {
		return lockTime == null ? null : lockTime.toDate();
	}

	@Override
	public TimeZone getTimeZone() {		
		if (timeZone == null) {
			// TODO may need to optimized as TimeZone.getTimeZone is probably the slowest method in the JDK
			timeZone = TimeZone.getTimeZone(timeZoneName);
		}
		return timeZone;
	}

	@Override
	public boolean isRegular() {
		return regular;
	}

	@Override
	public int getIntervalLength() {
		return intervalLength;
	}

	@Override
	public IntervalLengthUnit getIntervalLengthUnit() {
		return intervalLengthUnit;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public Vault getVault() {
		return vault.get();
	}
	
	@Override
	public RecordSpecImpl getRecordSpec() {
		return (RecordSpecImpl) recordSpec.get();
	}
	
	@Override
	public String toString() {
		return "TimeSeries " + id + " (version: " + version + " created: " + getCreateDate() + " modified: " + getModDate() + ")" ;
	}

	public Date getCreateDate() {
		return createTime.toDate();
	}
	
	public Date getModDate() {
		return modTime.toDate();
	}

	void persist() {
		dataModel.persist(this);		
	}
	
	@Override
	public boolean add(Date dateTime, boolean overrule, Object... values) {
		if (!isValid(dateTime)) {
			throw new IllegalArgumentException();
		}
		boolean result = ((VaultImpl) getVault()).add(this,dateTime,overrule,values);
		if (result) {
			updateRange(dateTime,dateTime);
		}
		return result;
	}
	
	void updateRange(Date minDate , Date maxDate) {
		List<String> updateAspects = new ArrayList<>();
		if (minDate != null) {
			if (this.firstTime == null || firstTime.after(minDate)) {
				firstTime = new UtcInstant(minDate);
				updateAspects.add("firstTime");
			}
		}
		if (maxDate != null) {		
			if (this.lastTime ==  null || lastTime.before(maxDate)) {
				lastTime = new UtcInstant(maxDate);
				updateAspects.add("lastTime");
			}
		}
		dataModel.update(this,updateAspects.toArray(new String[updateAspects.size()]));
	}
	
	Calendar getStartCalendar(Date date) {
		Calendar result = Calendar.getInstance(getTimeZone());
		result.setTime(date);
		result.add(getIntervalLengthUnit().getCalendarCode(), -getIntervalLength());
		if (getOffset() != 0) {
			// use set instead of add, because calendar behavior is unexpected for adding hours to midnight on a DST transition day.
			result.set(Calendar.HOUR_OF_DAY,offset);
		}		
		return result;
	}
	
	
	@Override
	public boolean isValidDateTime(Date date) {
		return getVault().isValidDateTime(date) && isValid(date);
	}
	
	private boolean isValid(Date date) {
		if (lockTime != null &&  lockTime.afterOrEqual(date)) {
			return false;
		}
		if (!isRegular()) {
			return true;
		}
        DateTime dateTime = new DateTime(date, DateTimeZone.forTimeZone(getTimeZone()));
        if (getIntervalLengthUnit() == MINUTE) {
            return dateTime.getMinuteOfHour() % getIntervalLength() == 0
                    && dateTime.getSecondOfMinute() == 0
                    && dateTime.getMillisOfSecond() == 0;
		}
        if (!validTimeOfDay(dateTime)) {
            return false;
        }
        return !MONTH.equals(intervalLengthUnit) || dateTime.getDayOfMonth() == 1;
    }

    private boolean validTimeOfDay(DateTime dateTime) {
        return millisOfHour(dateTime) == 0 && dateTime.getHourOfDay() == getOffset();
    }

    private int millisOfHour(DateTime dateTime) {
        return dateTime.getMillisOfDay() % DateTimeConstants.MILLIS_PER_HOUR;
    }

    @Override
	public List<TimeSeriesEntry> getEntries(Interval interval) {
		return ImmutableList.copyOf(((VaultImpl) getVault()).getEntries(this, interval));
	}
	
    
    @Override 
    public Optional<TimeSeriesEntry> getEntry(Date when) {
    	return ((VaultImpl) getVault()).getEntry(this,when);
    }
    
    TimeSeriesImpl lock() {
		return dataModel.mapper(TimeSeriesImpl.class).lock(getId());
	}
	
	@Override
	public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TimeSeriesImpl)) {
            return false;
        }
        return this.id == ((TimeSeriesImpl) other).id;
	}
	
	@Override
	public int hashCode() {
		return new Long(this.id).hashCode();
	}
	
	Date next(Date date , int numberOfEntries) {
		if (!isRegular()) {
			throw new UnsupportedOperationException();
		}
		if (!isValid(date)) {
			throw new IllegalArgumentException();
		}
		if (getIntervalLengthUnit() == IntervalLengthUnit.MINUTE) {
			return new Date(date.getTime() + numberOfEntries * getIntervalLength() * 60000L);
		}
		Calendar cal = Calendar.getInstance(getTimeZone());
		cal.setTime(date);
		cal.add(getIntervalLengthUnit().getCalendarCode(), numberOfEntries * getIntervalLength());
		return cal.getTime();
	}
}
