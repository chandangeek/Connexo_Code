package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import java.time.Instant;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Provides an implementation for the {@link UsagePointMetrologyConfiguration} interface.
 */
public class UsagePointMetrologyConfigurationImpl implements UsagePointMetrologyConfiguration {

    private final DataModel dataModel;

    @SuppressWarnings("unused")//Managed by ORM
    private Interval interval;
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    private Reference<MetrologyConfiguration> metrologyConfiguration = ValueReference.absent();

    @Inject
    public UsagePointMetrologyConfigurationImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    public UsagePointMetrologyConfigurationImpl initAndSave(UsagePoint usagePoint, MetrologyConfiguration metrologyConfiguration, Instant start) {
        this.usagePoint.set(usagePoint);
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.interval = Interval.startAt(start);
        this.dataModel.persist(this);
        return this;
    }

    @Override
    public MetrologyConfiguration getMetrologyConfiguration() {
        return metrologyConfiguration.get();
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
    public String toString() {
        return toStringHelper(this)
                .add("usagePoint", this.usagePoint)
                .add("metrologyConfiguration", this.metrologyConfiguration)
                .toString();
    }

}