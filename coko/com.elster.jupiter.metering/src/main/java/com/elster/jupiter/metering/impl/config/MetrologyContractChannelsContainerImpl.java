package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.impl.ChannelBuilder;
import com.elster.jupiter.metering.impl.ChannelsContainerImpl;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

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

    private Reference<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfiguration = ValueReference.absent();
    private Reference<MetrologyContract> metrologyContract = ValueReference.absent();

    @Inject
    public MetrologyContractChannelsContainerImpl(ServerMeteringService meteringService, EventService eventService, Provider<ChannelBuilder> channelBuilder) {
        super(meteringService, eventService, channelBuilder);
    }

    public MetrologyContractChannelsContainerImpl init(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint, MetrologyContract metrologyContract) {
        this.effectiveMetrologyConfiguration.set(effectiveMetrologyConfigurationOnUsagePoint);
        this.metrologyContract.set(metrologyContract);
        setInterval(effectiveMetrologyConfigurationOnUsagePoint.getInterval());
        return this;
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
}
