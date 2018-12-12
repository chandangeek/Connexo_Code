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

public class RegisterObisCodeSearchableProperty extends AbstractObisCodeSearchableProperty<RegisterObisCodeSearchableProperty> {

    static final String PROPERTY_NAME = "device.register.obiscode";

    @Inject
    public RegisterObisCodeSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(RegisterObisCodeSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.REGISTER_OBISCODE;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID IN ");
        sqlBuilder.openBracket();
        sqlBuilder.append(" SELECT DDC_DEVICE.ID FROM DDC_DEVICE ");
        sqlBuilder.append("RIGHT JOIN DTC_REGISTERSPEC ON DDC_DEVICE.DEVICECONFIGID = DTC_REGISTERSPEC.DEVICECONFIGID ");
        sqlBuilder.append("LEFT JOIN MDS_MEASUREMENTTYPE ON DTC_REGISTERSPEC.REGISTERTYPEID = MDS_MEASUREMENTTYPE.ID ");
        sqlBuilder.append("LEFT JOIN DDC_OVERRULEDOBISCODE ON DDC_DEVICE.ID = DDC_OVERRULEDOBISCODE.DEVICEID " +
                "AND MDS_MEASUREMENTTYPE.READINGTYPE = DDC_OVERRULEDOBISCODE.READINGTYPEMRID ");
        sqlBuilder.append(" WHERE ");
        sqlBuilder.openBracket();
        sqlBuilder.append("DDC_OVERRULEDOBISCODE.OBISCODE IS NOT NULL AND ");
        sqlBuilder.add(this.toSqlFragment("DDC_OVERRULEDOBISCODE.OBISCODE", condition, now));
        sqlBuilder.closeBracket();
        sqlBuilder.append(" OR ");
        sqlBuilder.openBracket();
        sqlBuilder.append("DDC_OVERRULEDOBISCODE.OBISCODE IS NULL AND DTC_REGISTERSPEC.DEVICEOBISCODE IS NOT NULL AND ");
        sqlBuilder.add(this.toSqlFragment("DTC_REGISTERSPEC.DEVICEOBISCODE", condition, now));
        sqlBuilder.closeBracket();
        sqlBuilder.append(" OR ");
        sqlBuilder.openBracket();
        sqlBuilder.append("DDC_OVERRULEDOBISCODE.OBISCODE IS NULL AND DTC_REGISTERSPEC.DEVICEOBISCODE IS NULL AND ");
        sqlBuilder.add(this.toSqlFragment("MDS_MEASUREMENTTYPE.OBISCODE", condition, now));
        sqlBuilder.closeBracket();
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
