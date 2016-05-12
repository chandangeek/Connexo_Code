package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
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
import java.util.logging.Logger;

/**
 * Created by bvn on 9/8/15.
 */
@Component(name = "com.energyict.mdc.api.rest.installer",
        service = {PrivilegesProvider.class},
        property = "name=" + PublicRestApplicationInstaller.COMPONENT_NAME,
        immediate = true)
public class PublicRestApplicationInstaller implements PrivilegesProvider {

    static final String COMPONENT_NAME = "MRI";

    private final Logger logger = Logger.getLogger(this.getClass().getName());

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
        upgradeService.register(InstallIdentifier.identifier(PublicRestApplicationInstaller.COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    static class Installer implements FullInstaller {
        private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());
        private final UserService userService;

        @Inject
        Installer(UserService userService) {
            this.userService = userService;
        }

        @Override
        public void install(DataModelUpgrader dataModelUpgrader) {
            createDefaultRoles();
            assignPrivilegesToDefaultRoles();
        }

        public void createDefaultRoles() {
            try {
                userService.createGroup(Roles.DEVELOPER.value(), Roles.DEVELOPER.description());
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
            }
        }

        private void assignPrivilegesToDefaultRoles() {
            userService.grantGroupWithPrivilege(Roles.DEVELOPER.value(), PublicRestApplication.APP_KEY, new String[] {Privileges.Constants.PUBLIC_REST_API});
            //TODO: workaround: attached Meter expert to user admin !!! to remove this line when the user can be created/added to system
            userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(Roles.DEVELOPER.value())).findFirst().get()));
        }

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

    enum Roles {
        DEVELOPER("Developer", "Grants access to public rest api");

        private String role;
        private String description;

        Roles(String r, String d) {
            role = r;
            description = d;
        }

        public String value() {
            return role;
        }

        public String description() {
            return description;
        }

    }

}
