/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.ReadingType;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * Provides factory services for {@link CalculatedReadingRecord}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-26 (13:05)
 */
public interface CalculatedReadingRecordFactory {

    /**
     * Consumes all the data in the ResultSet, converting to {@link CalculatedReadingRecord}s
     * that are organized by {@link ReadingType} to support ResultSets that contain data
     * for different {@link ReadingType}s.
     * Any SQLException thrown while reading from ResultSet will be wrapped
     * and rethrown as an UnderlyingSQLFailedException.
     *
     * @param resultSet The ResultSet
     * @return The List of CalculatedReadingRecord organized by ReadingType
     */
    Map<ReadingType, List<CalculatedReadingRecord>> consume(ResultSet resultSet, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation);

}