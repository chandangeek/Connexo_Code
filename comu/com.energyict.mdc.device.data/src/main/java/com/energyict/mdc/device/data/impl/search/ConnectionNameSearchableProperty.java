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

public class ConnectionNameSearchableProperty extends AbstractNameSearchableProperty<ConnectionNameSearchableProperty> {

    static final String PROPERTY_NAME = "device.connection.name";

    @Inject
    public ConnectionNameSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(ConnectionNameSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {

    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.append("dev.ID in (" + "select device from ddc_connectiontask left join dtc_partialconnectiontask" +
                " on dtc_partialconnectiontask.id=ddc_connectiontask.partialconnectiontask  where ddc_connectiontask.obsolete_date" +
                " is null and ");
        sqlBuilder.add(this.toSqlFragment("dtc_partialconnectiontask.name", condition, now));
        sqlBuilder.closeBracket();
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.CONNECTION_NAME;
    }

}
