package com.elster.jupiter.util;

import com.google.common.collect.Range;
import org.junit.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class RangesTest {

    @Test
    public void testMapAll() {
        assertThat(Ranges.map(Range.<Character>all(), indexInAlphabet())).isEqualTo(Range.<Integer>all());
    }

    private Function<Character, Integer> indexInAlphabet() {
        return c -> c - 'A' + 1;
    }

    @Test
    public void testMapAtMost() {
        assertThat(Ranges.map(Range.atMost('T'), indexInAlphabet())).isEqualTo(Range.atMost(20));
    }

    @Test
    public void testMapLessThan() {
        assertThat(Ranges.map(Range.lessThan('T'), indexInAlphabet())).isEqualTo(Range.lessThan(20));
    }

    @Test
    public void testMapAtLeast() {
        assertThat(Ranges.map(Range.atLeast('F'), indexInAlphabet())).isEqualTo(Range.atLeast(6));
    }

    @Test
    public void testMapGreaterThan() {
        assertThat(Ranges.map(Range.greaterThan('F'), indexInAlphabet())).isEqualTo(Range.greaterThan(6));
    }

    @Test
    public void testMapClosed() {
        assertThat(Ranges.map(Range.closed('F', 'T'), indexInAlphabet())).isEqualTo(Range.closed(6, 20));
    }

    @Test
    public void testMapOpenClosed() {
        assertThat(Ranges.map(Range.openClosed('F', 'T'), indexInAlphabet())).isEqualTo(Range.openClosed(6, 20));
    }

    @Test
    public void testMapClosedOpen() {
        assertThat(Ranges.map(Range.closedOpen('F', 'T'), indexInAlphabet())).isEqualTo(Range.closedOpen(6, 20));
    }

    @Test
    public void testMapOpen() {
        assertThat(Ranges.map(Range.open('F', 'T'), indexInAlphabet())).isEqualTo(Range.open(6, 20));
    }

}
