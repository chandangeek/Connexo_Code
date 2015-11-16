package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
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
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addLogbookType();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.add(this.toSqlFragment(JoinClauseBuilder.Aliases.LOGBOOK_SPEC + ".OBISCODE", condition, now));
        sqlBuilder.append(" OR ");
        sqlBuilder.openBracket();
        sqlBuilder.append(JoinClauseBuilder.Aliases.LOGBOOK_SPEC + ".OBISCODE is null");
        sqlBuilder.append(" AND ");
        sqlBuilder.add(this.toSqlFragment(JoinClauseBuilder.Aliases.LOGBOOK_TYPE + ".OBISCODE", condition, now));
        sqlBuilder.closeBracket();
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(PropertyTranslationKeys.LOGBOOK_OBISCODE).format();
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
