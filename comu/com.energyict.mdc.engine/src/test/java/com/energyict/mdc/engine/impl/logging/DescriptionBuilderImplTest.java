/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.logging;

import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-25 (10:53)
 */
public class DescriptionBuilderImplTest implements CanProvideDescriptionTitle {

    @Override
    public String getDescriptionTitle () {
        return this.getClass().getSimpleName();
    }

    @Test
    public void testNoProperties () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName());
    }

    @Test
    public void testSingleLabel () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addLabel("only one label");

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {only one label}");
    }

    @Test
    public void testMultipleLabels () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addLabel("First");
        builder.addLabel("Second");
        builder.addLabel("Third");

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {First; Second; Third}");
    }

    @Test
    public void testSingleProperty () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append(1).append("String");

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: 1String}");
    }

    @Test
    public void testFormattedProperty () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addFormattedProperty("Property1", "To dutch first={0} and last={1}", "eerst", "laatst");

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: To dutch first=eerst and last=laatst}");
    }

    @Test
    public void testStringPropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append("String");

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: String}");
    }

    @Test
    public void testStringPropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append("St").append("ri").append("ng");

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: String}");
    }

    @Test
    public void testCharSequencePropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append((CharSequence) "CharSequence");

        // Business methods
        CharSequence description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: CharSequence}");
    }

    @Test
    public void testCharSequencePropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append((CharSequence) "Char").append((CharSequence) "Seq").append((CharSequence) "uence");

        // Business methods
        CharSequence description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: CharSequence}");
    }

    @Test
    public void testCharPropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append('1');

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: 1}");
    }

    @Test
    public void testCharPropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append('1').append('2').append('3');

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: 123}");
    }

    @Test
    public void testBooleanPropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append(true);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: true}");
    }

    @Test
    public void testBooleanPropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append(true).append(false).append(false);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: truefalsefalse}");
    }

    @Test
    public void testIntegerPropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append(123);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: 123}");
    }

    @Test
    public void testIntegerPropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append(1).append(2).append(3);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: 123}");
    }

    @Test
    public void testLongPropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append(Long.MAX_VALUE);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: " + Long.MAX_VALUE + "}");
    }

    @Test
    public void testLongPropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append(1L).append(2L).append(3L);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: 123}");
    }

    @Test
    public void testObjectPropertyInOneGo () {
        Object o = new Object();
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addProperty("Property1").append(o);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: " + o + "}");
    }

    @Test
    public void testStringListPropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append("String");

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: String}");
    }

    @Test
    public void testStringListPropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append("St").append("ri").append("ng");

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: String}");
    }

    @Test
    public void testCharSequenceListPropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append((CharSequence) "CharSequence");

        // Business methods
        CharSequence description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: CharSequence}");
    }

    @Test
    public void testCharSequenceListPropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append((CharSequence) "Char").append((CharSequence) "Seq").append((CharSequence) "uence");

        // Business methods
        CharSequence description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: CharSequence}");
    }

    @Test
    public void testCharListPropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append('1');

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: 1}");
    }

    @Test
    public void testCharListPropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append('1').append('2').append('3');

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: 123}");
    }

    @Test
    public void testBooleanListPropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append(true);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: true}");
    }

    @Test
    public void testBooleanListPropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append(true).next().append(false).next().append(false);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: true, false, false}");
    }

    @Test
    public void testIntegerListPropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append(123);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: 123}");
    }

    @Test
    public void testIntegerListPropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append(1).append(2).append(3);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: 123}");
    }

    @Test
    public void testLongListPropertyInOneGo () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append(Long.MAX_VALUE);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: " + Long.MAX_VALUE + "}");
    }

    @Test
    public void testLongListPropertyInMultipleSteps () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append(1L).append(2L).append(3L);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: 123}");
    }

    @Test
    public void testObjectListProperty () {
        Object o1 = new Object();
        Object o2 = new Object();
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("Property1").append(o1).next().append(o2);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {Property1: " + o1 + ", " + o2 + "}");
    }

    @Test
    public void testMultipleProperties () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("StringListProperty").append("Fir").append("st").next().append("Second").next().append("Third");
        builder.addProperty("IntegerProperty").append(1).append(2).append(3);

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {StringListProperty: First, Second, Third; IntegerProperty: 123}");
    }

    @Test
    public void testMultiplePropertyValuesWithLastNotUsed () {
        DescriptionBuilderImpl builder = new DescriptionBuilderImpl(this);
        builder.addListProperty("StringListProperty").append("Fir").append("st").next().append("Second").next();

        // Business methods
        String description = builder.toString();

        // Asserts
        assertThat(description).isEqualTo(DescriptionBuilderImplTest.class.getSimpleName() + " {StringListProperty: First, Second}");
    }

}