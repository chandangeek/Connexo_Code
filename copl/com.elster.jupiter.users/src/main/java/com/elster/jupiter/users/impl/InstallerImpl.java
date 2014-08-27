package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.security.Resources;
import com.google.common.base.Optional;

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
            GroupImpl administrators = createAdministrators();
            createAdmin(directory, administrators);
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

    private GroupImpl createAdministrators() {
		GroupImpl group = GroupImpl.from(dataModel, "Administrators", "Administrative privileges");
		group.save();
        grantAllPrivileges(group);

		return group;
	}
	
	private void createAdmin(InternalDirectoryImpl directory, GroupImpl administrators) {
        UserImpl user = directory.newUser("admin", "System Administrator", true);

		user.setPassword("admin");
		user.save();
		user.join(administrators);
	}

    private Resource getOrCreateResource(UserService userService, Resources item) {
        Optional<Resource> found = userService.findResource(item.getName());
        if(found.isPresent()){
            return found.get();
        }
        else {
            return userService.createResource(dataModel.getName(), item.getName(), item.getDescription());
        }
    }
	
	private void createPrivileges(UserService userService) {
        for(Resources item : Resources.values()){
            Resource resource = getOrCreateResource(userService, item);

            for(String privilege : item.getPrivileges()){
                try{
                    resource.createPrivilege(privilege);
                }
                catch (Exception e) {
                    this.logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    private void grantAllPrivileges(GroupImpl group){
        for(Resources item : Resources.values()){
            for(String privilege : item.getPrivileges()){
                group.grant(privilege);
            }
        }
    }
}
