/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListPagerTest {

    @Test
    public void testListFindPaged() throws Exception {
        List<Integer> ints = new ArrayList<>();
        for (int i=1; i<100; i++) {
            ints.add(i);
        }
        List<Integer> found = ListPager.of(ints, new NullComparator()).paged(10, 10).find();
        assertThat(found).containsExactly(11,12,13,14,15,16,17,18,19,20,21);
    }

    @Test
    public void testListFindQueryParam() throws Exception {
        List<Integer> ints = new ArrayList<>();
        for (int i=1; i<100; i++) {
            ints.add(i);
        }
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getStart()).thenReturn(Optional.of(10));
        when(queryParameters.getLimit()).thenReturn(Optional.of(10));
        List<Integer> found = ListPager.of(ints, new NullComparator()).from(queryParameters).find();
        assertThat(found).containsExactly(11,12,13,14,15,16,17,18,19,20,21); // +1 for 'has next page'
    }

    @Test
    public void testGetIncompletePage() throws Exception {
        List<Integer> ints = Arrays.asList(1,2,3,4,5,6,7);
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getStart()).thenReturn(Optional.of(0));
        when(queryParameters.getLimit()).thenReturn(Optional.of(10));
        List<Integer> found = ListPager.of(ints, new NullComparator()).from(queryParameters).find();
        assertThat(found).containsExactly(1,2,3,4,5,6,7);
    }

    @Test
    public void testGetIncompleteSecondPage() throws Exception {
        List<Integer> ints = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12);
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getStart()).thenReturn(Optional.of(10));
        when(queryParameters.getLimit()).thenReturn(Optional.of(10));
        List<Integer> found = ListPager.of(ints, new NullComparator()).from(queryParameters).find();
        assertThat(found).containsExactly(11,12);
    }

    @Test
    public void testGetOutOfBoundsPage() throws Exception {
        List<Integer> ints = Arrays.asList(1,2,3,4,5,6,7);
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getStart()).thenReturn(Optional.of(10));
        when(queryParameters.getLimit()).thenReturn(Optional.of(10));
        List<Integer> found = ListPager.of(ints, new NullComparator()).from(queryParameters).find();
        assertThat(found).isEmpty();
    }

    @Test
    public void testGetFullPageSorted() throws Exception {
        List<Integer> ints = Arrays.asList(1,2,3,4,5,6,7,8,9,0);
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getStart()).thenReturn(Optional.of(0));
        when(queryParameters.getLimit()).thenReturn(Optional.of(10));
        List<Integer> found = ListPager.of(ints, new NullComparator()).from(queryParameters).find();
        assertThat(found).containsExactly(0,1,2,3,4,5,6,7,8,9);
    }

    @Test
    public void testGetFirstPage() throws Exception {
        List<Integer> ints = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30);
        assertThat(ints).hasSize(30);
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getStart()).thenReturn(Optional.of(0));
        when(queryParameters.getLimit()).thenReturn(Optional.of(10));
        List<Integer> found = ListPager.of(ints, new NullComparator()).from(queryParameters).find();
        assertThat(found).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
    }

    @Test
    public void testGetSecondPage() throws Exception {
        List<Integer> ints = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30);
        assertThat(ints).hasSize(30);
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getStart()).thenReturn(Optional.of(10));
        when(queryParameters.getLimit()).thenReturn(Optional.of(10));
        List<Integer> found = ListPager.of(ints, new NullComparator()).from(queryParameters).find();
        assertThat(found).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21);
    }

    class NullComparator implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }
    }
}
