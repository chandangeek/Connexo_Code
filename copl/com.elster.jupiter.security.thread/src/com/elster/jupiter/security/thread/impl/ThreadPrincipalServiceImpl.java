package com.elster.jupiter.security.thread.impl;

import java.security.Principal;

import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.security.thread.ThreadPrincipalService;

@Component (name = "com.elster.jupiter.security.thread")
public class ThreadPrincipalServiceImpl implements ThreadPrincipalService {
	private ThreadLocal<Principal> threadPrincipals = new ThreadLocal<>();
			
	@Override
	public void runAs(Principal user, Runnable runnable) {
		Principal oldUser = threadPrincipals.get();
		threadPrincipals.set(user);
		try {
			runnable.run();
		} finally {
			threadPrincipals.set(oldUser);
		}
	}

	@Override
	public Principal getPrincipal() {
		return threadPrincipals.get();
	}

	@Override
	public void setPrincipal(Principal principal) {
		threadPrincipals.set(principal);
		
	}

	@Override
	public void clearPrincipal() {
		threadPrincipals.remove();
	}

}
