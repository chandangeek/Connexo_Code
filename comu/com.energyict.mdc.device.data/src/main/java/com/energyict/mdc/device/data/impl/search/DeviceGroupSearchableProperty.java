/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;


import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class DeviceGroupSearchableProperty extends AbstractSearchableDeviceProperty {

    public static final String PROPERTY_NAME = DeviceFields.DEVICEGROUP.fieldName();

    private DeviceSearchDomain domain;

    private final MeteringGroupsService meteringGroupsService;
    private final PropertySpecService propertySpecService;

    @Inject
    public DeviceGroupSearchableProperty(MeteringGroupsService meteringGroupsService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.meteringGroupsService = meteringGroupsService;
        this.propertySpecService = propertySpecService;
    }

    DeviceGroupSearchableProperty init(DeviceSearchDomain domain) {
        this.domain = domain;
        return this;
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
        return Optional.empty();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.STICKY;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.DEVICE_GROUP;
    }

    @Override
    public PropertySpec getSpecification() {
        List<EndDeviceGroup> endDeviceGroups = this.meteringGroupsService.findEndDeviceGroups();
        return this.propertySpecService
                .referenceSpec(EndDeviceGroup.class)
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(endDeviceGroups.toArray(new EndDeviceGroup[endDeviceGroups.size()]))
                .markExhaustive()
                .finish();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // Nothing to refresh
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addEndDevice();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        Contains contains = (Contains) condition;
        Iterator<?> iterator = contains.getCollection().iterator();
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.openBracket();
        while (iterator.hasNext()) {
            EndDeviceGroup endDeviceGroup = (EndDeviceGroup) iterator.next();
            if (endDeviceGroup.isDynamic()) {
                sqlBuilder.add(buildSqlForQueryDeviceGroup(contains.getOperator(), (QueryEndDeviceGroup) endDeviceGroup));
            } else {
                sqlBuilder.add(buildSqlForEnumDeviceGroup(contains.getOperator(), (EnumeratedEndDeviceGroup) endDeviceGroup, now));
            }
            if (iterator.hasNext()) {
                sqlBuilder.append(contains.getOperator() == ListOperator.IN ? " OR " : " AND ");
            }
        }
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    private SqlFragment buildSqlForEnumDeviceGroup(ListOperator operator, EnumeratedEndDeviceGroup endDeviceGroup, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("ed.ID ");
        sqlBuilder.append(operator.getSymbol());
        sqlBuilder.spaceOpenBracket();
        sqlBuilder.append("select ENDDEVICE_ID from MTG_ENUM_ED_IN_GROUP where GROUP_ID = ");
        sqlBuilder.addLong(endDeviceGroup.getId());
        sqlBuilder.append(" AND ");
        sqlBuilder.append(MessageFormat.format(Operator.LESSTHANOREQUAL.getFormat(), "STARTTIME"));
        sqlBuilder.add(this.toSqlFragment(now));
        sqlBuilder.append(" AND ");
        sqlBuilder.append(MessageFormat.format(Operator.GREATERTHAN.getFormat(), "ENDTIME"));
        sqlBuilder.add(this.toSqlFragment(now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    private SqlFragment buildSqlForQueryDeviceGroup(ListOperator operator, QueryEndDeviceGroup endDeviceGroup) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("dev.ID ");
        sqlBuilder.append(operator.getSymbol());
        sqlBuilder.spaceOpenBracket();
        sqlBuilder.add(endDeviceGroup.toFragment());
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof EndDeviceGroup;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return ((EndDeviceGroup) value).getName();
    }
}
