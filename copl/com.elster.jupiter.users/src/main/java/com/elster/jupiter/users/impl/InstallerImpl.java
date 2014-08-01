package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.security.Resources;

import java.util.Map;

public class InstallerImpl {

    private DataModel dataModel;
    private String defaultDomain;

    public InstallerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install(String defaultDomain) {
		dataModel.install(true, true);
        this.defaultDomain = defaultDomain;
		createMasterData();
	}
	
	private void createMasterData() {
		createPrivileges();
        InternalDirectoryImpl directory = createDirectory();
        GroupImpl administrators = createAdministrators();
		createAdmin(directory, administrators);
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
	
	private void createPrivileges() {
        for(Resources item : Resources.values()){
            ResourceImpl resource = ResourceImpl.from(dataModel, dataModel.getName(), item.getValue(), item.getDescription());
            resource.persist();

            for(Map.Entry<Long, String> entry : item.getPrivilegeValues().entrySet()){
                resource.createPrivilege(dataModel.getName() + entry.getKey(), entry.getValue());
            }
        }
	}

    private void grantAllPrivileges(GroupImpl group){
        for(Resources item : Resources.values()){
            for(Map.Entry<Long, String> entry : item.getPrivilegeValues().entrySet()){
                group.grant(dataModel.getName() + entry.getKey());
            }
        }
    }
}
