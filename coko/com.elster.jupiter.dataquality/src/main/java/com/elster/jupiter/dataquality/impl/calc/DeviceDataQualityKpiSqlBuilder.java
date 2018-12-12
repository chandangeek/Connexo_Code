/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.LiteralSql;

@LiteralSql
class DeviceDataQualityKpiSqlBuilder extends DataQualityKpiSqlBuilder {

    private final EndDeviceGroup deviceGroup;

    DeviceDataQualityKpiSqlBuilder(EndDeviceGroup deviceGroup) {
        super();
        this.deviceGroup = deviceGroup;
    }

    @Override
    QualityCodeSystem getQualityCodeSystem() {
        return QualityCodeSystem.MDC;
    }

    @Override
    void appendChannelsSubQuery() {
        super.sqlBuilder.append("SELECT id FROM MTR_CHANNEL WHERE channel_container IN (");
        super.sqlBuilder.append("    SELECT id FROM MTR_CHANNEL_CONTAINER WHERE meter_activation IN (");
        super.sqlBuilder.append("        SELECT id FROM MTR_METERACTIVATION WHERE meterid IN (");
        super.sqlBuilder.add(this.deviceGroup.toSubQuery("id").toFragment());
        super.sqlBuilder.append(")))");
    }

}
