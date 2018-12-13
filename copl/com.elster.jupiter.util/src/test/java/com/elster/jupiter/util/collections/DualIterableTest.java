/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;


import com.elster.jupiter.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tom De Greyt (tgr)
 */
public class DualIterableTest {

    private List<String> threeStrings;
    private List<String> twoStrings;
    private List<String> noStrings;
    private List<Integer> twoInts;
    private List<Integer> noInts;
    private static final String ONE = "ONE";
    private static final String TWO = "TWO";
    private static final String THREE = "THREE";

    @Before
    public void setUp() {
        threeStrings = Arrays.asList(ONE, TWO, THREE);
        twoStrings = Arrays.asList(ONE, TWO);
        noStrings = Collections.emptyList();
        twoInts = Arrays.asList(1, 2);
        noInts = Collections.emptyList();
    }

    @Test
    public void testEndWithShortestHasNextFirstIsShorter() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithShortest(twoInts, threeStrings);
        Iterator<Pair<Integer, String>> iter = iterable.iterator();
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(Pair.of(1, ONE));
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(Pair.of(2, TWO));
        assertThat(iter.hasNext()).isFalse();
    }

    @Test
    public void testEndWithShortestHasNextLastIsShorter() throws Exception {
        DualIterable<String, Integer> iterable2 = DualIterable.endWithShortest(threeStrings, twoInts);
        Iterator<Pair<String, Integer>> iter2 = iterable2.iterator();
        assertThat(iter2.hasNext()).isTrue();
        assertThat(iter2.next()).isEqualTo(Pair.of(ONE, 1));
        assertThat(iter2.hasNext()).isTrue();
        assertThat(iter2.next()).isEqualTo(Pair.of(TWO, 2));
        assertThat(iter2.hasNext()).isFalse();
    }

    @Test
    public void testEndWithShortestHasNextWithLastEmpty() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithShortest(twoInts, noStrings);
        assertThat(iterable.iterator().hasNext()).isFalse();
    }

    @Test
    public void testEndWithShortestHasNextWithFirstEmpty() throws Exception {
        DualIterable<Integer, String> iterable2 = DualIterable.endWithShortest(noInts, threeStrings);
        assertThat(iterable2.iterator().hasNext()).isFalse();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEndWithShortestRemove() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithShortest(twoInts, twoStrings);
        Iterator<Pair<Integer, String>> iter = iterable.iterator();
        iter.next();
        iter.remove();
    }


    @Test
    public void testEndWithLongestHasNextNormalLastIsLonger() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithLongest(twoInts, threeStrings);
        Iterator<Pair<Integer, String>> iter = iterable.iterator();
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(Pair.of(1, ONE));
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(Pair.of(2, TWO));
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(Pair.of((Integer) null, THREE));
        assertThat(iter.hasNext()).isFalse();
    }

    @Test
    public void testEndWithLongestHasNextNormalFirstIsLonger() throws Exception {
        DualIterable<String, Integer> iterable2 = DualIterable.endWithLongest(threeStrings, twoInts);
        Iterator<Pair<String, Integer>> iter2 = iterable2.iterator();
        assertThat(iter2.hasNext()).isTrue();
        assertThat(iter2.next()).isEqualTo(Pair.of(ONE, 1));
        assertThat(iter2.hasNext()).isTrue();
        assertThat(iter2.next()).isEqualTo(Pair.of(TWO, 2));
        assertThat(iter2.hasNext()).isTrue();
        assertThat(iter2.next()).isEqualTo(Pair.of(THREE, (Integer) null));
        assertThat(iter2.hasNext()).isFalse();
    }

    @Test
    public void testEndWithLongestHasNextWithEmpty() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithLongest(twoInts, noStrings);
        Iterator<Pair<Integer, String>> iter = iterable.iterator();
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(Pair.of(1, (String) null));
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(Pair.of(2, (String) null));
        assertThat(iter.hasNext()).isFalse();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEndWithLongestRemove() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithLongest(twoInts, twoStrings);
        Iterator<Pair<Integer, String>> iter = iterable.iterator();
        iter.next();
        iter.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void testBeyondEnd() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithLongest(twoInts, twoStrings);
        Iterator<Pair<Integer, String>> iter = iterable.iterator();
        iter.next();
        iter.next();
        iter.next();
    }

}
