/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.time.TimeDuration;

import com.energyict.mdc.engine.impl.web.queryapi.QueryResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.remote.TimeDurationParser} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-29 (15:19)
 */
public class TimeDurationParserTest {




    private static final String QUERY_RESULT = "{\"query-id\":\"releaseTimedOutComTasks\",\"single-value\":{\"timeDuration\":{\"seconds\":1800}}}";

    @Test
    public void testParse () throws JSONException, IOException {
        TimeDuration testTimeDuration = new TimeDuration(1800, Calendar.SECOND);
        TimeDurationXmlWrapper xmlWrapper = new TimeDurationXmlWrapper(testTimeDuration);
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        mapper.writeValue(writer, QueryResult.forResult("test", xmlWrapper));
        // Business method
        TimeDuration timeDuration = new TimeDurationParser().parse(new JSONObject(writer.toString()));;
        // Asserts
        assertThat(timeDuration.getSeconds()).isEqualTo(1800);
    }

}