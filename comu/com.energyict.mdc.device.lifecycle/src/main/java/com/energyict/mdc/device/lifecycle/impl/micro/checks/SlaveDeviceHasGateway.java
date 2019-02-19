/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.EvaluableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;
import com.energyict.mdc.device.topology.TopologyService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Checks that a slave device has a gateway
 */
public class SlaveDeviceHasGateway extends TranslatableServerMicroCheck {

    private TopologyService topologyService;

    @Inject
    public final void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Override
    public String getCategory() {
        return MicroCategory.COMMUNICATION.name();
    }

    @Override
    public Optional<EvaluableMicroCheckViolation> evaluate(Device device, Instant effectiveTimestamp) {
        return (isLogicalSlave(device) && !this.hasGateway(device)) ?
                violationFailed(MicroCheckTranslationKeys.MICRO_CHECK_MESSAGE_SLAVE_DEVICE_HAS_GATEWAY) :
                Optional.empty();
    }

    @Override
    public Set<DefaultTransition> getOptionalDefaultTransitions() {
        return EnumSet.of(
                DefaultTransition.COMMISSION,
                DefaultTransition.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING,
                DefaultTransition.INSTALL_INACTIVE_WITHOUT_COMMISSIONING,
                DefaultTransition.INSTALL_AND_ACTIVATE,
                DefaultTransition.INSTALL_INACTIVE,
                DefaultTransition.ACTIVATE);
    }

    private boolean hasGateway(Device device) {
        return this.topologyService.getPhysicalGateway(device).isPresent();
    }

    private boolean isLogicalSlave(Device device) {
        return device.isLogicalSlave() || !device.getDeviceConfiguration().isDirectlyAddressable();
    }
}