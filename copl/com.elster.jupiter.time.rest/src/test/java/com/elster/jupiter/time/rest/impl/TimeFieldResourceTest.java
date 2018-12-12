/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest.impl;

import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeFieldResourceTest extends TimeApplicationJerseyTest {
    @Test
    public void testGetTimeUnits() {
        String response = target("/field/timeUnit").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<List>get("$.timeUnits")).hasSize(8);
        assertThat(model.<List>get("$.timeUnits[*].code")).containsExactly(14, 13, 12, 11, 5, 3, 2, 1);
        assertThat(model.<List>get("$.timeUnits[*].timeUnit")).containsExactly(
                "milliseconds",
                "seconds",
                "minutes",
                "hours",
                "days",
                "weeks",
                "months",
                "years");
    }
}
