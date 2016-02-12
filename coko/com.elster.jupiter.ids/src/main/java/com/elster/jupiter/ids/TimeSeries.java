package com.elster.jupiter.ids;

import com.elster.jupiter.util.sql.SqlFragment;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface TimeSeries {
	long getId();
	Instant getFirstDateTime();
	Instant getLastDateTime();
	Instant getLockDateTime();
	boolean isRegular();
	TemporalAmount interval();
	// offset in calendar hours (excluding DST transition hour).
	int getOffset();
	Vault getVault();
	RecordSpec getRecordSpec();
	boolean add(Instant dateTime , boolean overrule , Object... values);
    List<TimeSeriesEntry> getEntries(Range<Instant> interval);

	/**
	 * Returns a SqlFragment that selects the raw data of this TimeSeries
	 * for the specified period in time.
	 * The data returned by the sql is as follows:
	 * <ul>
	 * <li>id of this TimeSeries</li>
	 * <li>the values of the fields with the specified names</li>
	 * <li>the timestamp of the raw data value</li>
	 * <li>the timestamp of the raw data value in ORACLE date format to which trunc function can be applied</li>
	 * </ul>
	 * @param interval The period in time
	 * @return The SqlFragment
     */
	SqlFragment getRawValuesSql(Range<Instant> interval, String... fieldSpecNames);

	List<TimeSeriesEntry> getEntriesUpdatedSince(Range<Instant> interval, Instant since);

	Optional<TimeSeriesEntry> getEntry(Instant when);
    List<TimeSeriesEntry> getEntriesBefore(Instant when,int entryCount);
    List<TimeSeriesEntry> getEntriesOnOrBefore(Instant when, int entryCount);
	boolean isValidInstant(Instant instant);
	ZoneId getZoneId();
	void removeEntries(Range<Instant> range);
	default void removeEntry(Instant instant) {
		removeEntries(Range.closed(instant, instant));
	}
	List<Instant> toList(Range<Instant> range);

    Instant getNextDateTime(Instant instant);
    Instant getPreviousDateTime(Instant instant);
}
