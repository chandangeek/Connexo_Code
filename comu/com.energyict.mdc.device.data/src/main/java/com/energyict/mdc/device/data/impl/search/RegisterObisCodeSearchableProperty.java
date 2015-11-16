package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.time.Instant;

public class RegisterObisCodeSearchableProperty extends AbstractObisCodeSearchableProperty<RegisterObisCodeSearchableProperty> {

    static final String PROPERTY_NAME = "device.register.obiscode";

    @Inject
    public RegisterObisCodeSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(RegisterObisCodeSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addRegisterSpec();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.add(this.toSqlFragment("reg_spec.deviceobiscode", condition, now));
        sqlBuilder.append(" OR ");
        sqlBuilder.openBracket();
        sqlBuilder.append(" reg_spec.deviceobiscode is null ");
        sqlBuilder.append(" AND ");
        sqlBuilder.add(this.toSqlFragment("reg_msr_type.obiscode", condition, now));
        sqlBuilder.closeBracket();
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(PropertyTranslationKeys.REGISTER_OBISCODE).format();
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
