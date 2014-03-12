package com.energyict.mdc.device.config;

import com.energyict.mdc.common.IdBusinessObject;

/**
 * Describes an action that needs to be performed when communicating with a device.
 * A ProtocolTask can be used by only one {@link ComTask}
 *
 * @author gna
 * @since 19/04/12 - 13:49
 */
public interface ProtocolTask extends IdBusinessObject {

    /**
     * Returns the {@link ComTask} this ProtocolTask belongs to
     *
     * @return the ComTask of this ProtocolTask
     */
    public ComTask getComTask ();


}