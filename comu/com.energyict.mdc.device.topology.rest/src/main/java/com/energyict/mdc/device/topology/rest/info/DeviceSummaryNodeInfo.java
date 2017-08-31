package com.energyict.mdc.device.topology.rest.info;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.GraphLayer;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * NodeInfo for Device where properties are set when layer is added
 * Copyrights EnergyICT
 * Date: 6/01/2017
 * Time: 12:35
 */
@JsonIgnoreType
public class DeviceSummaryNodeInfo extends DeviceNodeInfo{

    private Map<String, Object> allProperties =  new HashMap<>();

    public static DeviceSummaryNodeInfo of(DeviceNodeInfo originalNode) {
       return new DeviceSummaryNodeInfo(originalNode);
    }

    public DeviceSummaryNodeInfo withDevice(Device device){
        setDevice(device);
        return this;
    }

    private DeviceSummaryNodeInfo(DeviceNodeInfo originalNode){
        super(originalNode.getDevice(), Optional.ofNullable(originalNode.getParent()), Optional.ofNullable(originalNode.realPeriod));
        this.allProperties.putAll(originalNode.getProperties());
    }

    @Override
    public boolean addLayer(GraphLayer<Device> graphLayer){
        allProperties.putAll(graphLayer.getProperties(this));
        return true;
    }

    @JsonAnyGetter
    @Override
    public Map<String, Object> getProperties(){
        return allProperties;
    }

}
