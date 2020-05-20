package com.energyict.mdc.device.data.impl.pki.tasks.command;


import com.energyict.mdc.common.device.data.SecurityAccessor;

public interface Command {

    /**
     * performs a specific renew action (filter, trigger actions ...)
     *
     * @param securityAccessor
     * @return true if following commands should be stopped. To be used by command executor
     */
    void run(SecurityAccessor securityAccessor) throws CommandErrorException, CommandAbortException;

}
