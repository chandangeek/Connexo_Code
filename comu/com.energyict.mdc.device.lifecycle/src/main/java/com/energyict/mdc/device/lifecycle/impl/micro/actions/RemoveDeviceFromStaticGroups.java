package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will remove the Device from all static device groups.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#REMOVE_DEVICE_FROM_STATIC_GROUPS}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-07 (12:40)
 */
public class RemoveDeviceFromStaticGroups implements ServerMicroAction {

    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;

    public RemoveDeviceFromStaticGroups(MeteringService meteringService, MeteringGroupsService meteringGroupsService) {
        super();
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
            .filter(each -> each.getEndDevice().getId() == endDevice.getId())
            .findFirst()
            .ifPresent(group::remove);
    }

}