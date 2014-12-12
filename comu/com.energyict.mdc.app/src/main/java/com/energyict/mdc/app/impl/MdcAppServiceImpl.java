package com.energyict.mdc.app.impl;

import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Component(
        name = "com.energyict.mdc.app",
        service = {MdcAppService.class, InstallService.class},
        property = "name="+MdcAppService.COMPONENTNAME,
        immediate = true
)
public class MdcAppServiceImpl implements MdcAppService, InstallService {

    private final Logger logger = Logger.getLogger(MdcAppServiceImpl.class.getName());

    private volatile UserService userService;

    public MdcAppServiceImpl(){
    }

    @Inject
    public MdcAppServiceImpl(UserService userService) {
        setUserService(userService);
        activate();
    }

    @Activate
    public final void activate() {
    }

    @Override
    public void install() {
        createDefaultRoles();
        assignPrivilegesToDefaultRoles();
    }


    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(UserService.COMPONENTNAME,"ISU","DTC","DDC","MDC","SCH","VAL");
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private void createDefaultRoles() {
        try {
            userService.createGroup(Roles.METER_EXPERT.value(), Roles.METER_EXPERT.description());
            userService.createGroup(Roles.METER_OPERATOR.value(), Roles.METER_OPERATOR.description());
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
    }

    private void assignPrivilegesToDefaultRoles() {
        List<String> availablePrivileges = getAvailablePrivileges();
        userService.grantGroupWithPrivilege(Roles.METER_EXPERT.value(), availablePrivileges.toArray(new String[availablePrivileges.size()]));
        //TODO: workaround: attached Meter expert to user admin !!! to remove this line when the user can be created/added to system
        userService.getUser(1).ifPresent(u -> u.join(userService.getGroups().stream().filter(e -> e.getName().equals(Roles.METER_EXPERT.value())).findFirst().get()));
    }

    @Override
    public List<String> getAvailablePrivileges() {
        List<String> privileges = new ArrayList<String>();
        userService.getResources(MdcAppService.APPLICATION_KEY).forEach(e -> e.getPrivileges().forEach(p -> privileges.add(p.getName())));
        return privileges;
    }

}
