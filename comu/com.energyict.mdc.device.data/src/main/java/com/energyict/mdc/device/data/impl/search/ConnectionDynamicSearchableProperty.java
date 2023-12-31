/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.google.inject.Inject;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class ConnectionDynamicSearchableProperty extends AbstractDynamicSearchableProperty<ConnectionDynamicSearchableProperty> {

    private ConnectionTypePluggableClass pluggableClass;

    @Inject
    public ConnectionDynamicSearchableProperty(Thesaurus thesaurus) {
        super(ConnectionDynamicSearchableProperty.class, thesaurus);
    }

    public ConnectionDynamicSearchableProperty init(SearchDomain domain, SearchablePropertyGroup group, PropertySpec propertySpec,
                                                    SearchableProperty constraintProperty, ConnectionTypePluggableClass pluggableClass) {
        super.init(domain, group, propertySpec, constraintProperty);
        this.pluggableClass = pluggableClass;
        return this;
    }

    @Override
    public String getName() {
        return getGroup().get().getId() + "." + this.pluggableClass.getJavaClassName() + "." + getPropertySpec().getName();
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        // Should be fine since I am overwritting getDisplayName() too
        return null;
    }

    @Override
    public String getDisplayName() {
        return super.getDisplayName() + " (" + this.pluggableClass.getName() + ")";
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addConnectionTaskProperties(this.pluggableClass);
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.openBracket();
        builder.add(this.toSqlFragment("props." + getPropertySpec().getName(), condition, now));
        builder.append(" AND props.STARTTIME < ");
        builder.addLong(now.toEpochMilli());
        builder.append(" AND props.ENDTIME > ");
        builder.addLong(now.toEpochMilli());
        builder.append(" OR props." + getPropertySpec().getName());
        builder.append(" IS NULL AND ");
        builder.openBracket();
        builder.append(" props.STARTTIME < ");
        builder.addLong(now.toEpochMilli());
        builder.append(" AND props.ENDTIME > ");
        builder.addLong(now.toEpochMilli());
        builder.append(" OR ");
        builder.append(" props.STARTTIME is NULL ");
        builder.append(" AND props.ENDTIME is NULL ");
        builder.closeBracket();
        builder.append(" AND ");
        builder.add(selectDeviceConfigurationProperties(condition, now));
        builder.closeBracket();
        return builder;
    }

    private SqlFragment selectDeviceConfigurationProperties(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append("ct.PARTIALCONNECTIONTASK IN (");
        builder.append("select ID \n" +
                " from DTC_PARTIALCONNECTIONTASK join DTC_PARTIALCONNECTIONTASKPROPS on " +
                " DTC_PARTIALCONNECTIONTASK.ID = DTC_PARTIALCONNECTIONTASKPROPS.PARTIALCONNECTIONTASK" +
                " where DTC_PARTIALCONNECTIONTASKPROPS.NAME = '");
        builder.append(getPropertySpec().getName());
        builder.append("' AND ");
        builder.add(toSqlFragment("DTC_PARTIALCONNECTIONTASKPROPS.VALUE", condition, now));
        builder.closeBracket();
        return builder;
    }

    @Override
    public List<String> getAvailableOperators() {
        return Arrays.asList(SearchablePropertyOperator.EQUAL.code(), SearchablePropertyOperator.IN.code(), SearchablePropertyOperator.NOT_EQUAL.code());
    }
}
