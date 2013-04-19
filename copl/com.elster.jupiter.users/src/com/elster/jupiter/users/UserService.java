package com.elster.jupiter.users;

public interface UserService {
	User createUser(String authenticationName , String firstName , String lastName);
	Role createRole(String name);
	PrivilegeDescription createPrivilegeDescription(Privilege privilege, String description);
	User findUser(String authenticationName);
	Role findRole(String roleName);
	PrivilegeDescription findPrivilegeDescription(Privilege privilege);
	User authenticateBase64(String base64String);
	String getRealm();
}
