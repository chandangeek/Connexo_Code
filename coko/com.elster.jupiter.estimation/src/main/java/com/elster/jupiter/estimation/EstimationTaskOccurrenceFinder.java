/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.tasks.TaskOccurrence;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface EstimationTaskOccurrenceFinder {

    EstimationTaskOccurrenceFinder setStart(Integer start);

    EstimationTaskOccurrenceFinder setLimit(Integer limit);

    EstimationTaskOccurrenceFinder withStartDateIn(Range<Instant> interval);

    EstimationTaskOccurrenceFinder withEndDateIn(Range<Instant> interval);

    List<? extends TaskOccurrence> find();
}
