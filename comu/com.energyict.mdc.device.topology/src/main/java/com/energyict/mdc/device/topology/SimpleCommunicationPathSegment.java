package com.energyict.mdc.device.topology;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a {@link CommunicationPathSegment} for the simplest of PLC cases.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (14:31)
 */
@ProviderType
public interface SimpleCommunicationPathSegment extends CommunicationPathSegment {

    public int getHopCount();

}