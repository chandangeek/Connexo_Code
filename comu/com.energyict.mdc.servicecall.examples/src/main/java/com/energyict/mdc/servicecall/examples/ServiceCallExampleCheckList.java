/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.servicecall.examples;

import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;

import java.util.Set;

/**
 * Provides an implementation for the {@link UpgradeCheckList} interface
 * that lists all expected CustomPropetySets that are provided by this examples bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-02 (13:26)
 */
@Component(
        name = "com.energyict.mdc.servicecall.example.checklist",
        service = {UpgradeCheckList.class, ServiceCallExampleCheckList.class},
        property = {"com.energyict.mdc.servicecall.checklist=mdc.servicecall.cps"},
        immediate = true)
public class ServiceCallExampleCheckList implements UpgradeCheckList {

    static final String APPLICATION_NAME = "ServiceCallExamples";

    @Override
    public String application() {
        return APPLICATION_NAME;
    }

    @Override
    public Set<InstallIdentifier> componentsToInstall() {
        return ImmutableSet.of(
                InstallIdentifier.identifier(application(), DeviceGroupCertificationCustomPropertySet.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), DisconnectRequestCustomPropertySet.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), BillingCycleCustomPropertySet.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), DeviceCertificationCustomPropertySet.COMPONENT_NAME));
    }

}