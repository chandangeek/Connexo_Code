package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.time.Instant;

public class ChannelObisCodeSearchableProperty extends AbstractObisCodeSearchableProperty<ChannelObisCodeSearchableProperty> {

    static final String PROPERTY_NAME = "device.channel.obiscode";

    @Inject
    public ChannelObisCodeSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(ChannelObisCodeSearchableProperty.class, propertySpecService, thesaurus);
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
                "where ");
        builder.add(this.toSqlFragment("DTC_CHANNELSPEC.OBISCODE", condition, now));
        builder.append(" OR ");
        builder.openBracket();
        builder.append(" DTC_CHANNELSPEC.OBISCODE is null ");
        builder.append(" AND ");
        builder.add(this.toSqlFragment("MDS_MEASUREMENTTYPE.obiscode", condition, now));
        builder.closeBracket();
        builder.closeBracket();
        return builder;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(PropertyTranslationKeys.CHANNEL_OBISCODE).format();
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
