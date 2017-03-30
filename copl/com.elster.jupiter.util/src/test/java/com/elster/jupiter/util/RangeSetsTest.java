/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

import org.junit.Test;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class RangeSetsTest	{
	private static final Instant ONE = LocalDate.of(2010, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
	private static final Instant TWO = LocalDate.of(2010, 1, 2).atStartOfDay(ZoneId.systemDefault()).toInstant();
	private static final Instant THREE = LocalDate.of(2010, 1, 3).atStartOfDay(ZoneId.systemDefault()).toInstant();
	private static final Instant FOUR = LocalDate.of(2010, 1, 4).atStartOfDay(ZoneId.systemDefault()).toInstant();
	private static final Instant FIVE = LocalDate.of(2010, 1, 5).atStartOfDay(ZoneId.systemDefault()).toInstant();
	private static final Instant SIX = LocalDate.of(2010, 1, 6).atStartOfDay(ZoneId.systemDefault()).toInstant();
	private static final Instant SEVEN = LocalDate.of(2010, 1, 7).atStartOfDay(ZoneId.systemDefault()).toInstant();
	private static final Instant EIGHT = LocalDate.of(2010, 1, 8).atStartOfDay(ZoneId.systemDefault()).toInstant();
	private static final Instant NINE = LocalDate.of(2010, 1, 9).atStartOfDay(ZoneId.systemDefault()).toInstant();
	private static final Range<Instant> NINE_TO_INFINITY = Range.atLeast(NINE);
	private static final Range<Instant> SEVEN_TO_INFINITY = Range.atLeast(SEVEN);
	private static final Range<Instant> SEVEN_TO_EIGHT = Range.closedOpen(SEVEN, EIGHT);
	private static final Range<Instant> ALWAYS = Range.all();
	private static final Range<Instant> THREE_TO_EIGHT = Range.closedOpen(THREE, EIGHT);
	private static final Range<Instant> FIVE_TO_SIX = Range.closedOpen(FIVE, SIX);
	private static final Range<Instant> THREE_TO_FIVE = Range.closedOpen(THREE, FIVE);
	private static final Range<Instant> THREE_TO_FOUR = Range.closedOpen(THREE, FOUR);
	private static final Range<Instant> FOUR_TO_FIVE = Range.closedOpen(FOUR, FIVE);
	private static final Range<Instant> ONE_TO_THREE = Range.closedOpen(ONE, THREE);
	private static final Range<Instant> ONE_TO_FIVE = Range.closedOpen(ONE, FIVE);
	private static final Range<Instant> ONE_TO_FOUR = Range.closedOpen(ONE, FOUR);
	private static final Range<Instant> ONE_TO_TWO = Range.closedOpen(ONE, TWO);
	private static final Range<Instant> TWO_TO_THREE = Range.closedOpen(TWO, THREE);
	private static final Range<Instant> SIX_TO_SEVEN = Range.closedOpen(SIX, SEVEN);
	
	private RangeSet<Instant> of(Range<Instant> ... ranges) {
		ImmutableRangeSet.Builder<Instant> builder = ImmutableRangeSet.builder();
		Arrays.stream(ranges).forEach(range -> builder.add(range));
		return builder.build();
	}
	
	@Test
	public void testIntersectionAll() {
		RangeSet<Instant> union1 = of(ONE_TO_TWO, THREE_TO_EIGHT, NINE_TO_INFINITY);
		RangeSet<Instant> union2 = ImmutableRangeSet.of(ALWAYS);
		assertEquals(union1, RangeSets.intersection(union1, union2));
		assertEquals(union1, RangeSets.intersection(union2, union1));
	}
	
	
	
	@Test
	public void testIntersectionComplex() {
		RangeSet<Instant> union1 = of(ONE_TO_TWO, THREE_TO_EIGHT, NINE_TO_INFINITY);
		RangeSet<Instant> union2 = of(ONE_TO_FOUR, FIVE_TO_SIX, SEVEN_TO_INFINITY);
		RangeSet<Instant> expected = of(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX, SEVEN_TO_EIGHT, NINE_TO_INFINITY);
		assertEquals(expected, RangeSets.intersection(union1,union2));
		assertEquals(expected, RangeSets.intersection(union2,union1));
	}
	
	@Test
	public void testIntersection() {
		assertEquals(of(THREE_TO_FIVE), RangeSets.intersection(of(ONE_TO_FIVE), of(THREE_TO_EIGHT, NINE_TO_INFINITY)));
		assertEquals(of(THREE_TO_FIVE), RangeSets.intersection(of(THREE_TO_EIGHT, NINE_TO_INFINITY), of(ONE_TO_FIVE)));
	}
	
	
	@Test
	public void testIntersectionSimple1() {
		assertEquals(of(THREE_TO_FIVE), RangeSets.intersection(of(ONE_TO_FIVE), of(THREE_TO_EIGHT, NINE_TO_INFINITY)));
		assertEquals(of(THREE_TO_FIVE), RangeSets.intersection(of(THREE_TO_EIGHT, NINE_TO_INFINITY), of(ONE_TO_FIVE)));
	}
	
	
	@Test
	public void testIntersectionSimple2() {
		RangeSet<Instant> union1 = of(ONE_TO_THREE, SEVEN_TO_EIGHT, NINE_TO_INFINITY);
		RangeSet<Instant> union2 = of(ONE_TO_THREE, SIX_TO_SEVEN);
		RangeSet<Instant> expected = of(ONE_TO_THREE);
		assertEquals(expected, RangeSets.intersection(union1,union2));
		assertEquals(expected, RangeSets.intersection(union2,union1));
	}
	
	
	@Test
	public void testIntersectionEmptyResult() {
		RangeSet<Instant> union1 = of(ONE_TO_TWO, THREE_TO_FOUR, FIVE_TO_SIX);
		RangeSet<Instant> union2 = of(TWO_TO_THREE, FOUR_TO_FIVE, SIX_TO_SEVEN);
		assertEquals(of(), RangeSets.intersection(union1,union2));
		assertEquals(of(), RangeSets.intersection(union2,union1));
	}
	
	@Test
	public void testIntersectionEqual() {
		assertEquals(of(ONE_TO_FIVE), RangeSets.intersection(of(ONE_TO_FIVE), of(ONE_TO_FIVE)));
	}
	
	@Test
	public void testIntersectionWithEmpty() {
		assertEquals(of(), RangeSets.intersection(of(ONE_TO_FIVE), of()));
		assertEquals(of(), RangeSets.intersection(of(), of(ONE_TO_FIVE)));
	}
	
}
