/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import javax.inject.Inject;
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
        return DefaultFinder.of(DataCollectionKpi.class, this.deviceDataModelService.dataModel(), EndDeviceGroup.class).defaultSortColumn(DataCollectionKpiImpl.Fields.END_DEVICE_GROUP.fieldName()+".name");
    }

    @Override
    public Optional<DataCollectionKpi> findDataCollectionKpi(long id) {
        return this.deviceDataModelService.dataModel().mapper(DataCollectionKpi.class).getOptional(id);
    }

    @Override
    public Optional<DataCollectionKpi> findAndLockDataCollectionKpiByIdAndVersion(long id, long version) {
        return this.deviceDataModelService.dataModel().mapper(DataCollectionKpi.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<DataCollectionKpi> findDataCollectionKpi(EndDeviceGroup group) {
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
                if (connectionKpiBuilder!=null) {
                    underConstruction.connectionKpiBuilder(connectionKpiBuilder);
                }
                if (communicationTaskBuilder!=null) {
                    underConstruction.communicationKpiBuilder(communicationTaskBuilder);
                }
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
        public DataCollectionKpiBuilder displayPeriod(TimeDuration displayPeriod) {
            this.underConstruction.setDisplayRange(displayPeriod);
            return this;
        }

        @Override
        public DataCollectionKpiBuilder frequency(TemporalAmount temporalAmount) {
            this.underConstruction.setFrequency(temporalAmount);
            return this;
        }

        @Override
        public KpiTargetBuilder calculateConnectionSetupKpi() {
            this.connectionKpiBuilder = deviceDataModelService.kpiService().newKpi();
            return new KpiTargetBuilderImpl(this.connectionKpiBuilder, underConstruction.getFrequency());
        }

        @Override
        public KpiTargetBuilder calculateComTaskExecutionKpi() {
            this.communicationKpiBuilder = deviceDataModelService.kpiService().newKpi();
            return new KpiTargetBuilderImpl(this.communicationKpiBuilder, underConstruction.getFrequency());
        }

        @Override
        public DataCollectionKpi save() {
            this.state = this.state.save(this.underConstruction, this.connectionKpiBuilder, this.communicationKpiBuilder);
            return this.underConstruction;
        }
    }

    static class KpiTargetBuilderImpl implements KpiTargetBuilder {
        private final KpiBuilder kpiBuilder;
        private final List<KpiBuilder.KpiMemberBuilder> memberBuilders;

        KpiTargetBuilderImpl(KpiBuilder kpiBuilder, TemporalAmount intervalLength) {
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