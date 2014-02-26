package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.util.conditions.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MultivaluedHashMap;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RestQueryImplTest {

    private RestQueryImpl<String> restQuery;

    @Mock
    private Condition condition,conditionResolved;
    @Mock
    private Query<String> query;

    @Before
    public void setUp() {
        restQuery = new RestQueryImpl<>(query);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testSelect() {
        when(condition.and(any(Condition.class))).thenReturn(conditionResolved);

        MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();
        map.put("start", Arrays.asList("0"));
        map.put("limit", Arrays.asList("100"));
        QueryParameters wrap = QueryParameters.wrap(map);
        restQuery.select(wrap, condition);

        verify(query).select(conditionResolved, 1, 101);
    }


}
