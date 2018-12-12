/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface SchedulingService {
    public static final String COMPONENT_NAME="SCH";

    public String FILTER_ITEMIZER_QUEUE_DESTINATION = "ItemizeSchFilterQD";
    public String FILTER_ITEMIZER_QUEUE_SUBSCRIBER = "ItemizeSchFilterQS";
    public String FILTER_ITEMIZER_QUEUE_DISPLAYNAME = "Itemize communication schedule addition/removal to/from device";
    public String COM_SCHEDULER_QUEUE_DESTINATION = "SchCommQD";
    public String COM_SCHEDULER_QUEUE_SUBSCRIBER = "SchCommQS";
    public String COM_SCHEDULER_QUEUE_DISPLAYNAME = "Handle communication schedule addition/removal to/from device";

    public NextExecutionSpecs findNextExecutionSpecs(long id);
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression);

    public List<ComSchedule> getAllSchedules();
    public Finder<ComSchedule> findAllSchedules();
    public Optional<ComSchedule> findSchedule(long id);

    /**
     * Finds the {@link ComSchedule} that is uniquely identified
     * by the specified master resource identifier.
     *
     * @param mRID the unique identifier of the ComSchedule
     * @return the requested Device or null if none was found
     */
    public Optional<ComSchedule> findScheduleBymRID(String mRID);


    public ComScheduleBuilder newComSchedule(String name, TemporalExpression temporalExpression, Instant startDate);

    public Optional<ComSchedule> findAndLockComScheduleByIdAndVersion(long id, long version);
}
