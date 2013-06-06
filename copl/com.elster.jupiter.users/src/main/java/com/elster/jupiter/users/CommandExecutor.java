package com.elster.jupiter.users;

public interface CommandExecutor {
	<T> T execute(PrivilegedCommand<T> command);
}
