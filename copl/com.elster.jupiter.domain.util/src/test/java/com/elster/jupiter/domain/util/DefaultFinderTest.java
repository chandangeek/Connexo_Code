/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 8/18/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultFinderTest {

    @Mock
    DataModel dataModel;
    @Mock
    QueryExecutor<Integer> queryExecutor;

    @Test
    public void testPaging() throws Exception {
        final List<Integer> integers = intList(240);
        mockQuery(integers);

        Finder<Integer> finder = DefaultFinder.of(Integer.class, dataModel).maxPageSize(null, 100);
        List<Integer> results = finder.stream().collect(toList());
        assertList(results, 240);
    }

    @Test
    public void testPagingWithExactPageSizeMatch() throws Exception {
        final List<Integer> integers = intList(200);
        mockQuery(integers);

        Finder<Integer> finder = DefaultFinder.of(Integer.class, dataModel).maxPageSize(null, 100);
        List<Integer> results = finder.stream().collect(toList());
        assertList(results, 200);
    }

    @Test
    public void testPagingWithSingleFullPage() throws Exception {
        final List<Integer> integers = intList(100);
        mockQuery(integers);

        Finder<Integer> finder = DefaultFinder.of(Integer.class, dataModel).maxPageSize(null, 100);
        List<Integer> results = finder.stream().collect(toList());
        assertList(results, 100);
    }

    @Test
    public void testPagingWithSinglePage() throws Exception {
        final List<Integer> integers = intList(99);
        mockQuery(integers);

        Finder<Integer> finder = DefaultFinder.of(Integer.class, dataModel).maxPageSize(null, 100);
        List<Integer> results = finder.stream().collect(toList());
        assertList(results, 99);
    }

    @Test
    public void testPagingWithEmptyList() throws Exception {
        final List<Integer> integers = Collections.emptyList();
        mockQuery(integers);

        Finder<Integer> finder = DefaultFinder.of(Integer.class, dataModel).maxPageSize(null, 100);
        List<Integer> results = finder.stream().collect(toList());
        assertThat(results).isEmpty();
    }

    @Test
    public void testStreamOverPagedFinder() throws Exception {
        final List<Integer> integers = intList(1000);
        mockQuery(integers);

        Finder<Integer> finder = DefaultFinder.of(Integer.class, dataModel).maxPageSize(null, 100).paged(0, 10);
        List<Integer> results = finder.stream().collect(toList());
        assertList(results, 11);
    }

    private void mockQuery(List<Integer> integers) {
        when(queryExecutor.select(any(Condition.class), anyObject(), anyObject(), anyBoolean(), anyObject(), anyInt(), anyInt())).thenAnswer(invocationOnMock -> integers.subList((int) invocationOnMock.getArguments()[5] - 1, Math.min((int) invocationOnMock.getArguments()[6], integers.size())));
        doReturn(queryExecutor).when(dataModel).query(any());
    }

    private void assertList(List<Integer> results, int max) {
        assertThat(results).hasSize(max);
        for (int t = 0; t < max; t++) {
            assertThat(results.get(t)).isEqualTo(t + 1);
        }
    }

    private List<Integer> intList(int max) {
        final List<Integer> integers = new ArrayList<>();
        for (int i = 1; i <= max; i++) {
            integers.add(i);
        }
        return integers;
    }
}