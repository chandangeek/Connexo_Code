package com.elster.jupiter.util.collections;


import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.elster.jupiter.util.collections.DualIterable.Pair;
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
        assertThat(iter.next()).isEqualTo(new MyPair<>(1, ONE));
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(new MyPair<>(2, TWO));
        assertThat(iter.hasNext()).isFalse();
    }

    @Test
    public void testEndWithShortestHasNextLastIsShorter() throws Exception {
        DualIterable<String, Integer> iterable2 = DualIterable.endWithShortest(threeStrings, twoInts);
        Iterator<Pair<String, Integer>> iter2 = iterable2.iterator();
        assertThat(iter2.hasNext()).isTrue();
        assertThat(iter2.next()).isEqualTo(new MyPair<>(ONE, 1));
        assertThat(iter2.hasNext()).isTrue();
        assertThat(iter2.next()).isEqualTo(new MyPair<>(TWO, 2));
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
        assertThat(iter.next()).isEqualTo(new MyPair<>(1, ONE));
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(new MyPair<>(2, TWO));
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(new MyPair<>((Integer) null, THREE));
        assertThat(iter.hasNext()).isFalse();
    }

    @Test
    public void testEndWithLongestHasNextNormalFirstIsLonger() throws Exception {
        DualIterable<String, Integer> iterable2 = DualIterable.endWithLongest(threeStrings, twoInts);
        Iterator<Pair<String, Integer>> iter2 = iterable2.iterator();
        assertThat(iter2.hasNext()).isTrue();
        assertThat(iter2.next()).isEqualTo(new MyPair<>(ONE, 1));
        assertThat(iter2.hasNext()).isTrue();
        assertThat(iter2.next()).isEqualTo(new MyPair<>(TWO, 2));
        assertThat(iter2.hasNext()).isTrue();
        assertThat(iter2.next()).isEqualTo(new MyPair<>(THREE, (Integer) null));
        assertThat(iter2.hasNext()).isFalse();
    }

    @Test
    public void testEndWithLongestHasNextWithEmpty() throws Exception {
        DualIterable<Integer, String> iterable = DualIterable.endWithLongest(twoInts, noStrings);
        Iterator<Pair<Integer, String>> iter = iterable.iterator();
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(new MyPair<>(1, (String) null));
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next()).isEqualTo(new MyPair<>(2, (String) null));
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


    private static class MyPair<A, B> implements Pair<A, B> {

        private final A a;
        private final B b;

        private MyPair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public A getFirst() {
            return a;
        }

        @Override
        public B getLast() {
            return b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pair)) {
                return false;
            }

            Pair<?, ?> myPair = (Pair<?, ?>) o;

            if (a != null ? !a.equals(myPair.getFirst()) : myPair.getFirst() != null) {
                return false;
            }
            if (b != null ? !b.equals(myPair.getLast()) : myPair.getLast() != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = a != null ? a.hashCode() : 0;
            result = 31 * result + (b != null ? b.hashCode() : 0);
            return result;
        }
    }
}
