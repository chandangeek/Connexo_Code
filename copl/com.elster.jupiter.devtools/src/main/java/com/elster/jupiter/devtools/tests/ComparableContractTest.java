/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ComparableContractTest<T extends Comparable<? super T>> {

	private T a;

	@Before
	public void setUp() {
		a = getInstanceA();
		assertThat(getInstanceA()).describedAs(
				"getInstanceA() returns a different instance each time.")
				.isSameAs(a);
		assertThat(getInstancesEqualByComparisonToA()).isNotEmpty();
		assertThat(getInstancesGreaterThanA()).isNotEmpty();
		assertThat(getInstancesLessThanA()).isNotEmpty();
		if (shouldTestWithSubclass()) {
			assertThat(getInstancesOfSubclassEqualByComparisonToA()).isNotEmpty();
			assertThat(getInstancesOfSubclassGreaterThanA()).isNotEmpty();
			assertThat(getInstancesOfSubclassLessThanA()).isNotEmpty();
			Set<T> allInstances = new HashSet<>();
			allInstances.addAll(getInstancesOfSubclassEqualByComparisonToA());
			allInstances.addAll(getInstancesOfSubclassGreaterThanA());
			allInstances.addAll(getInstancesOfSubclassLessThanA());
			for (T value : allInstances) {
				assertThat(value).isInstanceOf(a.getClass());
			}
		}
	}

	@After
	public void tearDown() {

	}

    @Test
    public void testReflexive() {
        assertThat(a.compareTo(getInstanceEqualToA())).isZero();
    }

	@Test
	public void testEqualByComparison() {
		assertThat(a.compareTo(getInstanceEqualToA())).isZero();
		for (T comparableWithA : getInstancesEqualByComparisonToA()) {
			assertThat(a.compareTo(comparableWithA)).isZero().describedAs(comparableWithA + " is not equal by comparison.");
		}
	}

    @Test
    public void testGreaterThan() {
        for (T lessThanA : getInstancesLessThanA()) {
            assertThat(a.compareTo(lessThanA)).isGreaterThan(0).describedAs(lessThanA + " is not less by comparison.");
        }
    }

    @Test
    public void testLessThan() {
        for (T greaterThanA : getInstancesGreaterThanA()) {
            assertThat(a.compareTo(greaterThanA)).isLessThan(0).describedAs(greaterThanA + " is not greater by comparison.");
        }
    }

    @Test
	public void testReflexivity() {
		assertThat(a.compareTo(a)).describedAs("compareTo() is not reflexive.")
				.isZero();
	}

    @Test
    public void testSymmetryForEqualButNotSame() {
        if (!isEqualsAndHashCodeOverridden() || !isConsistentWithEquals()) {
            return;
        }
        T value = getInstanceEqualToA();
        assertThat(signum(a.compareTo(value))).isEqualTo(-signum(value.compareTo(a)))
                .describedAs("compareTo() is not symmetric for " + value + ".");
    }

    @Test
    public void testSymmetryForInstancesGreaterThanA() {
        for (T value : getInstancesGreaterThanA()) {
            assertThat(signum(a.compareTo(value))).isEqualTo(-signum(value.compareTo(a)))
                    .describedAs("compareTo() is not symmetric for " + value + ".");
        }
    }

    @Test
    public void testSymmetryForInstancesLessThanA() {
        for (T value : getInstancesLessThanA()) {
            assertThat(signum(a.compareTo(value))).isEqualTo(-signum(value.compareTo(a)))
                    .describedAs("compareTo() is not symmetric for " + value + ".");
        }
    }

    @Test
    public void testSymmetryForInstancesEqualToButNotEqualByComparison() {
        if (isConsistentWithEquals()) {
            return;
        }
        T value = getInstanceNotEqualToAButEqualByComparisonToA();
        assertThat(signum(a.compareTo(value))).isEqualTo(-signum(value.compareTo(a)))
                .describedAs("compareTo() is not symmetric for " + value + ".");
    }

    @Test
    public void testSymmetryForInstancesEqualByComparisonToA() {
        for (T value : getInstancesEqualByComparisonToA()) {
            assertThat(signum(a.compareTo(value))).isEqualTo(-signum(value.compareTo(a)))
                    .describedAs("compareTo() is not symmetric for " + value + ".");
        }
    }

	@Test
	public void testTransitivity() {
		for (T lessThanA : getInstancesLessThanA()) {
			for (T greaterThanA : getInstancesGreaterThanA()) {
				assertThat(lessThanA.compareTo(greaterThanA)).isLessThan(0)
                        .describedAs("compareTo() is not transitive.");
			}
		}
	}

    /*
     * Finally, the implementor must ensure that x.compareTo(y)==0 implies that sgn(x.compareTo(z)) == sgn(y.compareTo(z)), for all z.
     */
    @Test
	public void testConsistencyForEqualByComparison() {
        Set<T> instancesEqualByComparisonToA = collectAllThatShouldBeEqualByComparison();
		for (T y : instancesEqualByComparisonToA) { // for all these should a.compareTo(y) == 0
            for (T z : instancesEqualByComparisonToA) {
                assertThat(y.compareTo(z))
                        .isEqualTo(0)
                        .describedAs("compareTo() is not consistent for instances equal by comparison.");
            }
        }
	}

    private Set<T> collectAllThatShouldBeEqualByComparison() {
        Set<T> instancesEqualByComparisonToA = new HashSet<>(getInstancesEqualByComparisonToA());
        if (isEqualsAndHashCodeOverridden() && isConsistentWithEquals()) {
            instancesEqualByComparisonToA.add(getInstanceEqualToA());
        }
        if (!isConsistentWithEquals()) {
			instancesEqualByComparisonToA
					.add(getInstanceNotEqualToAButEqualByComparisonToA());
		}
        return instancesEqualByComparisonToA;
    }

    /*
     * Finally, the implementor must ensure that x.compareTo(y)==0 implies that sgn(x.compareTo(z)) == sgn(y.compareTo(z)), for all z.
     */
    @Test
    public void testConsistencyForGreaterInstances() {
        for (T y : collectAllThatShouldBeEqualByComparison()) { // for all these should a.compareTo(y) == 0
            for (T z : getInstancesGreaterThanA()) {
                assertThat(signum(y.compareTo(z)))
                        .isEqualTo(-1)
                        .describedAs("compareTo() is not consistent for instances greater by comparison.");
            }
        }
    }

    /*
     * Finally, the implementor must ensure that x.compareTo(y)==0 implies that sgn(x.compareTo(z)) == sgn(y.compareTo(z)), for all z.
     */
    @Test
    public void testConsistencyForSmallerInstances() {
        for (T y : collectAllThatShouldBeEqualByComparison()) { // for all these should a.compareTo(y) == 0
            for (T z : getInstancesLessThanA()) {
                assertThat(signum(y.compareTo(z)))
                        .isEqualTo(1)
                        .describedAs("compareTo() is not consistent for instances greater by comparison.");
            }
        }
    }

    @Test(expected = NullPointerException.class)
	public void testCompareToNullIsNotZero() {
		a.compareTo(null);
	}

	@Test
	public void testConsistencyWithEquals() {
		if (!isConsistentWithEquals()) {
			return;
		}
		assertThat(a).isEqualTo(getInstanceEqualToA());
		Set<T> allInstances = new HashSet<>();
		allInstances.addAll(getInstancesEqualByComparisonToA());
		allInstances.addAll(getInstancesGreaterThanA());
		allInstances.addAll(getInstancesLessThanA());
		for (T value : allInstances) {
			assertThat(a.equals(value)).isEqualTo(a.compareTo(value) == 0);
		}
	}

    @Test
	public void testInconsistencyWithEquals() {
		if (isConsistentWithEquals()) {
			return;
		}
		assertThat(getInstanceNotEqualToAButEqualByComparisonToA()).describedAs(
				"getInstanceEqualToA() returns the same instance each time.")
				.isNotSameAs(getInstanceNotEqualToAButEqualByComparisonToA());
		T a1 = getInstanceNotEqualToAButEqualByComparisonToA();
		assertThat(a.equals(a1)).describedAs(
				"getInstanceNotEqualToAButComparableWithA() is equal to A.")
				.isFalse();
		assertThat(a.compareTo(a1)).isZero();
	}

    @Test
    public void testSubClassEqualByComparison() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        assertThat(a.compareTo(getInstanceEqualToA())).isZero();
        for (T comparableWithA : getInstancesOfSubclassEqualByComparisonToA()) {
            assertThat(a.compareTo(comparableWithA)).isZero().describedAs(comparableWithA + " is not equal by comparison.");
        }
    }

    @Test
    public void testSubClassGreaterThan() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        for (T lessThanA : getInstancesOfSubclassLessThanA()) {
            assertThat(a.compareTo(lessThanA)).isGreaterThan(0).describedAs(lessThanA + " is not less by comparison.");
        }
    }

    @Test
    public void testSubClassLessThan() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        for (T greaterThanA : getInstancesOfSubclassGreaterThanA()) {
            assertThat(a.compareTo(greaterThanA)).isLessThan(0).describedAs(greaterThanA + " is not greater by comparison.");
        }
    }


    @Test
    public void testSubClassSymmetryForEqualButNotSame() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        if (!isEqualsAndHashCodeOverridden() || !isConsistentWithEquals()) {
            return;
        }
        for (T value : getInstancesOfSubclassEqualByComparisonToA()) {
            assertThat(signum(a.compareTo(value))).isEqualTo(-signum(value.compareTo(a)))
                    .describedAs("compareTo() is not symmetric for " + value + ".");
        }
    }

    @Test
    public void testSubClassSymmetryForInstancesGreaterThanA() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        for (T value : getInstancesOfSubclassGreaterThanA()) {
            assertThat(signum(a.compareTo(value))).isEqualTo(-signum(value.compareTo(a)))
                    .describedAs("compareTo() is not symmetric for " + value + ".");
        }
    }

    @Test
    public void testSubClassSymmetryForInstancesLessThanA() {
        if (!shouldTestWithSubclass()) {
            return;
        }
        for (T value : getInstancesOfSubclassLessThanA()) {
            assertThat(signum(a.compareTo(value))).isEqualTo(-signum(value.compareTo(a)))
                    .describedAs("compareTo() is not symmetric for " + value + ".");
        }
    }

    @Test
	public void testTransitivityWithSubClass() {
		if (!shouldTestWithSubclass()) {
			return;
		}
		for (T lessThanA : getInstancesOfSubclassLessThanA()) {
			for (T greaterThanA : getInstancesOfSubclassGreaterThanA()) {
				assertThat(lessThanA.compareTo(greaterThanA)).describedAs(
						"compareTo() is not transitive for subclasses.")
						.isLessThan(0);
			}
		}
	}

	/**
	 * @return true if the class under test is designed for subclassing, false
	 *         if it is not.
	 */
	protected abstract boolean canBeSubclassed();

	/**
	 * @return true if the class under test is consistent with equals, false if
	 *         it is not.
	 */
	protected abstract boolean isConsistentWithEquals();

	/**
	 * @return an instance of the class under test, this method should return
	 *         the same instance each time
	 */
	protected abstract T getInstanceA();

	/**
	 * @return an instance of the class under test, equal to getInstanceA() yet not the same or null if isEqualsAndHashCodeOverridden() returns false
	 */
	protected abstract T getInstanceEqualToA();

	/**
	 * This method may return null if isConsistentWithEquals() is false.
	 * 
	 * @return an instance of the class under test, comparable with
	 *         getInstanceA(). In this case this method should return a new
	 *         instance each time.
	 */
	protected abstract T getInstanceNotEqualToAButEqualByComparisonToA();

	/**
	 * This method should return a representable set of different cases where
	 * getInstanceA() can be compared with.
	 * 
	 * This set should contain at least one value.
	 * 
	 * @return a set of instances of the class under test, comparable with
	 *         getInstanceA().
	 */
	protected abstract Set<T> getInstancesEqualByComparisonToA();

	/**
	 * This method should return a representable set of different cases less
	 * than getInstanceA().
	 * 
	 * This set should contain at least one value.
	 * 
	 * @return a set of instances of the class under test, less than
	 *         getInstanceA().
	 */
	protected abstract Set<T> getInstancesLessThanA();

	/**
	 * This method should return a representable set of different cases greater
	 * than getInstanceA().
	 * 
	 * This set should contain at least one value.
	 * 
	 * @return a set of instances of the class under test, greater than
	 *         getInstanceA().
	 */
	protected abstract Set<T> getInstancesGreaterThanA();

	/**
	 * This method should return a representable set of different subclass
	 * object comparable with getInstanceA().
	 * 
	 * This set should contain at least one value if canBeSubclassed() is
	 * false..
	 * 
	 * @return a set of subclass instances of the class under test, comparable
	 *         with getInstanceA().
	 */
	protected abstract Set<T> getInstancesOfSubclassEqualByComparisonToA();

	/**
	 * This method should return a representable set of different subclass
	 * object less than getInstanceA().
	 * 
	 * This set should contain at least one value if canBeSubclassed() is
	 * false..
	 * 
	 * @return a set of subclass instances of the class under test, less than
	 *         getInstanceA().
	 */
	protected abstract Set<T> getInstancesOfSubclassLessThanA();

	/**
	 * This method should return a representable set of different subclass
	 * object greater than getInstanceA().
	 * 
	 * This set should contain at least one value if canBeSubclassed() is
	 * false..
	 * 
	 * @return a set of subclass instances of the class under test, greater than
	 *         getInstanceA().
	 */
	protected abstract Set<T> getInstancesOfSubclassGreaterThanA();

    /**
     * This method should indicate whether equals() checks for internal values or is simply the default identity equals() of Object
     * @return true if equals() checks for internal values, false if it is simply the default identity equals() of Object
     */
    protected abstract boolean isEqualsAndHashCodeOverridden();

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

	private int signum(int temp) {
        if (temp == 0) {
            return 0;
        }
		return temp > 0 ? 1 : -1;
	}
}
