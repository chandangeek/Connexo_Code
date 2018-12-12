/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import java.time.Instant;

public abstract class AbstractTransitionSearchableProperty<T> extends AbstractDateSearchableProperty<T> {
    public AbstractTransitionSearchableProperty(Class<T> clazz, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(clazz, propertySpecService, thesaurus);
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addEndDevice();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return toSqlFragment(JoinClauseBuilder.Aliases.END_DEVICE + "." + getCIMDateColumnAlias(), condition, now);
    }

    protected abstract String getCIMDateColumnAlias();
}
