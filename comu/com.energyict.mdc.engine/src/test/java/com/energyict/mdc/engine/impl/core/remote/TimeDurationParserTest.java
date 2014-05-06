package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.impl.core.remote.TimeDurationParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.remote.TimeDurationParser} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-29 (15:19)
 */
public class TimeDurationParserTest {

    private static final String QUERY_RESULT = "{\"query-id\":\"releaseTimedOutComTasks\",\"single-value\":{\"timeDuration\":{\"seconds\":1800}}}";

    @Test
    public void testParse () throws JSONException {
        // Business method
        TimeDuration timeDuration = new TimeDurationParser().parse(new JSONObject(QUERY_RESULT));

        // Asserts
        Assertions.assertThat(timeDuration.getSeconds()).isEqualTo(1800);
    }

}