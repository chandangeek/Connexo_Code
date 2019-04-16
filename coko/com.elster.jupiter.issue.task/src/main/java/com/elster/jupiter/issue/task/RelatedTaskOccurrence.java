/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task;


import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.tasks.TaskOccurrence;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface RelatedTaskOccurrence {

    TaskOccurrence getTaskOccurrence();

    String getErrorMessage();

    Instant getFailureTime();
}
