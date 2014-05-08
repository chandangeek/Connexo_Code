package com.energyict.mdc.engine.impl.tools;

import com.elster.jupiter.util.Pair;
import org.junit.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

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
    public void testEndWithShortestHasNextNormal() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithShortest(twoInts, threeStrings);
        Iterator<Pair<Integer,String>> iter = iterable.iterator();
        assertTrue(iter.hasNext());
        Assert.assertEquals(Pair.of(1, ONE), iter.next());
        assertTrue(iter.hasNext());
        Assert.assertEquals(Pair.of(2, TWO), iter.next());
        assertFalse(iter.hasNext());
        DualIterable<String, Integer> iterable2 = DualIterable.endWithShortest(threeStrings, twoInts);
        Iterator<Pair<String, Integer>> iter2 = iterable2.iterator();
        assertTrue(iter2.hasNext());
        Assert.assertEquals(Pair.of(ONE, 1), iter2.next());
        assertTrue(iter2.hasNext());
        Assert.assertEquals(Pair.of(TWO, 2), iter2.next());
        assertFalse(iter2.hasNext());
    }

    @Test
    public void testEndWithShortestHasNextWithEmpty() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithShortest(twoInts, noStrings);
        assertFalse(iterable.iterator().hasNext());
        DualIterable<Integer, String> iterable2 = DualIterable.endWithShortest(noInts, threeStrings);
        assertFalse(iterable2.iterator().hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEndWithShortestRemove() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithShortest(twoInts, twoStrings);
        Iterator<Pair<Integer, String>> iter = iterable.iterator();
        iter.next();
        iter.remove();
    }


    @Test
    public void testEndWithLongestHasNextNormal() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithLongest(twoInts, threeStrings);
        Iterator<Pair<Integer, String>> iter = iterable.iterator();
        assertTrue(iter.hasNext());
        Assert.assertEquals(Pair.of(1, ONE), iter.next());
        assertTrue(iter.hasNext());
        Assert.assertEquals(Pair.of(2, TWO), iter.next());
        assertTrue(iter.hasNext());
        Assert.assertEquals(Pair.of(null, THREE), iter.next());
        assertFalse(iter.hasNext());
        DualIterable<String, Integer> iterable2 = DualIterable.endWithLongest(threeStrings, twoInts);
        Iterator<Pair<String, Integer>> iter2 = iterable2.iterator();
        assertTrue(iter2.hasNext());
        Assert.assertEquals(Pair.of(ONE, 1), iter2.next());
        assertTrue(iter2.hasNext());
        Assert.assertEquals(Pair.of(TWO, 2), iter2.next());
        assertTrue(iter2.hasNext());
        Assert.assertEquals(Pair.of(THREE, null), iter2.next());
        assertFalse(iter2.hasNext());
    }

    @Test
    public void testEndWithLongestHasNextWithEmpty() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithLongest(twoInts, noStrings);
        Iterator<Pair<Integer, String>> iter = iterable.iterator();
        assertTrue(iter.hasNext());
        Assert.assertEquals(Pair.of(1, null), iter.next());
        assertTrue(iter.hasNext());
        Assert.assertEquals(Pair.of(2, null), iter.next());
        assertFalse(iter.hasNext());
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
