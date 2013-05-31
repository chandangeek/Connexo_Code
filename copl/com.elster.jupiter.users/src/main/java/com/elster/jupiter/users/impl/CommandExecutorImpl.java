package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.CommandExecutor;
import com.elster.jupiter.users.PrivilegedCommand;
import com.elster.jupiter.users.User;

import java.security.AccessControlException;

public class CommandExecutorImpl implements CommandExecutor {
	private ThreadLocal<User> threadUsers = new ThreadLocal<>();
	
	@Override
	public void setThreadUser(String authenticationName) {
		setThreadUser(Bus.getOrmClient().getUserFactory().getUnique("authName", authenticationName));		
	}

	@Override
	public void setThreadUser(User user) {
		threadUsers.set(user);
	}

	@Override
	public void clearThreadUser() {
		threadUsers.remove();
	}

	@Override
	public <T> T execute(PrivilegedCommand<T> command) {
		User user = threadUsers.get();
		if (user == null) {
			throw new IllegalStateException("No user");
		}
		if (user.hasPrivilege(command.getPrivilege().getName())) {
			return Bus.getTransactionService().execute(command);
		} else {
			throw new AccessControlException("No access");
		}
	}

}
