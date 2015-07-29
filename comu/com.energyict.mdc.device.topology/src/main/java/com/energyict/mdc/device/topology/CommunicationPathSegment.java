package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.orm.associations.Effectivity;

/**
 * Models one segment of a {@link CommunicationPath}
 * and cannot be broken down any further.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (14:29)
 */
@ProviderType
public interface CommunicationPathSegment extends Effectivity {

    /**
     * Gets the source of the CommunicationPath.
     * The source is the Device that initiated the communication
     * that was established via this CommunicationPath.
     *
     * @return The source Device
     */
    public Device getSource();

    /**
     * Gets the target of the CommunicationPath.
     * The target is the final destination of the communication,
     * i.e. the Device that was intented to receive the message
     * from the source Device.
     *
     * @return The target Device
     * @see #getSource()
     */
    public Device getTarget();

}