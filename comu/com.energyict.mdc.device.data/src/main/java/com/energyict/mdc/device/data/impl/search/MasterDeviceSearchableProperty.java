/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MasterDeviceSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.topology.master";

    private DeviceSearchDomain domain;
    private SearchablePropertyGroup group;
    private final PropertySpecService propertySpecService;

    @Inject
    public MasterDeviceSearchableProperty(PropertySpecService mdcPropertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = mdcPropertySpecService;
    }

    MasterDeviceSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
        return this;
    }

    @Override
    public boolean allowsIsDefined() {
        return true;
    }

    @Override
    public boolean allowsIsUnDefined() {
        return true;
    }

    private SqlFragment visitComparison(Comparison comparison, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        if (comparison.getOperator() == Operator.ISNULL){         // device.topology.master IS NULL means that there is not (effective) physicalgateway reference with device is not linked to a mas
            sqlBuilder.append("not exists");
            sqlBuilder.spaceOpenBracket();
            sqlBuilder.append("select originid from DTL_PHYSICALGATEWAYREFERENCE where");
            sqlBuilder.add(isEffectiveGatewayReference(now, "DTL_PHYSICALGATEWAYREFERENCE"));
            sqlBuilder.append(" AND ");
            sqlBuilder.append(" originid = dev.id");
            sqlBuilder.closeBracket();
        }else{
            sqlBuilder.spaceOpenBracket();
            sqlBuilder.add(isEffectiveGatewayReference(now, "gateway_ref"));
            sqlBuilder.append(" AND ");
            sqlBuilder.append("gateway_ref.gatewayid");
            sqlBuilder.space();
            sqlBuilder.append(ListOperator.IN.getSymbol());
            sqlBuilder.spaceOpenBracket();
            sqlBuilder.append("select id from DDC_DEVICE where ");
            sqlBuilder.add(this.toSqlFragment("name", comparison, now));
            sqlBuilder.closeBracket();
            sqlBuilder.closeBracket();
        }
        return sqlBuilder;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof Device;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((Device) value).getName();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addTopologyForSlaves();
    }

    public void appendJoinClauses(JoinClauseBuilder builder, Comparison comparison){
        if (comparison.getOperator() == Operator.ISNULL)
            return;
        this.appendJoinClauses(builder);
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        if (condition.getClass().isAssignableFrom(Comparison.class)){
            Comparison comparison = (Comparison) condition;
            return visitComparison(comparison, now);
        }
        throw new IllegalArgumentException("Condition should be a comparison");
    }

    private SqlFragment isEffectiveGatewayReference(Instant now, String tablename){
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        sqlBuilder.append(MessageFormat.format(Operator.LESSTHANOREQUAL.getFormat(), tablename+".starttime"));
        sqlBuilder.add(this.toSqlFragment(now));
        sqlBuilder.append(" AND ");
        sqlBuilder.append(MessageFormat.format(Operator.GREATERTHAN.getFormat(), tablename+".endtime"));
        sqlBuilder.add(this.toSqlFragment(now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    public SearchDomain getDomain() {
        return this.domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(this.group);
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .stringSpec()
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.DEVICE_MASTER_SEARCH_CRITERION_NAME;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }
}
