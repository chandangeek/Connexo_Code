package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.AggregatedChannelImpl;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.metering.impl.ChannelImpl;
import com.elster.jupiter.metering.impl.ChannelsContainerImpl;
import com.elster.jupiter.metering.impl.IReadingType;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetrologyContractChannelsContainerImpl extends ChannelsContainerImpl {

    public enum Fields {
        EFFECTIVE_CONTRACT("effectiveMetrologyContract");

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final Provider<ChannelImpl> channelFactory;
    private List<EffectiveMetrologyContractOnUsagePoint> effectiveMetrologyContract = new ArrayList<>();
    private List<Channel> mappedChannels;

    @Inject
    public MetrologyContractChannelsContainerImpl(ServerMeteringService meteringService, EventService eventService, Provider<ChannelImpl> channelFactory) {
        super(meteringService, eventService, null);
        this.channelFactory = channelFactory;
    }

    public MetrologyContractChannelsContainerImpl init(EffectiveMetrologyContractOnUsagePoint effectiveMetrologyContract) {
        this.effectiveMetrologyContract.add(effectiveMetrologyContract);
        // Each channel must have just one reading type (main), which is equal to reading type from deliverable.
        effectiveMetrologyContract.getMetrologyContract().getDeliverables()
                .stream()
                .forEach(deliverable -> storeChannel(channelFactory.get().init(this, Collections.singletonList((IReadingType) deliverable.getReadingType()))));
        return this;
    }

    @Override
    public Interval getInterval() {
        return Interval.of(this.effectiveMetrologyContract.stream()
                .map(Effectivity::getRange)
                .reduce(Range::span)
                .get());
    }

    public MetrologyContract getMetrologyContract() {
        return this.effectiveMetrologyContract.get(0).getMetrologyContract();
    }

    @Override
    public Optional<BigDecimal> getMultiplier(MultiplierType type) {
        return Optional.empty();
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        return Optional.empty();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return this.effectiveMetrologyContract
                .stream()
                .map(EffectiveMetrologyContractOnUsagePoint::getMetrologyConfigurationOnUsagePoint)
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getUsagePoint)
                .findFirst();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = this.getMetrologyConfigurationOnUsagePoint();
        if (effectiveMetrologyConfiguration.getRange().contains(instant)) {
            return Optional.of(effectiveMetrologyConfiguration.getUsagePoint());
        }
        return Optional.empty();
    }

    @Override
    public List<Channel> getChannels() {
        if (this.mappedChannels == null) {
            Map<ReadingType, Channel> channelMap = super.getChannels()
                    .stream()
                    .collect(Collectors.toMap(Channel::getMainReadingType, Function.identity()));
            this.mappedChannels = getMetrologyContract().getDeliverables()
                    .stream()
                    .map(deliverable -> createAggregatedChannel((ChannelContract) channelMap.get(deliverable.getReadingType()), deliverable))
                    .collect(Collectors.toList());
        }
        return this.mappedChannels;
    }

    private AggregatedChannel createAggregatedChannel(ChannelContract channelContract, ReadingTypeDeliverable deliverable){
        return getMeteringService().getDataModel().getInstance(AggregatedChannelImpl.class)
                .init(channelContract,
                        deliverable,
                        this.getMetrologyConfigurationOnUsagePoint().getUsagePoint(),
                        this.getMetrologyContract(),
                        this);
    }

    private EffectiveMetrologyConfigurationOnUsagePoint getMetrologyConfigurationOnUsagePoint(){
        return this.effectiveMetrologyContract.get(0).getMetrologyConfigurationOnUsagePoint();
    }

    @Override
    public Channel createChannel(ReadingType main, ReadingType... readingTypes) {
        throw new UnsupportedOperationException("This channels container does not support manual creation for channels.");
    }

    @Override
    public ZoneId getZoneId() {
        return this.getMetrologyConfigurationOnUsagePoint()
                .getUsagePoint()
                .getZoneId();
    }
}
