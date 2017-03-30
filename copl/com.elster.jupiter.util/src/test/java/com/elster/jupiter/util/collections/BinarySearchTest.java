/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BinarySearchTest {

    @Test
    public void testFirstOccurrence() throws Exception {
        List<String> strings = Arrays.asList("A", "A", "B", "B", "B", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "D", "E", "E");

        assertThat(BinarySearch.usingValueAsKey(strings).firstOccurrence("C")).isEqualTo(5);
    }

    @Test
    public void testFirstOccurrenceOnEmptyList() throws Exception {
        List<String> strings = Collections.emptyList();

        assertThat(BinarySearch.usingValueAsKey(strings).firstOccurrence("C")).isEqualTo(-1);
    }

    @Test
    public void testLastOccurrenceOnEmptyList() throws Exception {
        List<String> strings = Collections.emptyList();

        assertThat(BinarySearch.usingValueAsKey(strings).lastOccurrence("C")).isEqualTo(-1);
    }

    @Test
    public void testAllFirstCombosTrue() {
        List<String> strings = Arrays.asList("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17");
        for (int i = 0; i < strings.size(); i++) {
            for (int j = i + 1; j < strings.size(); j++) {
                List<String> list = strings.subList(0, j);
                assertThat(BinarySearch.usingValueAsKey(list).firstOccurrence(strings.get(i))).as("finding " + strings.get(i) + " in list length " + j).isEqualTo(i);
            }

        }
    }

    @Test
    public void testAllFirstCombosFalse() {
        List<String> values = Arrays.asList("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17");
        List<String> strings = Arrays.asList("01", "03", "05", "07", "09", "11", "13", "15", "17");
        for (int i = 0; i < strings.size(); i+=2) {
            for (int j = i/2 + 1; j < strings.size(); j++) {
                List<String> list = strings.subList(0, j);
                assertThat(BinarySearch.usingValueAsKey(list).firstOccurrence(values.get(i))).as("finding " + values.get(i) + " in list length " + j).isEqualTo(-i/2 -1);
            }

        }
    }

    @Test
    public void testLastOccurrence() throws Exception {
        List<String> strings = Arrays.asList("A", "A", "B", "B", "B", "C", "C", "C", "C", "C", "C", "C", "C", "C", "C", "D", "E", "E");

        assertThat(BinarySearch.usingValueAsKey(strings).lastOccurrence("C")).isEqualTo(14);
    }

    @Test
    public void testAllLastCombosTrue() {
        List<String> strings = Arrays.asList("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17");
        for (int i = 0; i < strings.size(); i++) {
            for (int j = i + 1; j < strings.size(); j++) {
                List<String> list = strings.subList(0, j);
                assertThat(BinarySearch.usingValueAsKey(list).lastOccurrence(strings.get(i))).as("finding " + strings.get(i) + " in list length " + j).isEqualTo(i);
            }

        }
    }

    @Test
    public void testAllLastCombosFalse() {
        List<String> values = Arrays.asList("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17");
        List<String> strings = Arrays.asList("01", "03", "05", "07", "09", "11", "13", "15", "17");
        for (int i = 0; i < strings.size(); i+=2) {
            for (int j = i/2 + 1; j < strings.size(); j++) {
                List<String> list = strings.subList(0, j);
                assertThat(BinarySearch.usingValueAsKey(list).lastOccurrence(values.get(i))).as("finding " + values.get(i) + " in list length " + j).isEqualTo(-i / 2 - 1);
            }
        }
    }

}
