package com.elster.jupiter.users;

public interface CommandExecutor {
	void setThreadUser(String authorizationName);
	void setThreadUser(User user);
	void clearThreadUser();
	void execute(PrivilegedCommand command);
}
