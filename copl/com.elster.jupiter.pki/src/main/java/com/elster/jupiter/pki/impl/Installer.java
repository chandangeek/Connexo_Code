package com.elster.jupiter.pki.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.importers.csr.CSRImporterQueueInstaller;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

public class Installer implements FullInstaller, PrivilegesProvider {
    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;
    private final PrivilegesProviderV10_4 privilegesProviderV10_4;
    private final CSRImporterQueueInstaller csrImporterQueueInstaller;

    @Inject
    Installer(DataModel dataModel, EventService eventService, UserService userService,
              PrivilegesProviderV10_4 privilegesProviderV10_4, CSRImporterQueueInstaller csrImporterQueueInstaller) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
        this.privilegesProviderV10_4 = privilegesProviderV10_4;
        this.csrImporterQueueInstaller = csrImporterQueueInstaller;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry("Install event types", this::createEventTypes, logger);
        doTry("Install privileges", () -> userService.addModulePrivileges(this), logger);
        doTry("Install privileges for 10.4", privilegesProviderV10_4::install, logger);
        doTry("Create queue for CSR importer", csrImporterQueueInstaller::installIfNotPresent, logger);
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.createIfNotExists(this.eventService);
        }
    }

    @Override
    public String getModuleName() {
        return SecurityManagementServiceImpl.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Arrays.asList(
                userService.createModuleResourceWithPrivileges(
                        SecurityManagementService.COMPONENTNAME,
                        Privileges.RESOURCE_CERTIFICATE.getKey(),
                        Privileges.RESOURCE_CERTIFICATES_DESCRIPTION.getKey(),
                        Arrays.asList(Privileges.Constants.VIEW_CERTIFICATES,
                                Privileges.Constants.ADMINISTRATE_CERTIFICATES,
                                Privileges.Constants.ADMINISTRATE_TRUST_STORES)),
                userService.createModuleResourceWithPrivileges(
                        SecurityManagementService.COMPONENTNAME,
                        Privileges.RESOURCE_SECURITY_ACCESSOR_ATTRIBUTES.getKey(),
                        Privileges.RESOURCE_SECURITY_ACCESSOR_ATTRIBUTES_DESCRIPTION.getKey(),
                        Arrays.stream(SecurityAccessorUserAction.values())
                                .map(SecurityAccessorUserAction::getPrivilege)
                                .collect(toList()))
        );
    }
}
