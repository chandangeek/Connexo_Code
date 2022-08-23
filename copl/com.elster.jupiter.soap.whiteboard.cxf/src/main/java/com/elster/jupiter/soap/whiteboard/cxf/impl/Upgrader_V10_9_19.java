/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class Upgrader_V10_9_19 implements Upgrader, PrivilegesProvider {
    private final DataModel dataModel;
    private final UserService userService;
    private final EndPointConfigurationService endPointConfigurationService;
    private final WebServicesService webServicesService;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Inject
    Upgrader_V10_9_19(DataModel dataModel,
                      UserService userService,
                      EndPointConfigurationService endPointConfigurationService,
                      WebServicesService webServicesService) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        execute(dataModel, "update " + TableSpecs.WS_CALL_OCCURRENCE.name()
                + " set APPLICATIONNAME = '" + ApplicationSpecific.WebServiceApplicationName.SYSTEM.getName()
                + "' where APPLICATIONNAME = 'SCIM'"); // update all entries with a changed application name
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 9, 19));
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
                    .map(InboundEndPointConfiguration.class::cast)
                    .forEach(this::addInvokeToRole);
        }
    }

    private void addInvokeToRole(InboundEndPointConfiguration endPointConfiguration) {
        endPointConfiguration.getGroup().ifPresent(role -> {
            logger.info("Adding privilege " + Privileges.Constants.INVOKE_WEB_SERVICES + " to " + role.getName());
            String appName = webServicesService.getWebService(endPointConfiguration.getWebServiceName())
                    .map(WebService::getApplicationName)
                    .orElse(ApplicationSpecific.WebServiceApplicationName.UNDEFINED.getName());
            ApplicationSpecific.WebServiceApplicationName.fromName(appName).getApplicationCodes()
                    .forEach(app -> role.grant(app, Privileges.Constants.INVOKE_WEB_SERVICES));
        });
    }
}
