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
        METROLOGY_CONTRACT_ID(6),
        IS_EFFECTIVE_CONFIG(7),
        LAST_SUSPECT(8);

        private int index;

        ResultSetColumn(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }

    private String usagePointName;
    private ServiceKind serviceKind;
    private UsagePointConfigurationOverview usagePointConfigurationOverview;
    private DataQualityKpiResultsImpl dataQualityKpiResults;

    static DataQualityOverviewImpl from(ResultSet resultSet, DataQualityOverviewSpecificationImpl specification) throws SQLException {
        DataQualityOverviewImpl dataQualityOverview = new DataQualityOverviewImpl();
        dataQualityOverview.usagePointName = resultSet.getString(ResultSetColumn.USAGEPOINT_NAME.index());
        dataQualityOverview.serviceKind = fetchServiceKind(resultSet);
        dataQualityOverview.usagePointConfigurationOverview = UsagePointConfigurationOverviewImpl.from(resultSet);
        dataQualityOverview.dataQualityKpiResults = DataQualityKpiResultsImpl.from(resultSet, specification);
        return dataQualityOverview;
    }

    private static ServiceKind fetchServiceKind(ResultSet resultSet) throws SQLException {
        int serviceKindIndex = Long.valueOf(resultSet.getLong(ResultSetColumn.SERVICE_KIND_ID.index())).intValue() - 1;
        return ServiceKind.values()[serviceKindIndex];
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
    public UsagePointConfigurationOverview getConfigurationOverview() {
        return usagePointConfigurationOverview;
    }

    @Override
    public DataQualityKpiResults getDataQualityKpiResults() {
        return dataQualityKpiResults;
    }

    private static class UsagePointConfigurationOverviewImpl implements UsagePointConfigurationOverview {

        private long metrologyConfigurationId;
        private String metrologyConfigurationName;
        private long metrologyPurposeId;
        private long metrologyContractId;
        private boolean isEffective;

        private static UsagePointConfigurationOverview from(ResultSet resultSet) throws SQLException {
            UsagePointConfigurationOverviewImpl overview = new UsagePointConfigurationOverviewImpl();
            overview.metrologyConfigurationId = resultSet.getLong(ResultSetColumn.METROLOGY_CONFIG_ID.index());
            overview.metrologyConfigurationName = resultSet.getString(ResultSetColumn.METROLOGY_CONFIG_NAME.index());
            overview.metrologyPurposeId = resultSet.getLong(ResultSetColumn.METROLOGY_PURPOSE_ID.index());
            overview.metrologyContractId = resultSet.getLong(ResultSetColumn.METROLOGY_CONTRACT_ID.index());
            overview.isEffective = resultSet.getString(ResultSetColumn.IS_EFFECTIVE_CONFIG.index()).equalsIgnoreCase("Y");
            return overview;
        }

        @Override
        public long getMetrologyConfigurationId() {
            return metrologyConfigurationId;
        }

        @Override
        public String getMetrologyConfigurationName() {
            return metrologyConfigurationName;
        }

        @Override
        public boolean isEffective() {
            return isEffective;
        }

        @Override
        public long getMetrologyPurposeId() {
            return metrologyPurposeId;
        }

        @Override
        public long getMetrologyContractId() {
            return metrologyContractId;
        }
    }
}
