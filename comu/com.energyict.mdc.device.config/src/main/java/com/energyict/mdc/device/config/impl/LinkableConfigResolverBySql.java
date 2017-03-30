/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.DeviceConfiguration;

import java.util.List;

@LiteralSql
public class LinkableConfigResolverBySql implements LinkableConfigResolver {

    private final Query<DeviceConfiguration> query;

    public LinkableConfigResolverBySql(Query<DeviceConfiguration> query) {
        this.query = query;
        query.setEager();
    }

    @Override
    public List<DeviceConfiguration> getLinkableDeviceConfigurations(final ValidationRuleSet ruleSet) {
        return query.select(ListOperator.IN.contains(() -> getBuilderFor(ruleSet), "id"), Order.ascending("name"));
    }

    @Override
    public List<DeviceConfiguration> getLinkableDeviceConfigurations(final EstimationRuleSet ruleSet) {
        return query.select(ListOperator.IN.contains(() -> getBuilderFor(ruleSet), "id"), Order.ascending("name"));
    }

    /**
     * @return a subquery that holds all ids of the eligible DeviceConfigurations.
     */
    private SqlBuilder getBuilderFor(ValidationRuleSet ruleSet) {
        SqlBuilder builder = new SqlBuilder();

        builder.append("select distinct(id) from ((select rs.deviceconfigid as id from dtc_registerspec rs where rs.registertypeid in" +
                "  (select id from mds_measurementtype rm " +
                "  where rm.readingtype in (select tivr.readingtypemrid from val_readingtypeinvalrule tivr " +
                "    where tivr.ruleid in (select vr.id from val_validationrule vr where vr.rulesetversionid in (select id from val_validationrulesetversion vrsv where vrsv.rulesetid = ");
        builder.addLong(ruleSet.getId());
        builder.append(")) )))" +
                " union" +
                " (select lps.deviceconfigid as id from dtc_loadprofilespec lps where lps.id in " +
                "  (select cs.loadprofilespecid from dtc_channelspec cs where cs.channeltypeid in" +
                "  (select id from mds_measurementtype rm " +
                "  where rm.readingtype in (select tivr.readingtypemrid from val_readingtypeinvalrule tivr " +
                "    where tivr.ruleid in (select vr.id from val_validationrule vr where vr.rulesetversionid in (select id from val_validationrulesetversion vrsv where vrsv.rulesetid = ");
        builder.addLong(ruleSet.getId());
        builder.append(")) )))))" +
                "  where id not in (select rsu.deviceconfigid from DTC_DEVCFGVALRULESETUSAGE rsu where rsu.validationrulesetid = ");
        builder.addLong(ruleSet.getId());
        builder.append(")");
        return builder;
    }

    /**
     * @return a subquery that holds all ids of the eligible DeviceConfigurations.
     */
    private SqlBuilder getBuilderFor(EstimationRuleSet ruleSet) {
        SqlBuilder builder = new SqlBuilder();

        builder.append("select distinct(id) from ((select rs.deviceconfigid as id from dtc_registerspec rs where rs.registertypeid in" +
                "  (select id from mds_measurementtype rm " +
                "  where rm.readingtype in (select tier.readingtypemrid from est_readingtypeinestrule tier " +
                "    where tier.ruleid in (select er.id from est_estimationrule er where er.rulesetid = ");
        builder.addLong(ruleSet.getId());
        builder.append(") )))" +
                " union" +
                " (select lps.deviceconfigid as id from dtc_loadprofilespec lps where lps.id in " +
                "  (select cs.loadprofilespecid from dtc_channelspec cs where cs.channeltypeid in" +
                "  (select id from mds_measurementtype rm " +
                "  where rm.readingtype in (select tier.readingtypemrid from est_readingtypeinestrule tier " +
                "    where tier.ruleid in (select er.id from est_estimationrule er where er.rulesetid = ");
        builder.addLong(ruleSet.getId());
        builder.append(") )))))" +
                "  where id not in (select rsu.deviceconfig from DTC_DEVCFGESTRULESETUSAGE rsu where rsu.estimationruleset = ");
        builder.addLong(ruleSet.getId());
        builder.append(")");
        return builder;
    }

}
