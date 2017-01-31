/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;

import java.util.Set;

@Component(name = "com.energyict.mdc.checklist", service = {UpgradeCheckList.class}, property = {"com.elster.jupiter.checklist=Prepayment"})
public class PrepaymentChecklist implements UpgradeCheckList {

    public static final String APPLICATION_NAME = "Prepayment";

    @Override
    public String application() {
        return APPLICATION_NAME;
    }

    @Override
    public Set<InstallIdentifier> componentsToInstall() {
        return ImmutableSet.<InstallIdentifier>builder()
                .add(
                        InstallIdentifier.identifier(application(), PrepaymentApplication.COMPONENT_NAME)
                ).build();
    }
}