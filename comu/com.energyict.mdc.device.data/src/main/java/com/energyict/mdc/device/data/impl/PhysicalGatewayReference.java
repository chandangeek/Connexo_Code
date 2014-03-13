package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;

/**
 * Defines the link object between a {@link com.energyict.mdc.device.data.Device}
 * and his <i>physical</i> gateway {@link com.energyict.mdc.device.data.Device}.
 * (The physical gateway is the device which serves as a physical <i>master</i> device)
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 09:40
 */
public interface PhysicalGatewayReference extends GatewayReference {

    /**
     * @return the current physical gateway device
     */
    public Device getPhysicalGateway();

}
