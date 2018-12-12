/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests.assertions;

import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import org.assertj.core.api.Assertions;

public class JupiterAssertions extends Assertions {

    public static LogRecorderAssert assertThat(LogRecorder logRecorder) {
        return new LogRecorderAssert(logRecorder);
    }
}
