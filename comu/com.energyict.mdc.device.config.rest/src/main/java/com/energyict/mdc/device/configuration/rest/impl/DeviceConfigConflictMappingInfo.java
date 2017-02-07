/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceType;

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
    public long version;
    public VersionInfo<Long> parent;

    public DeviceConfigConflictMappingInfo() {
    }

    public DeviceConfigConflictMappingInfo(DeviceConfigConflictMapping deviceConfigConflictMapping, Thesaurus thesaurus) {
        this.id = deviceConfigConflictMapping.getId();
        this.isSolved = deviceConfigConflictMapping.isSolved();
        this.fromConfiguration = deviceConfigConflictMapping.getOriginDeviceConfiguration().getName();
        this.toConfiguration = deviceConfigConflictMapping.getDestinationDeviceConfiguration().getName();
        this.solved = thesaurus.getFormat(this.getTranslationKey(deviceConfigConflictMapping)).format();
        this.version = deviceConfigConflictMapping.getVersion();
        DeviceType deviceType = deviceConfigConflictMapping.getDeviceType();
        this.parent = new VersionInfo<>(deviceType.getId(), deviceType.getVersion());
    }

    private TranslationKeys getTranslationKey(DeviceConfigConflictMapping conflictMapping) {
        if (conflictMapping.isSolved()) {
            return TranslationKeys.HAS_SOLVED;
        } else {
            return TranslationKeys.HAS_UNSOLVED;
        }
    }

    public static List<DeviceConfigConflictMappingInfo> from(List<DeviceConfigConflictMapping> deviceConfigConflictMappings, Thesaurus thesaurus) {
        List<DeviceConfigConflictMappingInfo> deviceConfigConflictMappingInfos = new ArrayList<>(deviceConfigConflictMappings.size());
        for (DeviceConfigConflictMapping deviceConfigConflictMapping : deviceConfigConflictMappings) {
            deviceConfigConflictMappingInfos.add(new DeviceConfigConflictMappingInfo(deviceConfigConflictMapping, thesaurus));
        }
        return deviceConfigConflictMappingInfos;
    }
}