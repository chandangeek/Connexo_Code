package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made to execute
 * an {@link com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction}
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
    private final String actionName;
    private final long deviceId;

    public ActionNotPartOfDeviceLifeCycleException(AuthorizedTransitionAction action, Device device, Thesaurus thesaurus, MessageSeed messageSeed) {
        super();
        this.messageSeed = messageSeed;
        this.thesaurus = thesaurus;
        this.deviceId = device.getId();
        this.actionName = action.getStateTransition().getEventType().getSymbol();
    }

    @Override
    public String getLocalizedMessage() {
        return getFormat().format(this.actionName, this.deviceId);
    }

    private final NlsMessageFormat getFormat() {
        return this.thesaurus.getFormat(this.messageSeed);
    }

}