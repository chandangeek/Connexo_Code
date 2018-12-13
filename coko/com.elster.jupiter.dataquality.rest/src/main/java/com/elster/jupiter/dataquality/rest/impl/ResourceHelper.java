/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpi;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;

import javax.inject.Inject;

public class ResourceHelper {

    private final DataQualityKpiService dataQualityKpiService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public ResourceHelper(DataQualityKpiService dataQualityKpiService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.dataQualityKpiService = dataQualityKpiService;
        this.conflictFactory = conflictFactory;
    }

    public DataQualityKpi findAndLockDataQualityKpi(DataQualityKpiInfo info) {
        return dataQualityKpiService.findAndLockDataQualityKpiByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn("" + info.id)
                        .withActualVersion(() -> dataQualityKpiService.findDataQualityKpi(info.id).map(DataQualityKpi::getVersion).orElse(null))
                        .supplier());
    }
}
