package com.energyict.mdc.scheduling;

import com.elster.jupiter.time.TemporalExpression;
import java.util.Optional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public interface SchedulingService {
    public static final String COMPONENT_NAME="SCH";

    public NextExecutionSpecs findNextExecutionSpecs(long id);
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression);
    public NextExecutionSpecs previewNextExecutions(TemporalExpression temporalExpression, Date startDate);

    public ListPager<ComSchedule> findAllSchedules(Calendar calendar);
    public List<ComSchedule> findAllSchedules();
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
}
