/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.BadFilterException;
import com.energyict.mdc.firmware.DevicesInFirmwareCampaignFilter;
import com.energyict.mdc.firmware.FirmwareCampaignService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link DevicesInFirmwareCampaignFilter}
 */
public class DevicesInFirmwareCampaignFilterImpl implements DevicesInFirmwareCampaignFilter {

    private final FirmwareCampaignService firmwareCampaignService;
    private final DeviceService deviceService;

    private Optional<Long> firmwareCampaignId = Optional.empty();
    private Set<Long> deviceIds;
    /**
     * The Set of {@link ComTask} or an empty set
     * if you want all ComTasks that are configured in the system.
     */
    private Set<String> statusKeys;

    public DevicesInFirmwareCampaignFilterImpl(FirmwareCampaignService firmwareCampaignService, DeviceService deviceService) {
        this.firmwareCampaignService = firmwareCampaignService;
        this.deviceService = deviceService;
    }

    public DevicesInFirmwareCampaignFilterImpl withFirmwareCampaignId(Long firmwareCampaignId) {
        this.firmwareCampaignId = Optional.of(firmwareCampaignId);
        return this;
    }

    public DevicesInFirmwareCampaignFilterImpl withDeviceIds(List<Long> deviceIds) {
        this.deviceIds = new HashSet<>();
        this.deviceIds.addAll(deviceIds);
        return this;
    }

    public DevicesInFirmwareCampaignFilterImpl withStatus(Collection<String> firmwareManagementDeviceStatusKeys) {
        this.statusKeys = new HashSet<>();
        this.statusKeys.addAll(firmwareManagementDeviceStatusKeys);
        return this;
    }

    public Condition getCondition() {
        return conditionForCampaign().and(conditionForDevice().and(conditionForStatus()));
    }

    private Condition conditionForCampaign() {
        if (!firmwareCampaignId.isPresent()) {
            return Condition.TRUE;
        }
        if (firmwareCampaignService == null) {
            throw new IllegalStateException("FirmwareService is not set");
        }
        ServiceCall campaignServiceCall = firmwareCampaignService.getFirmwareCampaignById(firmwareCampaignId.get())
                .orElseThrow(() -> new BadFilterException(String.format("Campaign %d not found.", firmwareCampaignId.get())))
                .getServiceCall();
        return Operator.EQUAL.compare(FirmwareCampaignItemDomainExtension.FieldNames.DOMAIN.javaName() + ".parent", campaignServiceCall);
    }

    private Condition conditionForDevice() {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Condition.TRUE;
        }
        if (deviceService == null) {
            throw new IllegalStateException("DeviceService is not set");
        }
        List<Optional<Device>> devices = deviceIds.stream().map(deviceId -> deviceService.findDeviceById(deviceId)).collect(Collectors.toList());
        if (deviceIds.size() == 1) {
            Long deviceId = new ArrayList<>(deviceIds).get(0);
            if (!devices.get(0).isPresent()) {
                throw new BadFilterException(String.format("Device %d not found.", deviceId));
            }
            return Operator.EQUAL.compare(FirmwareCampaignItemDomainExtension.FieldNames.DEVICE.javaName(), devices.get(0).get());
        }
        return ListOperator.IN.contains(FirmwareCampaignItemDomainExtension.FieldNames.DEVICE.javaName(), devices.stream().map(Optional::get).collect(Collectors.toList()));
    }

    private Condition conditionForStatus() {
        List<DefaultState> statuses = getDeviceInFirmwareCampaignStatusOrdinals();
        if (statuses.isEmpty() || statuses.size() == DefaultState.values().length) {
            return Condition.TRUE;
        }
        if (statuses.size() == 1) {
            return Operator.EQUAL.compare(FirmwareCampaignItemDomainExtension.FieldNames.DOMAIN.javaName() + ".state.name", statuses.get(0));
        }
        return ListOperator.IN.contains(FirmwareCampaignItemDomainExtension.FieldNames.DOMAIN.javaName() + ".state.name", statuses);
    }

    private List<DefaultState> getDeviceInFirmwareCampaignStatusOrdinals() {
        List<DefaultState> statusOrdinals = new ArrayList<>();
        if (this.statusKeys != null) {
            for (DefaultState status : DefaultState.values()) {
                if (this.statusKeys.contains(status.getKey())) {
                    statusOrdinals.add(status);
                }
            }
        }
        return statusOrdinals;
    }
}
