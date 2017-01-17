package com.energyict.mdc.scheduling;

import com.elster.jupiter.time.TemporalExpression;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the specifications that will allow a component
 * to calculate the next execution timestamp of a task.
 * <p/>
 * The calucation is based on a {@link com.elster.jupiter.time.TemporalExpression}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @see NextExecutionCalculator
 * @since 2012-04-11 (17:51)
 */
@ProviderType
public interface NextExecutionSpecs extends NextExecutionCalculator {

    /**
     * Returns the number that uniquely identifies this NextExecutionSpecs.
     *
     * @return The unique identifier
     */
    long getId();

    /**
     * Gets the {@link com.elster.jupiter.time.TemporalExpression} that specifies
     * the recurring time of the execution of a task.
     *
     * @return The recurring time
     */
    TemporalExpression getTemporalExpression();

    void setTemporalExpression(TemporalExpression temporalExpression);

    void update();

    void delete();

}