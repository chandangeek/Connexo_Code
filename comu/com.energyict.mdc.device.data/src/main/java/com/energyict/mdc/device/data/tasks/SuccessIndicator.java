package com.energyict.mdc.device.data.tasks;

/**
 * Indicates the overall success of a {@link ConnectionTask},
 * relating to the overall success of the last communication session.
 */
public enum SuccessIndicator {
    /**
     * Indicates that the last ComSession was successful.
     */
    SUCCESS,

    /**
     * Indicates that the last ComSession was <strong>NOT</strong> successful.
     */
    FAILURE,

    /**
     * Indicates that there is not last ComSession.
     */
    NOT_APPLICABLE;

}