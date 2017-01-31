/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import org.json.JSONException;
import org.json.JSONObject;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.remote.BooleanParser} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-05-02 (11:09)
 */
public class BooleanParserTest {

    private static final String TRUE_AS_QUERY_RESULT = "{\"query-id\":\"attemptLock\",\"single-value\":\"true\"}";
    private static final String FALSE_AS_QUERY_RESULT = "{\"query-id\":\"attemptLock\",\"single-value\":\"false\"}";

    @Test
    public void testTrue () throws JSONException {
        BooleanParser parser = new BooleanParser();

        // Business method
        boolean parsed = parser.parse(new JSONObject(TRUE_AS_QUERY_RESULT));

        // Asserts
        assertThat(parsed).isTrue();
    }

    @Test
    public void testFalse () throws JSONException {
        BooleanParser parser = new BooleanParser();

        // Business method
        boolean parsed = parser.parse(new JSONObject(FALSE_AS_QUERY_RESULT));

        // Asserts
        assertThat(parsed).isFalse();
    }

}