/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
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
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ServiceCategorySearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.service.category";

    private DeviceSearchDomain domain;
    private final MeteringService meteringService;
    private final PropertySpecService propertySpecService;

    @Inject
    public ServiceCategorySearchableProperty(MeteringService meteringService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.meteringService = meteringService;
        this.propertySpecService = propertySpecService;
    }

    ServiceCategorySearchableProperty init(DeviceSearchDomain domain) {
        this.domain = domain;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return value instanceof ServiceCategory;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        ServiceCategory serviceCategory = (ServiceCategory) value;
        return serviceCategory.getDisplayName();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addServiceCategory();
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.spaceOpenBracket();
        sqlBuilder.openBracket();
        sqlBuilder.append(MessageFormat.format(Operator.LESSTHANOREQUAL.getFormat(), "ma.starttime"));
        sqlBuilder.add(this.toSqlFragment(now));
        sqlBuilder.append(" AND ");
        sqlBuilder.append(MessageFormat.format(Operator.GREATERTHAN.getFormat(), "ma.endtime"));
        sqlBuilder.add(this.toSqlFragment(now));
        sqlBuilder.closeBracket();
        sqlBuilder.append(" AND ");
        sqlBuilder.add(this.toSqlFragment("serv_cat.id", condition, now));
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
        return Optional.empty();
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.SERVICE_CATEGORY;
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .referenceSpec(ServiceCategory.class)
                .named(getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .addValues(Stream
                            .of(ServiceKind.values())
                            .map(meteringService::getServiceCategory)
                            .flatMap(Functions.asStream())
                            .toArray(ServiceCategory[]::new))
                .markExhaustive()
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
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