package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 8/18/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultFinderTest {

    @Mock
    DataModel dataModel;
    @Mock
    QueryExecutor queryExecutor;

    @Test
    public void testPaging() throws Exception {
        final List<Integer> integers = intList(240);
        mockQuery(integers);

        DefaultFinder<Integer> finder = DefaultFinder.of(Integer.class, dataModel);
        List<Integer> results = finder.stream().collect(toList());
        assertList(results, 240);
    }

    @Test
    public void testPagingWithExactPageSizeMatch() throws Exception {
        final List<Integer> integers = intList(200);
        mockQuery(integers);

        DefaultFinder<Integer> finder = DefaultFinder.of(Integer.class, dataModel);
        List<Integer> results = finder.stream().collect(toList());
        assertList(results, 200);
    }

    @Test
    public void testPagingWithSingleFullPage() throws Exception {
        final List<Integer> integers = intList(100);
        mockQuery(integers);

        DefaultFinder<Integer> finder = DefaultFinder.of(Integer.class, dataModel);
        List<Integer> results = finder.stream().collect(toList());
        assertList(results, 100);
    }

    @Test
    public void testPagingWithSinglePage() throws Exception {
        final List<Integer> integers = intList(99);
        mockQuery(integers);

        DefaultFinder<Integer> finder = DefaultFinder.of(Integer.class, dataModel);
        List<Integer> results = finder.stream().collect(toList());
        assertList(results, 99);
    }

    @Test
    public void testPagingWithEmptyList() throws Exception {
        final List<Integer> integers = Collections.emptyList();
        mockQuery(integers);

        DefaultFinder<Integer> finder = DefaultFinder.of(Integer.class, dataModel);
        List<Integer> results = finder.stream().collect(toList());
        assertThat(results).isEmpty();
    }

    private void mockQuery(List<Integer> integers) {
        when(queryExecutor.select(any(), any(), anyBoolean(), any(), anyInt(), anyInt())).thenAnswer(invocationOnMock -> integers.subList((int)invocationOnMock.getArguments()[4]-1, Math.min((int)invocationOnMock.getArguments()[5], integers.size())));
        when(dataModel.query(any(Class.class))).thenReturn(queryExecutor);
    }

    private void assertList(List<Integer> results, int max) {
        assertThat(results).hasSize(max);
        for (int t=0; t<max; t++) {
            assertThat(results.get(t)).isEqualTo(t+1);
        }
    }

    private List<Integer> intList(int max) {
        final List<Integer> integers = new ArrayList<>();
        for (int i=1; i<=max; i++) {
            integers.add(i);
        }
        return integers;
    }


}
