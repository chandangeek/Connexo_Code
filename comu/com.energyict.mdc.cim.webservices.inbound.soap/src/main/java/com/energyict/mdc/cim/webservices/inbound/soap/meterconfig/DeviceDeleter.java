/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.topology.TopologyService;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class DeviceDeleter {
    private final TopologyService topologyService;
    private final MeterConfigFaultMessageFactory faultMessageFactory;

    @Inject
    public DeviceDeleter(TopologyService topologyService,
                         MeterConfigFaultMessageFactory faultMessageFactory) {
        this.topologyService = topologyService;
        this.faultMessageFactory = faultMessageFactory;
    }

    public void delete(Device device) throws FaultMessage {
        checkUsagePointGapsAllowed(device);
        checkMaster(device);
        unlinkSlaveIfExists(device);
        device.delete();
    }

    private void checkUsagePointGapsAllowed(Device device) throws FaultMessage {
        Optional<UsagePoint> usagePoint = device.getUsagePoint();
        if (usagePoint.isPresent()) {
            Optional<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfigurationOnUsagePoint = usagePoint.get().getCurrentEffectiveMetrologyConfiguration();
            if (metrologyConfigurationOnUsagePoint.isPresent()) {
                if (!metrologyConfigurationOnUsagePoint.get().getMetrologyConfiguration().areGapsAllowed()) {
                    throw faultMessageFactory.meterConfigFaultMessageSupplier(device.getName(), MessageSeeds.METROLOGY_CONFIG_NOT_ALLOW_GAPS, device.getName(), device.getSerialNumber(), usagePoint.get().getName()).get();
                }
            }
        }
    }

    private void checkMaster(Device device) throws FaultMessage {
        List<Device> slaves = topologyService.getSlaveDevices(device);
        if (!slaves.isEmpty()) {
            throw faultMessageFactory.meterConfigFaultMessageSupplier(device.getName(), MessageSeeds.CANT_REMOVE_GATEWAY, device.getName(), device.getSerialNumber()).get();
        }
    }

    private void unlinkSlaveIfExists(Device device) {
        topologyService.clearPhysicalGateway(device);
    }
}
