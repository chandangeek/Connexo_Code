package com.elster.jupiter.util.time;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.*;

public class IntermittentIntervalTest {
	private static final Date ONE = new DateMidnight(2010, 1, 1).toDate();
	private static final Date TWO = new DateMidnight(2010, 1, 2).toDate();
	private static final Date THREE = new DateMidnight(2010, 1, 3).toDate();
	private static final Date FOUR = new DateMidnight(2010, 1, 4).toDate();
	private static final Date FIVE = new DateMidnight(2010, 1, 5).toDate();
	private static final Date SIX = new DateMidnight(2010, 1, 6).toDate();
	private static final Date SEVEN = new DateMidnight(2010, 1, 7).toDate();
	private static final Date EIGHT = new DateMidnight(2010, 1, 8).toDate();
	private static final Date NINE = new DateMidnight(2010, 1, 9).toDate();
	private static final Date BETWEEN_ONE_AND_TWO = new DateTime(ONE).plusHours(12).toDate();
	private static final Date BETWEEN_TWO_AND_THREE = new DateTime(TWO).plusHours(12).toDate();
	private static final Date BETWEEN_THREE_AND_FOUR = new DateTime(THREE).plusHours(12).toDate();
	private static final Interval NINE_TO_INFINITY = new Interval(NINE, null);
	private static final Interval SEVEN_TO_INFINITY = new Interval(SEVEN, null);
	private static final Interval SEVEN_TO_EIGHT = new Interval(SEVEN, EIGHT);
	private static final Interval ALWAYS = new Interval(null, null);
	private static final Interval THREE_TO_EIGHT = new Interval(THREE, EIGHT);
	private static final Interval FIVE_TO_INFINITY = new Interval(FIVE, null);
	private static final Interval FIVE_TO_SIX = new Interval(FIVE, SIX);
	private static final Interval FIVE_TO_SEVEN = new Interval(FIVE, SEVEN);
	private static final Interval THREE_TO_FIVE = new Interval(THREE, FIVE);
	private static final Interval THREE_TO_FOUR = new Interval(THREE, FOUR);
	private static final Interval FOUR_TO_FIVE = new Interval(FOUR, FIVE);
	private static final Interval BIGBANG_TO_TWO = new Interval(null, TWO);
	private static final Interval ONE_TO_THREE = new Interval(ONE, THREE);
	private static final Interval ONE_TO_FIVE = new Interval(ONE, FIVE);
	private static final Interval ONE_TO_FOUR = new Interval(ONE, FOUR);
	private static final Interval ONE_TO_TWO = new Interval(ONE, TWO);
	private static final Interval ONE_TO_INFINITY = new Interval(ONE, null);
	private static final Interval ONE_TO_EIGHT = new Interval(ONE, EIGHT);
	private static final Interval BIGBANG_TO_ONE = new Interval(null, ONE);
	private static final Interval EIGHT_TO_NINE = new Interval(EIGHT, NINE);
	private static final Interval TWO_TO_THREE = new Interval(TWO, THREE);
	private static final Interval SIX_TO_SEVEN = new Interval(SIX, SEVEN);
	private static final Interval FIVE_TO_EIGHT = new Interval(FIVE, EIGHT);
	private static final Interval ONE_TO_SEVEN = new Interval(ONE, SEVEN);
	private static final Interval EIGHT_TO_INFINITY = new Interval(EIGHT, null);

	@Test
	public void testIntersectionAll() {
		IntermittentInterval union1 = new IntermittentInterval(ONE_TO_TWO, THREE_TO_EIGHT, NINE_TO_INFINITY);
		IntermittentInterval union2 = new IntermittentInterval(ALWAYS);
		assertEquals(union1, union1.intersection(union2));
		assertEquals(union1, union2.intersection(union1));
	}

