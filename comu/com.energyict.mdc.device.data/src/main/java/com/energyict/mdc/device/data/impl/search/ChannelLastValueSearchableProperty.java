/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.time.Instant;

public class ChannelLastValueSearchableProperty extends AbstractDateSearchableProperty<ChannelLastValueSearchableProperty> {

    static final String PROPERTY_NAME = "device.channel.last.value";

    @Inject
    public ChannelLastValueSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(ChannelLastValueSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.CHANNEL_LAST_VALUE;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addEndDevice();
    }

    /**
     * <code>
     * select MTR_METERACTIVATION.METERID from MTR_CHANNEL
     * right join MTR_METERACTIVATION on MTR_METERACTIVATION.ID = MTR_CHANNEL.METERACTIVATIONID AND MTR_METERACTIVATION.STARTTIME > {now} AND MTR_METERACTIVATION.ENDTIME < {now}
     * left join IDS_TIMESERIES on MTR_CHANNEL.TIMESERIESID = IDS_TIMESERIES.ID
     * where IDS_TIMESERIES.LASTTIME = {criteria};
     * </code>
     */
    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.END_DEVICE + ".id IN (");
        builder.append("select MTR_METERACTIVATION.METERID from MTR_CHANNEL " +
                "right join MTR_METERACTIVATION on MTR_METERACTIVATION.ID = MTR_CHANNEL.METERACTIVATIONID AND MTR_METERACTIVATION.STARTTIME <= ");
        builder.addLong(now.toEpochMilli());
        builder.append(" AND MTR_METERACTIVATION.ENDTIME > ");
        builder.addLong(now.toEpochMilli());
        builder.append(" left join IDS_TIMESERIES on MTR_CHANNEL.TIMESERIESID = IDS_TIMESERIES.ID " +
                "where ");
        builder.add(toSqlFragment("IDS_TIMESERIES.LASTTIME", condition, now));
        builder.closeBracket();
        return builder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
