package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.impl.AggregatedChannelImpl;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.metering.impl.ChannelImpl;
import com.elster.jupiter.metering.impl.ChannelsContainerImpl;
import com.elster.jupiter.metering.impl.IReadingType;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
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
    private Reference<EffectiveMetrologyContractOnUsagePoint> effectiveMetrologyContract = ValueReference.absent();
    private List<Channel> mappedChannels;

    @Inject
    public MetrologyContractChannelsContainerImpl(ServerMeteringService meteringService, EventService eventService, Provider<ChannelImpl> channelFactory) {
        super(meteringService, eventService, null);
        this.channelFactory = channelFactory;
    }

    public MetrologyContractChannelsContainerImpl init(EffectiveMetrologyContractOnUsagePoint effectiveMetrologyContract) {
        this.effectiveMetrologyContract.set(effectiveMetrologyContract);
        // Each channel must have just one reading type (main), which is equal to reading type from deliverable.
        effectiveMetrologyContract.getMetrologyContract().getDeliverables()
                .stream()
                .forEach(deliverable -> storeChannel(channelFactory.get().init(this, Collections.singletonList((IReadingType) deliverable.getReadingType()))));
        return this;
    }

    @Override
    public Interval getInterval() {
        return this.effectiveMetrologyContract.get().getInterval();
    }

    public MetrologyContract getMetrologyContract() {
        return this.effectiveMetrologyContract.get().getMetrologyContract();
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
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = this.effectiveMetrologyContract.get().getMetrologyConfigurationOnUsagePoint();
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
                    .map(deliverable -> getMeteringService().getDataModel().getInstance(AggregatedChannelImpl.class)
                            .init((ChannelContract) channelMap.get(deliverable.getReadingType()), deliverable, this.effectiveMetrologyContract.get()))
                    .collect(Collectors.toList());
        }
        return this.mappedChannels;
    }

    @Override
    public Channel createChannel(ReadingType main, ReadingType... readingTypes) {
        throw new UnsupportedOperationException("This channels container does not support manual creation for channels.");
    }

    @Override
    public ZoneId getZoneId() {
        return this.effectiveMetrologyContract.get().getMetrologyConfigurationOnUsagePoint().getUsagePoint().getZoneId();
    }
}
