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

public class LoadProfileLastReadingSearchableProperty extends AbstractDateSearchableProperty<LoadProfileLastReadingSearchableProperty> {

    static final String PROPERTY_NAME = "device.loadprofile.last.reading";

    @Inject
    public LoadProfileLastReadingSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(LoadProfileLastReadingSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.LOADPROFILE_LAST_READING;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".id IN (");
        builder.append("select DEVICEID from DDC_LOADPROFILE where ");
        builder.add(toSqlFragment("LASTREADING", condition, now));
        builder.closeBracket();
        return builder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
