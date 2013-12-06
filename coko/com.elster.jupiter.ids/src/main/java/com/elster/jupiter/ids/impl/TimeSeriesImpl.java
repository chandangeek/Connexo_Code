package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IntervalLengthUnit;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.ids.plumbing.Bus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.elster.jupiter.ids.IntervalLengthUnit.MINUTE;
import static com.elster.jupiter.ids.IntervalLengthUnit.MONTH;

public final class TimeSeriesImpl implements TimeSeries {

    private static final int MINUTES_PER_HOUR = 60;
    // persistent fields
	private long id;
	private String vaultComponentName;
	private long vaultId;
	private String recordSpecComponentName;
	private long recordSpecId;
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
	private Vault vault;
	private RecordSpec recordSpec;
	
	// cached values
	private TimeZone timeZone;	
	
	@SuppressWarnings("unused")
	private TimeSeriesImpl() {		
	}

	TimeSeriesImpl(Vault vault , RecordSpec recordSpec, TimeZone timeZone) {
        validate(vault, recordSpec);
		this.vault = vault;
		this.vaultComponentName = vault.getComponentName();
		this.vaultId = vault.getId();
		this.recordSpec = recordSpec;
		this.recordSpecComponentName = recordSpec.getComponentName();
		this.recordSpecId = recordSpec.getId();
		this.timeZone = timeZone;
		this.timeZoneName = timeZone.getID();
		this.regular = false;		
	}

    private void validate(Vault vault, RecordSpec recordSpec) {
        if (vault == null) {
            throw new IllegalArgumentException("Vault cannot be null.");
        }
        if (recordSpec == null) {
            throw new IllegalArgumentException("RecordSpec cannot be null.");
        }
    }

    TimeSeriesImpl(Vault vault , RecordSpec recordSpec, TimeZone timeZone, int intervalLength, IntervalLengthUnit intervalLengthUnit, int offsetInHours) {
		this(vault,recordSpec,timeZone);
        validate(vault, recordSpec);
		this.regular = true;
        validate(intervalLength, intervalLengthUnit);
		this.intervalLength = intervalLength;
		this.intervalLengthUnit = intervalLengthUnit;
		this.offset = offsetInHours;
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
		if (vault == null) {
			vault = Bus.getOrmClient().getVaultFactory().getExisting(vaultComponentName, vaultId);
		}
		return vault;
	}
	
	@Override
	public RecordSpec getRecordSpec() {
		if (recordSpec == null) {
			recordSpec = Bus.getOrmClient().getRecordSpecFactory().getExisting(recordSpecComponentName, recordSpecId);
		}
		return recordSpec;
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
		getFactory().persist(this);		
	}
	
	private DataMapper<TimeSeries> getFactory() {
		return Bus.getOrmClient().getTimeSeriesFactory();
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
		getFactory().update(this,updateAspects.toArray(new String[updateAspects.size()]));
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
    
    TimeSeries lock() {
		return getFactory().lock(getId());
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
}
