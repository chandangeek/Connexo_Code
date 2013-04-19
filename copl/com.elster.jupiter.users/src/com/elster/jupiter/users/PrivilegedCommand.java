package com.elster.jupiter.users;

public interface PrivilegedCommand extends Runnable {
	Privilege getPrivilege();
	String auditMessage();
}
