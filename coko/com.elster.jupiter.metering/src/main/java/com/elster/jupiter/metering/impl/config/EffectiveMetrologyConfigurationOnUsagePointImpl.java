package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Provides an implementation for the {@link EffectiveMetrologyConfigurationOnUsagePoint} interface.
 */
public class EffectiveMetrologyConfigurationOnUsagePointImpl implements EffectiveMetrologyConfigurationOnUsagePoint {

    private final DataModel dataModel;

    @SuppressWarnings("unused")//Managed by ORM
    private Interval interval;
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    private Reference<UsagePointMetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    private boolean active;

    private List<MetrologyContractChannelsContainerImpl> channelsContainers = new ArrayList<>();

    @Inject
    public EffectiveMetrologyConfigurationOnUsagePointImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    public EffectiveMetrologyConfigurationOnUsagePointImpl init(UsagePoint usagePoint, UsagePointMetrologyConfiguration metrologyConfiguration, Instant start) {
        this.usagePoint.set(usagePoint);
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.interval = Interval.startAt(start);
        return this;
    }

    @Override
    public UsagePointMetrologyConfiguration getMetrologyConfiguration() {
        return metrologyConfiguration.get();
    }

    @Override
    public UsagePoint getUsagePoint() {
        return this.usagePoint.get();
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    @Override
    public void close(Instant closingDate) {
        if (!isEffectiveAt(closingDate)) {
            throw new IllegalArgumentException();
        }
        this.interval = this.interval.withEnd(closingDate);
        this.dataModel.update(this);
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void activate() {
        this.active = true;
        this.dataModel.update(this);
    }

    @Override
    public Optional<ChannelsContainer> getChannelsContainer(MetrologyContract metrologyContract) {
        return this.channelsContainers.stream()
                .filter(channelsContainer -> channelsContainer.getMetrologyContract().equals(metrologyContract))
                .map(ChannelsContainer.class::cast)
                .findAny();
    }

    public void createChannelsContainers() {
        getMetrologyConfiguration().getContracts()
                .stream()
                .filter(metrologyContract -> !metrologyContract.getDeliverables().isEmpty())
                .forEach(metrologyContract -> this.channelsContainers.add(this.dataModel.getInstance(MetrologyContractChannelsContainerImpl.class)
                        .init(this, metrologyContract)));
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("usagePoint", this.usagePoint)
                .add("metrologyConfiguration", this.metrologyConfiguration)
                .toString();
    }
}