package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.security.Privileges;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstallerImpl {
    private final Logger logger = Logger.getLogger(InstallerImpl.class.getName());

    private DataModel dataModel;
    private String defaultDomain;

    public InstallerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install(UserService userService, String defaultDomain) {
        try{
		    dataModel.install(true, true);
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }

        this.defaultDomain = defaultDomain;
        createPrivileges(userService);
        createMasterData();
	}
	
	private void createMasterData() {
        try{
            InternalDirectoryImpl directory = createDirectory();

            GroupImpl administrators = createRole(UserService.DEFAULT_ADMIN_ROLE, "Administrative privileges");
            GroupImpl meterExperts = createRole(UserService.DEFAULT_METER_EXPERT_ROLE, "Full meter management privileges");
            GroupImpl meterOperators = createRole(UserService.DEFAULT_METER_OPERATOR_ROLE, "Meter operation privileges");

            grantSystemAdministratorPrivileges(administrators);
            createAdministratorUser(directory, new GroupImpl[] {administrators, meterExperts, meterOperators});
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
	}

    private InternalDirectoryImpl createDirectory() {
        InternalDirectoryImpl directory = InternalDirectoryImpl.from(dataModel, defaultDomain);
        directory.setDefault(true);
        directory.save();
        return directory;
    }

    private GroupImpl createRole(String name, String description){
        GroupImpl group = GroupImpl.from(dataModel, name, description);
        group.save();
        return group;
    }
	
	private void createAdministratorUser(InternalDirectoryImpl directory, GroupImpl[] roles) {
        UserImpl user = directory.newUser("admin", "System administrator", true);

		user.setPassword("admin");
		user.save();
        for(GroupImpl role : roles){
		    user.join(role);
        }
	}

    private void createPrivileges(UserService userService) {
        userService.createResourceWithPrivileges("SYS", "user.users", "user.users.description", new String[] {Privileges.VIEW_USER, Privileges.UPDATE_USER});
        userService.createResourceWithPrivileges("SYS", "group.groups", "group.groups.description", new String[] {Privileges.VIEW_GROUP, Privileges.CREATE_GROUP, Privileges.UPDATE_GROUP, Privileges.DELETE_GROUP});
        userService.createResourceWithPrivileges("SYS", "domain.domains", "domain.domains.description", new String[] {Privileges.VIEW_DOMAIN});
    }

	private void grantSystemAdministratorPrivileges(GroupImpl group){
        Field[] fields = Privileges.class.getFields();
        for (Field each : fields) {
            try {
                group.grant((String) each.get(null));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
