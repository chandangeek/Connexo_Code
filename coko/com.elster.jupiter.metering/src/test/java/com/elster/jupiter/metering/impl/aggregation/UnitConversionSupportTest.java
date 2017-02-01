/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.config.Formula;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link UnitConversionSupport} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-01 (11:45)
 */
public class UnitConversionSupportTest {

    @Test
    public void wattIsCompatibleWithWattHour() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.WATT,
                        ReadingTypeUnit.WATTHOUR)).isTrue();
    }

    @Test
    public void wattHourIsCompatibleWithWatt() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.WATTHOUR,
                        ReadingTypeUnit.WATT)).isTrue();
    }

    @Test
    public void CubicMeterIsCompatibleWithCubicFeet() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.CUBICMETER,
                        ReadingTypeUnit.CUBICFEET)).isTrue();
    }

    @Test
    public void CubicFeetIsCompatibleWithCubicMeter() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.CUBICFEET,
                        ReadingTypeUnit.CUBICMETER)).isTrue();
    }

    @Test
    public void USGallonIsCompatibleWithImperialGallon() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.USGALLON,
                        ReadingTypeUnit.IMPERIALGALLON)).isTrue();
    }

    @Test
    public void ImperialGallonIsCompatibleWithUSGallon() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.IMPERIALGALLON,
                        ReadingTypeUnit.USGALLON)).isTrue();
    }

    @Test
    public void USGallonIsCompatibleWithLiter() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.USGALLON,
                        ReadingTypeUnit.LITRE)).isTrue();
    }

    @Test
    public void LiterIsCompatibleWithUSGallon() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.LITRE,
                        ReadingTypeUnit.USGALLON)).isTrue();
    }

    @Test
    public void ImperialGallonIsCompatibleWithLiter() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.IMPERIALGALLON,
                        ReadingTypeUnit.LITRE)).isTrue();
    }

    @Test
    public void LiterIsCompatibleWithImperialGallon() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.LITRE,
                        ReadingTypeUnit.IMPERIALGALLON)).isTrue();
    }

    @Test
    public void CubicMeterIsCompatibleWithLiter() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.CUBICMETER,
                        ReadingTypeUnit.LITRE)).isTrue();
    }

    @Test
    public void LiterIsCompatibleWithCubicMeter() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.LITRE,
                        ReadingTypeUnit.CUBICMETER)).isTrue();
    }

    @Test
    public void CubicFeetIsCompatibleWithLiter() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.CUBICFEET,
                        ReadingTypeUnit.LITRE)).isTrue();
    }

    @Test
    public void LiterIsCompatibleWithCubicFeet() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.LITRE,
                        ReadingTypeUnit.CUBICFEET)).isTrue();
    }

    @Test
    public void BarIsCompatibleWithPascal() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.BAR,
                        ReadingTypeUnit.PASCAL)).isTrue();
    }

    @Test
    public void PascalIsCompatibleWithBar() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.PASCAL,
                        ReadingTypeUnit.BAR)).isTrue();
    }

    @Test
    public void KelvinIsCompatibleWithCelcius() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.KELVIN,
                        ReadingTypeUnit.DEGREESCELSIUS)).isTrue();
    }

    @Test
    public void CelciusIsCompatibleWithKelvin() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.DEGREESCELSIUS,
                        ReadingTypeUnit.KELVIN)).isTrue();
    }

    @Test
    public void KelvinIsCompatibleWithFahrenheit() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.KELVIN,
                        ReadingTypeUnit.DEGREESFAHRENHEIT)).isTrue();
    }

    @Test
    public void FahrenheitIsCompatibleWithKelvin() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.DEGREESFAHRENHEIT,
                        ReadingTypeUnit.KELVIN)).isTrue();
    }

    @Test
    public void CelciusIsCompatibleWithFahrenheit() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.DEGREESCELSIUS,
                        ReadingTypeUnit.DEGREESFAHRENHEIT)).isTrue();
    }

    @Test
    public void FahrenheitIsCompatibleWithCelcius() {
        assertThat(
            UnitConversionSupport
                .areCompatibleForAutomaticUnitConversion(
                        ReadingTypeUnit.DEGREESFAHRENHEIT,
                        ReadingTypeUnit.DEGREESCELSIUS)).isTrue();
    }

    @Test
    public void MetricIsCompatibleWithImperial() {
        for (ReadingTypeUnit unit : this.imperialLengthUnits()) {
            assertThat(
                    UnitConversionSupport
                            .areCompatibleForAutomaticUnitConversion(
                                    ReadingTypeUnit.METER,
                                    unit)).isTrue();
        }
    }

    @Test
    public void ImperialIsCompatibleWithMetric() {
        for (ReadingTypeUnit unit : this.imperialLengthUnits()) {
            assertThat(
                    UnitConversionSupport
                            .areCompatibleForAutomaticUnitConversion(
                                    unit,
                                    ReadingTypeUnit.METER)).isTrue();
        }
    }

    @Test
    public void kelvinToCelcius() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.KELVIN, MetricMultiplier.ZERO, ReadingTypeUnit.DEGREESCELSIUS, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("(var - 273.15)");
    }

    @Test
    public void celciusToKelvin() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.DEGREESCELSIUS, MetricMultiplier.ZERO, ReadingTypeUnit.KELVIN, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("(273.15 + var)");
    }

    @Test
    public void kelvinToFahrenheit() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.KELVIN, MetricMultiplier.ZERO, ReadingTypeUnit.DEGREESFAHRENHEIT, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("((9 * (var - 255.372222222222)) / 5)");
    }

    @Test
    public void fahrenheitToKelvin() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.DEGREESFAHRENHEIT, MetricMultiplier.ZERO, ReadingTypeUnit.KELVIN, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("(255.372222222222 + ((5 * var) / 9))");
    }

    @Test
    public void inchToMeter() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.INCH, MetricMultiplier.ZERO, ReadingTypeUnit.METER, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("(0.0254 * var)");
    }

    @Test
    public void meterToInch() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.METER, MetricMultiplier.ZERO, ReadingTypeUnit.INCH, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("(var / 0.0254)");
    }

    @Test
    public void footToMeter() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.FOOT, MetricMultiplier.ZERO, ReadingTypeUnit.METER, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("(0.3048 * var)");
    }

    @Test
    public void meterToFoot() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.METER, MetricMultiplier.ZERO, ReadingTypeUnit.FOOT, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("(var / 0.3048)");
    }

    @Test
    public void kiloMeterToFoot() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.METER, MetricMultiplier.KILO, ReadingTypeUnit.FOOT, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("((1E+3 * var) / 0.3048)");
    }

    @Test
    public void mileToFoot() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.MILE, MetricMultiplier.ZERO, ReadingTypeUnit.FOOT, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("((1609.344 * var) / 0.3048)");
    }

    @Test
    public void mileToMeter() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.MILE, MetricMultiplier.ZERO, ReadingTypeUnit.METER, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("(1609.344 * var)");
    }

    @Test
    public void mileToKiloMeter() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.MILE, MetricMultiplier.ZERO, ReadingTypeUnit.METER, MetricMultiplier.KILO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("((1609.344 * var) / 1E+3)");
    }

    @Test
    public void meterToMile() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.METER, MetricMultiplier.ZERO, ReadingTypeUnit.MILE, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("(var / 1609.344)");
    }

    @Test
    public void meterToMeter() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.METER, MetricMultiplier.ZERO, ReadingTypeUnit.METER, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("var");
    }

    @Test
    public void meterToKiloMeter() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.METER, MetricMultiplier.ZERO, ReadingTypeUnit.METER, MetricMultiplier.KILO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("(var / 1E+3)");
    }

    @Test
    public void kiloMeterToMeter() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.METER, MetricMultiplier.KILO, ReadingTypeUnit.METER, MetricMultiplier.ZERO);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString(Formula.Mode.AUTO));
        assertThat(conversion).isEqualTo("(1E+3 * var)");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void literToCelcius() {
        SqlFragmentNode sql = new SqlFragmentNode("var");

        // Business method
        UnitConversionSupport.unitConversion(sql, ReadingTypeUnit.LITRE, MetricMultiplier.ZERO, ReadingTypeUnit.DEGREESCELSIUS, MetricMultiplier.ZERO);

        // Asserts: see expected exception rule
    }

    private Set<ReadingTypeUnit> imperialLengthUnits() {
        return EnumSet.of(
                ReadingTypeUnit.NAUTICALMILE,
                ReadingTypeUnit.INCH,
                ReadingTypeUnit.FOOT,
                ReadingTypeUnit.ROD,
                ReadingTypeUnit.FURLONG,
                ReadingTypeUnit.MILE);
    }

}