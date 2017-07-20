package com.energyict.mdc.device.topology.rest.info;

import com.energyict.mdc.device.data.Device;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public DeviceNodeInfo(Device device) {
        super(device);
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

    /**
     * Returns the childNode holding the ginven device
     */
    public Optional<DeviceNodeInfo> findChildNode(Device device){
        return getChildren().stream().map(DeviceNodeInfo.class::cast).filter((dn) -> dn.getDevice().getId() == device.getId()).findFirst();
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
