package com.elster.jupiter.users;

public interface UserService {
	User createUser(String authenticationName , String firstName , String lastName);
	Group createGroup(String name);
	Privilege createPrivilege(String componentName , String name, String description);
	User findUser(String authenticationName);
	Group findGroup(String name);
	Privilege getPrivilege(String name);
	User authenticateBase64(String base64String);
	String getRealm();
}
