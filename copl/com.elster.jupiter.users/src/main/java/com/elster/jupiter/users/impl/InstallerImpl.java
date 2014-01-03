package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.security.Privileges;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class InstallerImpl {

    private DataModel dataModel;

    public InstallerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install() {
		dataModel.install(true, true);
		createMasterData();
	}
	
	private void createMasterData() {
		createPrivileges();
		GroupImpl administrators = createAdministrators();
		createAdmin(administrators);		
	}

	private GroupImpl createAdministrators() {
		GroupImpl group = GroupImpl.from(dataModel, "Administrators");
		group.save();
		group.grant(Privileges.MANAGE_USERS);
		return group;
	}
	
	private void createAdmin(GroupImpl administrators) {
		UserImpl user = UserImpl.from(dataModel, "admin", "System Administrator");
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
