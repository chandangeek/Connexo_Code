package com.energyict.mdc.scheduling;

public interface SchedulingService {
    public static final String COMPONENT_NAME="SCH";

    public NextExecutionSpecs findNextExecutionSpecs(long id);
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression);
}
