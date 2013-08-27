package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IntervalLengthUnit;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.ids.plumbing.Bus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.elster.jupiter.ids.IntervalLengthUnit.DAY;
import static com.elster.jupiter.ids.IntervalLengthUnit.MINUTE;

public class TimeSeriesImpl implements TimeSeries {
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

    TimeSeriesImpl(Vault vault , RecordSpec recordSpec, TimeZone timeZone, int intervalLength, IntervalLengthUnit intervalLengthUnit, int offset) {
		this(vault,recordSpec,timeZone);
        validate(vault, recordSpec);
		this.regular = true;
		this.intervalLength = intervalLength;
		this.intervalLengthUnit = intervalLengthUnit;
		this.offset = offset;
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
			// may need to optimized as TimeZone.getTimeZone is probably the slowest method in the JDK
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
	
	boolean isValid(Date date) {	
		if (lockTime != null &&  lockTime.afterOrEqual(date)) {
			return false;
		}
		if (!isRegular()) {
			return true;
		}
		if (getIntervalLengthUnit() == MINUTE) {
			return date.getTime() % (getIntervalLength() * 60000L) == 0;
		}
		Calendar cal = Calendar.getInstance(getTimeZone());
		cal.setTime(date);
		if (cal.get(Calendar.MILLISECOND) != 0) {
			return false;
		}
		if (cal.get(Calendar.SECOND) != 0) {
			return false;
		}
		if (cal.get(Calendar.MINUTE) != 0) {
			return false;
		}	
		if (cal.get(Calendar.HOUR_OF_DAY) != getOffset()) {
			return false;
		}
		if (getIntervalLengthUnit() == DAY) {
			return true;
		}
		return cal.get(Calendar.DAY_OF_MONTH) == 1;
	}
	
	@Override
	public List<TimeSeriesEntry> getEntries(Date from , Date to) {
		return ImmutableList.copyOf(((VaultImpl) getVault()).getEntries(this, from, to));
	}
	
	TimeSeries lock() {
		return getFactory().lock(getId());
	}
	
	@Override
	public boolean equals(Object other) {
		try {
			TimeSeriesImpl o = (TimeSeriesImpl) other;
			return this.id == o.id;
		} catch (ClassCastException ex) {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return new Long(this.id).hashCode();
	}
}
