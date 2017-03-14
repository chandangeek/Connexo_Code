/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.impl;

import com.elster.jupiter.mdm.dataquality.DataQualityKpiResults;
import com.elster.jupiter.mdm.dataquality.DataQualityOverview;
import com.elster.jupiter.metering.ServiceKind;

import java.sql.ResultSet;
import java.sql.SQLException;

class DataQualityOverviewImpl implements DataQualityOverview {

    enum ResultSetColumn {
        USAGEPOINT_NAME(1),
        SERVICE_KIND_ID(2),
        METROLOGY_CONFIG_ID(3),
        METROLOGY_CONFIG_NAME(4),
        METROLOGY_PURPOSE_ID(5),
        LAST_SUSPECT(6);

        private int index;

        ResultSetColumn(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }

    private final String usagePointName;
    private final ServiceKind serviceKind;
    private final IdWithNameImpl metrologyConfiguration;
    private final Long metrologyPurposeId;
    private final DataQualityKpiResultsImpl deviceValidationKpiResults;

    static DataQualityOverviewImpl from(ResultSet resultSet, DataQualityOverviewSpecificationImpl specification) throws SQLException {
        String usagePointName = resultSet.getString(ResultSetColumn.USAGEPOINT_NAME.index());
        int serviceKindIndex = Long.valueOf(resultSet.getLong(ResultSetColumn.SERVICE_KIND_ID.index())).intValue() - 1;
        ServiceKind serviceKind = ServiceKind.values()[serviceKindIndex];

        IdWithNameImpl metrologyConfiguration = new IdWithNameImpl(
                resultSet.getLong(ResultSetColumn.METROLOGY_CONFIG_ID.index()),
                resultSet.getString(ResultSetColumn.METROLOGY_CONFIG_NAME.index())
        );
        long metrologyPurposeId = resultSet.getLong(ResultSetColumn.METROLOGY_PURPOSE_ID.index());
        return new DataQualityOverviewImpl(
                usagePointName,
                serviceKind,
                metrologyConfiguration,
                metrologyPurposeId,
                DataQualityKpiResultsImpl.from(resultSet, specification));
    }

    private DataQualityOverviewImpl(String usagePointName, ServiceKind serviceKind, IdWithNameImpl metrologyConfiguration,
                                    Long metrologyPurposeId, DataQualityKpiResultsImpl deviceValidationKpiResults) {
        this.usagePointName = usagePointName;
        this.serviceKind = serviceKind;
        this.metrologyConfiguration = metrologyConfiguration;
        this.metrologyPurposeId = metrologyPurposeId;
        this.deviceValidationKpiResults = deviceValidationKpiResults;
    }

    @Override
    public String getUsagePointName() {
        return usagePointName;
    }

    @Override
    public ServiceKind getServiceKind() {
        return serviceKind;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IdWithNameImpl getMetrologyConfiguration() {
        return metrologyConfiguration;
    }

    @Override
    public long getMetrologyPurposeId() {
        return metrologyPurposeId;
    }

    @Override
    public DataQualityKpiResults getDataQualityKpiResults() {
        return deviceValidationKpiResults;
    }
}
