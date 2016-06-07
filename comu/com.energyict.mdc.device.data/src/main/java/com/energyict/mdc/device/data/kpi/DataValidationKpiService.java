package com.energyict.mdc.device.data.kpi;


import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;

import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

public interface DataValidationKpiService {

    DataValidationKpiBuilder newDataValidationKpi(EndDeviceGroup group);

    List<DataValidationKpi> findAllDataValidationKpis();

    Finder<DataValidationKpi> dataValidationKpiFinder();

    Optional<DataValidationKpi> findDataValidationKpi(long id);

    Optional<DataValidationKpi> findAndLockDataValidationKpiByIdAndVersion(long id, long version);

    Optional<DataValidationKpi> findDataValidationKpi(EndDeviceGroup group);

    interface DataValidationKpiBuilder {

        DataValidationKpiBuilder frequency(TemporalAmount temporalAmount);

        DataValidationKpi build();

    }

}
