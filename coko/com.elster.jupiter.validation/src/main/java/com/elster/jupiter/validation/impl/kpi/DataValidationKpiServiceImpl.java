package com.elster.jupiter.validation.impl.kpi;


import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.kpi.DataValidationKpi;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

public class DataValidationKpiServiceImpl implements DataValidationKpiService{

    private final ValidationService validationService;

    public DataValidationKpiServiceImpl(ValidationService validationService){
        this.validationService = validationService;
    }

    @Override
    public DataValidationKpiBuilder newDataValidationKpi(EndDeviceGroup group) {
        return new DataValidationKpiBuilderImpl(group);
    }

    @Override
    public List<DataValidationKpi> findAllDataValidationKpis() {
        return this.validationService.dataModel().mapper(DataValidationKpi.class).find();
    }

    @Override
    public Finder<DataValidationKpi> dataValidationKpiFinder() {
        return DefaultFinder.of(DataValidationKpi.class, this.validationService.dataModel(), EndDeviceGroup.class).defaultSortColumn(DataValidationKpiImpl.Fields.END_DEVICE_GROUP.fieldName()+".name");
    }

    @Override
    public Optional<DataValidationKpi> findDataValidationKpi(long id) {
        return this.validationService.dataModel().mapper(DataValidationKpi.class).getOptional(id);
    }

    @Override
    public Optional<DataValidationKpi> findAndLockDataValidationKpiByIdAndVersion(long id, long version) {
        return this.validationService.dataModel().mapper(DataValidationKpi.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<DataValidationKpi> findDataValidationKpi(EndDeviceGroup group) {
        return this.validationService.dataModel().mapper(DataValidationKpi.class).getUnique(DataValidationKpiImpl.Fields.END_DEVICE_GROUP
                .fieldName(), group);
    }




    private class DataValidationKpiBuilderImpl implements DataValidationKpiBuilder {

        private final DataValidationKpiImpl underConstruction;
        private DataValidationKpiBuilderState state = DataValidationKpiBuilderState.INCOMPLETE;

        private DataValidationKpiBuilderImpl(EndDeviceGroup group){
            this.underConstruction = validationService.dataModel().getInstance(DataValidationKpiImpl.class).initialize(group);
        }

        @Override
        public DataValidationKpiBuilder frequency(TemporalAmount temporalAmount) {
            this.underConstruction.setFrequency(temporalAmount);
            return this;
        }

        @Override
        public DataValidationKpi build() {
            this.state = this.state.build(this.underConstruction, validationService.kpiService().newKpi());
            return this.underConstruction;
        }
    }

    private enum DataValidationKpiBuilderState {
        INCOMPLETE {
            @Override
            DataValidationKpiBuilderState build(DataValidationKpiImpl underConstruction, KpiBuilder dataValidationKpiBuilder) {
                if (dataValidationKpiBuilder != null) {
                  underConstruction.dataValidationKpiBuilder(dataValidationKpiBuilder, underConstruction.getDeviceGroup());
                }
                underConstruction.save();
                return COMPLETE;
            }
        },

        COMPLETE {
            @Override
            DataValidationKpiBuilderState build(DataValidationKpiImpl underConstruction, KpiBuilder dataValidationKpiBuilder) {
                throw new IllegalStateException("DataValidationKpi has already been saved");
            }
        };

        abstract DataValidationKpiBuilderState build(DataValidationKpiImpl underConstruction, KpiBuilder dataValidationKpiBuilder);
    }

}
