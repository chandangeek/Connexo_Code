package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link DataCollectionKpiService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-06 (11:43)
 */
public class DataCollectionKpiServiceImpl implements DataCollectionKpiService {

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public DataCollectionKpiServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public List<DataCollectionKpi> findAllDataCollectionKpis() {
        return this.deviceDataModelService.dataModel().mapper(DataCollectionKpi.class).find();
    }

    @Override
    public Finder<DataCollectionKpi> dataCollectionKpiFinder() {
        return DefaultFinder.of(DataCollectionKpi.class, this.deviceDataModelService.dataModel());
    }

    @Override
    public Optional<DataCollectionKpi> findDataCollectionKpi(long id) {
        return this.deviceDataModelService.dataModel().mapper(DataCollectionKpi.class).getOptional(id);
    }

    @Override
    public Optional<DataCollectionKpi> findDataCollectionKpi(QueryEndDeviceGroup group) {
        return this.deviceDataModelService.dataModel().mapper(DataCollectionKpi.class).getUnique(DataCollectionKpiImpl.Fields.END_DEVICE_GROUP
                .fieldName(), group);
    }

    @Override
    public DataCollectionKpiBuilder newDataCollectionKpi(EndDeviceGroup group) {
        return new DataCollectionKpiBuilderImpl(group);
    }

    private enum DataCollectionKpiBuilderState {
        INCOMPLETE {
            @Override
            DataCollectionKpiBuilderState save(DataCollectionKpiImpl underConstruction, KpiBuilder connectionKpiBuilder, KpiBuilder communicationTaskBuilder) {
                underConstruction.connectionKpiBuilder(connectionKpiBuilder);
                underConstruction.communicationKpiBuilder(communicationTaskBuilder);
                underConstruction.save();
                return COMPLETE;
            }
        },

        COMPLETE {
            @Override
            DataCollectionKpiBuilderState save(DataCollectionKpiImpl underConstruction, KpiBuilder connectionKpiBuilder, KpiBuilder communicationTaskBuilder) {
                throw new IllegalStateException("DataCollectionKpi has already been saved");
            }
        };

        abstract DataCollectionKpiBuilderState save(DataCollectionKpiImpl underConstruction, KpiBuilder connectionSetupKpiBuilder, KpiBuilder comTaskExecutionKpiBuilder);
    }

    private class DataCollectionKpiBuilderImpl implements DataCollectionKpiBuilder {
        private final DataCollectionKpiImpl underConstruction;
        private KpiBuilder connectionKpiBuilder;
        private KpiBuilder communicationKpiBuilder;
        private DataCollectionKpiBuilderState state = DataCollectionKpiBuilderState.INCOMPLETE;

        private DataCollectionKpiBuilderImpl(EndDeviceGroup group) {
            this.underConstruction = deviceDataModelService.dataModel().getInstance(DataCollectionKpiImpl.class).initialize(group);
        }

        @Override
        public KpiTargetBuilder calculateConnectionSetupKpi(TemporalAmount intervalLength) {
            this.connectionKpiBuilder = deviceDataModelService.kpiService().newKpi();
            return new KpiTargetBuilderImpl(this.connectionKpiBuilder, intervalLength);
        }

        @Override
        public KpiTargetBuilder calculateComTaskExecutionKpi(TemporalAmount intervalLength) {
            this.communicationKpiBuilder = deviceDataModelService.kpiService().newKpi();
            return new KpiTargetBuilderImpl(this.communicationKpiBuilder, intervalLength);
        }

        @Override
        public DataCollectionKpi save() {
            this.state = this.state.save(this.underConstruction, this.connectionKpiBuilder, this.communicationKpiBuilder);
            return this.underConstruction;
        }
    }

    private class KpiTargetBuilderImpl implements KpiTargetBuilder {
        private final KpiBuilder kpiBuilder;
        private final List<KpiBuilder.KpiMemberBuilder> memberBuilders;

        private KpiTargetBuilderImpl(KpiBuilder kpiBuilder, TemporalAmount intervalLength) {
            super();
            this.kpiBuilder = kpiBuilder;
            this.kpiBuilder.interval(intervalLength);
            this.memberBuilders =
                    Stream.of(MonitoredTaskStatus.values()).
                            map(s -> kpiBuilder.member().named(s.name())).
                            map(s -> {
                                s.add();
                                return s;
                            }).
                            collect(Collectors.toList());
        }

        @Override
        public void expectingAsMinimum(BigDecimal target) {
            for (KpiBuilder.KpiMemberBuilder builder : this.memberBuilders) {
                builder.asMinimum().withTargetSetAt(target);
            }
        }

        @Override
        public void expectingAsMaximum(BigDecimal target) {
            for (KpiBuilder.KpiMemberBuilder builder : this.memberBuilders) {
                builder.asMaximum().withTargetSetAt(target);
            }
        }

    }

}