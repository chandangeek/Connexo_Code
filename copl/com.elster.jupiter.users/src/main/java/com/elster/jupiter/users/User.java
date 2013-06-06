package com.elster.jupiter.users;

import java.security.Principal;

public interface User extends Principal {
	long getId();
	boolean hasPrivilege(String privilege);
	boolean isMemberOf(String groupName);
	String getDescription();
}
