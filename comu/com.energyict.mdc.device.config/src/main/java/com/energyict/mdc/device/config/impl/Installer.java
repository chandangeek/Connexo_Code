/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.DuplicateSubscriberNameException;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.device.config.DeviceConfigConstants;
import com.energyict.mdc.common.device.config.DeviceMessageUserAction;
import com.energyict.mdc.common.device.config.EventType;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.security.Privileges;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;
import static java.util.stream.Collectors.toList;

/**
 * Represents the Installer for the DeviceConfiguration module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer implements FullInstaller, PrivilegesProvider {

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;
    private final MessageService messageService;

    @Inject
    public Installer(DataModel dataModel, EventService eventService, UserService userService, MessageService messageService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        createEventTypes();
        userService.addModulePrivileges(this);

        DestinationSpec destinationSpec = messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get();
        try {
            logger.info("Adding subscriber " + TranslationKeys.DEVICE_TYPES_CHANGES_EVENT_SUBSC.getKey());
            destinationSpec.subscribe(
                    TranslationKeys.DEVICE_TYPES_CHANGES_EVENT_SUBSC,
                    DeviceConfigurationService.COMPONENTNAME,
                    Layer.DOMAIN,
                    whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/CREATED")
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/DELETED"))
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/config/devicetype/dlc/UPDATED"))
                            .or(whereCorrelationId().isEqualTo("com/energyict/mdc/device/lifecycle/config/dlc/update"))
                            .or(whereCorrelationId().isEqualTo("com/elster/jupiter/fsm/UPDATED")));
        } catch (DuplicateSubscriberNameException e) {
            // subscriber already exists, ignoring
        }
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(this.eventService);
        }
    }

    @Override
    public String getModuleName() {
        return DeviceConfigurationService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Arrays.asList(
                userService.createModuleResourceWithPrivileges(
                        DeviceConfigurationService.COMPONENTNAME,
                        Privileges.RESOURCE_MASTER_DATA.getKey(),
                        Privileges.RESOURCE_MASTER_DATA_DESCRIPTION.getKey(),
                        Arrays.asList(DeviceConfigConstants.ADMINISTRATE_MASTER_DATA,
                                DeviceConfigConstants.VIEW_MASTER_DATA)),
                userService.createModuleResourceWithPrivileges(
                        DeviceConfigurationService.COMPONENTNAME,
                        Privileges.RESOURCE_DEVICE_TYPES.getKey(),
                        Privileges.RESOURCE_DEVICE_TYPES_DESCRIPTION.getKey(),
                        Arrays.asList(DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE,
                                DeviceConfigConstants.VIEW_DEVICE_TYPE)),
                userService.createModuleResourceWithPrivileges(
                        DeviceConfigurationService.COMPONENTNAME,
                        Privileges.RESOURCE_DEVICE_COMMANDS.getKey(),
                        Privileges.RESOURCE_DEVICE_COMMANDS_DESCRIPTION.getKey(),
                        Arrays.stream(DeviceMessageUserAction.values())
                                .map(DeviceMessageUserAction::getPrivilege)
                                .collect(toList()))
        );
    }
}
