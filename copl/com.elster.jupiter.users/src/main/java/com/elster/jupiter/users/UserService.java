package com.elster.jupiter.users;

import com.google.common.base.Optional;

public interface UserService {
	User createUser(String authenticationName , String description);
	Group createGroup(String name);
	Privilege createPrivilege(String componentName , String name, String description);
	User findUser(String authenticationName);
	Group findGroup(String name);
	Optional<Privilege> getPrivilege(String privilegeName);
	User authenticateBase64(String base64String);
	String getRealm();	
}
