package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the event that is produced when a {@link StateChangeBusinessProcess} is executed.
 * @see StateChangeBusinessProcess#executeOnEntry(String, State)
 * @see StateChangeBusinessProcess#executeOnExit(String, State)
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-02 (11:06)
 */
@ProviderType
public interface StateChangeBusinessProcessStartEvent {

    /**
     * The topic on which this event is published.
     */
    public static final String TOPIC = "com/elster/jupiter/fsm/bpm/START";

    public enum Type {
        ENTRY, EXIT;

        /**
         * The value that will be passed to the external business process.
         *
         * @return The value
         */
        public String parameterValue() {
            return this.name().toLowerCase();
        }
    }

    /**
     * The deployment id of the external business process.
     *
     * @return The deployment id
     */
    public String deploymentId();

    /**
     * The process id of the external business process.
     *
     * @return The process id
     */
    public String processId();

    /**
     * The unique identifier of the object for which state is changing.
     *
     * @return The unique identifier
     */
    public String sourceId();

    public Type type();

    /**
     * The related {@link State}.
     * Depending on the type:
     * <ul>
     * <li>ENTRY: the State that is entered</li>
     * <li>EXIT: the State that is exited</li>
     * </ul>
     *
     * @return The related State
     */
    public State state();

}