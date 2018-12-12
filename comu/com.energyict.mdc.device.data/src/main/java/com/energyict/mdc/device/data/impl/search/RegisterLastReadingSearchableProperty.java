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

public class RegisterLastReadingSearchableProperty extends AbstractDateSearchableProperty<RegisterLastReadingSearchableProperty> {
    static final String PROPERTY_NAME = "device.register.last.reading";

    @Inject
    public RegisterLastReadingSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(RegisterLastReadingSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addEndDevice();
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.REGISTER_LAST_READING;
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.END_DEVICE + ".id IN (");
        builder.append("select MTR_METERACTIVATION.METERID " +
                "from IDS_VAULT_MTR_2 " +
                "join MTR_CHANNEL on MTR_CHANNEL.TIMESERIESID = IDS_VAULT_MTR_2.TIMESERIESID " +
                "join MTR_METERACTIVATION on MTR_METERACTIVATION.ID = MTR_CHANNEL.METERACTIVATIONID " +
                "group by MTR_METERACTIVATION.METERID, MTR_CHANNEL.MAINREADINGTYPEMRID " +
                "having ");
        builder.add(toSqlFragment("MAX(IDS_VAULT_MTR_2.RECORDTIME)", condition, now));
        builder.closeBracket();
        return builder;
    }
}
