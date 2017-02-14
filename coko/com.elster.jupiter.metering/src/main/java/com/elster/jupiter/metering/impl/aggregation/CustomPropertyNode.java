/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

/**
 * Models a {@link ServerExpressionNode} that represents.
 * one {@link PropertySpec property} of a {@link CustomPropertySet}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-26 (13:16)
 */
class CustomPropertyNode implements ServerExpressionNode {
    private final CustomPropertySetService customPropertySetService;
    private final PropertySpec propertySpec;
    private final RegisteredCustomPropertySet customPropertySet;
    private final UsagePoint usagePoint;
    private final MeterActivationSet meterActivationSet;

    CustomPropertyNode(CustomPropertySetService customPropertySetService, PropertySpec propertySpec, RegisteredCustomPropertySet customPropertySet, UsagePoint usagePoint, MeterActivationSet meterActivationSet) {
        this.customPropertySetService = customPropertySetService;
        this.propertySpec = propertySpec;
        this.customPropertySet = customPropertySet;
        this.usagePoint = usagePoint;
        this.meterActivationSet = meterActivationSet;
    }

    CustomPropertySet getCustomPropertySet() {
        return this.customPropertySet.getCustomPropertySet();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitProperty(this);
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        return IntermediateDimension.of(Dimension.DIMENSIONLESS);
    }

    protected Range<Instant> rawDataPeriod() {
        return this.meterActivationSet.getRange();
    }

    String sqlName() {
        return this.sqlName("cps");
    }

    protected String sqlName(String prefix) {
        return prefix + this.customPropertySet.getId() + "_" + Math.abs(this.propertySpec.getName().hashCode()) + "_" + this.meterActivationSet.sequenceNumber();
    }

    void appendDefinitionTo(ClauseAwareSqlBuilder sqlBuilder) {
        SqlBuilder withClauseBuilder = sqlBuilder.with(this.sqlName(), Optional.of(this.sqlComment()), this.withClauseSqlNames());
        this.appendWithClause(withClauseBuilder);
    }

    protected String sqlComment() {
        return "Value for custom property '" + this.propertySpec.getName() +
                "' of set '" + this.getCustomPropertySet().getName() + "' (id=" + this.customPropertySet.getId() +
                ") in " + this.rawDataPeriod();
    }

    protected String sqlComment(String type) {
        return "Value for " + type + "custom property '" + this.propertySpec.getName() +
                "' of set '" + this.getCustomPropertySet().getName() + "' (id=" + this.customPropertySet.getId() +
                ") in " + this.rawDataPeriod();
    }

    protected String[] withClauseSqlNames() {
        return new String[]{
                "starttime",
                "endtime",
                SqlConstants.TimeSeriesColumnNames.VALUE.sqlName(),
                SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName()};
    }

    @SuppressWarnings("unchecked")
    protected void appendWithClause(SqlBuilder withClauseBuilder) {
        withClauseBuilder.append("SELECT starttime, endtime, value, null FROM (");
        withClauseBuilder.add(
                this.customPropertySetService.getRawValuesSql(
                        this.getCustomPropertySet(),
                        this.propertySpec,
                        SqlConstants.TimeSeriesColumnNames.VALUE.sqlName(),
                        this.usagePoint,
                        this.rawDataPeriod()));
        withClauseBuilder.append(")");
    }

}