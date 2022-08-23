/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by bvn on 5/3/16.
 */
public class Installer implements FullInstaller, PrivilegesProvider {
    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;
    private final OrmService ormService;

    @Inject
    public Installer(DataModel dataModel, EventService eventService, UserService userService, OrmService ormService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
        this.ormService = ormService;
    }

    private void createEventTypes() {
        Arrays.stream(EventType.values()).forEach(eventType -> eventType.installIfNotPresent(eventService));
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry("Create event types", this::createEventTypes, logger);
        userService.addModulePrivileges(this);
        if (!ormService.isTest()) {
            execute(dataModel,
                    // TODO: support all this in the default upgrader
                    "alter table WS_OCC_RELATED_ATTR drop constraint WS_UQ_KEY_VALUE",
                    "alter table WS_OCC_RELATED_ATTR add constraint WS_UQ_KEY_VALUE unique (ATTR_KEY, ATTR_VALUE) using index compress 1",
                    "create index IX_WS_CALL_ATTR_VALUE on WS_OCC_RELATED_ATTR(upper(ATTR_VALUE))",
                    "create index IX_WS_CALL_START on WS_CALL_OCCURRENCE(STARTTIME desc)",
                    "create index IX_WS_CALL_END on WS_CALL_OCCURRENCE(ENDTIME desc)");
        }
    }

    @Override
    public String getModuleName() {
        return WebServicesService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_WEB_SERVICES.getKey(), Privileges.RESOURCE_WEB_SERVICES_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.VIEW_WEB_SERVICES,
                        Privileges.Constants.INVOKE_WEB_SERVICES,
                        Privileges.Constants.ADMINISTRATE_WEB_SERVICES,
                        Privileges.Constants.VIEW_HISTORY_WEB_SERVICES,
                        Privileges.Constants.RETRY_WEB_SERVICES,
                        Privileges.Constants.CANCEL_WEB_SERVICES)));
        return resources;
    }
}
