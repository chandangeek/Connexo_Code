package com.elster.jupiter.users;

public interface Role {
	long getId();
	String getName();	
	boolean hasPrivilege(Privilege privilege);	
}
