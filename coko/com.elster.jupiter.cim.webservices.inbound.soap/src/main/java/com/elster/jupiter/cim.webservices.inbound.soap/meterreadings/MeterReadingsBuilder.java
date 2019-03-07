/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.meterreadings;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.TranslationKeys;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;

import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.meterreadings.DateTimeInterval;
import ch.iec.tc57._2011.meterreadings.IntervalBlock;
import ch.iec.tc57._2011.meterreadings.IntervalReading;
import ch.iec.tc57._2011.meterreadings.MeterReading;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.meterreadings.Name;
import ch.iec.tc57._2011.meterreadings.NameType;
import ch.iec.tc57._2011.meterreadings.RationalNumber;
import ch.iec.tc57._2011.meterreadings.Reading;
import ch.iec.tc57._2011.meterreadings.ReadingInterharmonic;
import ch.iec.tc57._2011.meterreadings.ReadingQuality;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeterReadingsBuilder {

    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeterReadingFaultMessageFactory faultMessageFactory;

    private UsagePoint usagePoint;
    private List<EndDevice> endDevices;
    private Set<MetrologyPurpose> purposes;
    private Set<String> readingTypeMRIDs = Collections.EMPTY_SET;
    private Set<String> readingTypeFullAliasNames = Collections.EMPTY_SET;
    private RangeSet<Instant> timePeriods;

    private Set<ReadingType> referencedReadingTypes;
    private Set<ReadingQualityType> referencedReadingQualityTypes;

    @Inject
    public MeterReadingsBuilder(MeteringService meteringService,
                         MetrologyConfigurationService metrologyConfigurationService,
                         MeterReadingFaultMessageFactory faultMessageFactory) {
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.faultMessageFactory = faultMessageFactory;
    }

    public MeterReadingsBuilder withEndDevices(List<EndDevice> endDevices) {
        this.endDevices = endDevices;
        return this;
    }

    MeterReadingsBuilder fromUsagePointWithMRID(String mRID) throws FaultMessage {
        usagePoint = meteringService.findUsagePointByMRID(mRID)
                .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_USAGE_POINT_WITH_MRID, mRID));
        return this;
    }

    MeterReadingsBuilder fromUsagePointWithName(String name) throws FaultMessage {
        usagePoint = meteringService.findUsagePointByName(name)
                .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_USAGE_POINT_WITH_NAME, name));
        return this;
    }

    MeterReadingsBuilder fromPurposes(Set<String> purposeNames) throws FaultMessage {
        Set<String> requestedNames = new HashSet<>(purposeNames);
        purposes = metrologyConfigurationService.getMetrologyPurposes().stream()
                .filter(purpose -> purposeNames.contains(purpose.getName()))
                .peek(purpose -> requestedNames.remove(purpose.getName()))
                .collect(Collectors.toSet());
        if (!requestedNames.isEmpty()) {
            String unknownNames = requestedNames.stream().sorted().map(s -> '\'' + s + '\'').collect(Collectors.joining(", "));
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_PURPOSES_WITH_NAMES, unknownNames).get();
        }
        return this;
    }

    public MeterReadingsBuilder ofReadingTypesWithMRIDs(Set<String> mRIDs) {
        this.readingTypeMRIDs = mRIDs;
        return this;
    }

    public MeterReadingsBuilder ofReadingTypesWithFullAliasNames(Set<String> names) {
        this.readingTypeFullAliasNames = names;
        return this;
    }

    public MeterReadingsBuilder inTimeIntervals(RangeSet<Instant> timePeriods) {
        this.timePeriods = timePeriods;
        return this;
    }

    public MeterReadings build() throws FaultMessage {
        MeterReadings meterReadings = new MeterReadings();
        List<MeterReading> meterReadingsList = meterReadings.getMeterReading();
        List<ch.iec.tc57._2011.meterreadings.ReadingType> readingTypeList = meterReadings.getReadingType();
        List<ch.iec.tc57._2011.meterreadings.ReadingQualityType> readingQualityTypeList = meterReadings.getReadingQualityType();
        referencedReadingTypes = new HashSet<>();
        referencedReadingQualityTypes = new HashSet<>();

        if (endDevices == null || endDevices.isEmpty()) {
            usagePoint.getEffectiveMetrologyConfigurations().stream()
                    .filter(emc -> !timePeriods.subRangeSet(emc.getInterval().toOpenClosedRange()).isEmpty())
                    .flatMap(this::fetchReadingsFromEffectiveConfiguration)
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getLast, MeterReadingsBuilder::mergeMaps))
                    .forEach((purposeName, readingsByReadingTypes) -> wrapInMeterReading(purposeName, null, readingsByReadingTypes)
                            .ifPresent(meterReadingsList::add));
        } else if (endDevices.stream().anyMatch(ed -> ed instanceof Meter)) {
                endDevices.stream()
                        .filter(ed -> ed instanceof Meter)
                        .forEach(ed -> {
                                Meter meter = (Meter) ed;
                                meter.getChannelsContainers().stream()
                                .filter(cc -> !timePeriods.subRangeSet(cc.getInterval().toOpenClosedRange()).isEmpty())
                                .map(this::fetchReadingsFromContainer)
                                .forEach(readingsByReadingTypes -> wrapInMeterReading(null, ed, readingsByReadingTypes)
                                        .ifPresent(meterReadingsList::add));
                });
        }

        // filled in in scope of wrapInMeterReading
        referencedReadingTypes.stream()
                .map(MeterReadingsBuilder::createReadingType)
                .forEach(readingTypeList::add);
        // filled in in scope of wrapInMeterReading
        referencedReadingQualityTypes.stream()
                .map(MeterReadingsBuilder::createReadingQualityType)
                .forEach(readingQualityTypeList::add);

        return meterReadings;
    }

    private static <K, V, T extends Map<K, List<V>>> Map<K, List<V>> mergeMaps(T aMap, T bMap) {
        return Stream.of(aMap, bMap)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (aList, bList) -> {
                    aList.addAll(bList);
                    return aList;
                }));
    }

    private Stream<Pair<String, Map<ReadingType, List<ReadingWithQualities>>>> fetchReadingsFromEffectiveConfiguration(
            EffectiveMetrologyConfigurationOnUsagePoint effectiveMC) {
        return filterMetrologyContracts(effectiveMC)
                .map(metrologyContract -> Pair.of(metrologyContract.getMetrologyPurpose().getName(),
                        fetchReadingsFromContract(effectiveMC, metrologyContract)));
    }

    private Map<ReadingType, List<ReadingWithQualities>> fetchReadingsFromContract(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC,
                                                                                   MetrologyContract metrologyContract) {
        return effectiveMC.getChannelsContainer(metrologyContract)
                .map(channelsContainer -> {
                    RangeSet<Instant> currentTimePeriods = timePeriods.subRangeSet(channelsContainer.getInterval().toOpenClosedRange());
                    if (currentTimePeriods.isEmpty()) {
                        return Collections.<ReadingType, List<ReadingWithQualities>>emptyMap();
                    }
                    Set<ReadingType> readingTypes = filterReadingTypes(metrologyContract).collect(Collectors.toSet());
                    return channelsContainer.getChannels().stream()
                            .filter(channel -> readingTypes.contains(channel.getMainReadingType()))
                            .filter(AggregatedChannel.class::isInstance)
                            .map(AggregatedChannel.class::cast)
                            .collect(Collectors.toMap(AggregatedChannel::getMainReadingType,
                                    channel -> getReadingsWithQualities(channel, currentTimePeriods)));
                })
                .orElseGet(Collections::emptyMap);
    }

    private Map<ReadingType, List<ReadingWithQualities>> fetchReadingsFromContainer(ChannelsContainer channelsContainer) {
        RangeSet<Instant> currentTimePeriods = timePeriods.subRangeSet(channelsContainer.getInterval().toOpenClosedRange());
        if (currentTimePeriods.isEmpty()) {
            return Collections.<ReadingType, List<ReadingWithQualities>>emptyMap();
        }
        Map<ReadingType, List<ReadingWithQualities>>  result = new HashMap<>();
        for (Channel channel: channelsContainer.getChannels()) {
            for (ReadingType readingType: channel.getReadingTypes()) {
                if (readingTypeFullAliasNames.contains(readingType.getFullAliasName())
                        || readingTypeMRIDs.contains(readingType.getMRID())) {
                    channel.getCimChannel(readingType).ifPresent(cimChannel -> {
                        result.put(readingType, getReadingsWithQualities(cimChannel, currentTimePeriods));
                    });
                }
            }
        }
        return result;
    }

    private static List<ReadingWithQualities> getReadingsWithQualities(AggregatedChannel channel, RangeSet<Instant> timePeriods) {
        Map<Instant, ReadingWithQualities> readingsWithQualities = getReadingRecords(channel, timePeriods)
                .collect(Collectors.toMap(BaseReadingRecord::getTimeStamp, ReadingWithQualities::from, (a, b) -> a, TreeMap::new));
        Map<Instant, List<ReadingQualityRecord>> readingQualitiesByTimestamps = channel.findReadingQualities()
                .actual()
                .inTimeInterval(timePeriods.span())
                .stream()
                .filter(quality -> timePeriods.contains(quality.getReadingTimestamp()))
                .collect(Collectors.groupingBy(ReadingQualityRecord::getReadingTimestamp));
        readingQualitiesByTimestamps.forEach((timestamp, qualitiesList) ->
                readingsWithQualities.computeIfAbsent(timestamp, ReadingWithQualities::missing)
                        .setReadingQualities(qualitiesList));
        return new ArrayList<>(readingsWithQualities.values());
    }

    private static List<ReadingWithQualities> getReadingsWithQualities(CimChannel channel, RangeSet<Instant> timePeriods) {
        Map<Instant, ReadingWithQualities> readingsWithQualities = getReadingRecords(channel, timePeriods)
                .collect(Collectors.toMap(BaseReadingRecord::getTimeStamp, ReadingWithQualities::from, (a, b) -> a, TreeMap::new));
        Map<Instant, List<ReadingQualityRecord>> readingQualitiesByTimestamps = channel.findReadingQualities()
                .actual()
                .inTimeInterval(timePeriods.span())
                .stream()
                .filter(quality -> timePeriods.contains(quality.getReadingTimestamp()))
                .collect(Collectors.groupingBy(ReadingQualityRecord::getReadingTimestamp));
        readingQualitiesByTimestamps.forEach((timestamp, qualitiesList) ->
                readingsWithQualities.computeIfAbsent(timestamp, ReadingWithQualities::missing)
                        .setReadingQualities(qualitiesList));
        return new ArrayList<>(readingsWithQualities.values());
    }

    private static Stream<BaseReadingRecord> getReadingRecords(AggregatedChannel channel, RangeSet<Instant> timePeriods) {
        Collection<? extends BaseReadingRecord> records = (channel.isRegular() ?
                channel.getAggregatedIntervalReadings(timePeriods.span()) :
                getAggregatedRegisterReadings(channel, timePeriods.span()));
        return getReadingRecords(records, timePeriods);
    }

    private static Stream<BaseReadingRecord> getReadingRecords(CimChannel channel, RangeSet<Instant> timePeriods) {
        Collection<? extends BaseReadingRecord> records = (channel.isRegular() ?
                channel.getIntervalReadings(timePeriods.span()) :
                channel.getRegisterReadings(timePeriods.span()));
        return getReadingRecords(records, timePeriods);
    }

    private static Stream<BaseReadingRecord> getReadingRecords(Collection<? extends BaseReadingRecord> baseReadingRecords, RangeSet<Instant> timePeriods) {
        return baseReadingRecords.stream()
                .map(BaseReadingRecord.class::cast)
                .filter(reading -> timePeriods.contains(reading.getTimeStamp()))
                .filter(reading -> !isMissingWithOnlyDerivedQualities(reading));
    }

    /**
     * To filter readings that bring no information: no value, no reading qualities (derived is always present).
     * @param reading {@link BaseReadingRecord} to check.
     * @return {@code true} if it is missing with no reading qualities (except derived that is always present).
     */
    private static boolean isMissingWithOnlyDerivedQualities(BaseReadingRecord reading) {
        return reading.getValue() == null
                && reading.getReadingQualities().stream()
                .map(ReadingQualityRecord::getType)
                .mapToInt(ReadingQualityType::getCategoryCode)
                .allMatch(code -> QualityCodeCategory.DERIVED.ordinal() == code);
    }

    private static Collection<AggregatedRegisterReading> getAggregatedRegisterReadings(AggregatedChannel channel, Range<Instant> interval) {
        Map<Instant, AggregatedRegisterReading> readingsMap = channel.getCalculatedRegisterReadings(interval).stream()
                .collect(Collectors.toMap(ReadingRecord::getTimeStamp, AggregatedRegisterReading::fromCalculatedReading));
        channel.getPersistedRegisterReadings(interval).forEach(persisted ->
                readingsMap.computeIfAbsent(persisted.getTimeStamp(), timestamp -> AggregatedRegisterReading.fromPersistedReading(persisted))
                        .setPersistedReading(persisted));
        return readingsMap.values();
    }

    private Optional<MeterReading> wrapInMeterReading(String purpose, EndDevice endDevice, Map<ReadingType, List<ReadingWithQualities>> readingsByReadingTypes) {
        MeterReading meterReading = new MeterReading();
        if(purpose != null) {
            meterReading.setUsagePoint(createUsagePointPurpose(purpose));
        }
        if (endDevice != null) {
            meterReading.setMeter(createMeter(endDevice));
        }
        List<IntervalBlock> intervalBlocks = meterReading.getIntervalBlocks();
        List<Reading> registerReadings = meterReading.getReadings();
        readingsByReadingTypes.forEach((readingType, readingsWithQualities) -> {
            if (readingType.isRegular()) {
                if (!readingsWithQualities.isEmpty()) {
                    intervalBlocks.add(createIntervalBlock(readingType, readingsWithQualities));
                }
            } else {
                registerReadings.addAll(readingsWithQualities.stream()
                        .map(readingRecord -> createReading(readingType, readingRecord))
                        .collect(Collectors.toList()));
            }
        });
        if (intervalBlocks.isEmpty() && registerReadings.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(meterReading);
    }

    private ch.iec.tc57._2011.meterreadings.Meter createMeter(EndDevice endDevice) {
        ch.iec.tc57._2011.meterreadings.Meter meter = new ch.iec.tc57._2011.meterreadings.Meter();
        meter.setMRID(endDevice.getMRID());
        meter.getNames().add(createName(endDevice.getName()));
        return meter;
    }

    private ch.iec.tc57._2011.meterreadings.UsagePoint createUsagePointPurpose(String purpose) {
        ch.iec.tc57._2011.meterreadings.UsagePoint meterReadingUsagePoint = new ch.iec.tc57._2011.meterreadings.UsagePoint();
        meterReadingUsagePoint.setMRID(usagePoint.getMRID());
        meterReadingUsagePoint.getNames().add(createNameWithType(usagePoint.getName(), UsagePointNameTypeEnum.USAGE_POINT_NAME));
        meterReadingUsagePoint.getNames().add(createNameWithType(purpose, UsagePointNameTypeEnum.PURPOSE));
        return meterReadingUsagePoint;
    }

    private IntervalBlock createIntervalBlock(ReadingType readingType, List<ReadingWithQualities> records) {
        IntervalBlock.ReadingType intervalBlockReadingType = new IntervalBlock.ReadingType();
        reference(readingType, intervalBlockReadingType::setRef);

        IntervalBlock intervalBlock = new IntervalBlock();
        intervalBlock.setReadingType(intervalBlockReadingType);
        intervalBlock.getIntervalReadings().addAll(records.stream().map(this::createIntervalReading).collect(Collectors.toList()));

        return intervalBlock;
    }

    private IntervalReading createIntervalReading(ReadingWithQualities record) {
        IntervalReading info = new IntervalReading();
        info.setTimeStamp(record.getTimestamp());

        record.getReading().ifPresent(reading -> {
            Optional.ofNullable(reading.getValue())
                    .map(BigDecimal::toPlainString)
                    .ifPresent(info::setValue);
            info.setReportedDateTime(reading.getReportedDateTime());
        });

        List<ReadingQuality> readingQualities = info.getReadingQualities();
        record.getReadingQualities().stream()
                .map(this::createReadingQuality)
                .forEach(readingQualities::add);

        return info;
    }

    private Name createNameWithType(String itemName, UsagePointNameTypeEnum itemType) {
        NameType nameType = new NameType();
        nameType.setName(itemType.getNameType());

        Name name = createName(itemName);
        name.setNameType(nameType);

        return name;
    }

    private Reading createReading(ReadingType readingType, ReadingWithQualities record) {
        Reading.ReadingType readingReadingType = new Reading.ReadingType();
        reference(readingType, readingReadingType::setRef);

        Reading info = new Reading();
        info.setReadingType(readingReadingType);
        info.setTimeStamp(record.getTimestamp());

        record.getReading().ifPresent(reading -> {
            Optional.ofNullable(reading.getValue())
                    .map(BigDecimal::toPlainString)
                    .ifPresent(info::setValue);
            info.setReportedDateTime(reading.getReportedDateTime());
            reading.getTimePeriod()
                    .map(MeterReadingsBuilder::createDateTimeInterval)
                    .ifPresent(info::setTimePeriod);
        });

        List<ReadingQuality> readingQualities = info.getReadingQualities();
        record.getReadingQualities().stream()
                .map(this::createReadingQuality)
                .forEach(readingQualities::add);

        return info;
    }

    private ReadingQuality createReadingQuality(ReadingQualityRecord qualityRecord) {
        ReadingQuality info = new ReadingQuality();
        info.setTimeStamp(qualityRecord.getTimestamp());
        info.setReadingQualityType(reference(qualityRecord.getType()));
        info.setComment(qualityRecord.getComment());
        return info;
    }

    private void reference(ReadingType readingType, Consumer<String> referenceSetter) {
        referenceSetter.accept(readingType.getMRID());
        referencedReadingTypes.add(readingType);
    }

    private ReadingQuality.ReadingQualityType reference(ReadingQualityType readingQualityType) {
        ReadingQuality.ReadingQualityType reference = new ReadingQuality.ReadingQualityType();
        reference.setRef(readingQualityType.getCode());
        referencedReadingQualityTypes.add(readingQualityType);
        return reference;
    }

    private static DateTimeInterval createDateTimeInterval(Range<Instant> interval) {
        DateTimeInterval dateTimeInterval = new DateTimeInterval();
        Ranges.lowerBound(interval).ifPresent(dateTimeInterval::setStart);
        Ranges.upperBound(interval).ifPresent(dateTimeInterval::setEnd);
        return dateTimeInterval;
    }

    private static ch.iec.tc57._2011.meterreadings.ReadingQualityType createReadingQualityType(ReadingQualityType readingQualityType) {
        ch.iec.tc57._2011.meterreadings.ReadingQualityType info = new ch.iec.tc57._2011.meterreadings.ReadingQualityType();
        info.setMRID(readingQualityType.getCode());
        readingQualityType.system()
                .map(QualityCodeSystem::getTranslationKey)
                .map(TranslationKeys::getDefaultFormat)
                .ifPresent(info::setSystemId);
        readingQualityType.category()
                .map(QualityCodeCategory::getTranslationKey)
                .map(TranslationKeys::getDefaultFormat)
                .ifPresent(info::setCategory);
        readingQualityType.qualityIndex()
                .map(QualityCodeIndex::getTranslationKey)
                .map(TranslationKey::getDefaultFormat)
                .ifPresent(info::setSubCategory);
        return info;
    }

    private static ch.iec.tc57._2011.meterreadings.ReadingType createReadingType(ReadingType readingType) {
        ch.iec.tc57._2011.meterreadings.ReadingType info = new ch.iec.tc57._2011.meterreadings.ReadingType();
        info.setMRID(readingType.getMRID());
        info.getNames().add(createName(readingType.getFullAliasName()));
        info.setAccumulation(readingType.getAccumulation().getDescription());
        info.setAggregate(readingType.getAggregate().getDescription());
        info.setArgument(createRationalNumber(readingType.getArgument()));
        info.setCommodity(readingType.getCommodity().getDescription());
        info.setConsumptionTier(BigInteger.valueOf(readingType.getConsumptionTier()));
        info.setCpp(BigInteger.valueOf(readingType.getCpp()));
        info.setCurrency(readingType.getCurrency().getCurrencyCode());
        info.setFlowDirection(readingType.getFlowDirection().getDescription());
        info.setInterharmonic(createReadingInterharmonic(readingType.getInterharmonic()));
        info.setMacroPeriod(readingType.getMacroPeriod().getDescription());
        info.setMeasurementKind(readingType.getMeasurementKind().getDescription());
        info.setMeasuringPeriod(readingType.getMeasuringPeriod().getDescription());
        info.setMultiplier(readingType.getMultiplier().toString());
        info.setPhases(readingType.getPhases().getDescription());
        info.setTou(BigInteger.valueOf(readingType.getTou()));
        info.setUnit(readingType.getUnit().getName());
        return info;
    }

    private static RationalNumber createRationalNumber(com.elster.jupiter.cbo.RationalNumber rational) {
        return Optional.ofNullable(rational)
                .filter(number -> !com.elster.jupiter.cbo.RationalNumber.NOTAPPLICABLE.equals(number))
                .map(number -> {
                    RationalNumber info = new RationalNumber();
                    info.setNumerator(BigInteger.valueOf(rational.getNumerator()));
                    info.setDenominator(BigInteger.valueOf(rational.getDenominator()));
                    return info;
                })
                .orElse(null);
    }

    private static ReadingInterharmonic createReadingInterharmonic(com.elster.jupiter.cbo.RationalNumber rational) {
        return Optional.ofNullable(rational)
                .filter(number -> !com.elster.jupiter.cbo.RationalNumber.NOTAPPLICABLE.equals(number))
                .map(number -> {
                    ReadingInterharmonic info = new ReadingInterharmonic();
                    info.setNumerator(BigInteger.valueOf(rational.getNumerator()));
                    info.setDenominator(BigInteger.valueOf(rational.getDenominator()));
                    return info;
                })
                .orElse(null);
    }

    private static Name createName(String value) {
        Name name = new Name();
        name.setName(value);
        return name;
    }

    private Stream<MetrologyContract> filterMetrologyContracts(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC) {
        Stream<MetrologyContract> contracts = effectiveMC.getMetrologyConfiguration().getContracts().stream();
        if (!purposes.isEmpty()) {
            contracts = contracts.filter(metrologyContract -> purposes.contains(metrologyContract.getMetrologyPurpose()));
        }
        return contracts;
    }

    private Stream<ReadingType> filterReadingTypes(MetrologyContract contract) {
        Stream<ReadingType> readingTypes = contract.getDeliverables().stream()
                .map(ReadingTypeDeliverable::getReadingType);
        if (!readingTypeMRIDs.isEmpty() || !readingTypeFullAliasNames.isEmpty()) {
            readingTypes = readingTypes.filter(readingType ->
                    readingTypeMRIDs.contains(readingType.getMRID())
                            || readingTypeFullAliasNames.contains(readingType.getFullAliasName()));
        }
        return readingTypes;
    }
}
