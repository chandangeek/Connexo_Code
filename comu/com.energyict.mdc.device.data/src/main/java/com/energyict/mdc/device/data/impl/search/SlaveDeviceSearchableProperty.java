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
import com.elster.jupiter.util.conditions.Condition;
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

public class SlaveDeviceSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.topology.slave";

    private DeviceSearchDomain domain;
    private SearchablePropertyGroup group;
    private final PropertySpecService propertySpecService;

    @Inject
    public SlaveDeviceSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    SlaveDeviceSearchableProperty init(DeviceSearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
        return this;
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
        builder.addTopologyForMasters();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.spaceOpenBracket();
        sqlBuilder.openBracket();
        sqlBuilder.append(MessageFormat.format(Operator.LESSTHANOREQUAL.getFormat(), "gateway_ref.starttime"));
        sqlBuilder.add(this.toSqlFragment(now));
        sqlBuilder.append(" AND ");
        sqlBuilder.append(MessageFormat.format(Operator.GREATERTHAN.getFormat(), "gateway_ref.endtime"));
        sqlBuilder.add(this.toSqlFragment(now));
        sqlBuilder.closeBracket();
        sqlBuilder.append(" AND ");
        sqlBuilder.append(" gateway_ref.originid in ( select id from DDC_DEVICE where ");
        sqlBuilder.add(this.toSqlFragment("name", condition, now));
        sqlBuilder.closeBracket();
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
        return PropertyTranslationKeys.DEVICE_SLAVE_NAME;
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
