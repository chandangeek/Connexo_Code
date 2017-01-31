/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util.impl;

import java.util.Optional;

import com.elster.jupiter.orm.QueryExecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryImplTest {

    private QueryImpl<String> query;

    @Mock
    private QueryExecutor<String> queryExecutor;

    @Before
    public void setUp() {
        query = new QueryImpl<>(queryExecutor);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGet() {
        when(queryExecutor.get(new Object[]{"A"}, true, null)).thenReturn(Optional.of("Ok"));

        Optional<String> a = query.get("A");

        assertThat(a.get()).isEqualTo("Ok");
    }

}
