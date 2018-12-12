/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.LiteralSql;

@LiteralSql
class UsagePointDataQualityKpiSqlBuilder extends DataQualityKpiSqlBuilder {

    private final UsagePointGroup usagePointGroup;
    private final MetrologyPurpose metrologyPurpose;

    UsagePointDataQualityKpiSqlBuilder(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose) {
        super();
        this.usagePointGroup = usagePointGroup;
        this.metrologyPurpose = metrologyPurpose;
    }

    @Override
    QualityCodeSystem getQualityCodeSystem() {
        return QualityCodeSystem.MDM;
    }

    @Override
    void appendChannelsSubQuery() {
        super.sqlBuilder.append("SELECT ch.id FROM MTR_CHANNEL ch");
        super.sqlBuilder.append("    JOIN MTR_EFFECTIVE_CONTRACT efc ON efc.channels_container = ch.channel_container");
        super.sqlBuilder.append("    JOIN MTR_METROLOGY_CONTRACT mc ON mc.id = efc.metrology_contract");
        super.sqlBuilder.append("    JOIN MTR_USAGEPOINTMTRCONFIG upmc ON upmc.id = efc.effective_conf");
        super.sqlBuilder.append("    WHERE mc.metrology_purpose = ");
        super.sqlBuilder.addLong(this.metrologyPurpose.getId());
        super.sqlBuilder.append("      AND upmc.usagepoint IN (");
        super.sqlBuilder.add(this.usagePointGroup.toSubQuery("id").toFragment());
        super.sqlBuilder.append(")");
    }
}
