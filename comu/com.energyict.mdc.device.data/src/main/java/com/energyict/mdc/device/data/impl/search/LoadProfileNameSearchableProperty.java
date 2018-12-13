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

public class LoadProfileNameSearchableProperty extends AbstractNameSearchableProperty<LoadProfileNameSearchableProperty> {

    static final String PROPERTY_NAME = "device.loadprofile.name";

    @Inject
    public LoadProfileNameSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(LoadProfileNameSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".id in (");
        sqlBuilder.append("select DEVICEID " +
                "from DDC_LOADPROFILE " +
                "join DTC_LOADPROFILESPEC on DTC_LOADPROFILESPEC.ID = DDC_LOADPROFILE.LOADPROFILESPECID " +
                "join MDS_LOADPROFILETYPE on MDS_LOADPROFILETYPE.ID = DTC_LOADPROFILESPEC.LOADPROFILETYPEID " +
                "where ");
        sqlBuilder.add(this.toSqlFragment("MDS_LOADPROFILETYPE.name", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.LOADPROFILE_NAME;
    }

}
