package com.elster.jupiter.system.app.impl;

import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.system.app.SysAppService;
import com.elster.jupiter.users.UserService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Component(
        name = "com.elster.jupiter.system.app",
        service = {SysAppService.class, InstallService.class},
        property = "name=" + SysAppService.COMPONENTNAME,
        immediate = true
)
public class SysAppServiceImpl implements SysAppService, InstallService {

    private final Logger logger = Logger.getLogger(SysAppServiceImpl.class.getName());

    private volatile UserService userService;

    public SysAppServiceImpl() {
    }

    @Inject
    public SysAppServiceImpl(UserService userService) {
        setUserService(userService);
        activate();
    }

    @Activate
    public final void activate() {
    }

    @Override
    public void install() {
        assignPrivilegesToDefaultRoles();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(UserService.COMPONENTNAME, "DES", "LIC", "TME", "BPM", "APR", "LFC");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private void assignPrivilegesToDefaultRoles() {
        List<String> availablePrivileges = getAvailablePrivileges();
        userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, availablePrivileges.toArray(new String[availablePrivileges.size()]));
    }

    @Override
    public List<String> getAvailablePrivileges() {
        List<String> privileges = new ArrayList<String>();
        userService.getResources(SysAppService.APPLICATION_KEY).forEach(e -> e.getPrivileges().forEach(p -> privileges.add(p.getName())));
        return privileges;
    }
}
