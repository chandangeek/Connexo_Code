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

public class LogbookObisCodeSearchableProperty extends AbstractObisCodeSearchableProperty<LogbookObisCodeSearchableProperty> {

    static final String PROPERTY_NAME = "device.logbook.obiscode";

    @Inject
    public LogbookObisCodeSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(LogbookObisCodeSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.LOGBOOK_OBISCODE;
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
        sqlBuilder.add(this.toSqlFragment("DTC_LOGBOOKSPEC.OBISCODE", condition, now));
        sqlBuilder.append(" OR ");
        sqlBuilder.openBracket();
        sqlBuilder.append("DTC_LOGBOOKSPEC.OBISCODE is null");
        sqlBuilder.append(" AND ");
        sqlBuilder.add(this.toSqlFragment("MDS_LOGBOOKTYPE.OBISCODE", condition, now));
        sqlBuilder.closeBracket();
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
