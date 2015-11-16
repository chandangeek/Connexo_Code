package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
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
        builder.addLoadProfileType();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.add(this.toSqlFragment(JoinClauseBuilder.Aliases.LOADPROFILE_TYPE + ".name", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(PropertyTranslationKeys.LOADPROFILE_NAME).format();
    }
}
