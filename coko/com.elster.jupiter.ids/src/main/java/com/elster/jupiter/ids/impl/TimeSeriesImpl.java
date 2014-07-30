package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IntervalLength;
import com.elster.jupiter.ids.IntervalLengthUnit;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import static com.elster.jupiter.ids.IntervalLengthUnit.MINUTE;
import static com.elster.jupiter.ids.IntervalLengthUnit.MONTH;
import static org.joda.time.DateTimeConstants.MILLIS_PER_MINUTE;
import static org.joda.time.DateTimeConstants.MINUTES_PER_HOUR;

public final class TimeSeriesImpl implements TimeSeries {

    // persistent fields
	private long id;
	private UtcInstant firstTime;
	private UtcInstant lastTime;
	private UtcInstant lockTime;
	private String timeZoneName;
	private boolean regular;
	private int intervalLength;
	private IntervalLengthUnit intervalLengthUnit;
    private transient IntervalLength interval;
	private int offset;
	private long version;
	private UtcInstant createTime;
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	// association
	private Reference<VaultImpl> vault = ValueReference.absent();
	private Reference<RecordSpec> recordSpec = ValueReference.absent();
	
	// cached values
	private TimeZone timeZone;	
	
	private final DataModel dataModel;
	
    @Inject
	TimeSeriesImpl(DataModel dataModel) {
    	this.dataModel = dataModel;
	}

	TimeSeriesImpl init(VaultImpl vault , RecordSpec recordSpec, TimeZone timeZone) {
		this.vault.set(Objects.requireNonNull(vault));
		this.recordSpec.set(Objects.requireNonNull(recordSpec));
		this.timeZone = timeZone;
		this.timeZoneName = timeZone.getID();
		this.regular = false;
		return this;
	}

    TimeSeriesImpl init(VaultImpl vault , RecordSpec recordSpec, TimeZone timeZone, IntervalLength intervalLength, int offsetInHours) {
		init(vault,recordSpec,timeZone);
        this.regular = true;
        validate(intervalLength);
        this.interval = intervalLength;
		this.intervalLength = intervalLength.getLength();
		this.intervalLengthUnit = intervalLength.getUnitCode();
		this.offset = offsetInHours;
		return this;
	}
    
    private void validate(IntervalLength intervalLength) {
        if (IntervalLengthUnit.MINUTE.equals(intervalLengthUnit)) {
            if (MINUTES_PER_HOUR % intervalLength.getLength() != 0) {
                throw new IllegalArgumentException("Only minute interval lengths that are divisors of one hour are supported.");
            }
        } else if (intervalLength.getLength() != 1) {
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
	public IntervalLength getIntervalLength() {
        if (interval == null) {
            interval = intervalLengthUnit.withLength(intervalLength);
        }
		return interval;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public VaultImpl getVault() {
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
		boolean result = getVault().add(this,dateTime,overrule,values);
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
		result.add(getIntervalLength().getUnitCode().getCalendarCode(), -getIntervalLength().getLength());
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
        if (getIntervalLength().getUnitCode() == MINUTE) {
            return dateTime.getMinuteOfHour() % getIntervalLength().getLength() == 0
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
		return getVault().getEntries(this, interval);
	}
	
    
    @Override 
    public Optional<TimeSeriesEntry> getEntry(Date when) {
    	return getVault().getEntry(this,when);
    }
    
    TimeSeriesImpl lock() {
        //TODO review with Karel.
        //Would like to have a lock(T) method
        TimeSeriesImpl lock = dataModel.mapper(TimeSeriesImpl.class).lock(getId());
        if (lock.version != this.version) {
            throw new OptimisticLockException();
        }
        return this;
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
		if (getIntervalLength().getUnitCode() == IntervalLengthUnit.MINUTE) {
			return new Date(date.getTime() + numberOfEntries * getIntervalLength().getLength() * MILLIS_PER_MINUTE);
		}
        Calendar cal = Calendar.getInstance(getTimeZone());
		cal.setTime(date);
		cal.add(getIntervalLength().getUnitCode().getCalendarCode(), numberOfEntries * getIntervalLength().getLength());
		return cal.getTime();
	}

	@Override
	public List<TimeSeriesEntry> getEntriesBefore(Date when,int entryCount) {
		return getVault().getEntriesBefore(this,when,entryCount,false);
	}

	@Override
	public List<TimeSeriesEntry> getEntriesOnOrBefore(Date when, int entryCount) {
		return getVault().getEntriesBefore(this,when,entryCount,true);
	}
}
