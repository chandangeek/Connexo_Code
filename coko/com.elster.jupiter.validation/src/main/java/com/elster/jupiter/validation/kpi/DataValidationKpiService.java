/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.kpi;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;

import aQute.bnd.annotation.ProviderType;

import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface DataValidationKpiService {

    DataValidationKpiBuilder newDataValidationKpi(EndDeviceGroup group);

    List<DataValidationKpi> findAllDataValidationKpis();

    Finder<DataValidationKpi> dataValidationKpiFinder();

    Optional<DataValidationKpi> findDataValidationKpi(long id);

    Optional<DataValidationKpi> findAndLockDataValidationKpiByIdAndVersion(long id, long version);

    Optional<DataValidationKpi> findDataValidationKpi(EndDeviceGroup group);

    @ProviderType
    interface DataValidationKpiBuilder {

        DataValidationKpiBuilder frequency(TemporalAmount temporalAmount);

        DataValidationKpi build();

    }

}