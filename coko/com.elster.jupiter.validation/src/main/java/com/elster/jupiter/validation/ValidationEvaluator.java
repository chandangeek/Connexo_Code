/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by tgr on 5/09/2014.
 */
@ProviderType
public interface ValidationEvaluator {

    /**
     * @deprecated use {@link DataValidationStatus#getValidationResult()}
     */
    @Deprecated
    default ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities) {
        return ValidationResult.getValidationResult(qualities);
    }

    boolean isAllDataValidated(ChannelsContainer channelsContainer);

    boolean isAllDataValidated(List<Channel> channels);

    /**
     * Checks if there's at least one suspect put to {@link ChannelsContainer} by one of the {@code qualityCodeSystems}.
     * @param qualityCodeSystems Systems to take into account when checking for suspects; empty set means all systems.
     * @param channelsContainer {@link ChannelsContainer} to check.
     * @return {@code true} if there's at least a suspect, {@code false} otherwise.
     */
    boolean areSuspectsPresent(Set<QualityCodeSystem> qualityCodeSystems, ChannelsContainer channelsContainer);

    /**
     * Gets validation status taking into account qualities of systems among {@code qualityCodeSystems}.
     * @param qualityCodeSystems Only systems to take into account for computation of validation status; empty set means all systems.
     * @param channel The channel to check.
     * @param readings Provided list of readings.
     * @return List of {@link DataValidationStatus}.
     */
    default List<DataValidationStatus> getValidationStatus(Set<QualityCodeSystem> qualityCodeSystems, Channel channel,
                                                           List<? extends BaseReading> readings) {
        return getValidationStatus(qualityCodeSystems, channel, readings, readings.stream()
                .map(BaseReading::getTimeStamp)
                .map(Range::singleton)
                .reduce(Range::span)
                .orElse(null));
    }

    /**
     * Gets validation status taking into account qualities of systems among {@code qualityCodeSystems}.
     * @param qualityCodeSystems Only systems to take into account for computation of validation status; empty set means all systems.
     * @param channel The channel to check.
     * @param readings Provided list of readings.
     * @param interval Specific interval to check.
     * @return List of {@link DataValidationStatus}.
     */
    default List<DataValidationStatus> getValidationStatus(Set<QualityCodeSystem> qualityCodeSystems, Channel channel,
                                                           List<? extends BaseReading> readings, Range<Instant> interval) {
        List<CimChannel> channels = new ArrayList<>(2);
        channel.getCimChannel(channel.getMainReadingType()).ifPresent(channels::add);
        channel.getBulkQuantityReadingType().ifPresent(bulkReadingType -> channel.getCimChannel(bulkReadingType).ifPresent(channels::add));
        return getValidationStatus(qualityCodeSystems, channels, readings, interval);
    }


    /**
     * Gets validation status taking into account qualities of systems among {@code qualityCodeSystems}.
     * @param qualityCodeSystems Only systems to take into account for computation of validation status; empty set means all systems.
     * @param channels A list of one or two (1st main + 2nd bulk) channels. Other cases are not supported by implementation and may lead to unexpected errors!
     * @param readings Provided list of readings.
     * @param interval Specific interval to check.
     * @return List of {@link DataValidationStatus}.
     */
    List<DataValidationStatus> getValidationStatus(Set<QualityCodeSystem> qualityCodeSystems, List<CimChannel> channels,
                                                   List<? extends BaseReading> readings, Range<Instant> interval);

    default DataValidationStatus getValidationStatus(Set<QualityCodeSystem> qualityCodeSystems, Channel channel,
                                                     Instant timeStamp, List<ReadingQualityRecord> readingQualities) {
        List<CimChannel> channels = new ArrayList<>(2);
        channel.getCimChannel(channel.getMainReadingType()).ifPresent(channels::add);
        channel.getBulkQuantityReadingType().ifPresent(bulkReadingType -> channel.getCimChannel(bulkReadingType).ifPresent(channels::add));

        List<List<ReadingQualityRecord>> readingQualitiesList = new ArrayList<>(2);
        readingQualitiesList.add(readingQualities.stream().filter(rqr -> rqr.getReadingType() == channel.getMainReadingType()).collect(Collectors.toList()));
        channel.getBulkQuantityReadingType().ifPresent(bulkReadingType -> {
            readingQualitiesList.add(readingQualities.stream().filter(rqr -> rqr.getReadingType() == bulkReadingType).collect(Collectors.toList()));
        });
        return getValidationStatus(qualityCodeSystems, channels, timeStamp, readingQualitiesList);
    }

    ;

    DataValidationStatus getValidationStatus(Set<QualityCodeSystem> qualityCodeSystems, List<CimChannel> channels,
                                             Instant timeStamp, List<List<ReadingQualityRecord>> readingQualities);

    boolean isValidationEnabled(Meter meter);

    boolean isValidationOnStorageEnabled(Meter meter);

    boolean isValidationEnabled(Channel channel);

    boolean isValidationEnabled(ReadingContainer meter, ReadingType readingType);

    Optional<Instant> getLastChecked(ReadingContainer meter, ReadingType readingType);
}
