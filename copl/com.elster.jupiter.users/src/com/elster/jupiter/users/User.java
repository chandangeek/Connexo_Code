package com.elster.jupiter.users;

import java.security.Principal;

public interface User extends Principal {
	long getId();
	boolean hasPrivilege(Privilege privilege);
	boolean hasRole(String roleName);
}
