package com.energyict.mdc.upl.properties;


import aQute.bnd.annotation.ProviderType;

import java.util.*;

/**
 * Interface used in UPL to link with MDC.DATA and read & write properties of a device connection task.
 * Used now in Wake-Up mechanisms
 *
 * To avoid fancy errors between bundles low-level information is transported: string value of the custom property set,
 * MRID of the device, communication task id.
 */

@ProviderType
public interface DevicePropertiesDelegate{

    /**
     * Set a ConnectionTask property using a direct transaction.
     *
     * @param propertyName              name of the property, ex "host"
     * @param value                     new value of the property, ex "127.0.0.1"
     * @param customPropertySetClass    class of the property set, as string, ex: com.energyict.protocols.impl.channels.ip.socket.dsmr.OutboundTcpIpWithWakeupConnectionProperties
     * @param deviceMRID                the MRID of the device to set the property (use of MRID is preferred to avoid duplicates)
     * @param connectionTaskId          the ID of the connection task to set the, like in connectionTask.getId();
     * @return
     */
    boolean setConnectionMethodProperty(String propertyName, Object value, String customPropertySetClass, String deviceMRID, Long connectionTaskId);



    /**
     * Gets the current ConnectionTask properties for a specified device and connection task.
     *
     * @param customPropertySetClass   class of the property set, as string, ex: com.energyict.protocols.impl.channels.ip.socket.dsmr.OutboundTcpIpWithWakeupConnectionProperties
     * @param deviceMRID               the MRID of the device to set the property (use of MRID is preferred to avoid duplicates)
     * @param connectionTaskId         the ID of the connection task to set the, like in connectionTask.getId();
     * @return
     */
    Optional<Map<String, Object>> getConnectionMethodProperties(String customPropertySetClass, String deviceMRID, Long connectionTaskId);
}