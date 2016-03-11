package com.elster.jupiter.metering.impl.aggregation;

import java.sql.ResultSet;
import java.util.List;

/**
 * Provides factory services for {@link AggregatedReadingRecord}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-26 (13:05)
 */
public interface AggregatedReadingRecordFactory {

    /**
     * Consumes all the data in the ResultSet, converting to {@link AggregatedReadingRecord}s.
     * When the ResultSet contains data for different {@link com.elster.jupiter.metering.ReadingType}s
     * then all interval data for the same timestamp are grouped together in a single AggregatedReadingRecord.
     * Any SQLException thrown while reading from ResultSet will be wrapped
     * and rethrown as an UnderlyingSQLFailedException.
     *
     * @param resultSet The ResultSet
     * @return The List of AggregatedReadingRecord
     * @see com.elster.jupiter.metering.BaseReadingRecord#getQuantity(com.elster.jupiter.metering.ReadingType)
     */
    List<AggregatedReadingRecord> consume(ResultSet resultSet);

}