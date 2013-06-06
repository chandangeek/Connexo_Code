package com.elster.jupiter.users.impl;

import java.lang.reflect.Field;
import java.util.*;

import com.elster.jupiter.users.security.*;

public class InstallerImpl {	
	
	public void install() {
		Bus.getOrmClient().install(true,true);
		createMasterData();
	}
	
	private void createMasterData() {
		createPrivileges();
		GroupImpl administrators = createAdministrators();
		createAdmin(administrators);		
	}

	private GroupImpl createAdministrators() {
		GroupImpl group = new GroupImpl("Administrators");
		group.save();
		group.grant(Privileges.MANAGE_USERS);
		return group;
	}
	
	private void createAdmin(GroupImpl administrators) {
		UserImpl user = new UserImpl("admin", "System Administrator");
		user.save();
		administrators.add(user);
	}
	
	private void createPrivileges() {
		for (String each : getPrivileges()) {
			PrivilegeImpl result = new PrivilegeImpl("USR",each,"");
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
