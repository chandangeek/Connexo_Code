package com.energyict.mdc.scheduling;

import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.scheduling.model.ComSchedule;
import java.util.Calendar;
import java.util.List;

public interface SchedulingService {
    public static final String COMPONENT_NAME="SCH";

    public NextExecutionSpecs findNextExecutionSpecs(long id);
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression);

    public ListPager<ComSchedule> findAllSchedules(Calendar calendar);
    public List<ComSchedule> findAllSchedules();
    public ComSchedule findSchedule(long id);

    public ComSchedule newComSchedule(String name, TemporalExpression temporalExpression, UtcInstant startDate);
}
