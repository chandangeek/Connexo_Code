package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyContractOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;

public class EffectiveMetrologyContractOnUsagePointImpl implements EffectiveMetrologyContractOnUsagePoint {

    public enum Fields {
        INTERVAL("interval"),
        EFFECTIVE_CONF("metrologyConfiguration"),
        METROLOGY_CONTRACT("metrologyContract"),
        CHANNELS_CONTAINER("channelsContainer"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;

    private long id;
    private Interval interval;
    private Reference<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfiguration = ValueReference.absent();
    private Reference<MetrologyContract> metrologyContract = ValueReference.absent();
    private Reference<ChannelsContainer> channelsContainer = ValueReference.absent();

    @Inject
    public EffectiveMetrologyContractOnUsagePointImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public EffectiveMetrologyContractOnUsagePointImpl init(EffectiveMetrologyConfigurationOnUsagePoint metrologyConfiguration, MetrologyContract metrologyContract) {
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.metrologyContract.set(metrologyContract);
        this.interval = metrologyConfiguration.getInterval();
        this.channelsContainer.set(this.dataModel.getInstance(MetrologyContractChannelsContainerImpl.class).init(this));
        return this;
    }

    @Override
    public EffectiveMetrologyConfigurationOnUsagePoint getMetrologyConfigurationOnUsagePoint() {
        return this.metrologyConfiguration.get();
    }

    @Override
    public MetrologyContract getMetrologyContract() {
        return this.metrologyContract.get();
    }

    @Override
    public ChannelsContainer getChannelsContainer() {
        return this.channelsContainer.get();
    }

    @Override
    public Interval getInterval() {
        return getMetrologyContract().isMandatory() ? getMetrologyConfigurationOnUsagePoint().getInterval() : this.interval;
    }

    @Override
    public long getId() {
        return this.id;
    }
}
