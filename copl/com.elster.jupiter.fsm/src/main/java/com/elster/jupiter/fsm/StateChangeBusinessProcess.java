package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

/**
 * Wraps an existing external business process that was designed
 * explicitly as a state-change process that will or can be executed
 * when a State is entered or exited.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-29 (13:48)
 */
@ProviderType
public interface StateChangeBusinessProcess {

    /**
     * The name of the parameter of the external business process
     * that is used to pass the unique identifier of the object
     * for which a state change has occurred.
     * Note that the parameter is passed as a String
     * so make sure that your external business process
     * parameter is of type String.
     */
    public static final String SOURCE_ID_BPM_PARAMETER_NAME = "sourceId";

    /**
     * The name of the parameter of the external business process
     * that is used to pass the unique identifier of the {@link State}
     * to which or from which the state change has occurred.
     * Note that the parameter is passed as a Long
     * so make sure that your external business process
     * parameter is of type Long.
     */
    public static final String STATE_ID_BPM_PARAMETER_NAME = "stateId";

    /**
     * The name of the parameter of the external business process
     * that is used to pass the type of {@link State} change that has occurred.
     * Note that the parameter is passed as a String
     * so make sure that your external business process
     * parameter is of type String.
     * The following values can be expected:
     * <ul>
     * <li>entry: denotes that the State is entered, i.e. the object's state is changing to the specified state</li>
     * <li>exit: denotes that the State is exited, i.e. the object's state is changing from the specified state to another</li>
     * </ul>
     */
    public static final String CHANGE_TYPE_BPM_PARAMETER_NAME = "changeType";

    public long getId();

    public String getDeploymentId();

    public String getProcessId();

    /**
     * Executes the related external process in the case
     * the state of the object with the specified identifier is changing
     * from an unspecified {@link State} to the specified State.
     *
     * @param sourceId The unique identifier of the object for which state is changing
     * @param state The {@link State}
     */
    public void executeOnEntry(String sourceId, State state);

    /**
     * Executes the related external process in the case
     * the state of the object with the specified identifier is changing
     * from the specified {@link State} to another unspecified State.
     *
     * @param sourceId The unique identifier of the object for which state is changing
     * @param state The {@link State}
     */
    public void executeOnExit(String sourceId, State state);

}