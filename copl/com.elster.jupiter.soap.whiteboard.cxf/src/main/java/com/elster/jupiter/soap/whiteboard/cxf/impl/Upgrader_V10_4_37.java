/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;


public class Upgrader_V10_4_37 implements Upgrader, PrivilegesProvider {
    public static final String SYS = "SYS";
    private final DataModel dataModel;
    private final UserService userService;
    private final EndPointConfigurationService endPointConfigurationService;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Inject
    Upgrader_V10_4_37(DataModel dataModel,
                      UserService userService,
                      EndPointConfigurationService endPointConfigurationService) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 37));
        userService.addModulePrivileges(this);
        addInvokePrivilegeToExistingEndpointRoles();
    }

    @Override
    public String getModuleName() {
        return WebServicesService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(createInvokeWebServicePrivilegeResource());
        return resources;
    }

    private ResourceDefinition createInvokeWebServicePrivilegeResource() {
        logger.info("Registering new privilege: " + Privileges.Constants.INVOKE_WEB_SERVICES);

        return userService.createModuleResourceWithPrivileges(
                getModuleName(),
                Privileges.RESOURCE_WEB_SERVICES.getKey(),
                Privileges.RESOURCE_WEB_SERVICES_DESCRIPTION.getKey(),
                Collections.singletonList(Privileges.Constants.INVOKE_WEB_SERVICES));
    }


    private void addInvokePrivilegeToExistingEndpointRoles() {
        if (endPointConfigurationService != null) {
            endPointConfigurationService.findEndPointConfigurations()
                    .stream()
                    .filter(EndPointConfiguration::isInbound)
                    .map(epc -> ((InboundEndPointConfiguration) epc))
                    .filter(inboundEndPointConfiguration -> inboundEndPointConfiguration.getGroup().isPresent())
                    .map(inboundEndPointConfiguration -> inboundEndPointConfiguration.getGroup().get())
                    .forEach(this::addInvokeToRole);
        }
    }

    private void addInvokeToRole(Group role) {
        logger.info("Adding privilege " + Privileges.Constants.INVOKE_WEB_SERVICES + " to " + role.getName());
        // TODO add application name, as declared by each endpoint
        role.grant(SYS, Privileges.Constants.INVOKE_WEB_SERVICES);
    }

}

