/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractValidationEvaluator implements ValidationEvaluator {

    @Override
    public boolean areSuspectsPresent(Set<QualityCodeSystem> qualityCodeSystems, ChannelsContainer channelsContainer) {
        return channelsContainer.getChannels().stream()
                .anyMatch(channel -> channel.findReadingQualities()
                        .ofQualitySystems(qualityCodeSystems)
                        .ofQualityIndex(QualityCodeIndex.SUSPECT)
                        .inTimeInterval(channelsContainer.getRange())
                        .actual()
                        .anyMatch());
    }

    /**
     * @param channels supports only 1 or 2 cim channels due to {@link #createDataValidationStatusListFor(Instant, boolean, Iterator, Iterator)},
     * 1st must be main channel, 2nd must be bulk.
     */
    @Override
    public List<DataValidationStatus> getValidationStatus(Set<QualityCodeSystem> qualityCodeSystems, List<CimChannel> channels,
                                                          List<? extends BaseReading> readings, Range<Instant> interval) {
        List<DataValidationStatus> result = new ArrayList<>();
        List<ChannelValidationContainer> channelValidations = channels.stream()
                .map(channel -> getChannelValidationContainer(channel.getChannel()))
                .collect(Collectors.toList());
        // cannot validate only one cim channel from GUI in case there're related main & bulk channels,
        // so we find out if they are validated by one (main) channel only
        ChannelValidationContainer mainChannelValidations = channelValidations.get(0);
        boolean configured = !mainChannelValidations.isEmpty();
        int requestedQCSNumber = qualityCodeSystems == null ? 0 : qualityCodeSystems.size();
        Multimap<QualityCodeSystem, ChannelValidation> mainChannelValidationsPerSystemMultimap
                = indexValidationsBySystem(mainChannelValidations, requestedQCSNumber, qualityCodeSystems);

        List<Multimap<String, IValidationRule>> validationRuleMaps = channelValidations.stream()
                .map(this::getMapQualityToRule)
                .collect(Collectors.toList());

        List<ListMultimap<Instant, ReadingQualityRecord>> readingQualityMaps = channels.stream()
                .map(channel -> getActualReadingQualities(channel, interval, qualityCodeSystems))
                .collect(Collectors.toList());

        Set<Instant> unprocessedQualityTimes = readingQualityMaps.stream()
                .map(ListMultimap::keySet)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        readings.forEach(reading -> {
            Instant timeStamp = reading.getTimeStamp();
            unprocessedQualityTimes.remove(timeStamp);
            result.add(getValidationStatusForTimeStamp(timeStamp, channels, configured,
                    readingQualityMaps, validationRuleMaps, mainChannelValidationsPerSystemMultimap));
        });

        unprocessedQualityTimes.forEach(timeStamp -> result.add(getValidationStatusForTimeStamp(timeStamp, channels, configured,
                readingQualityMaps, validationRuleMaps, mainChannelValidationsPerSystemMultimap)));
        return result;
    }


    @Override
    public DataValidationStatus getValidationStatus(Set<QualityCodeSystem> qualityCodeSystems, List<CimChannel> channels,
                                                    Instant timeStamp, List<List<ReadingQualityRecord>> readingQualities) {

        List<DataValidationStatus> result = new ArrayList<>();
        List<ChannelValidationContainer> channelValidations = channels.stream()
                .map(channel -> getChannelValidationContainer(channel.getChannel()))
                .collect(Collectors.toList());

        ChannelValidationContainer mainChannelValidations = channelValidations.get(0);
        boolean configured = !mainChannelValidations.isEmpty();
        int requestedQCSNumber = qualityCodeSystems == null ? 0 : qualityCodeSystems.size();
        Multimap<QualityCodeSystem, ChannelValidation> mainChannelValidationsPerSystemMultimap
                = indexValidationsBySystem(mainChannelValidations, requestedQCSNumber, qualityCodeSystems);

        List<Multimap<String, IValidationRule>> validationRuleMaps = channelValidations.stream()
                .map(this::getMapQualityToRule)
                .collect(Collectors.toList());

        List<ListMultimap<Instant, ReadingQualityRecord>> readingQualityMaps = new ArrayList<>(2);
        ListMultimap<Instant, ReadingQualityRecord> readingQualitiesList = ArrayListMultimap.create();
        readingQualities.get(0).stream().forEach(readingQualityRecord -> readingQualitiesList.put(timeStamp, readingQualityRecord));
        readingQualityMaps.add(readingQualitiesList);
        ListMultimap<Instant, ReadingQualityRecord> readingQualitiesBulkList = ArrayListMultimap.create();
        readingQualities.get(1).stream().forEach(readingQualityRecord -> readingQualitiesBulkList.put(timeStamp, readingQualityRecord));
        readingQualityMaps.add(readingQualitiesBulkList);

        return getValidationStatusForTimeStamp(timeStamp, channels, configured,
                readingQualityMaps, validationRuleMaps, mainChannelValidationsPerSystemMultimap);
    }

    /**
     * @param timeStamp
     * @param channels must be of the same length as validationRuleMaps and readingQualityMaps, all of 1 or 2 elements, 1st is main channel, 2nd is bulk.
     * @param validationConfigured
     * @param readingQualityMaps must be of the same length as validationRuleMaps and channels, all of 1 or 2 elements, 1st is main channel, 2nd is bulk.
     * @param validationRuleMaps must be of the same length as channels and readingQualityMaps, all of 1 or 2 elements, 1st is main channel, 2nd is bulk.
     * @param mainChannelValidationsPerSystemMultimap
     * @return
     */
    private static DataValidationStatus getValidationStatusForTimeStamp(Instant timeStamp,
                                                                        List<CimChannel> channels,
                                                                        boolean validationConfigured,
                                                                        List<ListMultimap<Instant, ReadingQualityRecord>> readingQualityMaps,
                                                                        List<Multimap<String, IValidationRule>> validationRuleMaps,
                                                                        Multimap<QualityCodeSystem, ChannelValidation> mainChannelValidationsPerSystemMultimap) {
        List<List<ReadingQualityRecord>> qualities = readingQualityMaps.stream()
                .map(readingQualityMap -> readingQualityMap.get(timeStamp))
                .map(ArrayList::new)
                .collect(Collectors.toList());
        boolean fullyValidated = validationConfigured && mainChannelValidationsPerSystemMultimap.asMap().entrySet().stream().map(entry -> {
            boolean validated = wasValidated(ChannelValidationContainer.getLastChecked(entry.getValue().stream()).orElse(null), timeStamp);
            if (validated) {
                int i = 0;
                for (CimChannel channel : channels) {
                    addValidatedAndOkReadingQuality(entry.getKey(), timeStamp, channel, qualities.get(i++));
                }
            }
            return validated;
        }).reduce(Boolean::logicalAnd).orElse(false);
        return createDataValidationStatusListFor(timeStamp, fullyValidated, qualities.iterator(), validationRuleMaps.iterator());
    }

    private static Multimap<QualityCodeSystem, ChannelValidation> indexValidationsBySystem(ChannelValidationContainer channelValidations,
                                                                                           int requestedQCSNumber, Set<QualityCodeSystem> qualityCodeSystems) {
        Multimap<QualityCodeSystem, ChannelValidation> validationsPerSystemMultimap
                = ArrayListMultimap.create(requestedQCSNumber == 0 ? QualityCodeSystem.values().length : requestedQCSNumber, 3); // default number of values is 3
        channelValidations.stream().forEach(validation -> {
            QualityCodeSystem validationQualityCodeSystem = validation.getChannelsContainerValidation().getRuleSet().getQualityCodeSystem();
            if (requestedQCSNumber == 0 || qualityCodeSystems.contains(validationQualityCodeSystem)) {
                validationsPerSystemMultimap.put(validationQualityCodeSystem, validation);
            }
        });
        return validationsPerSystemMultimap;
    }

    private static void addValidatedAndOkReadingQuality(QualityCodeSystem qualityCodeSystem, Instant readingTimeStamp,
                                                        CimChannel channel, List<ReadingQualityRecord> qualities) {
        if (qualities.stream().noneMatch(ReadingQualityRecord::isSuspect)) {
            qualities.add(new TransientReadingQuality(channel, ReadingQualityType.of(qualityCodeSystem, QualityCodeIndex.VALIDATED), readingTimeStamp));
        }
    }

    private static ListMultimap<Instant, ReadingQualityRecord> getActualReadingQualities(CimChannel channel, Range<Instant> interval,
                                                                                         Set<QualityCodeSystem> qualityCodeSystems) {
        return Multimaps.index(channel.findReadingQualities()
                .ofQualitySystems(qualityCodeSystems)
                .inTimeInterval(interval)
                .actual()
                .collect(), ReadingQualityRecord::getReadingTimestamp);
    }

    private static List<IValidationRule> filterDuplicates(Collection<IValidationRule> iValidationRules) {
        Map<String, IValidationRule> collect = iValidationRules.stream()
                .collect(Collectors.toMap(IValidationRule::getImplementation, Function.identity(), (a, b) -> a.isObsolete() ? b : a));
        return new ArrayList<>(collect.values());
    }

    /**
     * @param timeStamp
     * @param completelyValidated
     * @param qualities must be of the same length as validationRuleMaps, both of 1 or 2 elements, 1st is main channel, 2nd is bulk.
     * @param validationRuleMaps must be of the same length as qualities, both of 1 or 2 elements, 1st is main channel, 2nd is bulk.
     * @return
     */
    private static DataValidationStatus createDataValidationStatusListFor(Instant timeStamp, boolean completelyValidated,
                                                                          Iterator<? extends List<? extends ReadingQuality>> qualities,
                                                                          Iterator<Multimap<String, IValidationRule>> validationRuleMaps) {
        DataValidationStatusImpl validationStatus = new DataValidationStatusImpl(timeStamp, completelyValidated);
        Multimap<String, IValidationRule> validationRuleMap = validationRuleMaps.next();
        qualities.next().forEach(quality -> validationStatus.addReadingQuality(quality, filterDuplicates(validationRuleMap.get(quality.getTypeCode()))));
        if (qualities.hasNext()) {
            Multimap<String, IValidationRule> bulkValidationRuleMap = validationRuleMaps.next();
            qualities.next().forEach(quality -> validationStatus.addBulkReadingQuality(quality, filterDuplicates(bulkValidationRuleMap.get(quality.getTypeCode()))));
        }
        return validationStatus;
    }

    abstract ChannelValidationContainer getChannelValidationContainer(Channel channel);

    abstract Multimap<String, IValidationRule> getMapQualityToRule(ChannelValidationContainer channelValidations);

    private static boolean wasValidated(Instant lastChecked, Instant readingTimestamp) {
        return lastChecked != null && readingTimestamp.compareTo(lastChecked) <= 0;
    }

    private static final class TransientReadingQuality implements ReadingQualityRecord {

        private final CimChannel cimChannel;
        private final ReadingQualityType readingQualityType;
        private final Instant readingTimestamp;

        private TransientReadingQuality(CimChannel cimChannel, ReadingQualityType readingQualityType, Instant readingTimestamp) {
            this.cimChannel = cimChannel;
            this.readingQualityType = readingQualityType;
            this.readingTimestamp = readingTimestamp;
        }

        @Override
        public Instant getTimestamp() {
            return readingTimestamp;
        }

        @Override
        public Channel getChannel() {
            return cimChannel.getChannel();
        }

        @Override
        public CimChannel getCimChannel() {
            return cimChannel;
        }

        @Override
        public ReadingType getReadingType() {
            return cimChannel.getReadingType();
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public void setComment(String comment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<BaseReadingRecord> getBaseReadingRecord() {
            return Optional.empty();
        }

        @Override
        public void update() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant getReadingTimestamp() {
            return readingTimestamp;
        }

        @Override
        public void delete() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getVersion() {
            return 0;
        }

        @Override
        public boolean isActual() {
            return true;
        }

        @Override
        public void makePast() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void makeActual() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getComment() {
            return "";
        }

        @Override
        public String getTypeCode() {
            return getType().getCode();
        }

        @Override
        public ReadingQualityType getType() {
            return readingQualityType;
        }
    }
}
