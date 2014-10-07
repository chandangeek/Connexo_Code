package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;

import javax.inject.Inject;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Provides an implementation for the {@link DataCollectionKpi} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-06 (10:25)
 */
@MustHaveEitherConnectionSetupOrComTaskExecution(groups = {Save.Create.class, Save.Update.class})
public class DataCollectionKpiImpl implements DataCollectionKpi, PersistenceAware {

    public enum Fields {
        CONNECTION_KPI("connectionKpi"),
        COMMUNICATION_KPI("communicationKpi"),
        END_DEVICE_GROUP("endDeviceGroup");

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
    private Reference<Kpi> connectionKpi = ValueReference.absent();
    private KpiBuilder connectionKpiBuilder = new StubKpiBuilder();
    private Reference<Kpi> communicationKpi = ValueReference.absent();
    private KpiBuilder communicationKpiBuilder = new StubKpiBuilder();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_GROUP_IS_REQUIRED + "}")
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();

    @Inject
    public DataCollectionKpiImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    DataCollectionKpiImpl initialize(EndDeviceGroup group) {
        this.endDeviceGroup.set(group);
        return this;
    }

    public void save() {
        this.connectionKpi.set(this.connectionKpiBuilder.build());
        this.communicationKpi.set(this.communicationKpiBuilder.build());
        Save.action(this.getId()).save(this.dataModel, this);
    }

    @Override
    public void postLoad() {
        this.connectionKpiBuilder = new KpiBuilderForUpdate(this.connectionKpi.orNull());
        this.communicationKpiBuilder = new KpiBuilderForUpdate(this.communicationKpi.orNull());
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public EndDeviceGroup getDeviceGroup() {
        return this.endDeviceGroup.get();
    }

    @Override
    public boolean calculatesConnectionSetupKpi() {
        return this.connectionKpi.isPresent();
    }

    @Override
    public Optional<TemporalAmount> connectionSetupKpiCalculationIntervalLength() {
        if (this.connectionKpi.isPresent()) {
            return Optional.of(this.connectionKpi.get().getIntervalLength());
        } else {
            return Optional.empty();
        }
    }

    Optional<Kpi> connectionKpi() {
        if (this.connectionKpi.isPresent()) {
            return Optional.of(this.connectionKpi.get());
        } else {
            return Optional.empty();
        }
    }

    KpiBuilder connectionKpiBuilder() {
        return this.connectionKpiBuilder;
    }

    void connectionKpiBuilder(KpiBuilder kpiBuilder) {
        this.connectionKpiBuilder = kpiBuilder;
    }

    @Override
    public boolean calculatesComTaskExecutionKpi() {
        return this.communicationKpi.isPresent();
    }

    @Override
    public Optional<TemporalAmount> comTaskExecutionKpiCalculationIntervalLength() {
        if (this.communicationKpi.isPresent()) {
            return Optional.of(this.communicationKpi.get().getIntervalLength());
        } else {
            return Optional.empty();
        }
    }

    Optional<Kpi> communicationKpi() {
        if (this.communicationKpi.isPresent()) {
            return Optional.of(this.communicationKpi.get());
        } else {
            return Optional.empty();
        }
    }

    KpiBuilder communicationKpiBuilder() {
        return this.communicationKpiBuilder;
    }

    void communicationKpiBuilder(KpiBuilder kpiBuilder) {
        this.communicationKpiBuilder = kpiBuilder;
    }

    @Override
    public void delete() {
        /* Todo: Kpi has no delete method yet due to problems with journalling
                 Remove the @Ignore from com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiImplTest.testDeleteAlsoDeletesKoreKPIs
                 once this is implemented properly
        if (this.connectionKpi.isPresent()) {
            this.connectionKpi.get().delete();
        }
        if (this.communicationKpi.isPresent()) {
            this.communicationKpi.get().delete();
        }
        */
        this.dataModel.remove(this);
    }

    /**
     * Provides a stub implementation for the KpiBuilder interface.
     */
    private class StubKpiBuilder implements KpiBuilder {
        @Override
        public Kpi build() {
            return null;
        }

        @Override
        public KpiBuilder named(String s) {
            return this;
        }

        @Override
        public KpiMemberBuilder member() {
            return null;
        }

        @Override
        public KpiBuilder timeZone(TimeZone timeZone) {
            return this;
        }

        @Override
        public KpiBuilder interval(TemporalAmount temporalAmount) {
            return this;
        }
    }

    private class KpiBuilderForUpdate implements KpiBuilder {
        private final Kpi kpi;

        private KpiBuilderForUpdate(Kpi kpi) {
            super();
            this.kpi = kpi;
        }

        @Override
        public Kpi build() {
            return this.kpi;
        }

        @Override
        public KpiBuilder named(String s) {
            return this;
        }

        @Override
        public KpiMemberBuilder member() {
            return null;
        }

        @Override
        public KpiBuilder timeZone(TimeZone timeZone) {
            return this;
        }

        @Override
        public KpiBuilder interval(TemporalAmount temporalAmount) {
            return this;
        }
    }

}