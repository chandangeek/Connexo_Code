package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
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

    /**
     * checks if there's at least one suspect put to {@link ChannelsContainer} by one of the {@param qualityCodeSystems}
     * @param qualityCodeSystems systems to take into account when checking for suspects
     * @param channelsContainer {@link ChannelsContainer} to check
     * @return <code>true</code> if there's at least a suspect, <code>false</code> otherwise
     */
    boolean areSuspectsPresent(Set<QualityCodeSystem> qualityCodeSystems, ChannelsContainer channelsContainer);

    default List<DataValidationStatus> getValidationStatus(Set<QualityCodeSystem> qualityCodeSystems, Channel channel,
                                                           List<? extends BaseReading> readings) {
        return getValidationStatus(qualityCodeSystems, channel, readings, readings.stream()
                .map(BaseReading::getTimeStamp)
                .map(Range::singleton)
                .reduce(Range::span)
                .orElse(null));
    }

    default List<DataValidationStatus> getValidationStatus(Set<QualityCodeSystem> qualityCodeSystems, Channel channel,
                                                           List<? extends BaseReading> readings, Range<Instant> interval) {
        List<CimChannel> channels = new ArrayList<>(2);
        channel.getCimChannel(channel.getMainReadingType()).ifPresent(channels::add);
        channel.getBulkQuantityReadingType().ifPresent(bulkReadingType -> channel.getCimChannel(bulkReadingType).ifPresent(channels::add));
        return getValidationStatus(qualityCodeSystems, channels, readings, interval);
    }

    /**
     * gets validation status taking into account qualities of systems among {@param qualityCodeSystems}
     * @param qualityCodeSystems only systems to take into account for computation of validation status
     * @param channels a list of one or two (1st main + 2nd bulk) channels. other cases are not supported by implementation and may lead to unexpected errors!
     * @param readings provided list of readings
     * @param interval interval to check
     * @return list of {@link DataValidationStatus}
     */
    List<DataValidationStatus> getValidationStatus(Set<QualityCodeSystem> qualityCodeSystems, List<CimChannel> channels,
                                                   List<? extends BaseReading> readings, Range<Instant> interval);

    boolean isValidationEnabled(Meter meter);

    boolean isValidationOnStorageEnabled(Meter meter);

    boolean isValidationEnabled(Channel channel);

    boolean isValidationEnabled(ReadingContainer meter, ReadingType readingType);

    Optional<Instant> getLastChecked(ReadingContainer meter, ReadingType readingType);
}
