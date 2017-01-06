package com.energyict.mdc.device.topology.rest.info;

import com.energyict.mdc.device.data.Device;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * NodeInfo for Device
 * Copyrights EnergyICT
 * Date: 6/01/2017
 * Time: 12:35
 */
@JsonPropertyOrder({ "id", "gateway" })
public class DeviceNodeInfo extends NodeInfo<Device>{

    public DeviceNodeInfo(Device device) {
        super(device);
    }

    @Override
    public Class getObjectClass() {
        return Device.class;
    }
    @JsonIgnore
    public Device getDevice(){
        return super.getNodeObject();
    }

    @JsonGetter
    public boolean isGateway() {
        return this.getParent() == null;
    }
}
