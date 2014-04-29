package com.energyict.mdc.scheduling;

import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.scheduling.model.ComSchedule;
import java.util.Calendar;

public interface SchedulingService {
    public static final String COMPONENT_NAME="SCH";

    public NextExecutionSpecs findNextExecutionSpecs(long id);
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression);

    public ListPager<ComSchedule> findAllSchedules(Calendar calendar);
    public ComSchedule findSchedule(long id);

    public ComSchedule newComSchedule(String name, TemporalExpression temporalExpression);
}
