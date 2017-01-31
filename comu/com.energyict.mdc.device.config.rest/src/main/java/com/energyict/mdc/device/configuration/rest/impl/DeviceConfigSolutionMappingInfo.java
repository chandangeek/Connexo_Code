/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfigSolutionMappingInfo {

    public long id;
    public DeviceConfigConfigurationInfo fromConfiguration;
    public DeviceConfigConfigurationInfo toConfiguration;
    public List<DeviceConfigConflictConfigurationInfo> connectionMethods = new ArrayList<>();
    public List<DeviceConfigSolutionConfigurationInfo> connectionMethodSolutions = new ArrayList<>();
    public List<DeviceConfigConflictConfigurationInfo> securitySets = new ArrayList<>();
    public List<DeviceConfigSolutionConfigurationInfo> securitySetSolutions = new ArrayList<>();
    public long version;
    public VersionInfo<Long> parent;

    public DeviceConfigSolutionMappingInfo() {
    }

    public DeviceConfigSolutionMappingInfo(DeviceConfigConflictMapping deviceConfigConflictMapping) {
        this.id = deviceConfigConflictMapping.getId();
        this.fromConfiguration = new DeviceConfigConfigurationInfo(deviceConfigConflictMapping.getOriginDeviceConfiguration().getId(),
                deviceConfigConflictMapping.getOriginDeviceConfiguration().getName());
        this.toConfiguration = new DeviceConfigConfigurationInfo(deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId(),
                deviceConfigConflictMapping.getDestinationDeviceConfiguration().getName());
        deviceConfigConflictMapping.getConflictingConnectionMethodSolutions()
                .stream()
                .forEach(conflictingConnectionMethodSolution -> {
                    List<DeviceConfigConfigurationInfo> to = new ArrayList<>();
                    conflictingConnectionMethodSolution.getMappableToDataSources()
                            .stream()
                            .forEach(connectionMethod ->
                                    to.add(new DeviceConfigConfigurationInfo(connectionMethod.getId(),
                                            connectionMethod.getName())));
                    DeviceConfigConfigurationInfo from = new DeviceConfigConfigurationInfo(
                            conflictingConnectionMethodSolution.getOriginDataSource().getId(),
                            conflictingConnectionMethodSolution.getOriginDataSource().getName());
                    connectionMethods.add(new DeviceConfigConflictConfigurationInfo(from, to));
                    switch (conflictingConnectionMethodSolution.getConflictingMappingAction()) {
                        case MAP:
                            connectionMethodSolutions.add(new DeviceConfigSolutionConfigurationInfo(
                                    conflictingConnectionMethodSolution.getConflictingMappingAction().toString(),
                                    from,
                                    new DeviceConfigConfigurationInfo(
                                            conflictingConnectionMethodSolution.getDestinationDataSource().getId(),
                                            conflictingConnectionMethodSolution.getDestinationDataSource().getName()
                                    )
                            ));
                            break;
                        case REMOVE:
                            connectionMethodSolutions.add(new DeviceConfigSolutionConfigurationInfo(
                                    conflictingConnectionMethodSolution.getConflictingMappingAction().toString(), from));
                            break;
                    }
                });
        deviceConfigConflictMapping.getConflictingSecuritySetSolutions()
                .stream()
                .forEach(conflictingSecuritySetSolution -> {
                    List<DeviceConfigConfigurationInfo> to = new ArrayList<>();
                    conflictingSecuritySetSolution.getMappableToDataSources()
                            .stream()
                            .forEach(securitySet ->
                                    to.add(new DeviceConfigConfigurationInfo(securitySet.getId(),
                                            securitySet.getName())));
                    DeviceConfigConfigurationInfo from = new DeviceConfigConfigurationInfo(
                            conflictingSecuritySetSolution.getOriginDataSource().getId(),
                            conflictingSecuritySetSolution.getOriginDataSource().getName());
                    securitySets.add(new DeviceConfigConflictConfigurationInfo(from, to));
                    switch (conflictingSecuritySetSolution.getConflictingMappingAction()) {
                        case MAP:
                            securitySetSolutions.add(new DeviceConfigSolutionConfigurationInfo(
                                    conflictingSecuritySetSolution.getConflictingMappingAction().toString(),
                                    from,
                                    new DeviceConfigConfigurationInfo(
                                            conflictingSecuritySetSolution.getDestinationDataSource().getId(),
                                            conflictingSecuritySetSolution.getDestinationDataSource().getName()
                                    )
                            ));
                            break;
                        case REMOVE:
                            securitySetSolutions.add(new DeviceConfigSolutionConfigurationInfo(
                                    conflictingSecuritySetSolution.getConflictingMappingAction().toString(), from));
                            break;
                    }
                });
        this.version = deviceConfigConflictMapping.getVersion();
        DeviceType deviceType = deviceConfigConflictMapping.getDeviceType();
        this.parent = new VersionInfo<>(deviceType.getId(), deviceType.getVersion());
    }
}