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

public class LogbookLastEventTimestampSearchableProperty extends AbstractDateSearchableProperty<LogbookLastEventTimestampSearchableProperty> {

    static final String PROPERTY_NAME = "device.logbook.last.event";

    @Inject
    public LogbookLastEventTimestampSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(LogbookLastEventTimestampSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.LOGBOOK_LAST_EVENT;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".id IN (" +
                "select DEVICEID from DDC_LOGBOOK where ");
        builder.add(toSqlFragment("DDC_LOGBOOK.LASTLOGBOOK", condition, now));
        builder.closeBracket();
        return builder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
