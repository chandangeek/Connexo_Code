/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

import com.elster.jupiter.util.Pair;
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

	List<TimeSeriesJournalEntry> getJournalEntries(Range<Instant> interval, Range<Instant> changed);

	/**
	 * Returns a SqlFragment that selects the requested raw data of this TimeSeries
	 * for the specified period in time.
     * The specification of the fields of interest is done with the {@link Pair} class.
     * The Pair's first value is the name of the field.
     * The Pair's last value is the alias name for that field.
	 *
	 * @param interval The period in time
     * @param fieldSpecAndAliasNames The names of the raw data fields along with an alias name that allows you to refer to the fields in surrounding SQL constructs
	 * @return The SqlFragment
     */
	SqlFragment getRawValuesSql(Range<Instant> interval, Pair<String, String>... fieldSpecAndAliasNames);

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
