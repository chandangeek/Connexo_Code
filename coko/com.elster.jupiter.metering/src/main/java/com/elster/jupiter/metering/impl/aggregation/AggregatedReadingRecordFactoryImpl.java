package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

/**
 * Provides an implementation for the {@link AggregatedReadingRecordFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-26 (13:12)
 */
public class AggregatedReadingRecordFactoryImpl implements AggregatedReadingRecordFactory {

    private final DataModel dataModel;
    private final Map<Instant, AggregatedReadingRecord> records = new TreeMap<>();    // Keep the keys sorted

    @Inject
    public AggregatedReadingRecordFactoryImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public List<AggregatedReadingRecord> consume(ResultSet resultSet) {
        try {
            while (resultSet.next()) {
                this.records.compute(
                        Instant.ofEpochMilli(resultSet.getLong(4)),
                        this.createOrUpdate(resultSet));
            }
            return new ArrayList<>(this.records.values());
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private BiFunction<? super Instant, ? super AggregatedReadingRecord, ? extends AggregatedReadingRecord> createOrUpdate(final ResultSet resultSet) {
        return (timestamp, readingRecord) -> readingRecord == null ? this.createFrom(resultSet) : readingRecord.addFrom(resultSet);
    }

    private AggregatedReadingRecord createFrom(ResultSet resultSet) {
        return this.dataModel.getInstance(AggregatedReadingRecord.class).init(resultSet);
    }

}