package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.units.Dimension;

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

    CustomPropertyNode(CustomPropertySetService customPropertySetService, PropertySpec propertySpec, RegisteredCustomPropertySet customPropertySet, UsagePoint usagePoint) {
        this.customPropertySetService = customPropertySetService;
        this.propertySpec = propertySpec;
        this.customPropertySet = customPropertySet;
        this.usagePoint = usagePoint;
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

    String sqlName() {
        return "rid_cps_" + this.customPropertySet.getId() + "_" + propertySpec.getName();
    }

    void appendDefinitionTo(ClauseAwareSqlBuilder sqlBuilder) {
        SqlBuilder withClauseBuilder = sqlBuilder.with(this.sqlName(), Optional.of(this.sqlComment()), SqlConstants.TimeSeriesColumnNames.names());
        this.appendWithClause(withClauseBuilder);
    }

    private String sqlComment() {
        return "Value for custom property '" + this.propertySpec.getName() + "' of set '" + this.getCustomPropertySet().getName() + "' (id=" + this.getCustomPropertySet().getId() + ")";
    }

    @SuppressWarnings("unchecked")
    private void appendWithClause(SqlBuilder withClauseBuilder) {
        withClauseBuilder.append("SELECT -1, starttime, 0, 0, 0, value, null FROM (");
        withClauseBuilder.add(
                this.customPropertySetService.getRawValuesSql(
                        this.getCustomPropertySet(),
                        this.propertySpec,
                        SqlConstants.TimeSeriesColumnNames.VALUE.sqlName(),
                        this.usagePoint));
        withClauseBuilder.append(")");
    }

}