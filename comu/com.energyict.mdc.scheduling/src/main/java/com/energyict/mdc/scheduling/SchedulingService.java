package com.energyict.mdc.scheduling;

import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.scheduling.model.ComSchedule;

public interface SchedulingService {
    public static final String COMPONENT_NAME="SCH";

    public NextExecutionSpecs findNextExecutionSpecs(long id);
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression);

    public Finder<ComSchedule> findAllSchedules();
    public ComSchedule findSchedule(long id);

    ComSchedule newComSchedule(String name, TemporalExpression temporalExpression);
}
