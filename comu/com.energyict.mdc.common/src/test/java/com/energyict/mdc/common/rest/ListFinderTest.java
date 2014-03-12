package com.energyict.mdc.common.rest;

import com.energyict.mdc.common.services.ListFinder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListFinderTest {

    @Test
    public void testListFindPaged() throws Exception {
        List<Integer> ints = new ArrayList<>();
        for (int i=1; i<100; i++) {
            ints.add(i);
        }
        List<Integer> found = ListFinder.of(ints, new NullComparator()).paged(10, 10).find();
        assertThat(found).containsExactly(11,12,13,14,15,16,17,18,19,20,21);
    }

    @Test
    public void testListFindQueryParam() throws Exception {
        List<Integer> ints = new ArrayList<>();
        for (int i=1; i<100; i++) {
            ints.add(i);
        }
        QueryParameters queryParameters = mock(QueryParameters.class);
        when(queryParameters.getStart()).thenReturn(10);
        when(queryParameters.getLimit()).thenReturn(10);
        List<Integer> found = ListFinder.of(ints, new NullComparator()).from(queryParameters).find();
        assertThat(found).containsExactly(11,12,13,14,15,16,17,18,19,20,21); // +1 for 'has next page'
    }

    class NullComparator implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }
    }
}
