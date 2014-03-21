package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.security.Privileges;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
		group.grant(Privileges.MANAGE_USERS);
		return group;
	}
	
	private void createAdmin(InternalDirectoryImpl directory, GroupImpl administrators) {
        UserImpl user = directory.newUser("admin", "System Administrator");

		user.setPassword("admin");
		user.save();
		user.join(administrators);
	}
	
	private void createPrivileges() {
		for (String each : getPrivileges()) {
			PrivilegeImpl result = PrivilegeImpl.from(dataModel, "USR",each,"");
			result.persist();			
		}
	}
	
	private List<String> getPrivileges() {
		Field[] fields = Privileges.class.getFields();
		List<String> result = new ArrayList<>(fields.length);
		for (Field each : fields) {
			try {
				result.add((String) each.get(null));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}
	
}
