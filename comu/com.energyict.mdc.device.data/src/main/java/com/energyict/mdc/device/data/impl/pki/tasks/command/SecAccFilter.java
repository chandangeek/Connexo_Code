package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.energyict.mdc.common.device.data.SecurityAccessor;

public class SecAccFilter implements Command {

    @Override
    public void run(SecurityAccessor securityAccessor) throws CommandErrorException {
        if (!securityAccessor.isEditable()) {
            throw new CommandErrorException("Security accessor is not editable (stopping renew action):" + securityAccessor);
        }
    }
}
