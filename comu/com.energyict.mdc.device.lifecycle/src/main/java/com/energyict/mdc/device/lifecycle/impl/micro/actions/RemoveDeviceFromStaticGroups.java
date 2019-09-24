/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will remove the Device from all static device groups.
 * @see {@link MicroAction#REMOVE_DEVICE_FROM_STATIC_GROUPS}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-07 (12:40)
 */
public class RemoveDeviceFromStaticGroups extends TranslatableServerMicroAction {

    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;

    public RemoveDeviceFromStaticGroups(Thesaurus thesaurus, MeteringService meteringService, MeteringGroupsService meteringGroupsService) {
        super(thesaurus);
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        this.toEndDevice(device).ifPresent(this::execute);
    }

    private void execute(EndDevice endDevice) {
        this.meteringGroupsService
                .findEnumeratedEndDeviceGroupsContaining(endDevice)
                .forEach(group -> this.removeDeviceFromGroup(group, endDevice));
    }

    private Optional<EndDevice> toEndDevice(Device device) {
        Optional<AmrSystem> amrSystem = this.getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            return this.findEndDevice(amrSystem.get(), device);
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<AmrSystem> getMdcAmrSystem() {
        return this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
    }

    private Optional<EndDevice> findEndDevice(AmrSystem amrSystem, Device device) {
        return amrSystem.findMeter(String.valueOf(device.getId())).map(EndDevice.class::cast);
    }

    private void removeDeviceFromGroup(EnumeratedEndDeviceGroup group, EndDevice endDevice) {
        group
            .getEntries()
            .stream()
            .filter(each -> each.getMember().getId() == endDevice.getId())
            .findFirst()
            .ifPresent(group::remove);
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS;
    }
}
