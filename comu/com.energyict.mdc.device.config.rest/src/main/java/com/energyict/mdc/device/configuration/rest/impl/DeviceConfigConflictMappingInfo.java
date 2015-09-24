package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfigConflictMappingInfo {

    public long id;
    public boolean isSolved;
    public String fromConfiguration;
    public String toConfiguration;
    public String solved;

    public DeviceConfigConflictMappingInfo() {
    }

    public DeviceConfigConflictMappingInfo(DeviceConfigConflictMapping deviceConfigConflictMapping, Thesaurus thesaurus) {
        this.id = deviceConfigConflictMapping.getId();
        this.isSolved = deviceConfigConflictMapping.isSolved();
        this.fromConfiguration = deviceConfigConflictMapping.getOriginDeviceConfiguration().getName();
        this.toConfiguration = deviceConfigConflictMapping.getDestinationDeviceConfiguration().getName();
        this.solved = deviceConfigConflictMapping.isSolved() ?
                thesaurus.getString(MessageSeeds.HAS_SOLVED.getKey(), "Solved") :
                thesaurus.getString(MessageSeeds.HAS_UNSOLVED.getKey(), "Unsolved");
    }

    public static List<DeviceConfigConflictMappingInfo> from(List<DeviceConfigConflictMapping> deviceConfigConflictMappings, Thesaurus thesaurus) {
        List<DeviceConfigConflictMappingInfo> deviceConfigConflictMappingInfos = new ArrayList<>(deviceConfigConflictMappings.size());
        for (DeviceConfigConflictMapping deviceConfigConflictMapping : deviceConfigConflictMappings) {
            deviceConfigConflictMappingInfos.add(new DeviceConfigConflictMappingInfo(deviceConfigConflictMapping, thesaurus));
        }
        return deviceConfigConflictMappingInfos;
    }
}