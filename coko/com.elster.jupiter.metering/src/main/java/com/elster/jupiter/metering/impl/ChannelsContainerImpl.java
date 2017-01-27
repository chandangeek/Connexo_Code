package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.streams.ExtraCollectors;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ChannelsContainerImpl implements ChannelsContainer {

    @SuppressWarnings("unused")
    private long id;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private List<Channel> channels = new ArrayList<>();

    private final ServerMeteringService meteringService;
    private final EventService eventService;
    private final Provider<ChannelBuilder> channelBuilder;

    @Inject
    public ChannelsContainerImpl(ServerMeteringService meteringService, EventService eventService, Provider<ChannelBuilder> channelBuilder) {
        this.meteringService = meteringService;
        this.eventService = eventService;
        this.channelBuilder = channelBuilder;
    }

    protected ServerMeteringService getMeteringService() {
        return this.meteringService;
    }

    @Override
    public Channel createChannel(ReadingType main, ReadingType... readingTypes) {
        //TODO: check for duplicate channel
        ReadingTypeImpl[] extraTypes = new ReadingTypeImpl[readingTypes.length];
        for (int i = 0; i < readingTypes.length; i++) {
            extraTypes[i] = (ReadingTypeImpl) readingTypes[i];
        }
        Channel channel = channelBuilder.get().channelsContainer(this).readingTypes(main, extraTypes).build();
        storeChannel(channel);
        return channel;
    }

    protected Channel storeChannel(Channel channel) {
        channels.add(channel);
        eventService.postEvent(EventType.CHANNEL_CREATED.topic(), channel);
        return channel;
    }

    @Override
    public List<Channel> getChannels() {
        return Collections.unmodifiableList(channels);
    }

    @Override
    public Instant getStart() {
        return getRange().lowerEndpoint();
    }

    @Override
    public Optional<Meter> getMeter() {
        return getMeter(this.meteringService.getClock().instant());
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return getUsagePoint(this.meteringService.getClock().instant());
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Set<ReadingType> getReadingTypes(Range<Instant> range) {
        if (getRange().isConnected(range)) {
            return getChannels().stream()
                    .flatMap(channel -> channel.getReadingTypes().stream())
                    .collect(ExtraCollectors.toImmutableSet());
        }
        return Collections.emptySet();
    }

    @Override
    public List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType) {
        if (!getRange().isConnected(range)) {
            return Collections.emptyList();
        }
        return getChannel(readingType)
                .map(channel -> {
                    Function<Interval, Range<Instant>> toRange = channel.isRegular() ? Interval::toOpenClosedRange : Interval::toClosedRange;
                    Range<Instant> active = range.intersection(toRange.apply(getInterval()));
                    return readingType.isRegular() ? channel.getIntervalReadings(readingType, active) : channel.getRegisterReadings(readingType, active);
                })
                .orElse(Collections.emptyList());
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since) {
        if (!range.isConnected(getRange())) {
            return Collections.emptyList();
        }
        Range<Instant> active = range.intersection(getRange());
        return getChannel(readingType)
                .map(channel -> channel.getReadingsUpdatedSince(readingType, active, since))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType, int count) {
        return getChannel(readingType)
                .map(channel -> channel.getReadingsBefore(when, count))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType, int count) {
        return getChannel(readingType)
                .map(channel -> channel.getReadingsOnOrBefore(when, count))
                .orElse(Collections.emptyList());
    }

    @Override
    public boolean hasData() {
        return getChannels().stream().anyMatch(Channel::hasData);
    }

    @Override
    public boolean is(ReadingContainer other) {
        return other instanceof ChannelsContainerImpl && ((ChannelsContainerImpl) other).getId() == getId();
    }

    @Override
    public ZoneId getZoneId() {
        Set<ZoneId> candidates = getChannels().stream()
                .map(Channel::getZoneId)
                .collect(Collectors.toSet());
        if (candidates.size() > 1) {
            throw new RuntimeException("More than one zone id for this meter activation");
        }
        return candidates.stream().findFirst().orElse(this.meteringService.getClock().getZone());
    }

    @Override
    public List<Instant> toList(ReadingType readingType, Range<Instant> exportInterval) {
        return getChannel(readingType).map(channel -> channel.toList(exportInterval)).orElseGet(Collections::emptyList);
    }

    @Override
    public List<ReadingQualityRecord> getReadingQualities(Set<QualityCodeSystem> qualityCodeSystems, QualityCodeIndex qualityCodeIndex, ReadingType readingType, Range<Instant> interval) {
        return getChannel(readingType)
                .flatMap(channel -> channel.getCimChannel(readingType))
                .map(cimChannel -> cimChannel.findReadingQualities()
                        .ofQualitySystems(qualityCodeSystems)
                        .ofQualityIndex(qualityCodeIndex)
                        .inTimeInterval(interval)
                        .actual()
                        .collect())
                .orElse(Collections.emptyList());
    }

    @Override
    public List<ChannelsContainer> getChannelsContainers() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelsContainerImpl channelsContainer = (ChannelsContainerImpl) o;
        return id == channelsContainer.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
