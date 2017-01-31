/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the contract for equals() and hashCode() on classes that override them.
 * Note that subclasses are responsible for testing the semantical aspects of equals().
 *
 */
public abstract class EqualsContractTest {

    private Object a;

    @Before
    public void equalsContractSetUp() {
        a = getInstanceA();
        assertThat(getInstanceA()).describedAs("getInstanceA() returns a different instance each time.").isSameAs(a);
        assertThat(getInstanceEqualToA()).describedAs("getInstanceEqualToA() returns the same instance each time.").isNotSameAs(getInstanceEqualToA());
        if (shouldTestWithSubclass()) {
            assertThat((Class) getInstanceOfSubclassEqualToA().getClass()).isNotEqualTo((Class)a.getClass());
        }
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testEquality() {
        assertThat(a.equals(getInstanceEqualToA())).isTrue();
        for (Object b : getInstancesNotEqualToA()) {
            assertThat(a.equals(b)).isFalse();
        }
    }

    @Test
    public void testTrivialInequality() {
        assertThat(a.equals(new Object())).isFalse();
    }

    @Test
    public void testReflexivity() {
        assertThat(a.equals(a)).describedAs("equals() is not reflexive.").isTrue();
    }

    @Test
    public void testSymmetry() {
        Object a1 = getInstanceEqualToA();
        assertThat(a.equals(a1) == a1.equals(a)).describedAs("equals() is not symmetric on equal instances.").isTrue();

        for (Object b : getInstancesNotEqualToA()) {
            assertThat(a.equals(b) == b.equals(a)).describedAs("equals() is not symmetric on non equal instances.").isTrue();
        }
    }

    @Test
    public void testTransitivity() {
        Object b = getInstanceEqualToA();
        Object c = getInstanceEqualToA();
        
        assertThat(a.equals(b) && b.equals(c) && a.equals(c)).describedAs("equals() is not transitive.").isTrue();
    }

    @Test
    public void testConsistency() {
        Object a1 = getInstanceEqualToA();
        for (Object b : getInstancesNotEqualToA()) {
            for (int i = 0; i < 20; i++) {
                assertThat(a.equals(a1)).isTrue();
                assertThat(a.equals(b)).isFalse();
            }
        }
    }

    @Test
    public void testEqualsNullIsFalse() {
        assertThat(a.equals(null)).isFalse();
    }

    @Test
    public void testEqualityWithSubClass() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        assertThat(a.equals(getInstanceEqualToA())).isTrue();
        for (Object b : getInstancesNotEqualToA()) {
            assertThat(a.equals(b)).isFalse();
        }
    }


    @Test
    public void testSymmetryWithSubClass() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        Object a1 = getInstanceOfSubclassEqualToA();

        assertThat(a.equals(a1) == a1.equals(a)).describedAs("equals() is not symmetric on equal instances.").isTrue();
    }

    @Test
    public void testTransitivityWithSubClass() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        Object b = getInstanceEqualToA();
        Object c = getInstanceOfSubclassEqualToA();

        assertThat(a.equals(b) && b.equals(c) && a.equals(c)).describedAs("equals() is not transitive.").isTrue();
    }

    @Test
    public void testConsistencyWithSubClass() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        Object a1 = getInstanceOfSubclassEqualToA();
        for (int i = 0; i < 20; i++) {
            assertThat(a.equals(a1)).isTrue();
        }
    }

    @Test
    public void testHashCodeConsistency() {
        int hash = a.hashCode();
        for (int i = 0; i < 20; i++) {
            assertThat(a.hashCode()).describedAs("hashCode() is not consistent").isEqualTo(hash);
        }
    }

    @Test
    public void testEqualHashCodesForEqualInstances() {
        int hash = a.hashCode();
        
        assertThat(getInstanceEqualToA().hashCode()).describedAs("hashCode() is not equal for equal objects").isEqualTo(hash);
    }

    /**
     * While this is not a strict requirement as it may simply not be possible, this test exists to encourage at least minimal effort.
     */
    @Test
    public void testDifferentHashCodesForDifferentInstances() {
        int hash = a.hashCode();

        for (Object b : getInstancesNotEqualToA()) {
            assertThat(b.hashCode()).describedAs("hashCode() is strongly recommended to be different for different objects").isNotEqualTo(hash);
        }
    }

    @Test
    public void testHashCodeWithSubclass() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        int hash = a.hashCode();

        assertThat(getInstanceOfSubclassEqualToA().hashCode()).describedAs("hashCode() is not equal for equal objects from a subclass").isEqualTo(hash);
    }

    @Test
    public void testHashCodeIsFinalOnExtendableClass() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        try {
            Method hashCode = a.getClass().getMethod("hashCode", new Class[]{});
            assertThat(Modifier.isFinal(hashCode.getModifiers())).describedAs("hashCode() is not final, yet the class is designed for extension. To guard against LSP violations this class should prohibit overriding hashCode()").isTrue();
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Class does not have hashCode() method");
        }
    }

    @Test
    public void testEqualsIsFinalOnExtendableClass() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        try {
            Method hashCode = a.getClass().getMethod("equals", new Class[]{Object.class});
            assertThat(Modifier.isFinal(hashCode.getModifiers())).describedAs("equals() is not final, yet the class is designed for extension. To guard against LSP violations this class should prohibit overriding hashCode()").isTrue();
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Class does not have hashCode() method");
        }
    }

    /**
     * @return an instance of the class under test, this method should return the same instance each time
     */
    protected abstract Object getInstanceA();

    /**
     * @return an instance of the class under test equal to getInstanceA(), yet not the same, this method should return a new instance each time.
     */
    protected abstract Object getInstanceEqualToA();

    /**
     * @return an instance of the class under test equal to getInstanceA(), yet not the same, this method should return a new instance each time.
     */
    protected abstract Iterable<?> getInstancesNotEqualToA();

    /**
     * @return true if the class under test is designed for subclassing, false if it is not.
     */
    protected abstract boolean canBeSubclassed();

    /**
     * Implementors should provide an instance of a subclass that is equal to A. The easiest way is probably by supplying an instance of an empty anonymous subclass.
     * e.g. new MyClass(){}; Making sure the fields under consideration for equals and hashCode are set to be equal to getInstanceA().
     *
     * Note that "There is no way to extend an instantiable class and add a value component while preserving the equals contract". - Joshua Bloch - Effective Java 2nd Ed.
     *
     * This method may return null it canBeSubclassed() is false.
     *
     * @return an instance of a subclass under test equal to getInstanceA(), yet not the same, this method should return a new instance each time.
     */
    protected abstract Object getInstanceOfSubclassEqualToA();

    private boolean shouldTestWithSubclass() {
        if (isFinalClass()) {
            return false;
        }
        if (canBeSubclassed()) {
            return true;
        }
        throw new AssertionError("Class that cannot be subclassed should be marked final");
    }

    private boolean isFinalClass() {
        return Modifier.isFinal(a.getClass().getModifiers());
    }
}
