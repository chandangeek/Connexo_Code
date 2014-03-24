package com.energyict.mdc.tasks.task;

/**
 * Describes an action that needs to be performed when communicating with a device.
 * A ProtocolTask can be used by only one ComTask
 *
 * @author gna
 * @since 19/04/12 - 13:49
 */
public interface ProtocolTask {

    /**
     * Returns the {@link ComTask} this ProtocolTask belongs to
     *
     * @return the ComTask of this ProtocolTask
     */
    public ComTask getComTask ();

    public void save();

}