package com.elster.jupiter.users;

public interface CommandExecutor {
	void setThreadUser(String authorizationName);
	void setThreadUser(User user);
	void clearThreadUser();
	<T> T execute(PrivilegedCommand<T> command);
}
