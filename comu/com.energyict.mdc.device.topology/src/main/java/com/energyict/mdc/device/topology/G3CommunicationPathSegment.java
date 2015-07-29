package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;
import java.util.Optional;

/**
 * Models a {@link CommunicationPathSegment} that is specific to the G3 case.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (14:31)
 */
@ProviderType
public interface G3CommunicationPathSegment extends CommunicationPathSegment {

    public Duration getTimeToLive();

    public int getCost();

    public Optional<Device> getNextHopDevice();

}