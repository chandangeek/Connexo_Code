package com.energyict.mdc.scheduling;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface SchedulingService {
    public static final String COMPONENT_NAME="SCH";

    public NextExecutionSpecs findNextExecutionSpecs(long id);
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression);

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
