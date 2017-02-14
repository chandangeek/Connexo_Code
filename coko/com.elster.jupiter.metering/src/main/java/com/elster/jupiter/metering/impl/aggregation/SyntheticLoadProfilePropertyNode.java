/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.slp.SyntheticLoadProfile;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.units.Unit;

/**
 * Models a {@link ServerExpressionNode} that represents.
 * one {@link PropertySpec property} of a {@link CustomPropertySet}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-26 (13:16)
 */
class SyntheticLoadProfilePropertyNode  extends CustomPropertyNode {

    private final SyntheticLoadProfile actual;

    SyntheticLoadProfilePropertyNode(CustomPropertySetService customPropertySetService, PropertySpec propertySpec, RegisteredCustomPropertySet customPropertySet, UsagePoint usagePoint, MeterActivationSet meterActivationSet) {
        super(customPropertySetService, propertySpec, customPropertySet, usagePoint, meterActivationSet);
        this.actual = this.getPropertyValue(customPropertySetService, propertySpec, customPropertySet.getCustomPropertySet(), usagePoint, meterActivationSet);
    }

    private SyntheticLoadProfile getPropertyValue(CustomPropertySetService customPropertySetService, PropertySpec propertySpec, CustomPropertySet customPropertySet, UsagePoint usagePoint, MeterActivationSet meterActivationSet) {
        CustomPropertySetValues values;
        if (customPropertySet.isVersioned()) {
            values = customPropertySetService.getUniqueValuesFor(customPropertySet, usagePoint, meterActivationSet.getRange().lowerEndpoint());
        } else {
            values = customPropertySetService.getUniqueValuesFor(customPropertySet, usagePoint);
        }
        return (SyntheticLoadProfile) values.getProperty(propertySpec.getName());
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitSyntheticLoadProfile(this);
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        return IntermediateDimension.of(this.actual.getUnitOfMeasure().getDimension());
    }

    String sqlName() {
        return this.sqlName("slp");
    }

    protected String sqlComment() {
        return this.sqlComment("SLP ");
    }

    protected String[] withClauseSqlNames() {
        return new String[]{
                SqlConstants.TimeSeriesColumnNames.ID.sqlName(),
                SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName(),
                SqlConstants.TimeSeriesColumnNames.VERSIONCOUNT.sqlName(),
                SqlConstants.TimeSeriesColumnNames.RECORDTIME.sqlName(),
                SqlConstants.TimeSeriesColumnNames.VALUE.sqlName(),
                SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName()};
    }

    @SuppressWarnings("unchecked")
    protected void appendWithClause(SqlBuilder withClauseBuilder) {
        withClauseBuilder.append("SELECT slpvalues.* FROM (");
        withClauseBuilder.add(
            this.actual
                    .getRawValuesSql(
                            this.rawDataPeriod(),
                            this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.VALUE),
                            this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.LOCALDATE)));
        withClauseBuilder.append(") slpvalues");
    }

    private Pair<String, String> toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames columnName) {
        return Pair.of(columnName.fieldSpecName(), columnName.sqlName());
    }

    IntervalLength getIntervalLength() {
        return IntervalLength.from(this.actual.getInterval());
    }

    VirtualReadingType getSourceReadingType() {
        return VirtualReadingType.from(
                this.getIntervalLength(),
                this.actual.getUnitOfMeasure().getDimension(),
                Accumulation.NOTAPPLICABLE,
                this.commodityFromUnit(this.actual.getUnitOfMeasure()));
    }

    private Commodity commodityFromUnit(Unit unitOfMeasure) {
        switch (unitOfMeasure) {
            case AMPERE:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case KELVIN:
                return Commodity.COOLINGFLUID;
            case DEGREES_CELSIUS:
                return Commodity.HEATINGFLUID;
            case VOLT:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case JOULE:
                return Commodity.STEAM;
            case WATT:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case CUBIC_METER:
                return Commodity.POTABLEWATER;
            case VOLT_AMPERE:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case VOLT_AMPERE_REACTIVE:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case PHASE_ANGLE:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case POWER_FACTOR:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case VOLT_SECONDS:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case VOLT_SQUARED:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case AMPERE_SECONDS:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case AMPERE_SQUARED:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case AMPERE_SQUARED_SECOND:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case VOLT_AMPERE_HOUR:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case WATT_HOUR:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case VOLT_AMPERE_REACTIVE_HOUR:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case WATT_PER_SECOND:
                return Commodity.ELECTRICITY_SECONDARY_METERED;
            case LITRE_PER_SECOND:
                return Commodity.POTABLEWATER;
            case CUBIC_FEET:
                return Commodity.POTABLEWATER;
            case CUBIC_FEET_COMPENSATED:
                return Commodity.POTABLEWATER;
            case CUBIC_FEET_UNCOMPENSATED:
                return Commodity.POTABLEWATER;
            case CUBIC_FEET_PER_HOUR:
                return Commodity.POTABLEWATER;
            case CUBIC_FEET_COMPENSATED_PER_HOUR:
                return Commodity.POTABLEWATER;
            case CUBIC_FEET_UNCOMPENSATED_PER_HOUR:
                return Commodity.POTABLEWATER;
            case CUBIC_METER_PER_HOUR:
                return Commodity.POTABLEWATER;
            case CUBIC_METER_COMPENSATED_PER_HOUR:
                return Commodity.POTABLEWATER;
            case CUBIC_METER_UNCOMPENSATED_PER_HOUR:
                return Commodity.POTABLEWATER;
            case USGALLON:
                return Commodity.POTABLEWATER;
            case USGALLON_PER_HOUR:
                return Commodity.POTABLEWATER;
            case IMPERIALGALLON:
                return Commodity.POTABLEWATER;
            case IMPERIALGALLON_PER_HOUR:
                return Commodity.POTABLEWATER;
            case BRITISH_THERMAL_UNIT:
                return Commodity.STEAM;
            case BRITISH_THERMAL_UNIT_PER_HOUR:
                return Commodity.STEAM;
            case LITRE:
                return Commodity.POTABLEWATER;
            case LITRE_PER_HOUR:
                return Commodity.POTABLEWATER;
            case LITRE_COMPENSATED_PER_HOUR:
                return Commodity.POTABLEWATER;
            case LITRE_UNCOMPENSATED_PER_HOUR:
                return Commodity.POTABLEWATER;
            case LITRE_UNCOMPENSATED:
                return Commodity.POTABLEWATER;
            case LITRE_COMPENSATED:
                return Commodity.POTABLEWATER;
            case CUBIC_METER_UNCOMPENSATED:
                return Commodity.POTABLEWATER;
            case CUBIC_METER_COMPENSATED:
                return Commodity.POTABLEWATER;
            case THERM:
                return Commodity.STEAM;
            case US_LIQUID_PINT:
                return Commodity.POTABLEWATER;
            case US_LIQUID_QUART:
                return Commodity.POTABLEWATER;
            case US_DRY_PINT:
                return Commodity.POTABLEWATER;
            case US_DRY_QUART:
                return Commodity.POTABLEWATER;
            case DEGREES_FAHRENHEIT:
                return Commodity.HEATINGFLUID;
            case NORMAL_CUBIC_METER:
                return Commodity.POTABLEWATER;
            case NORMAL_CUBIC_METER_PER_HOUR:
                return Commodity.POTABLEWATER;
            case CUBIC_METER_PER_DAY:
                return Commodity.POTABLEWATER;
            case NORMAL_CUBIC_METER_PER_DAY:
                return Commodity.POTABLEWATER;
            default: {
                return Commodity.NOTAPPLICABLE;
            }
        }
    }
}