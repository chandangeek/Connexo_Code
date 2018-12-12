/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.time.Instant;

public class LogbookNameSearchableProperty extends AbstractNameSearchableProperty<LogbookNameSearchableProperty> {

    static final String PROPERTY_NAME = "device.logbook.name";

    @Inject
    public LogbookNameSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(LogbookNameSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".id IN (");
        sqlBuilder.append("select DEVICEID " +
                "from DDC_LOGBOOK " +
                "join DTC_LOGBOOKSPEC on DTC_LOGBOOKSPEC.ID = DDC_LOGBOOK.LOGBOOKSPECID " +
                "join MDS_LOGBOOKTYPE on MDS_LOGBOOKTYPE.ID = DTC_LOGBOOKSPEC.LOGBOOKTYPEID " +
                "where ");
        sqlBuilder.add(this.toSqlFragment("MDS_LOGBOOKTYPE.NAME", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.LOGBOOK_NAME;
    }

}