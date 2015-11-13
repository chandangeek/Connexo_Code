package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.time.Instant;

public class LogbookLastReadingSearchableProperty extends AbstractDateSearchableProperty<LogbookLastReadingSearchableProperty> {

    static final String PROPERTY_NAME = "device.logbook.last.reading";

    @Inject
    public LogbookLastReadingSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(LogbookLastReadingSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".id IN (" +
                "select DEVICEID from DDC_LOGBOOK where ");
        builder.add(toSqlFragment("DDC_LOGBOOK.LASTLOGBOOKCREATETIME", condition, now));
        builder.closeBracket();
        return builder;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(PropertyTranslationKeys.LOGBOOK_LAST_READING).format();
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
