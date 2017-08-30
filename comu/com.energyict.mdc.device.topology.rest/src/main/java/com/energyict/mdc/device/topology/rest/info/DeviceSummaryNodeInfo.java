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

    public DeviceSummaryNodeInfo(Device device) {
        super(device, Optional.empty(), Optional.empty());
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
