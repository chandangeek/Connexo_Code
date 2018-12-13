/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.rest.api.util.Roles;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.multisense.api.security.Privileges;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by bvn on 9/8/15.
 */
@Component(name = "com.energyict.mdc.api.rest.installer",
        service = {PublicRestApplicationInstaller.class},
        property = "name=" + PublicRestApplicationInstaller.COMPONENT_NAME,
        immediate = true)
public class PublicRestApplicationInstaller {

    static final String COMPONENT_NAME = "MRI";

    private volatile UserService userService;
    private volatile UpgradeService upgradeService;

    public PublicRestApplicationInstaller() {
    }

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("MultiSense", PublicRestApplicationInstaller.COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    static class Installer implements FullInstaller, PrivilegesProvider {
        private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());
        private final UserService userService;

        @Inject
        Installer(UserService userService) {
            this.userService = userService;
        }

        @Override
        public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
            doTry(
                    "Create default role: Developer",
                    this::createDeveloperRole,
                    logger
            );
            doTry(
                    "Assign privileges to Developer role",
                    this::assignPrivilegesToDeveloperRole,
                    logger
            );
            userService.addModulePrivileges(this);
        }

        private void createDeveloperRole() {
            Optional<Group> developer = userService.findGroup(Roles.DEVELOPER.value());
            if (!developer.isPresent()) {
                userService.createGroup(Roles.DEVELOPER.value(), Roles.DEVELOPER.description());
            }
        }

        private void assignPrivilegesToDeveloperRole() {
            userService.grantGroupWithPrivilege(Roles.DEVELOPER.value(), PublicRestApplication.APP_KEY, new String[]{Privileges.Constants.PUBLIC_REST_API});
        }

        @Override
        public String getModuleName() {
            return PublicRestApplication.COMPONENT_NAME;
        }

        @Override
        public List<ResourceDefinition> getModuleResources() {
            return Collections.singletonList(userService.createModuleResourceWithPrivileges(getModuleName(),
                    Privileges.RESOURCE_PUBLIC_API.getKey(), Privileges.RESOURCE_PUBLIC_API_DESCRIPTION.getKey(),
                    Collections.singletonList(Privileges.Constants.PUBLIC_REST_API)));
        }
    }
}
