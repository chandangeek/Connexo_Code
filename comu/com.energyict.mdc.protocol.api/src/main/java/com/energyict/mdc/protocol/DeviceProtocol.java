package com.energyict.mdc.protocol;

import com.energyict.mdc.protocol.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.tasks.support.ConnectionTypeSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceAccessSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceBasicSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceCachingSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceClockSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceLogBookSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceProtocolDialectSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceSecuritySupport;
import com.energyict.mdc.protocol.tasks.support.DeviceStatusInformationSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceTopologySupport;

import java.util.List;

/**
 * Defines an Interface between the Data Collection System and a Device. The interface can both be
 * used at operational time and at configuration time.
 */
public interface DeviceProtocol extends Pluggable, DeviceAccessSupport, DeviceClockSupport, DeviceProtocolDialectSupport,
        DeviceBasicSupport, DeviceLoadProfileSupport, DeviceLogBookSupport, DeviceRegisterSupport, DeviceStatusInformationSupport,
        DeviceTopologySupport, DeviceMessageSupport, DeviceCachingSupport, DeviceSecuritySupport, ConnectionTypeSupport {

    /**
     * Initializes the DeviceProtocol. This method is called after the physical connection has been
     * created and before the protocol <i>logOn</i> will occur.
     * <p/>
     * Implementers should save the arguments for future use.
     *
     * @param offlineDevice contains the complete definition/configuration of a Device
     * @param comChannel    the used ComChannel where all read/write actions are going to be performed
     */
    public void init(final OfflineDevice offlineDevice, ComChannel comChannel);

    /**
     * This method is called by the collection software before the physical disconnect,
     * and after the protocol <i>logOff</i>. This can be used to free resources that
     * cannot be freed in the disconnect() method.
     */
    public void terminate();

    /**
     * Provide the {@link DeviceProtocolCapabilities}
     *
     * @return the possible Capabilities
     */
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities();

    public String getProtocolDescription();

}