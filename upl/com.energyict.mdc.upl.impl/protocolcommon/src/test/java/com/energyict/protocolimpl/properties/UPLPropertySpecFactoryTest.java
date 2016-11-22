package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.mdw.uplimpl.NlsServiceImpl;
import com.energyict.mdw.uplimpl.PropertySpecServiceImpl;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link UPLPropertySpecFactory} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-21 (14:33)
 */
public class UPLPropertySpecFactoryTest {

    @Before
    public void initializeUplServices() {
        Services.propertySpecService(new PropertySpecServiceImpl());
        Services.nlsService(new NlsServiceImpl());
    }

    @Test
    public void simpleIntegerSpec() {
        // Business method
        String expectedName = "integers";
        PropertySpec<Integer> test = UPLPropertySpecFactory.integer(expectedName, true);

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNull();
    }

    @Test
    public void integerSpecWithFixedSetOfValues() {
        // Business method
        String expectedName = "fixedIntegers";
        PropertySpec<Integer> test = UPLPropertySpecFactory.integer(expectedName, true, 11, 13, 17, 19);

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNotNull();
        List<? super Integer> values = test.getPossibleValues().getAllValues();
        assertThat(values).containsOnly(11, 13, 17, 19);
    }

    @Test
    public void integerSpecWithClosedRange() {
        // Business method
        String expectedName = "closedRangeIntegers";
        PropertySpec<Integer> test = UPLPropertySpecFactory.integer(expectedName, true, Range.closed(10, 15));

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNotNull();
        List<? super Integer> values = test.getPossibleValues().getAllValues();
        assertThat(values).containsOnly(10, 11, 12, 13, 14, 15);
    }

    @Test
    public void integerSpecWithClosedOpenRange() {
        // Business method
        String expectedName = "closedOpenRangeIntegers";
        PropertySpec<Integer> test = UPLPropertySpecFactory.integer(expectedName, true, Range.closedOpen(10, 15));

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNotNull();
        List<? super Integer> values = test.getPossibleValues().getAllValues();
        assertThat(values).containsOnly(10, 11, 12, 13, 14);
    }

    @Test
    public void integerSpecWithOpenRange() {
        // Business method
        String expectedName = "openRangeIntegers";
        PropertySpec<Integer> test = UPLPropertySpecFactory.integer(expectedName, true, Range.open(10, 15));

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNotNull();
        List<? super Integer> values = test.getPossibleValues().getAllValues();
        assertThat(values).containsOnly(11, 12, 13, 14);
    }

    @Test
    public void integerSpecWithOpenClosedRange() {
        // Business method
        String expectedName = "openClosedRangeIntegers";
        PropertySpec<Integer> test = UPLPropertySpecFactory.integer(expectedName, true, Range.openClosed(10, 15));

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNotNull();
        List<? super Integer> values = test.getPossibleValues().getAllValues();
        assertThat(values).containsOnly(11, 12, 13, 14, 15);
    }

    @Test
    public void simpleLongSpec() {
        // Business method
        String expectedName = "longs";
        PropertySpec<Long> test = UPLPropertySpecFactory.longValue(expectedName, true);

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNull();
    }

    @Test
    public void longSpecWithFixedSetOfValues() {
        // Business method
        String expectedName = "fixedLongs";
        PropertySpec<Long> test = UPLPropertySpecFactory.longValue(expectedName, true, 11L, 13L, 17L, 19L);

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNotNull();
        List<? super Long> values = test.getPossibleValues().getAllValues();
        assertThat(values).containsOnly(11L, 13L, 17L, 19L);
    }

    @Test
    public void longSpecWithClosedRange() {
        // Business method
        String expectedName = "closedRangeLongs";
        PropertySpec<Long> test = UPLPropertySpecFactory.longValue(expectedName, true, Range.closed(10L, 15L));

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNotNull();
        List<? super Long> values = test.getPossibleValues().getAllValues();
        assertThat(values).containsOnly(10L, 11L, 12L, 13L, 14L, 15L);
    }

    @Test
    public void longSpecWithClosedOpenRange() {
        // Business method
        String expectedName = "closedOpenRangeLongs";
        PropertySpec<Long> test = UPLPropertySpecFactory.longValue(expectedName, true, Range.closedOpen(10L, 15L));

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNotNull();
        List<? super Long> values = test.getPossibleValues().getAllValues();
        assertThat(values).containsOnly(10L, 11L, 12L, 13L, 14L);
    }

    @Test
    public void longSpecWithOpenRange() {
        // Business method
        String expectedName = "openRangeLongs";
        PropertySpec<Long> test = UPLPropertySpecFactory.longValue(expectedName, true, Range.open(10L, 15L));

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNotNull();
        List<? super Long> values = test.getPossibleValues().getAllValues();
        assertThat(values).containsOnly(11L, 12L, 13L, 14L);
    }

    @Test
    public void longSpecWithOpenClosedRange() {
        // Business method
        String expectedName = "openClosedRangeLongs";
        PropertySpec<Long> test = UPLPropertySpecFactory.longValue(expectedName, true, Range.openClosed(10L, 15L));

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNotNull();
        List<? super Long> values = test.getPossibleValues().getAllValues();
        assertThat(values).containsOnly(11L, 12L, 13L, 14L, 15L);
    }

    @Test
    public void simpleBigDecimalSpec() {
        // Business method
        String expectedName = "bigdecimals";
        PropertySpec<BigDecimal> test = UPLPropertySpecFactory.bigDecimal(expectedName, true);

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNull();
    }

    @Test
    public void bigDecimalSpecWithFixedSetOfValues() {
        // Business method
        String expectedName = "fixedBigDecimals";
        PropertySpec<BigDecimal> test = UPLPropertySpecFactory.bigDecimal(expectedName, true, BigDecimal.valueOf(11L), BigDecimal.valueOf(13L), BigDecimal.valueOf(17L), BigDecimal.valueOf(19L));

        // Asserts
        assertThat(test).isNotNull();
        assertThat(test.getName()).isEqualTo(expectedName);
        assertThat(test.isRequired()).isTrue();
        assertThat(test.getDefaultValue()).isEmpty();
        assertThat(test.getPossibleValues()).isNotNull();
        List<? super BigDecimal> values = test.getPossibleValues().getAllValues();
        assertThat(values).containsOnly(BigDecimal.valueOf(11L), BigDecimal.valueOf(13L), BigDecimal.valueOf(17L), BigDecimal.valueOf(19L));
    }

}