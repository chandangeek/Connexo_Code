package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;

/**
 * Defines the link object between a {@link com.energyict.mdc.device.data.Device}
 * and his <i>communication</i> gateway {@link com.energyict.mdc.device.data.Device}.
 * (The communication gateway is the device which serves as an <i>entrypoint</i>
 * of communication for the <i>underlying topology</i>)
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/03/14
 * Time: 09:44
 */
public interface CommunicationGatewayReference extends GatewayReference {

    /**
     * @return the current communication gateway device
     */
    public Device getCommunicationGateway();

}
