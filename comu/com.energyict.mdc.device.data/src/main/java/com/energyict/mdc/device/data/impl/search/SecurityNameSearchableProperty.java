/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class SecurityNameSearchableProperty extends AbstractNameSearchableProperty<SecurityNameSearchableProperty> {

    static final String PROPERTY_NAME = "device.security.name";
    private SearchableProperty parent;

    @Inject
    public SecurityNameSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(SecurityNameSearchableProperty.class, propertySpecService, thesaurus);
    }

    SecurityNameSearchableProperty init(DeviceSearchDomain domain, DeviceTypeSearchableProperty parent, SearchablePropertyGroup group) {
        super.init(domain, group);
        this.parent = parent;
        return this;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.append(" deviceconfigid in ");
        sqlBuilder.openBracket();
        sqlBuilder.append(" select deviceconfig from DTC_SECURITYPROPERTYSET where ");
        sqlBuilder.add(this.toSqlFragment("name", condition, now));
        sqlBuilder.closeBracket();
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.SECURITY_NAME;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(this.parent);
    }
}