	@Test
	public void testIntersectionComplex() {
		IntermittentInterval union1 = new IntermittentInterval(ONE_TO_TWO, THREE_TO_EIGHT, NINE_TO_INFINITY);
		IntermittentInterval union2 = new IntermittentInterval(ONE_TO_FOUR, FIVE_TO_SIX, SEVEN_TO_INFINITY);
		IntermittentInterval expected = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX, SEVEN_TO_EIGHT, NINE_TO_INFINITY);
		assertEquals(expected, union1.intersection(union2));
		assertEquals(expected, union2.intersection(union1));
	}

	@Test
	public void testIntersection() {
		assertEquals(new IntermittentInterval(THREE_TO_FIVE), new IntermittentInterval(ONE_TO_FIVE).intersection(new IntermittentInterval(THREE_TO_EIGHT, NINE_TO_INFINITY)));
		assertEquals(new IntermittentInterval(THREE_TO_FIVE), new IntermittentInterval(THREE_TO_EIGHT, NINE_TO_INFINITY).intersection(new IntermittentInterval(ONE_TO_FIVE)));
	}

	@Test
	public void testIntersectionSimple1() {
		assertEquals(new IntermittentInterval(THREE_TO_FIVE), new IntermittentInterval(ONE_TO_FIVE).intersection(new IntermittentInterval(THREE_TO_EIGHT, NINE_TO_INFINITY)));
		assertEquals(new IntermittentInterval(THREE_TO_FIVE), new IntermittentInterval(THREE_TO_EIGHT, NINE_TO_INFINITY).intersection(new IntermittentInterval(ONE_TO_FIVE)));
	}

	@Test
	public void testIntersectionSimple2() {
		IntermittentInterval union1 = new IntermittentInterval(ONE_TO_THREE, SEVEN_TO_EIGHT, NINE_TO_INFINITY);
		IntermittentInterval union2 = new IntermittentInterval(ONE_TO_THREE, SIX_TO_SEVEN);
		IntermittentInterval expected = new IntermittentInterval(ONE_TO_THREE);
		assertEquals(expected, union1.intersection(union2));
		assertEquals(expected, union2.intersection(union1));
	}

	@Test
	public void testIntersectionEmptyResult() {
		IntermittentInterval union1 = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		IntermittentInterval union2 = new IntermittentInterval(TWO_TO_THREE, FOUR_TO_FIVE, SIX_TO_SEVEN);
		assertEquals(new IntermittentInterval(), union1.intersection(union2));
		assertEquals(new IntermittentInterval(), union2.intersection(union1));
	}

	@Test
	public void testIntersectionEqual() {
		assertEquals(new IntermittentInterval(ONE_TO_FIVE), new IntermittentInterval(ONE_TO_FIVE).intersection(new IntermittentInterval(ONE_TO_FIVE)));
	}

	@Test
	public void testIntersectionWithEmpty() {
		assertEquals(new IntermittentInterval(), new IntermittentInterval(ONE_TO_FIVE).intersection(new IntermittentInterval()));
		assertEquals(new IntermittentInterval(), new IntermittentInterval().intersection(new IntermittentInterval(ONE_TO_FIVE)));
	}

	@Test
	public void testOverlapWithEmpty() {
		assertFalse(new IntermittentInterval(ONE_TO_FIVE).overlaps(new IntermittentInterval()));
		assertFalse(new IntermittentInterval().overlaps(new IntermittentInterval(ONE_TO_FIVE)));
	}

	@Test
	public void testOverlapCompletelyBeforeFalse() {
		IntermittentInterval first = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR);
		IntermittentInterval second = new IntermittentInterval(FIVE_TO_SIX, SEVEN_TO_EIGHT);
		assertFalse(first.overlaps(second));
		assertFalse(second.overlaps(first));
	}

	@Test
	public void testOverlapTrivialFalse() {
		IntermittentInterval first = new IntermittentInterval(ONE_TO_TWO, FIVE_TO_SIX);
		IntermittentInterval second = new IntermittentInterval(THREE_TO_FOUR, SEVEN_TO_EIGHT);
		assertFalse(first.overlaps(second));
		assertFalse(second.overlaps(first));
	}

	@Test
	public void testOverlapTrivialTrue() {
		IntermittentInterval first = new IntermittentInterval(ONE_TO_FIVE, NINE_TO_INFINITY);
		IntermittentInterval second = new IntermittentInterval(THREE_TO_FOUR, SEVEN_TO_EIGHT);
		assertTrue(first.overlaps(second));
		assertTrue(second.overlaps(first));
	}

	@Test
	public void testDefaultConstructor() {
		IntermittentInterval union = new IntermittentInterval();
		assertNotNull(union);
		assertEquals(0, union.getIntervals().size());
	}

	@Test
	public void testIntervalConstructor() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO);
		assertNotNull(union);
		assertEquals(1, union.getIntervals().size());
		assertEquals(ONE_TO_TWO, union.getIntervals().get(0));
	}

	@Test
	public void testHasGapsTrivial() {
		assertFalse(new IntermittentInterval(ONE_TO_TWO).hasGaps());
	}

	@Test
	public void testHasGapsTrue() {
		assertTrue(new IntermittentInterval(ONE_TO_TWO, FOUR_TO_FIVE).hasGaps());
	}

	@Test
	public void testHasGapsEmpty() {
		assertFalse(new IntermittentInterval().hasGaps());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetPeriodsImmutable() {
		new IntermittentInterval(ONE_TO_TWO).getIntervals().add(ONE_TO_TWO);
	}

	@Test
	public void testAddNonOverlapping() {
		IntermittentInterval union = new IntermittentInterval(THREE_TO_FOUR);
		union = union.addInterval(ONE_TO_TWO);
		assertEquals(2, union.getIntervals().size());
		assertEquals(ONE_TO_TWO, union.getIntervals().get(0));
		assertEquals(THREE_TO_FOUR, union.getIntervals().get(1));
	}

	@Test
	public void testAddToAlways() {
		IntermittentInterval always = new IntermittentInterval(ALWAYS);
		always = always.addInterval(ONE_TO_TWO);
		assertEquals(1, always.getIntervals().size());
		assertEquals(ALWAYS, always.getIntervals().get(0));
	}

	@Test
	public void testAddOverlapping() {
		IntermittentInterval union = new IntermittentInterval(THREE_TO_FIVE);
		union = union.addInterval(SEVEN_TO_EIGHT);
		union = union.addInterval(ONE_TO_FOUR);
		assertEquals(2, union.getIntervals().size());
		assertEquals(ONE_TO_FIVE, union.getIntervals().get(0));
		assertEquals(SEVEN_TO_EIGHT, union.getIntervals().get(1));
	}

	@Test
	public void testAddAbutting() {
		IntermittentInterval union = new IntermittentInterval(THREE_TO_FIVE);
		union = union.addInterval(SEVEN_TO_EIGHT);
		union = union.addInterval(ONE_TO_THREE);
		assertEquals(2, union.getIntervals().size());
		assertEquals(ONE_TO_FIVE, union.getIntervals().get(0));
		assertEquals(SEVEN_TO_EIGHT, union.getIntervals().get(1));
	}

	@Test
	public void testAddConnector() {
		IntermittentInterval union = new IntermittentInterval(THREE_TO_FIVE);
		union = union.addInterval(SEVEN_TO_EIGHT);
		union = union.addInterval(FIVE_TO_SEVEN);
		assertEquals(1, union.getIntervals().size());
		assertEquals(THREE_TO_EIGHT, union.getIntervals().get(0));
	}

	@Test
	public void testArrayConstructor() {
		IntermittentInterval union = new IntermittentInterval(NINE_TO_INFINITY, THREE_TO_FIVE, SEVEN_TO_EIGHT, FIVE_TO_SEVEN, ONE_TO_TWO);
		assertEquals(3, union.getIntervals().size());
		assertEquals(ONE_TO_TWO, union.getIntervals().get(0));
		assertEquals(THREE_TO_EIGHT, union.getIntervals().get(1));
		assertEquals(NINE_TO_INFINITY, union.getIntervals().get(2));
	}

	@Test
	public void testIterableConstructor() {
		Collection<Interval> collection = new HashSet<Interval>();
		collection.add(NINE_TO_INFINITY);
		collection.add(THREE_TO_FIVE);
		collection.add(SEVEN_TO_EIGHT);
		collection.add(FIVE_TO_SEVEN);
		collection.add(ONE_TO_TWO);
		IntermittentInterval union = new IntermittentInterval(collection);
		assertEquals(3, union.getIntervals().size());
		assertEquals(ONE_TO_TWO, union.getIntervals().get(0));
		assertEquals(THREE_TO_EIGHT, union.getIntervals().get(1));
		assertEquals(NINE_TO_INFINITY, union.getIntervals().get(2));
	}

	@Test
	public void testRemoveIntervalOnEmpty() {
		IntermittentInterval union = new IntermittentInterval();
		union = union.remove(THREE_TO_FIVE);
		assertTrue(union.getIntervals().isEmpty());
	}

	@Test
	public void testHashcodeInLineWithEquals() {
		IntermittentInterval union1 = new IntermittentInterval(NINE_TO_INFINITY);
		IntermittentInterval union2 = new IntermittentInterval(NINE_TO_INFINITY);
		assertTrue(union1.hashCode() == union2.hashCode());
	}

	@Test
	public void testRemoveIntervalExact() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		union = union.remove(THREE_TO_FOUR);
		assertEquals(new IntermittentInterval(ONE_TO_TWO, FIVE_TO_SIX), union);
	}

	@Test
	public void testRemoveLeavesOriginalAlone() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		IntermittentInterval newUnion = union.remove(THREE_TO_FOUR);
		assertFalse(newUnion.equals(union));
	}

	@Test
	public void testRemoveCompleteOverlap() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		union = union.remove(new Interval(BETWEEN_TWO_AND_THREE, FOUR));
		assertEquals(new IntermittentInterval(ONE_TO_TWO, FIVE_TO_SIX), union);
	}

	@Test
	public void testRemoveMultipleCompleteOverlap() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		union = union.remove(new Interval(BETWEEN_TWO_AND_THREE, EIGHT));
		assertEquals(new IntermittentInterval(ONE_TO_TWO), union);
	}

	@Test
	public void testRemovePartialOverlap() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		union = union.remove(new Interval(BETWEEN_TWO_AND_THREE, BETWEEN_THREE_AND_FOUR));
		assertEquals(new IntermittentInterval(ONE_TO_TWO, new Interval(BETWEEN_THREE_AND_FOUR, FOUR), FIVE_TO_SIX), union);
	}

	@Test
	public void testRemoveDoublePartialOverlap() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		union = union.remove(new Interval(BETWEEN_ONE_AND_TWO, BETWEEN_THREE_AND_FOUR));
		assertEquals(new IntermittentInterval(new Interval(ONE, BETWEEN_ONE_AND_TWO), new Interval(BETWEEN_THREE_AND_FOUR, FOUR), FIVE_TO_SIX), union);
	}

	@Test
	public void testRemoveInternalOverlap() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_FOUR, FIVE_TO_SIX, SEVEN_TO_EIGHT);
		union = union.remove(new Interval(TWO, THREE));
		assertEquals(new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX, SEVEN_TO_EIGHT), union);
	}

	@Test
	public void testRemoveAll() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		union = union.remove(ALWAYS);
		assertEquals(new IntermittentInterval(), union);
	}

	@Test
	public void testRemoveNone() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, FIVE_TO_SIX);
		union = union.remove(THREE_TO_FOUR);
		assertEquals(new IntermittentInterval(ONE_TO_TWO, FIVE_TO_SIX), union);
	}

	@Test
	public void testToSpanningPeriod() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		assertEquals(new Interval(ONE, SIX), union.toSpanningPeriod());
		assertEquals(new Interval(ONE, SIX), union.toSpanningPeriod());
	}

	@Test
	public void testToSpanningPeriodOpenEnded() {
		IntermittentInterval union = new IntermittentInterval(BIGBANG_TO_TWO, THREE_TO_FOUR, FIVE_TO_INFINITY);
		assertEquals(ALWAYS, union.toSpanningPeriod());
	}

	@Test
	public void testToSpanningPeriodEmpty() {
		assertNull(new IntermittentInterval().toSpanningPeriod());
	}

	@Test
	public void testIsEmptyFalse() {
		assertFalse(new IntermittentInterval(BIGBANG_TO_TWO, THREE_TO_FOUR, FIVE_TO_INFINITY).isEmpty());
	}

	@Test
	public void testIsEmptyTrue() {
		assertTrue(new IntermittentInterval().isEmpty());
	}

	@Test
	public void testContainsForEmpty() {
		assertFalse(new IntermittentInterval().contains(ONE));
	}

	@Test
	public void testContainsTrivialTrue() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		assertTrue(union.contains(BETWEEN_THREE_AND_FOUR));
	}

	@Test
	public void testContainsTrivialFalse() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		assertFalse(union.contains(new Date(45000L)));
	}

	@Test
	public void testContainsBoundaryTrue() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		assertTrue(union.contains(THREE));
	}

	@Test
	public void testContainsBoundaryFalse() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		assertFalse(union.contains(FOUR));
	}

	@Test
	public void testUnionAll() {
		IntermittentInterval union1 = new IntermittentInterval(ONE_TO_TWO, THREE_TO_EIGHT, NINE_TO_INFINITY);
		IntermittentInterval union2 = new IntermittentInterval(ALWAYS);
		assertEquals(union2, union1.union(union2));
		assertEquals(union2, union2.union(union1));
	}

	@Test
	public void testUnionComplex() {
		IntermittentInterval union1 = new IntermittentInterval(ONE_TO_TWO, THREE_TO_EIGHT, NINE_TO_INFINITY);
		IntermittentInterval union2 = new IntermittentInterval(ONE_TO_FOUR, FIVE_TO_SIX, SEVEN_TO_INFINITY);
		IntermittentInterval expected = new IntermittentInterval(ONE_TO_INFINITY, NINE_TO_INFINITY);
		assertEquals(expected, union1.union(union2));
		assertEquals(expected, union2.union(union1));
	}

	@Test
	public void testUnionSimple() {
		assertEquals(new IntermittentInterval(ONE_TO_EIGHT, NINE_TO_INFINITY), new IntermittentInterval(ONE_TO_FIVE).union(new IntermittentInterval(THREE_TO_EIGHT, NINE_TO_INFINITY)));
		assertEquals(new IntermittentInterval(ONE_TO_EIGHT, NINE_TO_INFINITY), new IntermittentInterval(THREE_TO_EIGHT, NINE_TO_INFINITY).union(new IntermittentInterval(ONE_TO_FIVE)));
	}

	@Test
	public void testUnionEqual() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_FIVE);
		assertEquals(union, union.union(union));
	}

	@Test
	public void testUnionWithEmpty() {
		IntermittentInterval union1 = new IntermittentInterval(ONE_TO_FIVE);
		assertEquals(union1, union1.union(new IntermittentInterval()));
		assertEquals(union1, new IntermittentInterval().union(union1));
	}

	@Test
	public void testDifferenceAll() {
		IntermittentInterval union1 = new IntermittentInterval(ONE_TO_TWO, THREE_TO_EIGHT, NINE_TO_INFINITY);
		IntermittentInterval union2 = new IntermittentInterval(ALWAYS);
		IntermittentInterval expected = new IntermittentInterval(BIGBANG_TO_ONE, TWO_TO_THREE, EIGHT_TO_NINE);
		assertEquals(new IntermittentInterval(), union1.difference(union2));
		assertEquals(expected, union2.difference(union1));
	}

	@Test
	public void testDifferenceComplex() {
		IntermittentInterval union1 = new IntermittentInterval(ONE_TO_TWO, THREE_TO_EIGHT, NINE_TO_INFINITY);
		IntermittentInterval union2 = new IntermittentInterval(ONE_TO_FOUR, FIVE_TO_SIX, SEVEN_TO_INFINITY);
		IntermittentInterval expected1Minus2 = new IntermittentInterval(FOUR_TO_FIVE, SIX_TO_SEVEN);
		IntermittentInterval expected2Minus1 = new IntermittentInterval(TWO_TO_THREE, EIGHT_TO_NINE);
		assertEquals(expected1Minus2, union1.difference(union2));
		assertEquals(expected2Minus1, union2.difference(union1));
	}

	@Test
	public void testDifferenceSimple() {
		assertEquals(new IntermittentInterval(ONE_TO_THREE), new IntermittentInterval(ONE_TO_FIVE).difference(new IntermittentInterval(THREE_TO_EIGHT, NINE_TO_INFINITY)));
		assertEquals(new IntermittentInterval(FIVE_TO_EIGHT, NINE_TO_INFINITY), new IntermittentInterval(THREE_TO_EIGHT, NINE_TO_INFINITY).difference(new IntermittentInterval(ONE_TO_FIVE)));
	}

	@Test
	public void testDifferenceEqual() {
		IntermittentInterval union = new IntermittentInterval(ONE_TO_FIVE);
		assertEquals(new IntermittentInterval(), union.difference(union));
	}

	@Test
	public void testDifferenceWithEmpty() {
		IntermittentInterval union1 = new IntermittentInterval(ONE_TO_FIVE);
		assertEquals(union1, union1.difference(new IntermittentInterval()));
		assertEquals(new IntermittentInterval(), new IntermittentInterval().difference(union1));
	}

	@Test
	public void testNegativeSimple() {
		IntermittentInterval negative = new IntermittentInterval(BIGBANG_TO_ONE, SEVEN_TO_INFINITY);
		IntermittentInterval positive = new IntermittentInterval(ONE_TO_SEVEN);
		assertEquals(negative, positive.negative());
		assertEquals(positive, negative.negative());
	}

	@Test
	public void testNegativeAllAndEmpty() {
		IntermittentInterval negative = new IntermittentInterval();
		IntermittentInterval positive = new IntermittentInterval(ALWAYS);
		assertEquals(negative, positive.negative());
		assertEquals(positive, negative.negative());
	}

	@Test
	public void testNegativeComplex() {
		IntermittentInterval negative = new IntermittentInterval(BIGBANG_TO_ONE, TWO_TO_THREE, FOUR_TO_FIVE, SEVEN_TO_EIGHT);
		IntermittentInterval positive = new IntermittentInterval(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SEVEN, EIGHT_TO_INFINITY);
		assertEquals(negative, positive.negative());
		assertEquals(positive, negative.negative());
	}

	@Test
	public void testEquals() {
		assertFalse(new IntermittentInterval().equals(null));
		assertFalse(new IntermittentInterval().equals("A"));
		assertTrue(new IntermittentInterval().equals(new IntermittentInterval()));
		IntermittentInterval always = new IntermittentInterval(ALWAYS);
		assertTrue(always.equals(always));
	}

    @Test
    public void testIgnoreEmptyIntervalsInConstructor() {
        assertTrue(new IntermittentInterval(new Interval(ONE, ONE)).isEmpty());
        assertTrue(new IntermittentInterval(THREE_TO_EIGHT, new Interval(ONE, ONE)).equals(new IntermittentInterval(THREE_TO_EIGHT)));
    }

    @Test
    public void testIgnoreEmptyIntervalsForAdding() {
        assertTrue(new IntermittentInterval().addInterval(new Interval(ONE, ONE)).isEmpty());
        assertTrue(new IntermittentInterval(THREE_TO_EIGHT).addInterval(new Interval(ONE, ONE)).equals(new IntermittentInterval(THREE_TO_EIGHT)));
    }

}
