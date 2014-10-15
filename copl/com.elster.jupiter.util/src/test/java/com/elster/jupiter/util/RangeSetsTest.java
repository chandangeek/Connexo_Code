package com.elster.jupiter.util;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Arrays;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.Test;

import com.elster.jupiter.util.time.IntermittentInterval;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class RangeSetsTest	{
	private static final Instant ONE = new DateMidnight(2010, 1, 1).toDate().toInstant();
	private static final Instant TWO = new DateMidnight(2010, 1, 2).toDate().toInstant();
	private static final Instant THREE = new DateMidnight(2010, 1, 3).toDate().toInstant();
	private static final Instant FOUR = new DateMidnight(2010, 1, 4).toDate().toInstant();
	private static final Instant FIVE = new DateMidnight(2010, 1, 5).toDate().toInstant();
	private static final Instant SIX = new DateMidnight(2010, 1, 6).toDate().toInstant();
	private static final Instant SEVEN = new DateMidnight(2010, 1, 7).toDate().toInstant();
	private static final Instant EIGHT = new DateMidnight(2010, 1, 8).toDate().toInstant();
	private static final Instant NINE = new DateMidnight(2010, 1, 9).toDate().toInstant();
	private static final Instant BETWEEN_ONE_AND_TWO = new DateTime(ONE.toEpochMilli()).plusHours(12).toDate().toInstant();
	private static final Instant BETWEEN_TWO_AND_THREE = new DateTime(TWO.toEpochMilli()).plusHours(12).toDate().toInstant();
	private static final Instant BETWEEN_THREE_AND_FOUR = new DateTime(THREE.toEpochMilli()).plusHours(12).toDate().toInstant();
	private static final Range<Instant> NINE_TO_INFINITY = Range.atLeast(NINE);
	private static final Range<Instant> SEVEN_TO_INFINITY = Range.atLeast(SEVEN);
	private static final Range<Instant> SEVEN_TO_EIGHT = Range.closedOpen(SEVEN, EIGHT);
	private static final Range<Instant> ALWAYS = Range.all();
	private static final Range<Instant> THREE_TO_EIGHT = Range.closedOpen(THREE, EIGHT);
	private static final Range<Instant> FIVE_TO_INFINITY = Range.atLeast(FIVE);
	private static final Range<Instant> FIVE_TO_SIX = Range.closedOpen(FIVE, SIX);
	private static final Range<Instant> FIVE_TO_SEVEN = Range.closedOpen(FIVE, SEVEN);
	private static final Range<Instant> THREE_TO_FIVE = Range.closedOpen(THREE, FIVE);
	private static final Range<Instant> THREE_TO_FOUR = Range.closedOpen(THREE, FOUR);
	private static final Range<Instant> FOUR_TO_FIVE = Range.closedOpen(FOUR, FIVE);
	private static final Range<Instant> BIGBANG_TO_TWO = Range.lessThan(TWO);
	private static final Range<Instant> ONE_TO_THREE = Range.closedOpen(ONE, THREE);
	private static final Range<Instant> ONE_TO_FIVE = Range.closedOpen(ONE, FIVE);
	private static final Range<Instant> ONE_TO_FOUR = Range.closedOpen(ONE, FOUR);
	private static final Range<Instant> ONE_TO_TWO = Range.closedOpen(ONE, TWO);
	private static final Range<Instant> ONE_TO_INFINITY = Range.atLeast(ONE);
	private static final Range<Instant> ONE_TO_EIGHT = Range.closedOpen(ONE, EIGHT);
	private static final Range<Instant> BIGBANG_TO_ONE = Range.lessThan(ONE);
	private static final Range<Instant> EIGHT_TO_NINE = Range.closedOpen(EIGHT, NINE);
	private static final Range<Instant> TWO_TO_THREE = Range.closedOpen(TWO, THREE);
	private static final Range<Instant> SIX_TO_SEVEN = Range.closedOpen(SIX, SEVEN);
	private static final Range<Instant> FIVE_TO_EIGHT = Range.closedOpen(FIVE, EIGHT);
	private static final Range<Instant> ONE_TO_SEVEN = Range.closedOpen(ONE, SEVEN);
	private static final Range<Instant> EIGHT_TO_INFINITY = Range.atLeast(EIGHT);
	
	private RangeSet<Instant> of(Range<Instant> ... ranges) {
		ImmutableRangeSet.Builder<Instant> builder = ImmutableRangeSet.builder();
		Arrays.stream(ranges).forEach(range -> builder.add(range));
		return builder.build();
	}
	
	@Test
	public void testIntersectionAll() {
		RangeSet<Instant> union1 = RangeSets.of(ONE_TO_TWO, THREE_TO_EIGHT, NINE_TO_INFINITY);
		RangeSet<Instant> union2 = ImmutableRangeSet.of(ALWAYS);
		assertEquals(union1, RangeSets.intersection(union1, union2));
		assertEquals(union1, RangeSets.intersection(union2, union1));
	}
	
	
	
	@Test
	public void testIntersectionComplex() {
		RangeSet<Instant> union1 = RangeSets.of(ONE_TO_TWO, THREE_TO_EIGHT, NINE_TO_INFINITY);
		RangeSet<Instant> union2 = RangeSets.of(ONE_TO_FOUR, FIVE_TO_SIX, SEVEN_TO_INFINITY);
		RangeSet<Instant> expected = RangeSets.of(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX, SEVEN_TO_EIGHT, NINE_TO_INFINITY);
		assertEquals(expected, RangeSets.intersection(union1,union2));
		assertEquals(expected, RangeSets.intersection(union2,union1));
	}
	
	@Test
	public void testIntersection() {
		assertEquals(RangeSets.of(THREE_TO_FIVE), RangeSets.intersection(RangeSets.of(ONE_TO_FIVE), RangeSets.of(THREE_TO_EIGHT, NINE_TO_INFINITY)));
		assertEquals(RangeSets.of(THREE_TO_FIVE), RangeSets.intersection(RangeSets.of(THREE_TO_EIGHT, NINE_TO_INFINITY), RangeSets.of(ONE_TO_FIVE)));
	}
	
	
	@Test
	public void testIntersectionSimple1() {
		assertEquals(RangeSets.of(THREE_TO_FIVE), RangeSets.intersection(RangeSets.of(ONE_TO_FIVE), RangeSets.of(THREE_TO_EIGHT, NINE_TO_INFINITY)));
		assertEquals(RangeSets.of(THREE_TO_FIVE), RangeSets.intersection(RangeSets.of(THREE_TO_EIGHT, NINE_TO_INFINITY), RangeSets.of(ONE_TO_FIVE)));
	}
	
	
	@Test
	public void testIntersectionSimple2() {
		RangeSet<Instant> union1 = RangeSets.of(ONE_TO_THREE, SEVEN_TO_EIGHT, NINE_TO_INFINITY);
		RangeSet<Instant> union2 = RangeSets.of(ONE_TO_THREE, SIX_TO_SEVEN);
		RangeSet<Instant> expected = RangeSets.of(ONE_TO_THREE);
		assertEquals(expected, RangeSets.intersection(union1,union2));
		assertEquals(expected, RangeSets.intersection(union2,union1));
	}
	
	
	@Test
	public void testIntersectionEmptyResult() {
		RangeSet<Instant> union1 = RangeSets.of(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		RangeSet<Instant> union2 = RangeSets.of(TWO_TO_THREE, FOUR_TO_FIVE, SIX_TO_SEVEN);
		assertEquals(RangeSets.of(), RangeSets.intersection(union1,union2));
		assertEquals(RangeSets.of(), RangeSets.intersection(union2,union1));
	}
	
	@Test
	public void testIntersectionEqual() {
		assertEquals(RangeSets.of(ONE_TO_FIVE), RangeSets.intersection(RangeSets.of(ONE_TO_FIVE), RangeSets.of(ONE_TO_FIVE)));
	}
	
	@Test
	public void testIntersectionWithEmpty() {
		assertEquals(RangeSets.of(), RangeSets.intersection(RangeSets.of(ONE_TO_FIVE), RangeSets.of()));
		assertEquals(RangeSets.of(), RangeSets.intersection(RangeSets.of(), RangeSets.of(ONE_TO_FIVE)));
	}
	
}
