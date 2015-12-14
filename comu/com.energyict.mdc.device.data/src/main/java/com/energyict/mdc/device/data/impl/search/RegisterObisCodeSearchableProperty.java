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
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.DEVICE + ".DEVICECONFIGID IN (");
        builder.append("select DTC_REGISTERSPEC.DEVICECONFIGID " +
                "from DTC_REGISTERSPEC " +
                "join MDS_MEASUREMENTTYPE on MDS_MEASUREMENTTYPE.ID = DTC_REGISTERSPEC.REGISTERTYPEID " +
                "where ");
        builder.add(this.toSqlFragment("DTC_REGISTERSPEC.deviceobiscode", condition, now));
        builder.append(" OR ");
        builder.openBracket();
        builder.append(" DTC_REGISTERSPEC.deviceobiscode is null ");
        builder.append(" AND ");
        builder.add(this.toSqlFragment("MDS_MEASUREMENTTYPE.obiscode", condition, now));
        builder.closeBracket();
        builder.closeBracket();
        return builder;
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
