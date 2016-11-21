package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.mdw.uplimpl.NlsServiceImpl;
import com.energyict.mdw.uplimpl.PropertySpecServiceImpl;
import com.google.common.collect.Range;

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

}