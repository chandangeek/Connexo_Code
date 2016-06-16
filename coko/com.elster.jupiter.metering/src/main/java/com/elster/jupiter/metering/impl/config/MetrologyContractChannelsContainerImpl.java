package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
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
        METROLOGY_CONFIG("effectiveMetrologyConfiguration"),
        METROLOGY_CONTRACT("metrologyContract"),;

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final Provider<ChannelImpl> channelFactory;

    private Reference<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfiguration = ValueReference.absent();
    private Reference<MetrologyContract> metrologyContract = ValueReference.absent();
    private List<Channel> mappedChannels;

    @Inject
    public MetrologyContractChannelsContainerImpl(ServerMeteringService meteringService, EventService eventService, Provider<ChannelImpl> channelFactory) {
        super(meteringService, eventService, null);
        this.channelFactory = channelFactory;
    }

    public MetrologyContractChannelsContainerImpl init(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint, MetrologyContract metrologyContract) {
        this.effectiveMetrologyConfiguration.set(effectiveMetrologyConfigurationOnUsagePoint);
        this.metrologyContract.set(metrologyContract);
        setInterval(effectiveMetrologyConfigurationOnUsagePoint.getInterval());
        // Each channel must have just one reading type (main), which is equal to reading type from deliverable.
        metrologyContract.getDeliverables()
                .stream()
                .forEach(deliverable -> storeChannel(channelFactory.get().init(this, Collections.singletonList((IReadingType) deliverable.getReadingType()))));
        return this;
    }

    public MetrologyContract getMetrologyContract() {
        return this.metrologyContract.get();
    }

    public void save() {
        if (getId() == 0) {
            Save.CREATE.save(getMeteringService().getDataModel(), this);
        } else {
            Save.UPDATE.save(getMeteringService().getDataModel(), this);
        }
    }

    @Override
    public Optional<BigDecimal> getMultiplier(MultiplierType type) {
        return Optional.empty();
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        /*
        It is important to return empty optional here, because validation service uses that for validation status check:
        ValidationServerImpl#isValidationActive(ChannelsContainer).
        In other words if you want to get a meter, be ready to change the logic in ValidationServerImpl.
        */
        return Optional.empty();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = this.effectiveMetrologyConfiguration.get();
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
            this.mappedChannels = this.metrologyContract.get().getDeliverables()
                    .stream()
                    .map(deliverable -> getMeteringService().getDataModel().getInstance(AggregatedChannelImpl.class)
                            .init((ChannelContract) channelMap.get(deliverable.getReadingType()), deliverable, this.effectiveMetrologyConfiguration.get(), this.metrologyContract.get()))
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
        return this.effectiveMetrologyConfiguration.get().getUsagePoint().getZoneId();
    }
}
