/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.time.Instant;

public class ChannelReadingTypeNameSearchableProperty extends AbstractReadingTypeNameSearchableProperty<ChannelReadingTypeNameSearchableProperty> {

    static final String PROPERTY_NAME = "device.channel.reading.type.name";

    @Inject
    public ChannelReadingTypeNameSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(ChannelReadingTypeNameSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID IN (");
        builder.append("select DTC_CHANNELSPEC.DEVICECONFIGID " +
                "from DTC_CHANNELSPEC " +
                "join MDS_MEASUREMENTTYPE on MDS_MEASUREMENTTYPE.ID = DTC_CHANNELSPEC.CHANNELTYPEID " +
                "join MTR_READINGTYPE on MTR_READINGTYPE.MRID = MDS_MEASUREMENTTYPE.READINGTYPE " +
                "where ");
        builder.add(this.toSqlFragment("MTR_READINGTYPE.ALIASNAME", condition, now));
        builder.closeBracket();
        return builder;
    }
}
