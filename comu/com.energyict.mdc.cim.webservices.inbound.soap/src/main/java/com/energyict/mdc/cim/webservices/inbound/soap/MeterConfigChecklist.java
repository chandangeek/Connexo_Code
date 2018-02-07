/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap;

import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;

import java.util.Set;

@Component(name = "com.energyict.mdc.checklist", service = {UpgradeCheckList.class}, property = {"com.energyict.mdc.checklist=MeterConfig"})
public class MeterConfigChecklist implements UpgradeCheckList {

    public static final String APPLICATION_NAME = "MultiSense";

    @Override
    public String application() {
        return APPLICATION_NAME;
    }

    @Override
    public Set<InstallIdentifier> componentsToInstall() {
        return ImmutableSet.<InstallIdentifier>builder()
                .add(
                        InstallIdentifier.identifier(application(), InboundSoapEndpointsActivator.COMPONENT_NAME)
                ).build();
    }
}