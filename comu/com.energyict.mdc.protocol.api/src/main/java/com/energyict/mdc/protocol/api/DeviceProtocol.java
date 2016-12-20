package com.energyict.mdc.protocol.api;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.pluggable.Pluggable;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.tasks.support.ConnectionTypeSupport;
import com.energyict.mdc.protocol.api.tasks.support.DeviceProtocolDialectSupport;
import com.energyict.mdc.protocol.api.tasks.support.DeviceTopologySupport;

/**
 * Defines an Interface between the Data Collection System and a Device. The interface can both be
 * used at operational time and at configuration time.
 */
@ProviderType
public interface DeviceProtocol extends Pluggable, DeviceProtocolDialectSupport,
        DeviceTopologySupport, DeviceSecuritySupport, ConnectionTypeSupport, com.energyict.mdc.upl.DeviceProtocol {

    /**
     * Initializes the DeviceProtocol.
     * This method is called after the physical connection has been
     * created and before the protocol <i>logOn</i> will occur.
     * <p>
     * Implementers should save the arguments for future use.
     *
     * @param offlineDevice contains the complete definition/configuration of a Device
     * @param comChannel    the used ComChannel where all read/write actions are going to be performed
     */
    void init(final OfflineDevice offlineDevice, ComChannel comChannel);

}