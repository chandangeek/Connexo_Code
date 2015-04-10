package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made to execute
 * an {@link com.energyict.mdc.device.lifecycle.config.AuthorizedAction}
 * against a {@link com.energyict.mdc.device.data.Device}
 * that is actually not part of the Device's
 * {@link com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle life cycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (16:38)
 */
public class ActionNotPartOfDeviceLifeCycleException extends DeviceLifeCycleActionViolationException {

    private final MessageSeed messageSeed;
    private final Thesaurus thesaurus;
    private final Object[] messageParameters;

    public ActionNotPartOfDeviceLifeCycleException(AuthorizedTransitionAction action, Device device, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(messageSeed.getKey());
        this.messageSeed = messageSeed;
        this.thesaurus = thesaurus;
        this.messageParameters = new Object[2];
        this.messageParameters[0] = action.getStateTransition().getEventType().getSymbol();
        this.messageParameters[1] = device.getId();
    }

    public ActionNotPartOfDeviceLifeCycleException(AuthorizedBusinessProcessAction action, Device device, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(messageSeed.getKey());
        this.messageSeed = messageSeed;
        this.thesaurus = thesaurus;
        this.messageParameters = new Object[3];
        this.messageParameters[0] = action.getDeploymentId();
        this.messageParameters[1] = action.getProcessId();
        this.messageParameters[2] = device.getId();
    }

    @Override
    public String getLocalizedMessage() {
        return getFormat().format(this.messageParameters);
    }

    private NlsMessageFormat getFormat() {
        return this.thesaurus.getFormat(this.messageSeed);
    }

}