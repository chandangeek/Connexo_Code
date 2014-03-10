package com.energyict.mdc.device.config;

/**
 * Models the specifications that will allow a component
 * to calculate the next execution timestamp of a task.
 * <p/>
 * The calucation is based on a {@link TemporalExpression}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @see NextExecutionCalculator
 * @since 2012-04-11 (17:51)
 */
public interface NextExecutionSpecs extends NextExecutionCalculator {

    /**
     * Returns the number that uniquely identifies this NextExecutionSpecs.
     *
     * @return The unique identifier
     */
    public long getId ();

    /**
     * Gets the {@link TemporalExpression} that specifies
     * the recurring time of the execution of a task.
     *
     * @return The recurring time
     */
    public TemporalExpression getTemporalExpression();

    public void setTemporalExpression(TemporalExpression temporalExpression);

    public void save();

    public void delete();

}