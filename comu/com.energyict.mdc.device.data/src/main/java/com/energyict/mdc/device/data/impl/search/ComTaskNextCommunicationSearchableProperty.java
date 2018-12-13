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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class ComTaskNextCommunicationSearchableProperty extends AbstractDateSearchableProperty<ComTaskNextCommunicationSearchableProperty> {
    static final String PROPERTY_NAME = "device.comtask.next.communication";

    @Inject
    public ComTaskNextCommunicationSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(ComTaskNextCommunicationSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.COMTASK_NEXT_COMMUNICATION;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID IN ");
        sqlBuilder.openBracket();
        sqlBuilder.append("select DEVICE from DDC_COMTASKEXEC where OBSOLETE_DATE IS NULL AND ");
        sqlBuilder.add(toSqlFragment("DDC_COMTASKEXEC.NEXTEXECUTIONTIMESTAMP", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException {
        statement.setLong(bindPosition, ((Instant) value).toEpochMilli() / TimeUnit.SECONDS.toMillis(1));
    }
}
