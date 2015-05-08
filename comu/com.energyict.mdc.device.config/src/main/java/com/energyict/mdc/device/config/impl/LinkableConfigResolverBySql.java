package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.DeviceConfiguration;

import java.util.List;

/**
 * LinkableConfigResolver implementation that determines the DeviceConfigurations entirely by delegating to a literal SQL query.
 * This implementation was made as a performance improvement over puzzling the result together in code.
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/07/2014
 * Time: 14:14
 */
@LiteralSql
public class LinkableConfigResolverBySql implements LinkableConfigResolver {

    private final Query<DeviceConfiguration> query;

    public LinkableConfigResolverBySql(Query<DeviceConfiguration> query) {
        this.query = query;
        query.setEager();
    }

    @Override
    public List<DeviceConfiguration> getLinkableDeviceConfigurations(final ValidationRuleSet ruleSet) {
        return query.select(ListOperator.IN.contains(new Subquery() {
            @Override
            public SqlFragment toFragment() {
                return getBuilderFor(ruleSet);
            }
        }, "id"), "name");
    }

    /**
     * @return a subquery that holds all ids of the eligible DeviceConfigurations.
     */
    private SqlBuilder getBuilderFor(ValidationRuleSet ruleSet) {
        SqlBuilder builder = new SqlBuilder();

        builder.append("select distinct(id) from ((select rs.deviceconfigid as id from dtc_registerspec rs where rs.registertypeid in" +
                "  (select id from mds_measurementtype rm " +
                "  where rm.readingtype in (select tivr.readingtypemrid from val_readingtypeinvalrule tivr " +
                "    where tivr.ruleid in (select vr.id from val_validationrule vr where vr.rulesetid = ");
        builder.addLong(ruleSet.getId());
        builder.append(") )))" +
                " union" +
                " (select lps.deviceconfigid as id from dtc_loadprofilespec lps where lps.id in " +
                "  (select cs.loadprofilespecid from dtc_channelspec cs where cs.channeltypeid in" +
                "  (select id from mds_measurementtype rm " +
                "  where rm.readingtype in (select tivr.readingtypemrid from val_readingtypeinvalrule tivr " +
                "    where tivr.ruleid in (select vr.id from val_validationrule vr where vr.rulesetid = ");
        builder.addLong(ruleSet.getId());
        builder.append(") )))))" +
                "  where id not in (select rsu.deviceconfigid from DTC_DEVCFGVALRULESETUSAGE rsu where rsu.validationrulesetid = ");
        builder.addLong(ruleSet.getId());
        builder.append(")");
        return builder;
    }
}
