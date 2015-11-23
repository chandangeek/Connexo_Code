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
        builder.addChannelSpec();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.add(this.toSqlFragment("ch_spec.obiscode", condition, now));
        sqlBuilder.append(" OR ");
        sqlBuilder.openBracket();
        sqlBuilder.append(" ch_spec.obiscode is null ");
        sqlBuilder.append(" AND ");
        sqlBuilder.add(this.toSqlFragment("ch_msr_type.obiscode", condition, now));
        sqlBuilder.closeBracket();
        sqlBuilder.closeBracket();
        return sqlBuilder;
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
