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

/**
 * For now the same logic as for {@link LoadProfileLastReadingSearchableProperty}, due to: <br />
 *      1) Java doc of {@link com.energyict.mdc.device.data.impl.LoadProfileImpl.ChannelImpl}<br />
 *      2) field initialization logic in {@link com.energyict.mdc.protocol.api.device.data.ChannelInfo}
 */
public class ChannelLastReadingSearchableProperty extends AbstractDateSearchableProperty<ChannelLastReadingSearchableProperty> {

    static final String PROPERTY_NAME = "device.channel.last.reading";

    @Inject
    public ChannelLastReadingSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(ChannelLastReadingSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.CHANNEL_LAST_READING;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".id IN (");
        builder.append("select DDC_LOADPROFILE.DEVICEID from DTC_CHANNELSPEC " +
                "left join DDC_LOADPROFILE on DTC_CHANNELSPEC.LOADPROFILESPECID = DDC_LOADPROFILE.LOADPROFILESPECID " +
                "where ");
        builder.add(toSqlFragment("DDC_LOADPROFILE.LASTREADING", condition, now));
        builder.closeBracket();
        return builder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
