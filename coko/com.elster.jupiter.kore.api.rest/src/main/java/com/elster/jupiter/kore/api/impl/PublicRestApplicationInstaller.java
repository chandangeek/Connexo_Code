package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.kore.api.impl.security.Privileges;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by bvn on 9/8/15.
 */
//@Component(name = "com.elster.jupiter.pulse.api.rest.installer",
//        service = {InstallService.class, PrivilegesProvider.class},
//        property = "name=" + PublicRestApplicationInstaller.COMPONENT_NAME,
//        immediate = true)
public class PublicRestApplicationInstaller implements InstallService, PrivilegesProvider {

    static final String COMPONENT_NAME = "PRI";

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private volatile UserService userService;

    public PublicRestApplicationInstaller() {
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void install() {
        createDefaultRoles();
        assignPrivilegesToDefaultRoles();
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

    public void createDefaultRoles() {
        try {
            userService.createGroup(Roles.DEVELOPER.value(), Roles.DEVELOPER.description());
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    private void assignPrivilegesToDefaultRoles() {
        userService.grantGroupWithPrivilege(Roles.DEVELOPER.value(), PublicRestApplication.APP_KEY, new String[]{Privileges.Constants.PUBLIC_REST_API});
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(NlsService.COMPONENTNAME, UserService.COMPONENTNAME);
    }

    enum Roles {
        DEVELOPER("Developer", "Grants access to platform public rest api");

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
