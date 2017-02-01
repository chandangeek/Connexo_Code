/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.impl.IReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.Pair;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link CalculatedReadingRecordFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-26 (13:12)
 */
public class CalculatedReadingRecordFactoryImpl implements CalculatedReadingRecordFactory {

    private final DataModel dataModel;
    private final MeteringService meteringService;
    private final Map<String, List<CalculatedReadingRecord>> records = new HashMap<>();

    @Inject
    public CalculatedReadingRecordFactoryImpl(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    @Override
    public Map<ReadingType, List<CalculatedReadingRecord>> consume(ResultSet resultSet, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
        try {
            while (resultSet.next()) {
                String mRID = resultSet.getString(1);
                this.records.compute(mRID, this.createOrUpdate(resultSet, deliverablesPerMeterActivation));
            }
            return this.records
                    .entrySet()
                    .stream()
                    .map(this::toPair)
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Pair<ReadingType, List<CalculatedReadingRecord>> toPair(Map.Entry<String, List<CalculatedReadingRecord>> entry) {
        IReadingType readingType = this.findReadingType(entry.getKey());
        entry.getValue().forEach(record -> record.setReadingType(readingType));
        return Pair.of(readingType, entry.getValue());
    }

    private IReadingType findReadingType(String mRID) {
        return (IReadingType) meteringService.getReadingType(mRID).get();
    }

    private BiFunction<? super String, ? super List<CalculatedReadingRecord>, ? extends List<CalculatedReadingRecord>> createOrUpdate(ResultSet resultSet, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
        return (mRID, readingRecords) -> readingRecords == null ? this.newListFrom(resultSet, deliverablesPerMeterActivation) : this.addToList(resultSet, readingRecords, deliverablesPerMeterActivation);
    }

    private List<CalculatedReadingRecord> newListFrom(ResultSet resultSet, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
        List<CalculatedReadingRecord> records = new ArrayList<>();
        records.add(this.createFrom(resultSet, deliverablesPerMeterActivation));
        return records;
    }

    private List<CalculatedReadingRecord> addToList(ResultSet resultSet, List<CalculatedReadingRecord> readingRecords, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
        readingRecords.add(this.createFrom(resultSet, deliverablesPerMeterActivation));
        return readingRecords;
    }

    private CalculatedReadingRecord createFrom(ResultSet resultSet, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
        return this.dataModel.getInstance(CalculatedReadingRecord.class).init(resultSet, deliverablesPerMeterActivation);
    }

}