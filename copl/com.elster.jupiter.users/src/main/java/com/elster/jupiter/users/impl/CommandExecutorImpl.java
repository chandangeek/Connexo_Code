package com.elster.jupiter.users.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.CommandExecutor;
import com.elster.jupiter.users.PrivilegedCommand;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.AccessControlException;
import java.security.Principal;

@Component(name = "com.elster.jupiter.users.command")
public class CommandExecutorImpl implements CommandExecutor {
	
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile Publisher publisher;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
	
	@Override
	public <T> T execute(final PrivilegedCommand<T> command) {
		Principal principal = threadPrincipalService.getPrincipal();
		if (principal == null) {
			throw new IllegalStateException("No user");
		}
		User user = (User) principal;
		if (!user.hasPrivilege(command.getPrivilegeName())) {
			throw new AccessControlException("No access");
		}
		threadPrincipalService.set(command.getModule(),command.getAction());
		return transactionService.execute(
				new Transaction<T>() {

					@Override
					public T perform() {
						T result = command.perform();
						publisher.publish(command);
						return result;
					}
				}
		);
	}

	@Reference
	void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;
	}
	
	@Reference
	void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

    @Reference
    void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
