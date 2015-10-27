package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.time.Instant;

public class RegisterReadingTypeNameSearchableProperty extends AbstractReadingTypeNameSearchableProperty <RegisterReadingTypeNameSearchableProperty>{

    static final String PROPERTY_NAME = "device.register.reading.type.name";

    @Inject
    public RegisterReadingTypeNameSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(RegisterReadingTypeNameSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addRegisterReadingType();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        return this.toSqlFragment("reg_rt.aliasname", condition, now);
    }
}
