package com.energyict.mdc.device.topology.rest.info;

import com.energyict.mdc.device.data.Device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Optional;

/**
 * NodeInfo for Device
 * Copyrights EnergyICT
 * Date: 6/01/2017
 * Time: 12:35
 */
@JsonIgnoreType
@JsonPropertyOrder({ "id", "gateway"})
public class DeviceNodeInfo extends NodeInfo<Device>{

    public DeviceNodeInfo(Device device, Optional<Device> parent) {
        super(device);
        if (parent.isPresent()) {
            setParent(parent.get());
        }
    }

    @Override
    @JsonIgnore
    public Class getObjectClass() {
        return Device.class;
    }
    @JsonIgnore
    public Device getDevice(){
        return super.getNodeObject();
    }

    @Override
    public boolean equals(Object another){
        return (another != null && another instanceof DeviceNodeInfo)&& (getDevice().getId() == ((DeviceNodeInfo)another).getDevice().getId());
    }

    @Override
    public int hashCode() {
        return (int) (this.getDevice().getId() ^ (this.getDevice().getId() >>> 32));
    }

}
