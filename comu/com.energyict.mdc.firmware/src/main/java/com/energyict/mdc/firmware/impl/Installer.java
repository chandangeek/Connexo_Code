/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.security.Privileges;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Installer implements FullInstaller, PrivilegesProvider {
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private final UserService userService;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final FirmwareCampaignServiceCallLifeCycleInstaller firmwareCampaignServiceCallLifeCycleInstaller;

    @Inject
    Installer(DataModel dataModel, EventService eventService, MessageService messageService, UserService userService,
              ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
              FirmwareCampaignServiceCallLifeCycleInstaller firmwareCampaignServiceCallLifeCycleInstaller) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
        this.userService = userService;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.firmwareCampaignServiceCallLifeCycleInstaller = firmwareCampaignServiceCallLifeCycleInstaller;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        execute(dataModel, "ALTER TABLE FWC_CAMPAIGN_PROPS MODIFY CPS_ID NUMBER NOT NULL");
        doTry(
                "Create event types for FRM",
                this::createEventTypesIfNotExist,
                logger
        );
        doTry(
                "Create service call types",
                firmwareCampaignServiceCallLifeCycleInstaller::createServiceCallTypes,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return FirmwareService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(FirmwareService.COMPONENTNAME, Privileges.RESOURCE_FIRMWARE_CAMPAIGNS.getKey(), Privileges.RESOURCE_FIRMWARE_CAMPAIGNS_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_FIRMWARE_CAMPAIGN, Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN)));
        return resources;
    }

    private void createEventTypesIfNotExist() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.createIfNotExists(this.eventService);
            } catch (Exception e) {
                this.logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
