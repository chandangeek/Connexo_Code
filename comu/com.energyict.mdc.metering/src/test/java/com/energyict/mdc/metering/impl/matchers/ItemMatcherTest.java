package com.energyict.mdc.metering.impl.matchers;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 18/12/13
 * Time: 16:14
 */
public class ItemMatcherTest {

    @Test
    public void positiveMatchTest() {
        List<Integer> tests = Arrays.asList(3, 5, 7, 10, 13);
        ItemMatcher positiveMatcher = ItemMatcher.itemMatcherFor((Integer[]) tests.toArray());
        for (int i = 0; i < 20; i++) {
            if(tests.contains(i)){
                assertThat(positiveMatcher.match(i)).isTrue();
            } else {
                assertThat(positiveMatcher.match(i)).isFalse();
            }
        }
    }

    @Test
    public void negativeMatchTest() {
        List<Integer> tests = Arrays.asList(3, 5, 7, 10, 13);
        ItemMatcher positiveMatcher = ItemMatcher.itemsDontMatchFor((Integer[]) tests.toArray());
        for (int i = 0; i < 20; i++) {
            if(tests.contains(i)){
                assertThat(positiveMatcher.match(i)).isFalse();
            } else {
                assertThat(positiveMatcher.match(i)).isTrue();
            }
        }
    }

}
