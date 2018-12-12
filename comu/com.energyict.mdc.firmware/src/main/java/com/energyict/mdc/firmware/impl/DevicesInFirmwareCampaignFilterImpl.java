/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.BadFilterException;
import com.energyict.mdc.firmware.DevicesInFirmwareCampaignFilter;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.tasks.ComTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link DevicesInFirmwareCampaignFilter}
 */
public class DevicesInFirmwareCampaignFilterImpl implements DevicesInFirmwareCampaignFilter {

    private FirmwareService firmwareService;
    private DeviceService deviceService;

    private Optional<Long> firmwareCampaignId = Optional.empty();
    private Set<Long> deviceIds;
    /**
     * The Set of {@link ComTask} or an empty set
     * if you want all ComTasks that are configured in the system.
     */
    private Set<String> statusKeys;

    public DevicesInFirmwareCampaignFilterImpl(FirmwareService firmwareService, DeviceService deviceService ){
        this.firmwareService = firmwareService;
        this.deviceService = deviceService;
    }

    public DevicesInFirmwareCampaignFilterImpl withFirmwareCampaignId(Long firmwareCampaignId){
        this.firmwareCampaignId = Optional.of(firmwareCampaignId);
        return this;
    }

    public DevicesInFirmwareCampaignFilterImpl withDeviceIds(List<Long> deviceIds){
        this.deviceIds = new HashSet<>();
        this.deviceIds.addAll(deviceIds);
        return this;
    }

    public DevicesInFirmwareCampaignFilterImpl withStatus(List<String> firmwareManagementDeviceStatusKeys){
        this.statusKeys = new HashSet<>();
        this.statusKeys.addAll(firmwareManagementDeviceStatusKeys);
        return this;
    }

    public Condition getCondition(){
        return conditionForCampaign().and(conditionForDevice().and(conditionForStatus()));
    }

    private Condition conditionForCampaign(){
       if (!firmwareCampaignId.isPresent()){
           return Condition.TRUE;
       }
       if (firmwareService == null) {
           throw new IllegalStateException("FirmwareService is not set");
       }
       FirmwareCampaign campaign = firmwareService.getFirmwareCampaignById(firmwareCampaignId.get()).orElseThrow(() -> new BadFilterException(String.format("Campaign %d not found.", firmwareCampaignId.get())));
       return Operator.EQUAL.compare(DeviceInFirmwareCampaignImpl.Fields.CAMPAIGN.fieldName(), campaign);

    }

    private Condition conditionForDevice(){
        if (deviceIds == null || deviceIds.isEmpty()){
            return Condition.TRUE;
        }
        if (deviceService == null){
            throw new IllegalStateException("DeviceService is not set");
        }
        List<Optional<Device>> devices = deviceIds.stream().map(deviceId -> deviceService.findDeviceById(deviceId)).collect(Collectors.toList());
        if (deviceIds.size() == 1){
            Long deviceId =  new ArrayList<>(deviceIds).get(0);
            if (!devices.get(0).isPresent()){
                throw new BadFilterException(String.format("Device %d not found.", deviceId));
            }
            return Operator.EQUAL.compare(DeviceInFirmwareCampaignImpl.Fields.DEVICE.fieldName(), devices.get(0).get());
        }
        return ListOperator.IN.contains(DeviceInFirmwareCampaignImpl.Fields.DEVICE.fieldName(), devices.stream().map(Optional::get).collect(Collectors.toList()));
    }

    private Condition conditionForStatus(){
        List<FirmwareManagementDeviceStatus> statuses = getDeviceInFirmwareCampaignStatusOrdinals();
        if (statuses.isEmpty() || statuses.size() == FirmwareManagementDeviceStatus.values().length){
            return Condition.TRUE;
        }
        if (statuses.size() == 1){
            return Operator.EQUAL.compare(DeviceInFirmwareCampaignImpl.Fields.STATUS.fieldName(), statuses.get(0));
        }
        return ListOperator.IN.contains(DeviceInFirmwareCampaignImpl.Fields.STATUS.fieldName(), statuses);
    }

    private List<FirmwareManagementDeviceStatus> getDeviceInFirmwareCampaignStatusOrdinals() {
        List<FirmwareManagementDeviceStatus> statusOrdinals = new ArrayList<>();
        if (this.statusKeys != null) {
            for (FirmwareManagementDeviceStatus status : FirmwareManagementDeviceStatus.values()) {
                if (this.statusKeys.contains(status.key())) {
                    statusOrdinals.add(status);
                }
            }
        }
        return statusOrdinals;
    }



}