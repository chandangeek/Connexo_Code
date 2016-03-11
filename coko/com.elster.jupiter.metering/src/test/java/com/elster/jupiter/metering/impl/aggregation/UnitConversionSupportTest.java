package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.ReadingTypeUnit;

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
        VariableReferenceNode variable = new VariableReferenceNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(variable, ReadingTypeUnit.KELVIN, ReadingTypeUnit.DEGREESCELSIUS);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString());
        assertThat(conversion).isEqualTo("(var - 273.15)");
    }

    @Test
    public void celciusToKelvin() {
        VariableReferenceNode variable = new VariableReferenceNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(variable, ReadingTypeUnit.DEGREESCELSIUS, ReadingTypeUnit.KELVIN);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString());
        assertThat(conversion).isEqualTo("(273.15 + var)");
    }

    @Test
    public void kelvinToFahrenheit() {
        VariableReferenceNode variable = new VariableReferenceNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(variable, ReadingTypeUnit.KELVIN, ReadingTypeUnit.DEGREESFAHRENHEIT);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString());
        assertThat(conversion).isEqualTo("((9 * (var - 255.372222222222)) / 5)");
    }

    @Test
    public void fahrenheitToKelvin() {
        VariableReferenceNode variable = new VariableReferenceNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(variable, ReadingTypeUnit.DEGREESFAHRENHEIT, ReadingTypeUnit.KELVIN);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString());
        assertThat(conversion).isEqualTo("(255.372222222222 + ((5 * var) / 9))");
    }

    @Test
    public void inchToMeter() {
        VariableReferenceNode variable = new VariableReferenceNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(variable, ReadingTypeUnit.INCH, ReadingTypeUnit.METER);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString());
        assertThat(conversion).isEqualTo("(0.0254 * var)");
    }

    @Test
    public void meterToInch() {
        VariableReferenceNode variable = new VariableReferenceNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(variable, ReadingTypeUnit.METER, ReadingTypeUnit.INCH);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString());
        assertThat(conversion).isEqualTo("(var / 0.0254)");
    }

    @Test
    public void footToMeter() {
        VariableReferenceNode variable = new VariableReferenceNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(variable, ReadingTypeUnit.FOOT, ReadingTypeUnit.METER);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString());
        assertThat(conversion).isEqualTo("(0.3048 * var)");
    }

    @Test
    public void meterToFoot() {
        VariableReferenceNode variable = new VariableReferenceNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(variable, ReadingTypeUnit.METER, ReadingTypeUnit.FOOT);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString());
        assertThat(conversion).isEqualTo("(var / 0.3048)");
    }

    @Test
    public void mileToMeter() {
        VariableReferenceNode variable = new VariableReferenceNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(variable, ReadingTypeUnit.MILE, ReadingTypeUnit.METER);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString());
        assertThat(conversion).isEqualTo("(1609.344 * var)");
    }

    @Test
    public void meterToMile() {
        VariableReferenceNode variable = new VariableReferenceNode("var");

        // Business method
        ServerExpressionNode expression = UnitConversionSupport.unitConversion(variable, ReadingTypeUnit.METER, ReadingTypeUnit.MILE);

        // Asserts
        String conversion = expression.accept(new ExpressionNodeToString());
        assertThat(conversion).isEqualTo("(var / 1609.344)");
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