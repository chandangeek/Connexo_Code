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

import com.google.common.collect.Range;

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
    private List<EffectiveMetrologyContractOnUsagePoint> effectiveContracts = new ArrayList<>();

    private long id;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public EffectiveMetrologyConfigurationOnUsagePointImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    public EffectiveMetrologyConfigurationOnUsagePointImpl initAndSave(UsagePoint usagePoint, UsagePointMetrologyConfiguration metrologyConfiguration, Instant start) {
        this.usagePoint.set(usagePoint);
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.interval = Interval.startAt(start);
        return this;
    }

    public EffectiveMetrologyConfigurationOnUsagePointImpl initAndSaveWithInterval(UsagePoint usagePoint, UsagePointMetrologyConfiguration metrologyConfiguration, Interval interval) {
        this.usagePoint.set(usagePoint);
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.interval = interval;
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
        return effectiveContracts.stream()
                .filter(effectiveContract -> effectiveContract.getMetrologyContract().equals(metrologyContract))
                .map(EffectiveMetrologyContractOnUsagePoint::getChannelsContainer)
                .findAny();
    }

    @Override
    public Optional<ChannelsContainer> getChannelsContainer(MetrologyContract metrologyContract, Instant when) {
        return effectiveContracts.stream()
                .filter(effectiveContract -> effectiveContract.getMetrologyContract()
                        .equals(metrologyContract) && effectiveContract.isEffectiveAt(when))
                .map(EffectiveMetrologyContractOnUsagePoint::getChannelsContainer)
                .findAny();
    }

    public void createEffectiveMetrologyContracts() {
        getMetrologyConfiguration().getContracts()
                .stream()
                .filter(metrologyContract -> !metrologyContract.getDeliverables().isEmpty())
                .filter(MetrologyContract::isMandatory)
                .forEach(metrologyContract -> this.effectiveContracts.add(this.dataModel.getInstance(EffectiveMetrologyContractOnUsagePointImpl.class)
                        .init(this, metrologyContract)));
    }

    @Override
    public void activateOptionalMetrologyContract(MetrologyContract metrologyContract, Instant when) {
        this.effectiveContracts.add(this.dataModel.getInstance(EffectiveMetrologyContractOnUsagePointImpl.class)
                .init(this, metrologyContract, Range.atLeast(when)));
    }

    @Override
    public void deactivateOptionalMetrologyContract(MetrologyContract metrologyContract, Instant when) {
        this.effectiveContracts
                .stream()
                .filter(effectiveMetrologyContract -> !effectiveMetrologyContract.getMetrologyContract().isMandatory())
                .filter(effectiveMetrologyContract -> effectiveMetrologyContract.getMetrologyContract()
                        .equals(metrologyContract))
                .filter(effectiveMetrologyContract -> !effectiveMetrologyContract.getRange().hasUpperBound())
                .findFirst()
                .ifPresent(effectiveMetrologyContract -> effectiveMetrologyContract.close(when));
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("usagePoint", this.usagePoint)
                .add("configuration", this.metrologyConfiguration)
                .toString();
    }

    public void prepareDelete() {
        effectiveContracts.clear();
    }

    @Override
    public Instant getStart() {
        return getRange().lowerEndpoint();
    }

    @Override
    public Instant getEnd() {
        Range<Instant> range = getRange();
        return range.hasUpperBound() ? range.upperEndpoint() : null;
    }

    @Override
    public long getId() {
        return id;
    }
}
