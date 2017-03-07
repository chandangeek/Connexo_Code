/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.validation.Validator;
import com.energyict.mdc.device.dataquality.DataQualityKpiResults;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

class DataQualityKpiResultsImpl implements DataQualityKpiResults {

    private Instant lastSuspect;
    private Map<KpiType, Long> kpiTypes = new HashMap<>();

    static DataQualityKpiResultsImpl from(ResultSet resultSet, DataQualityOverviewSpecificationImpl specification) throws SQLException {
        DataQualityKpiResultsImpl result = new DataQualityKpiResultsImpl();
        result.lastSuspect = Instant.ofEpochMilli(resultSet.getLong(DataQualityOverviewImpl.ResultSetColumn.LAST_SUSPECT.index()));
        for (KpiType kpiType : specification.getAvailableKpiTypes()) {
            result.kpiTypes.put(kpiType, resultSet.getLong(kpiType.withClauseAliasName()));
        }
        return result;
    }

    @Override
    public Instant getLastSuspect() {
        return lastSuspect;
    }

    @Override
    public long getAmountOfSuspects() {
        return kpiTypes.get(KpiType.SUSPECT);
    }

    @Override
    public long getChannelSuspects() {
        return kpiTypes.get(KpiType.CHANNEL);
    }

    @Override
    public long getRegisterSuspects() {
        return kpiTypes.get(KpiType.REGISTER);
    }

    @Override
    public long getAmountOfAdded() {
        return kpiTypes.get(KpiType.ADDED);
    }

    @Override
    public long getAmountOfEdited() {
        return kpiTypes.get(KpiType.EDITED);
    }

    @Override
    public long getAmountOfRemoved() {
        return kpiTypes.get(KpiType.REMOVED);
    }

    @Override
    public long getAmountOfConfirmed() {
        return kpiTypes.get(KpiType.CONFIRMED);
    }

    @Override
    public long getAmountOfEstimates() {
        return kpiTypes.get(KpiType.ESTIMATED);
    }

    @Override
    public long getAmountOfInformatives() {
        return kpiTypes.get(KpiType.INFORMATIVE);
    }

    @Override
    public long getAmountOfSuspectsBy(Validator validator) {
        return kpiTypes.get(new KpiType.ValidatorKpiType(validator));
    }

    @Override
    public long getAmountOfEstimatesBy(Estimator estimator) {
        return kpiTypes.get(new KpiType.EstimatorKpiType(estimator));
    }
}