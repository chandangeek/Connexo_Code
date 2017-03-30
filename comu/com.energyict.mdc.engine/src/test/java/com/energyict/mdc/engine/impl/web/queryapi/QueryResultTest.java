/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.queryapi;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link QueryResult} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-10 (11:03)
 */
public class QueryResultTest {

    private static final String QUERYID = "QueryResultTest";
    private static final String QUERY_STRING_RESULT = "Query String Result";

    @Test
    public void testFactoryMethod () {
        // Business method
        QueryResult queryResult = QueryResult.forResult(QUERYID, QUERY_STRING_RESULT);

        // Asserts
        assertThat(queryResult.queryId).isEqualTo(QUERYID);
        assertThat(queryResult.value).isEqualTo(QUERY_STRING_RESULT);
    }

    @Test
    public void testMarshallWithStringResult () throws IOException {
        StringWriter writer = new StringWriter();
        QueryResult queryResult = QueryResult.forResult(QUERYID, QUERY_STRING_RESULT);

        // Business method
        new ObjectMapper().writeValue(writer, queryResult);

        // Asserts
        String marshalled = writer.toString();
        assertThat(marshalled).contains(QUERYID);
        assertThat(marshalled).contains(QUERY_STRING_RESULT);
    }

}