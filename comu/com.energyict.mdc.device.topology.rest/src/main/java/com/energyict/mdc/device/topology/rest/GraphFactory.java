package com.energyict.mdc.device.topology.rest;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.info.GraphInfo;

import aQute.bnd.annotation.ProviderType;

/**
 * Copyrights EnergyICT
 * Date: 20/12/2016
 * Time: 17:09
 */
@ProviderType
public interface GraphFactory {

    GraphInfo from(Device device);

}
